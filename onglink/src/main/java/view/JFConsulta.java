/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import controller.AdminController;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.bson.types.ObjectId;
import java.util.Collections;
import javax.swing.event.ListSelectionEvent; // Import necessário para ListSelectionEvent
 
/**
 *
 * @author Felipe
 */
public class JFConsulta extends javax.swing.JInternalFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JFConsulta.class.getName());

    private final AdminController controller = new AdminController();
    
    // Mapeia o ID do Usuário (String) para a Razão Social da ONG (String)
    private Map<String, String> userOngMap = new HashMap<>();
    
    // Variáveis de Estado
    private String contaSelecionadaId;
    private String statusAtualSelecionado;
    
    // Variáveis para a Lógica de JList/Detalhes da aba ONGs
    private DefaultListModel<String> listModelOngs;
    private Map<String, Document> mapOngs = new HashMap<>();
    private Document ongSelecionada;
    
    
    private Map<String, Document> mapAprovacoes = new HashMap<>(); // O mapa que armazena os documentos
    private Document solicitacaoSelecionada;
    private DefaultListModel<String> listModelAprovacoes;

// Construtor
    public JFConsulta() {
        initComponents();
        
        // Ocultar componentes de ação inicialmente
        if (btnSalvarPerfil != null) {
            btnSalvarPerfil.setVisible(false);
        }
        if (cbxNovoPerfil != null) {
            cbxNovoPerfil.setVisible(false);
        }
        
        try {
            // Inicializa e carrega as JTables
            mapearUsuariosParaOngs(); // 🚨 NOVO: Mapeia as atribuições antes de carregar a tabela
            carregarDadosNaTabela("contas", JTContas);
            carregarDadosNaTabela("publicacoes", JTPublicacoes);
            
            // Inicializa a JList para ONGs
            carregarListaOngs(controller.getOngs());
            
        } catch (Exception e) {
            System.err.println("ERRO FATAL AO CARREGAR DADOS INICIAIS: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Configurações do InternalFrame
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Consultas de Dados");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    // --- LÓGICA DE CARREGAMENTO DE DADOS NAS TABELAS ---
    // --- LÓGICA AUXILIAR DE CARREGAMENTO DE LISTA (APROVAÇÕES PENDENTES) ---

    
// --- LÓGICA DE CARREGAMENTO E ROTEAMENTO (JTables) ---

    private void carregarDadosNaTabela(String tipo, JTable tabela) {
        List<Document> dados;

        // 1. ROTEAMENTO DO MODEL: OBTÉM OS DADOS DO CONTROLLER
        if (tipo.equals("contas")) {
            dados = controller.getContas();
        } else if (tipo.equals("publicacoes")) {
            dados = controller.getPublicacoes();
        } else { 
            // Tipo desconhecido (ONGs é carregado separadamente), limpa a tabela e retorna
            tabela.setModel(new DefaultTableModel());
            return;
        }

        if (dados.isEmpty()) {
            tabela.setModel(new DefaultTableModel());
            return;
        }

        // 2. LÓGICA DE EXIBIÇÃO PARA CONTAS E PUBLICAÇÕES
        String[] colunas;
        DefaultTableModel model;

        if (tipo.equals("contas")) {
            // --- CONFIGURAÇÃO DA TABELA DE CONTAS ---
            
            
            colunas = new String[]{"Cód. Usuário (ID)", "Nome", "Status", "CPF", "Email", "ONG Atrelada"}; 
            model = new DefaultTableModel(colunas, 0);

            for (Document doc : dados) {
                // Obtém o ID do usuário (ObjectId) e converte para String
                String userId = doc.getObjectId("_id").toString();

                // 1. Consulta o novo mapa: Busca a Razão Social da ONG atrelada
                // (Assume que 'userOngMap' está acessível e preenchido)
                String ongAtrelada = userOngMap.getOrDefault(userId, "Nenhuma"); 

                // 2. Montagem da Linha (com a nova coluna)
                model.addRow(new Object[]{
                    userId.substring(0, 24), // ID Truncado
                    doc.getString("nome"),
                    doc.getString("status"),
                    doc.getString("cpf"),
                    doc.getString("email"),
                    ongAtrelada   
                });
            }
        } else { // tipo.equals("publicacoes")
            // --- CONFIGURAÇÃO DA TABELA DE PUBLICAÇÕES ---
            colunas = new String[]{"Cód. Publicação (ID)", "Título", "Descrição", "Criado Por (ID)", "Data", "Imagens"}; 
            model = new DefaultTableModel(colunas, 0);

            for (Document doc : dados) {
                String pubId = doc.getObjectId("_id").toString(); 
                
                String criadorId = doc.get("criadoPor") instanceof ObjectId
                                   ? doc.getObjectId("criadoPor").toString()
                                   : "N/A";
                                   
                String dataPub = "N/A";
                if (doc.get("createdAt") instanceof java.util.Date) {
                    dataPub = new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDate("createdAt"));
                }
                
                List<?> imagens = doc.get("imagem", List.class);
                String numImagens = imagens != null ? String.valueOf(imagens.size()) : "0";

                model.addRow(new Object[]{
                    pubId.substring(0, 24),
                    doc.getString("titulo"),
                    doc.getString("descricao"),
                    criadorId.substring(0, 24),
                    dataPub,
                    numImagens + " Arquivos"
                });
            }
        }

        tabela.setModel(model);
    }
    
    
    private void carregarContasNaTabela (List<Document> dados){

        if (dados == null || dados.isEmpty()){
            JTContas.setModel(new DefaultTableModel());
            return;
        }

        // Colunas (incluindo a Razão Social)
        String[] colunas = new String[]{"Cód. Usuário (ID)", "Nome", "Status", "CNPJ", "Email", "ONG Atrelada"}; 
        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        for (Document userDoc : dados){
            String userId = userDoc.getObjectId("_id").toString();
            String ongAtrelada = "Nenhuma";

            // 1. Tenta obter o ID da ONG (campo 'assignedTo' no documento do usuário)
            // O método get() retorna Object, que pode ser ObjectId ou null.
            Object assignedToId = userDoc.get("assignedTo"); 

            if (assignedToId instanceof ObjectId) {
                ObjectId ongId = (ObjectId) assignedToId;

                // 2. Busca a ONG diretamente pelo ID (método getOngById no AdminController)
                Document ongDoc = controller.getOngById(ongId); 

                if (ongDoc != null) {
                    // 3. Extrai a Razão Social para exibição (assumindo campo 'razaoSocial' na coleção 'ongs')
                    ongAtrelada = ongDoc.getString("razaoSocial");
                }
            }

            // 4. Montagem da Linha
            model.addRow(new Object[]{
                userId.substring(0, 24), // ID Truncado 
                userDoc.getString("nome"),
                userDoc.getString("status"),
                userDoc.getString("cnpj"),
                userDoc.getString("email"),
                ongAtrelada // Valor final encontrado
            });
        }

        JTContas.setModel(model);
    }
    
    private void carregarPublicacoesNaTabela(List<Document> dados) {
        if (dados == null || dados.isEmpty()) {
                JTPublicacoes.setModel(new DefaultTableModel());  
                return;
            }

            // ========================================================
            // 1. CRIAÇÃO DO MAPA DE LOOKUP LOCAL (ID da ONG -> Razão Social)
            // ========================================================
            Map<String, String> razaoSocialLookupMap = new HashMap<>();

            List<Document> todasOngs = controller.getOngs();
            for (Document ong : todasOngs) {
                String ongIdStr = ong.getObjectId("_id").toString();
                String razaoSocial = ong.getString("razaoSocial");
                if (razaoSocial != null) {
                    razaoSocialLookupMap.put(ongIdStr, razaoSocial);
                }
            }
            // ========================================================

            // 2. Definição das Colunas
            String[] colunas = new String[]{"Cód. Publicação (ID)","Razão Social", "Título", "Descrição", "Data", "Imagens"}; 
            DefaultTableModel model = new DefaultTableModel(colunas, 0);

            // 3. Preenchimento da Tabela
            for (Document doc : dados) {
                String pubId = doc.getObjectId("_id").toString();

                // Extrai o ID do Criador (ObjectId)
                Object criadorObj = doc.get("criadoPor");
                String razaoSocialAtrelada = "N/A";

                if (criadorObj instanceof ObjectId) {
                    String criadorIdStr = ((ObjectId) criadorObj).toString();

                    // EXECUTA O LOOKUP NO MAPA LOCAL
                    razaoSocialAtrelada = razaoSocialLookupMap.getOrDefault(criadorIdStr, "ONG Desconhecida");
                }

                String dataPub = "N/A";
                if (doc.get("createdAt") instanceof java.util.Date) {
                    dataPub = new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDate("createdAt"));
                }

                List<?> imagens = doc.get("imagem", List.class);
                String numImagens = imagens != null ? String.valueOf(imagens.size()) : "0";

                // Montagem da linha
                model.addRow(new Object[]{
                    pubId.substring(0, 24),
                    razaoSocialAtrelada,
                    doc.getString("titulo"),
                    doc.getString("descricao"),
                    dataPub,
                    numImagens + " Arquivos"
                });
            }

            JTPublicacoes.setModel(model);
    }
    



// --- LÓGICA AUXILIAR DE CARREGAMENTO DE LISTA (ONGs) ---

    private void carregarListaOngs(List<Document> lista) {
        if (lista == null) {
            lista = Collections.emptyList();
        }
        listModelOngs = new DefaultListModel<>();  
        mapOngs.clear(); 

        for (Document d : lista) {
            String ongId = d.getObjectId("_id").toString();
            String razaoSocial = d.getString("razaoSocial");
            String cnpj = d.getString("cnpj");
            
            String chaveExibicao = ongId.substring(0, 24) + " | " + razaoSocial + " (" + cnpj + ")";
            
            listModelOngs.addElement(chaveExibicao);
            mapOngs.put(chaveExibicao, d);
        }
        
        // Atualiza o modelo da nova JList de ONGs
        jListOngs.setModel(listModelOngs); 
    }

private void exibirDetalhesOng(javax.swing.event.ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
        
        String chaveSelecionada = jListOngs.getSelectedValue();
        
        // --- 1. Lógica de Verificação e Atribuição ---
        // Usamos mapOngs aqui, que deve conter ONGs REGISTRADAS
        if (chaveSelecionada == null || mapOngs.isEmpty()) { 
            TADetalhesOngs.setText("Selecione uma ONG para ver os detalhes.");
            // 🚨 Use ongSelecionada, pois esta é a variável de estado para ONGs
            ongSelecionada = null; 
            return;
        }
        
        // CRÍTICO: Busca o Documento no mapOngs
        ongSelecionada = mapOngs.get(chaveSelecionada); 
        
        // Checagem de segurança
        if (ongSelecionada == null) {
            TADetalhesOngs.setText("Erro ao carregar detalhes: Documento não encontrado no cache do mapa.");
            return;
        }

        // --- 2. Montagem do Texto de Detalhes (Usando ongSelecionada) ---
        
        // Endereço
        String enderecoDisplay = "N/A";
        Document endereco = ongSelecionada.get("endereco", Document.class);
        if (endereco != null) {
            String rua = endereco.getString("rua") != null ? endereco.getString("rua") : "";
            String numeroEnd = endereco.getString("numeroEnd") != null ? endereco.getString("numeroEnd") : "";
            String complemento = endereco.getString("complemento") != null ? endereco.getString("complemento") : "";
            String bairro = endereco.getString("bairro") != null ? endereco.getString("bairro") : "";
            String cep = endereco.getString("cep") != null ? endereco.getString("cep") : "";
            String cidade = endereco.getString("cidade") != null ? endereco.getString("cidade") : "";
            String estado = endereco.getString("estado") != null ? endereco.getString("estado") : ""; 
            
            enderecoDisplay = String.format("Logradouro: %s \n Numero: %s \n Complemento: %s \n Bairro: %s \n CEP: %s \n Cidade: %s \n Estado: %s \n", rua, numeroEnd, complemento, bairro, cep, cidade, estado).trim();
        }
        
        // Redes Sociais
        String redesDisplay = "Nenhuma";
        Document redeSocial = ongSelecionada.get("redeSocial", Document.class);
        if (redeSocial != null) { 
            String instagram = redeSocial.getString("instagram") != null ? redeSocial.getString("instagram") : "";
            String facebook = redeSocial.getString("facebook") != null ? redeSocial.getString("facebook") : "";
            String linkedin = redeSocial.getString("linkedin") != null ? redeSocial.getString("linkedin") : "";
            String site = redeSocial.getString("site") != null ? redeSocial.getString("site") : "";
            
            redesDisplay = String.format("Instagram: %s \n Facebook: %s \n Linkedin: %s \n Site: %s \n", instagram, facebook, linkedin, site );
        }

        List<String> documentosDisplay = ongSelecionada.getList("arquivosLegais", String.class);

        String ataDeCriacao = "Nenhum link de documento encontrado";
        String estatutoSocial = "Nenhum link de documento encontrado"; 

        // 2. Verifica se a lista não está vazia
        if (documentosDisplay != null && !documentosDisplay.isEmpty()) {
            // 3. Extrai o primeiro link do array (índice 0)
            ataDeCriacao = documentosDisplay.get(0);
            estatutoSocial = documentosDisplay.get(1);
            
          
        }
        
        
        
        List<?> assignedToList = ongSelecionada.get("assignedTo", List.class);
        int totalAtribuidos = (assignedToList != null) ? assignedToList.size() : 0;

        
        List<Document> assignedUsers = controller.getAssignedUsersDetails(ongSelecionada);
        StringBuilder assignedUsersDisplay = new StringBuilder();
        assignedUsersDisplay.append("\n--------------------------------------------\n");
        assignedUsersDisplay.append(" USUÁRIOS ATRIBUÍDOS \n");
        assignedUsersDisplay.append("--------------------------------------------\n");

        if (assignedUsers.isEmpty()) {
            assignedUsersDisplay.append("Nenhum usuário atribuído a esta ONG.");
        } else {
            for (Document user : assignedUsers) {
                String userId = user.getObjectId("_id").toString();
                String nome = user.getString("nome") != null ? user.getString("nome") : "N/A";
                String email = user.getString("email") != null ? user.getString("email") : "N/A";

                assignedUsersDisplay.append("ID: ").append(userId).append("\n");
                assignedUsersDisplay.append("Nome: ").append(nome).append("\n");
                assignedUsersDisplay.append("Email: ").append(email).append("\n");

                assignedUsersDisplay.append("--- \n");
            }
        }
        
        
        

        String detalhesComplementares = String.format(
            "--- INFORMAÇÕES DE REGISTRO ---\n" +
            "ID da ONG: %s\n Situação Cadastral: %s \n Razão Social: %s\nNome Fantasia: %s\nCNPJ: %s (CPF: %s)\n" +
            "Rep. Legal: %s\nCausa Social: %s\nTelefone: %s\nEmail: %s\n\n" +
            "Endereço: \n %s \n\nRedes:\n %s\nDescrição: \n %s \n  \n Documentos: \n  Ata de criação: %s \n Estatuto social: %s \n\n Atribuídos a %d usuário(s)",
            ongSelecionada.getObjectId("_id").toString(),
            ongSelecionada.getString("situacaoCadastral"),
            ongSelecionada.getString("razaoSocial"),
            ongSelecionada.getString("nomeFantasia"),
            ongSelecionada.getString("cnpj"),
            ongSelecionada.getString("cpf"),
            ongSelecionada.getString("repLegal"),
            ongSelecionada.getString("causaSocial"),
            ongSelecionada.getString("telefone"),
            ongSelecionada.getString("email"),
            enderecoDisplay,
            redesDisplay,
            ongSelecionada.getString("descricao"),
            ataDeCriacao,
            estatutoSocial,
            totalAtribuidos
        );
        
        String textoFinal = detalhesComplementares + assignedUsersDisplay.toString();
        
        if (TADetalhesOngs != null) {
            // Limpa e anexa os detalhes completos no JTextArea principal
            TADetalhesOngs.setText("");
            TADetalhesOngs.append(textoFinal);
            TADetalhesOngs.setCaretPosition(0); 
        }
        
        
        

    }    
}
    
    public AdminController getController() {
     return this.controller;
    }
    
   /**
    * Mapeia qual Razão Social de ONG está atrelada a cada ID de Usuário.
    */
    private void mapearUsuariosParaOngs() {
       userOngMap.clear();

       // 1. Obtém todas as ONGs registradas
       List<Document> todasOngs = controller.getOngs(); 

       for (Document ong : todasOngs) {
           String razaoSocial = ong.getString("razaoSocial");

           // 2. Extrai a lista de IDs atribuídos
           List<?> assignedToList = ong.get("assignedTo", List.class);

           if (assignedToList != null && !assignedToList.isEmpty()) {
               Object firstAssigned = assignedToList.get(0);

               if (firstAssigned instanceof ObjectId) {
                   String userIdStr = ((ObjectId) firstAssigned).toString();

                   // 3. Mapeia: ID do Usuário -> Razão Social da ONG
                   userOngMap.put(userIdStr, razaoSocial);
               }
               // OBS: Este código só mapeia o PRIMEIRO usuário atribuído (índice 0).
           }
       }
    } 
    
  
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        Contas = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        JTFCampoPesquisaContas = new javax.swing.JTextField();
        btnPesquisarContas = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        JTContas = new javax.swing.JTable();
        cbxNovoPerfil = new javax.swing.JComboBox<>();
        btnSalvarPerfil = new javax.swing.JButton();
        Publicacoes = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        JTFCampoPesquisaPublicacoes = new javax.swing.JTextField();
        btnPesquisarPublicacoes = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        JTPublicacoes = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        Ongs = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        JTFCampoPesquisaOngs = new javax.swing.JTextField();
        btnPesquisarOngs = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListOngs = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        TADetalhesOngs = new javax.swing.JTextArea();
        btnGerarRelatorio = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabbedPaneMouseClicked(evt);
            }
        });

        jLabel1.setText("Pesquisar (CódUsuário, Nome, CPF, Status):");

        btnPesquisarContas.setText("Pesquisar");
        btnPesquisarContas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarContasActionPerformed(evt);
            }
        });

        JTContas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        JTContas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JTContasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(JTContas);

        cbxNovoPerfil.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "ong", "user" }));
        cbxNovoPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxNovoPerfilActionPerformed(evt);
            }
        });

        btnSalvarPerfil.setText("Mudar Perfil");
        btnSalvarPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarPerfilActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ContasLayout = new javax.swing.GroupLayout(Contas);
        Contas.setLayout(ContasLayout);
        ContasLayout.setHorizontalGroup(
            ContasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ContasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ContasLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JTFCampoPesquisaContas, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPesquisarContas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE)
                        .addComponent(cbxNovoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSalvarPerfil))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        ContasLayout.setVerticalGroup(
            ContasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ContasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(JTFCampoPesquisaContas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisarContas)
                    .addComponent(cbxNovoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalvarPerfil))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Contas", Contas);

        jLabel2.setText("Pesquisar (Cód.Publicação, Título, Descrição, Cód.Usuário):");

        btnPesquisarPublicacoes.setText("Pesquisar");
        btnPesquisarPublicacoes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarPublicacoesActionPerformed(evt);
            }
        });

        JTPublicacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(JTPublicacoes);

        javax.swing.GroupLayout PublicacoesLayout = new javax.swing.GroupLayout(Publicacoes);
        Publicacoes.setLayout(PublicacoesLayout);
        PublicacoesLayout.setHorizontalGroup(
            PublicacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PublicacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PublicacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PublicacoesLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(JTFCampoPesquisaPublicacoes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPesquisarPublicacoes))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1075, Short.MAX_VALUE))
                .addContainerGap())
        );
        PublicacoesLayout.setVerticalGroup(
            PublicacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PublicacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PublicacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(JTFCampoPesquisaPublicacoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisarPublicacoes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Publicações", Publicacoes);

        jLabel3.setText("Pesquisar (Cód.ONG, Razão Social, CNPJ, etc...):");

        btnPesquisarOngs.setText("Pesquisar");
        btnPesquisarOngs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarOngsActionPerformed(evt);
            }
        });

        jListOngs.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jListOngs.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOngsValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListOngs);

        TADetalhesOngs.setColumns(20);
        TADetalhesOngs.setRows(5);
        jScrollPane4.setViewportView(TADetalhesOngs);

        btnGerarRelatorio.setText("Gerar Relatório");
        btnGerarRelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerarRelatorioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OngsLayout = new javax.swing.GroupLayout(Ongs);
        Ongs.setLayout(OngsLayout);
        OngsLayout.setHorizontalGroup(
            OngsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OngsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OngsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(OngsLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JTFCampoPesquisaOngs)
                        .addGap(33, 33, 33)
                        .addComponent(btnPesquisarOngs)
                        .addGap(56, 56, 56)
                        .addComponent(btnGerarRelatorio))
                    .addGroup(OngsLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        OngsLayout.setVerticalGroup(
            OngsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OngsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OngsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(JTFCampoPesquisaOngs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisarOngs)
                    .addComponent(btnGerarRelatorio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(OngsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1087, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(Ongs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 417, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(Ongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("ONGs", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPesquisarContasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarContasActionPerformed
        // TODO add your handling code here:
        final String termo = JTFCampoPesquisaContas.getText().trim();
        
        List<Document> resultados;
        
        if (termo.isEmpty()){
            resultados = controller.getContas();
        } else {
            resultados = controller.filtrarContas(termo);
        }
        
        
        carregarContasNaTabela(resultados);
        
    }//GEN-LAST:event_btnPesquisarContasActionPerformed

    private void btnPesquisarPublicacoesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarPublicacoesActionPerformed
        // TODO add your handling code here:
        String termo = JTFCampoPesquisaPublicacoes.getText().trim();
    
    List<Document> resultados;
    
    if (termo.isEmpty()) {
        // Se o campo estiver vazio, carrega todos os dados
        resultados = controller.getPublicacoes();
    } else {
        // Se houver um termo, chama o novo método de filtro
        resultados = controller.filtrarPublicacoes(termo);
    }

    carregarPublicacoesNaTabela(resultados);
    }//GEN-LAST:event_btnPesquisarPublicacoesActionPerformed

    private void JTContasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JTContasMouseClicked
    int linha = JTContas.getSelectedRow();
        if (linha == -1) { 
            btnSalvarPerfil.setVisible(false);
            cbxNovoPerfil.setVisible(false);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) JTContas.getModel();
        
        // CORREÇÃO 1: Lê o status atual (o nome da coluna é 'Status' na UI, mas o dado é 'Status')
        // Coluna 2 na tabela gerada é a coluna 'Status'.
        final String statusAtual = (String) model.getValueAt(linha, 2); 
        final String email = (String) model.getValueAt(linha, 4); // Coluna 4 é Email
        
        statusAtualSelecionado = statusAtual; 

        // Lógica para encontrar o ID no Documento completo (usamos o email para garantir a busca)
        // Usaremos o email para garantir a unicidade, já que CodUsuario foi removido do Model.
        controller.getContas().stream()
            .filter(doc -> doc.getString("email").equalsIgnoreCase(email))
            .findFirst()
            .ifPresent(doc -> {
                // Obtém o ID do MongoDB (_id) e armazena como String para o update
                contaSelecionadaId = doc.getObjectId("_id").toString(); 
                
                // 1. Define o item atual na ComboBox
                cbxNovoPerfil.setSelectedItem(statusAtual);
                
                // 2. EXIBIR COMPONENTES (tornar visíveis)
                btnSalvarPerfil.setVisible(true);
                cbxNovoPerfil.setVisible(true); 
            });
    }//GEN-LAST:event_JTContasMouseClicked

    private void btnSalvarPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarPerfilActionPerformed
        // TODO add your handling code here:
if (contaSelecionadaId == null) return;

    // Obtém o valor final que o usuário escolheu na caixa de seleção
    final String novoPerfil = cbxNovoPerfil.getSelectedItem().toString();
    
    
    boolean sucesso = controller.setStatus(contaSelecionadaId, novoPerfil); 


    if (sucesso) {
        
        JOptionPane.showMessageDialog(this, 
            "Perfil alterado com sucesso para " + novoPerfil + ".",
            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        
        // 1. Recarrega os dados da tabela
        carregarDadosNaTabela("contas", JTContas); 
        
        // 2. Esconde o painel de ação
        btnSalvarPerfil.setVisible(false);
        cbxNovoPerfil.setVisible(false);
        
    } else {
        JOptionPane.showMessageDialog(this, 
            "Falha ao salvar o novo perfil no banco de dados. Verifique o console.", 
            "Erro", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnSalvarPerfilActionPerformed

    private void tabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbedPaneMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabbedPaneMouseClicked

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        // TODO add your handling code here:
        // Verifica se a aba "Publicações" (índice 1) está selecionada
    if (tabbedPane.getSelectedIndex() == 1) { 
        // Carrega os dados somente agora
        carregarPublicacoesNaTabela(controller.getPublicacoes());
    }
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void btnPesquisarOngsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarOngsActionPerformed
        // Assume que você tem um campo de texto chamado JTFCampoPesquisaOngs
    String termo = JTFCampoPesquisaOngs.getText().trim();
    
    List<Document> resultados;
    
    if (termo.isEmpty()) {
        // Se o campo estiver vazio, carrega todos os dados
        resultados = controller.getOngs();
    } else {
        // Se houver um termo, chama o método de filtro
        resultados = controller.filtrarOngs(termo);
    }
    
    // Chama o método auxiliar para exibir os resultados na JTable de ONGs
    carregarListaOngs(resultados);
    
    }//GEN-LAST:event_btnPesquisarOngsActionPerformed

    private void jListOngsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListOngsValueChanged
        // TODO add your handling code here:
        // Chamada ao método de exibição de detalhes da ONG
        exibirDetalhesOng(evt);
    

    // Nota: O método auxiliar exibirDetalhesOng(evt) deve existir em sua classe.
    }//GEN-LAST:event_jListOngsValueChanged

    private void cbxNovoPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxNovoPerfilActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxNovoPerfilActionPerformed

    private void btnGerarRelatorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerarRelatorioActionPerformed
        // TODO add your handling code here:
        // 1. Obtém a lista completa de ONGs a partir do Controller
        List<Document> listaOngs = controller.getOngs(); 

        if (listaOngs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há ONGs cadastradas para gerar o relatório.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Configuração e Abertura do Diálogo para Salvar o Arquivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório de ONGs");

        // Define o nome de arquivo padrão
        fileChooser.setSelectedFile(new java.io.File("Relatorio_ONGs_" + java.time.LocalDate.now() + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Garante a extensão .pdf se o usuário não a forneceu
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            // 3. Chama a lógica de geração de PDF (Instancia o Controller de Relatório)
            controller.RelatorioOngs relatorio = new controller.RelatorioOngs();

            if (relatorio.gerar(listaOngs, filePath)) {
                JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso em: " + filePath, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao gerar o arquivo PDF. Verifique as permissões de escrita.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnGerarRelatorioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new JFConsulta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Contas;
    private javax.swing.JTable JTContas;
    private javax.swing.JTextField JTFCampoPesquisaContas;
    private javax.swing.JTextField JTFCampoPesquisaOngs;
    private javax.swing.JTextField JTFCampoPesquisaPublicacoes;
    private javax.swing.JTable JTPublicacoes;
    private javax.swing.JPanel Ongs;
    private javax.swing.JPanel Publicacoes;
    private javax.swing.JTextArea TADetalhesOngs;
    private javax.swing.JButton btnGerarRelatorio;
    private javax.swing.JButton btnPesquisarContas;
    private javax.swing.JButton btnPesquisarOngs;
    private javax.swing.JButton btnPesquisarPublicacoes;
    private javax.swing.JButton btnSalvarPerfil;
    private javax.swing.JComboBox<String> cbxNovoPerfil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList<String> jListOngs;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
