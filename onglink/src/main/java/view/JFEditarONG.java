/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import controller.AdminController;
import org.bson.Document;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Felipe
 */
public class JFEditarONG extends javax.swing.JInternalFrame {

private static final Logger logger = Logger.getLogger(JFEditarONG.class.getName());
    private final AdminController controller = new AdminController();
    
    // --- VARIÁVEIS DE ESTADO E MAPEAMENTO ---
    private Map<String, Document> ongMap = new HashMap<>(); // Mapeia Nome Fantasia para Documento
    private Document ongSelecionada; // ONG selecionada para edição
    private String ongIdSelecionada;
    


    // Construtor
    public JFEditarONG() {
        initComponents();
        carregarOngsNoComboBox(); // 🚨 Carrega as opções no ComboBox na inicialização
        
        setTitle("Edição de Status Cadastral de ONGs");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        
        
    }
    

     
    
    // --- LÓGICA DE CARREGAMENTO E MUDANÇA DE ESTADO ---
    
    /**
     * Carrega todas as ONGs registradas no ComboBox, mostrando o Nome Fantasia.
     */
    private void carregarOngsNoComboBox() {
        List<Document> todasOngs = controller.getOngs(); 
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        model.addElement("--- Selecione para Editar ---");
        
        for (Document ong : todasOngs) {
            String nomeFantasia = ong.getString("nomeFantasia");
            if (nomeFantasia != null) {
                model.addElement(nomeFantasia);
                ongMap.put(nomeFantasia, ong); // Mapeia para o Documento completo
            }
        }
        
        if (cbxOngs != null) {
            cbxOngs.setModel(model);
            cbxOngs.addActionListener(this::cbxOngsSelectionChanged);
        }
    }

    /**
     * Método disparado ao selecionar um item no ComboBox.
     */
    private void cbxOngsSelectionChanged(ActionEvent evt) {
        String nomeFantasiaSelecionado = (String) cbxOngs.getSelectedItem();
        
        if (nomeFantasiaSelecionado == null || nomeFantasiaSelecionado.startsWith("---")) {
           
            return;
        }
        
        Document ongDoc = ongMap.get(nomeFantasiaSelecionado);
        
        if (ongDoc != null) {
            ongSelecionada = ongDoc; 
            ongIdSelecionada = ongDoc.getObjectId("_id").toString();
            preencherFormulario(ongDoc);
             
        }  
    }

    /**
     * Insere os dados da ONG selecionada nos campos de texto para edição/visualização.
     */
    private void preencherFormulario(Document ongDoc) {
        if (ongDoc == null) return;
        
        
        String enderecoDisplay = "N/A";
        Document endereco = ongDoc.get("endereco", Document.class);
        if (endereco != null) {
            String rua = endereco.getString("rua") != null ? endereco.getString("rua") : "";
            String numeroEnd = endereco.getString("numeroEnd") != null ? endereco.getString("numeroEnd") : "";
            String complemento = endereco.getString("complemento") != null ? endereco.getString("complemento") : "";
            String bairro = endereco.getString("bairro") != null ? endereco.getString("bairro") : "";
            String cep = endereco.getString("cep") != null ? endereco.getString("cep") : "";
            String cidade = endereco.getString("cidade") != null ? endereco.getString("cidade") : "";
            String estado = endereco.getString("estado") != null ? endereco.getString("estado") : ""; // Atenção: deve ser ongSelecionada.getString("estado") se for um campo simples
            
            enderecoDisplay = String.format("Logradouro: %s \n Numero: %s \n Complemento: %s \n Bairro: %s \n CEP: %s \n Cidade: %s \n Estado: %s \n", rua, numeroEnd, complemento, bairro, cep, cidade, estado).trim();
        }
        
        // Redes Sociais
        String redesDisplay = "Nenhuma";
        Document redeSocial = ongDoc.get("redeSocial", Document.class);
        if (redeSocial != null) { 
            String instagram = redeSocial.getString("instagram") != null ? redeSocial.getString("instagram") : "";
            String facebook = redeSocial.getString("facebook") != null ? redeSocial.getString("facebook") : "";
            String linkedin = redeSocial.getString("linkedin") != null ? redeSocial.getString("linkedin") : "";
            String site = redeSocial.getString("site") != null ? redeSocial.getString("site") : "";
            
            redesDisplay = String.format("Instagram: %s \n Facebook: %s \n Linkedin: %s \n Site: %s \n", instagram, facebook, linkedin, site );
        }
        

  
        List<String> documentosDisplay = ongDoc.getList("arquivosLegais", String.class);

        String ataDeCriacao = "Nenhum link de documento encontrado";
        String estatutoSocial = "Nenhum link de documento encontrado"; 

        // 2. Verifica se a lista não está vazia
        if (documentosDisplay != null && !documentosDisplay.isEmpty()) {
            // 3. Extrai o primeiro link do array (índice 0)
            ataDeCriacao = documentosDisplay.get(0);
            estatutoSocial = documentosDisplay.get(1);
            
            
        }

        
        // ========================================================
        // NOVO: Lógica de Carregamento e Seleção do Status
        // ========================================================
        
        // 1. Define as opções disponíveis no ComboBox (você deve ter feito isso no Design, mas
        // garantimos a lista aqui para fins de demonstração):
        String[] opcoesStatus = {"EM ANALISE", "APROVADO", "REPROVADO", "SUSPENSO", "INATIVO"};
        cbxSituacaoCadastral.setModel(new DefaultComboBoxModel<>(opcoesStatus));
        
        // 2. Lê o status atual do MongoDB (assumindo campo 'situacaoCadastral')
        String statusAtual = ongDoc.getString("situacaoCadastral");
        
        if (cbxSituacaoCadastral != null && statusAtual != null) {
            // 3. Pré-seleciona o valor correspondente no ComboBox (case-insensitive)
            // Isso garante que o valor do banco esteja visível
            cbxSituacaoCadastral.setSelectedItem(statusAtual.toUpperCase());
        }
        
        List<?> assignedToList = ongDoc.get("assignedTo", List.class);
        int totalAtribuidos = (assignedToList != null) ? assignedToList.size() : 0;

        
        List<Document> assignedUsers = controller.getAssignedUsersDetails(ongDoc);
        StringBuilder assignedUsersDisplay = new StringBuilder();
        assignedUsersDisplay.append("\n--------------------------------------------\n");
        assignedUsersDisplay.append(" USUÁRIOS ATRIBUÍDOS ");
        assignedUsersDisplay.append("\n--------------------------------------------\n");

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
            ongDoc.getObjectId("_id").toString(),
            ongDoc.getString("situacaoCadastral"),
            ongDoc.getString("razaoSocial"),
            ongDoc.getString("nomeFantasia"),
            ongDoc.getString("cnpj"),
            ongDoc.getString("cpf"),
            ongDoc.getString("repLegal"),
            ongDoc.getString("causaSocial"),
            ongDoc.getString("telefone"),
            ongDoc.getString("email"),
            enderecoDisplay,
            redesDisplay,
            ongDoc.getString("descricao"),
            ataDeCriacao,
            estatutoSocial,
            totalAtribuidos
        );
        
        String textoFinal = detalhesComplementares + assignedUsersDisplay.toString();
        
        if (TADetalhesOng != null) {
            // Limpa e anexa os detalhes completos no JTextArea principal
            TADetalhesOng.setText("");
            TADetalhesOng.append(textoFinal);
            TADetalhesOng.setCaretPosition(0); 
        }
        
        
        

    }    

         private void vincularAcoes() {
                if (btnSalvarEdicao != null) btnSalvarEdicao.addActionListener(this::btnSalvarEdicaoActionPerformed);
                // Não vinculamos Aprovar/Reprovar para desativar a função
                if (brnClose != null) brnClose.addActionListener(evt -> this.dispose());
        }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        TADetalhesOng = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        btnSalvarEdicao = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        brnClose = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        cbxSituacaoCadastral = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        cbxOngs = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        TADetalhesOng.setColumns(20);
        TADetalhesOng.setRows(5);
        jScrollPane7.setViewportView(TADetalhesOng);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addGap(278, 278, 278))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        jScrollPane5.setViewportView(jPanel5);

        btnSalvarEdicao.setText("Salvar Edição");
        btnSalvarEdicao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarEdicaoActionPerformed(evt);
            }
        });

        jLabel13.setText("Salvar edição! -->");

        jLabel16.setText("Fechar! -->");

        brnClose.setText("Fechar");
        brnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brnCloseActionPerformed(evt);
            }
        });

        jLabel9.setText("Situação cadastral:");

        cbxSituacaoCadastral.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brnClose))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSalvarEdicao)
                            .addComponent(cbxSituacaoCadastral, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(138, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(cbxSituacaoCadastral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(btnSalvarEdicao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 151, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(brnClose))
                .addGap(14, 14, 14))
        );

        jLabel17.setText("Pesquisar ONG:");

        cbxOngs.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxOngs, 0, 340, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbxOngs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void brnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_brnCloseActionPerformed

    private void btnSalvarEdicaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarEdicaoActionPerformed
        // 1. Checagem de Seleção
        if (ongSelecionada == null || ongIdSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma ONG válida antes de salvar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. COLETAR NOVO STATUS CADASTRAL DO COMBOBOX
        String novoStatusCadastral = (String) cbxSituacaoCadastral.getSelectedItem();

        if (novoStatusCadastral == null || novoStatusCadastral.startsWith("---")) {
            JOptionPane.showMessageDialog(this, "Selecione um Status Cadastral válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Monta o Documento de Update (APENAS COM O CAMPO DE STATUS)
        Document updates = new Document();
        updates.append("situacaoCadastral", novoStatusCadastral); 

        // Variável para armazenar o ID do usuário atribuído (o responsável)
        ObjectId userIdParaPromover = null;

        // Tenta obter o ID do usuário do campo 'assignedTo' (o primeiro da lista)
        List<?> assignedToList = ongSelecionada.get("assignedTo", List.class);
        if (assignedToList != null && !assignedToList.isEmpty() && assignedToList.get(0) instanceof ObjectId) {
            userIdParaPromover = (ObjectId) assignedToList.get(0);
        }

        // 4. PERSISTÊNCIA NA COLEÇÃO 'ONGS'
        boolean edicaoONGSucesso = controller.atualizarDadosONG(ongIdSelecionada, updates);

        if (edicaoONGSucesso) {

            // 5. LÓGICA CRÍTICA: PROMOÇÃO DE STATUS (Se for APROVADO)
            if ("APROVADO".equalsIgnoreCase(novoStatusCadastral)) {

                if (userIdParaPromover != null) {
                    String userIdStr = userIdParaPromover.toString(); 

                    // Reutiliza o método setStatus existente para mudar o perfil para "ONG"
                    if (controller.setStatus(userIdStr, "ong")) {
                        JOptionPane.showMessageDialog(this, "Usuário responsável PROMOVIDO para status 'ONG'!", "Status Atualizado", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Alerta: ONG salva, mas FALHA ao promover status do usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                     JOptionPane.showMessageDialog(this, "Alerta: ONG salva, mas ID de usuário em 'assignedTo' é inválido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }

            // 6. Feedback final e sincronização
            JOptionPane.showMessageDialog(this, "Status da ONG salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            carregarOngsNoComboBox(); // Re-carrega o ComboBox principal
             // Limpa o estado da tela
            ongSelecionada = null; 

        } else {
            JOptionPane.showMessageDialog(this, "Falha ao salvar as edições da ONG.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSalvarEdicaoActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new JFEditarONG().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TADetalhesOng;
    private javax.swing.JButton brnClose;
    private javax.swing.JButton btnSalvarEdicao;
    private javax.swing.JComboBox<String> cbxOngs;
    private javax.swing.JComboBox<String> cbxSituacaoCadastral;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    // End of variables declaration//GEN-END:variables


}
