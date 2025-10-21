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
import java.util.Vector;
import java.util.Collections; // Import para garantir o carregamento das tabelas


/**
 *
 * @author Felipe
 */
public class JFConsulta extends javax.swing.JInternalFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JFConsulta.class.getName());

private final AdminController controller = new AdminController();
    

    private String contaSelecionadaId; 
    private String perfilAtualSelecionado;
    // Construtor
    public JFConsulta() {
        initComponents();
          
            if (btnSalvarPerfil != null) {
                btnSalvarPerfil.setVisible(false);
            }
            if (cbxNovoPerfil != null) {
                cbxNovoPerfil.setVisible(false);
            }
        // Inicializa e carrega os dados nas tabelas
        carregarDadosNaTabela("contas", JTContas);
        carregarDadosNaTabela("publicacoes", JTPublicacoes);
        
        // Configurações do InternalFrame
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Consultas de Dados");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    // --- LÓGICA DE CARREGAMENTO DE DADOS NAS TABELAS ---

    private void carregarDadosNaTabela(String tipo, JTable tabela) {
        List<Document> dados = tipo.equals("contas") ? controller.getContas() : controller.getPublicacoes();

        if (dados.isEmpty()) {
            tabela.setModel(new DefaultTableModel()); // Limpa a tabela
            return;
        }

        // 1. Define as Colunas com os Novos Nomes
        String[] colunas;
        if (tipo.equals("contas")) {
            colunas = new String[]{"Cód. Usuário", "Nome", "Perfil", "CNPJ", "Email", "Descrição"}; 
        } else { // Publicações - NOVAS COLUNAS
            colunas = new String[]{"Cód. Publicação", "Cód. Usuário", "Título", "Texto", "Data", "Imagens"}; 
        }

        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        // 2. Preenche as Linhas com os Novos Dados
        for (Document doc : dados) {
            if (tipo.equals("contas")) {
                model.addRow(new Object[]{
                    doc.getInteger("CodUsuario"), 
                    doc.getString("Nome"),          // Novo: Nome
                    doc.getString("Perfil"),        // Novo: Perfil
                    doc.getString("CNPJ"),          // Novo: CNPJ
                    doc.getString("Email"),         // Novo: Email
                    doc.getString("Descricao")      // Novo: Descricao
                });
            } else { // publicacoes
                // Formata a data para exibição
                String dataPub = "N/A";
                if (doc.get("DataPublicacao") instanceof java.util.Date) {
                    // Formata o objeto Date do MongoDB para uma String amigável
                    dataPub = new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDate("DataPublicacao"));
                }

                // Obtém a lista de imagens (como ArrayList)
                List<String> imagens = doc.get("Imagens", List.class);
                String numImagens = imagens != null ? String.valueOf(imagens.size()) : "0";

                model.addRow(new Object[]{
                    doc.getInteger("CodPubli"), // Novo: CodPubli
                    doc.getInteger("CodUsuario"), // Novo: CodUsuario
                    doc.getString("Titulo"),    // Novo: Titulo
                    doc.getString("Texto"),
                    dataPub,                    // Novo: Data Formatada
                    numImagens + " Arquivos"     // Novo: Contagem de Imagens
                });
            }
        }

        // 3. Define o modelo na tabela
        tabela.setModel(model);
    }
    
        private void carregarPublicacoesNaTabela(List<Document> dados) {
        if (dados == null || dados.isEmpty()) {
            JTPublicacoes.setModel(new DefaultTableModel()); // Limpa a tabela
            return;
        }

        // 1. Define as Colunas
        String[] colunas = new String[]{"Cód. Publicação", "Título", "Texto", "Data", "Cód. Usuário", "Imagens"}; 
        DefaultTableModel model = new DefaultTableModel(colunas, 0);

        // 2. Preenche as Linhas
        for (Document doc : dados) {
            // Formatação da Data
            String dataPub = "N/A";
            if (doc.get("DataPublicacao") instanceof java.util.Date) {
                dataPub = new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDate("DataPublicacao"));
            }

            // Contagem de Imagens
            List<String> imagens = doc.get("Imagens", List.class);
            String numImagens = imagens != null ? String.valueOf(imagens.size()) : "0";

            // Montagem da linha
            model.addRow(new Object[]{
                doc.getInteger("CodPubli"), 
                doc.getString("Titulo"),    
                doc.getString("Texto"),     
                dataPub,                    
                doc.getInteger("CodUsuario"), 
                numImagens + " Arquivos"     
            });
        }

        // 3. Define o modelo na tabela
        JTPublicacoes.setModel(model);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Pesquisar (CódUsuário, Razão Social, CNPJ):");

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

        cbxNovoPerfil.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ADMIN", "ONG", "USER" }));

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
                .addGap(60, 60, 60)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JTFCampoPesquisaContas, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPesquisarContas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxNovoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSalvarPerfil)
                .addContainerGap(26, Short.MAX_VALUE))
            .addGroup(ContasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Contas", Contas);

        jLabel2.setText("Pesquisar (Cód.Usuário, Cód.Publicação, Texto, Título):");

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
                .addGroup(PublicacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PublicacoesLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JTFCampoPesquisaPublicacoes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPesquisarPublicacoes))
                    .addGroup(PublicacoesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)))
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Publicações", Publicacoes);

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
        final String termo = JTFCampoPesquisaContas.getText();
        JOptionPane.showMessageDialog(this, 
            "Simulando filtro de Contas por termo: " + termo + 
            "\n(Em produção, faria uma consulta filtrada ao MongoDB).");
        // Em um projeto real, você chamaria um método filtrado no controller aqui:
        // carregarDadosNaTabela("contas", controller.filtrarContas(termo));
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
        // TODO add your handling code here:
    int linha = JTContas.getSelectedRow();
    // Se nenhuma linha estiver selecionada, ou se o clique for fora da tabela
    if (linha == -1) { 
        // Se desejar, pode-se ocultar aqui também, caso o clique seja deselecionar
        btnSalvarPerfil.setVisible(false);
        cbxNovoPerfil.setVisible(false);
        return;
    }

    // Variáveis de estado
    DefaultTableModel model = (DefaultTableModel) JTContas.getModel();
    final Integer codUsuario = (Integer) model.getValueAt(linha, 0); // Coluna 0 é CodUsuario
    final String perfilAtual = (String) model.getValueAt(linha, 2);  // Coluna 2 é Perfil
    
    perfilAtualSelecionado = perfilAtual; // Armazena o perfil para a ação de salvar     

    // Lógica para encontrar o ID no Documento completo
    controller.getContas().stream()
        .filter(doc -> doc.getString("CodUsuario").equals(codUsuario))
        .findFirst()
        .ifPresent(doc -> {
            contaSelecionadaId = doc.getObjectId("_id").toString(); 
            
            // 1. Define o item atual na ComboBox
            cbxNovoPerfil.setSelectedItem(perfilAtual);
            
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
    
    
    boolean sucesso = controller.setPerfil(contaSelecionadaId, novoPerfil); // Chamada ideal


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
    private javax.swing.JTextField JTFCampoPesquisaPublicacoes;
    private javax.swing.JTable JTPublicacoes;
    private javax.swing.JPanel Publicacoes;
    private javax.swing.JButton btnPesquisarContas;
    private javax.swing.JButton btnPesquisarPublicacoes;
    private javax.swing.JButton btnSalvarPerfil;
    private javax.swing.JComboBox<String> cbxNovoPerfil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
