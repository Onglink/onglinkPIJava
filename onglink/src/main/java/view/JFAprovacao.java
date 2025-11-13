/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import controller.AdminController;
import org.bson.Document;
import javax.swing.event.ListSelectionEvent;
import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import java.awt.event.ActionEvent;
/**
 *
 * @author Felipe
 */
public class JFAprovacao extends javax.swing.JInternalFrame {
private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JFAprovacao.class.getName());
    private final AdminController controller = new AdminController();
    private DefaultListModel<String> listModel; // O MODELO AGORA É STRING!
    
    // NOVO: Map para armazenar o objeto Document completo, usando a String de exibição como chave.
    private Map<String, Document> mapSolicitacoes; 
    private Document solicitacaoSelecionada;


    
    // --- CONSTRUTOR ---
    public JFAprovacao() {
        initComponents(); 
        
         
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        // Garante que a tela pode ser fechada, redimensionada e movida
        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gerenciar Aprovações");  
        
        // Inicializa o Map e carrega a lista
        mapSolicitacoes = new HashMap<>(); 
        carregarLista(controller.getAprovacoes());
        
        // Configurações do Frame
        setTitle("Gerenciar Aprovações");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        
        // Oculta o botão de documentos inicialmente
        btnDocumentos.setVisible(false); 
        
        this.setVisible(true);
    }
    
    // --- MÉTODOS DE DADOS E LAYOUT (CORRIGIDOS) ---
    
     
    private void carregarLista(List<Document> lista) {
        listModel = new DefaultListModel<>();  
        mapSolicitacoes.clear(); // Limpa o mapa a cada recarregamento
        
        for (Document d : lista) {
            String id = d.getString("_id");
            String razaoSocial = d.getString("razaoSocial");
            String nomeFantasia = d.getString("nomeFantasia");
            String status = d.containsKey("dataAprovacao") ? " (APROVADO)" : "";
            
             
            String chaveExibicao = id + " | " + razaoSocial + nomeFantasia + status;
            
            listModel.addElement(chaveExibicao);
            mapSolicitacoes.put(chaveExibicao, d); // Mapeia a string para o Document
        }
        
        jListAprovacoes.setModel(listModel); 
    }

    private void exibirDetalhes(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            
            String chaveSelecionada = jListAprovacoes.getSelectedValue();
            
            // 1. Limpeza e Verificação Inicial
            if (chaveSelecionada == null || mapSolicitacoes.isEmpty()) {
                TADetalhesArea.setText("Selecione uma solicitação.");
                btnDocumentos.setVisible(false);
                solicitacaoSelecionada = null;
                return;
            }
            
            // 2. BUSCA O DOCUMENTO COMPLETO NO MAP USANDO A CHAVE SELECIONADA
            solicitacaoSelecionada = mapSolicitacoes.get(chaveSelecionada);
            
            // Garante que o documento foi encontrado
            if (solicitacaoSelecionada == null) return;

            // Obtém o status dos documentos
            boolean temDocumentos = solicitacaoSelecionada.getBoolean("documentosEnviados", false);
            String documentosStatus = temDocumentos ? "SIM" : "NÃO";
            
            // 3. Montagem do Texto (CORRIGIDO O FORMATO DA STRING)
        String detalhes = String.format(
            "ID: %s\nRazão Social: %s\nNome Fantasia: %s\nCNPJ: %s\nRepresentante Legal: %s\nCausa Social: %s\nDescrição: %s\nEndereço: %s\nEmail: %s\n\nSTATUS DOCUMENTOS: %s",
            solicitacaoSelecionada.getString("_id"), 
            solicitacaoSelecionada.getString("razaoSocial"),
            solicitacaoSelecionada.getString("nomeFantasia"),
            solicitacaoSelecionada.getString("cnpj"),
            solicitacaoSelecionada.getString("repLegal"),
            solicitacaoSelecionada.getString("causaSocial"),
            solicitacaoSelecionada.getString("descricao"),
            solicitacaoSelecionada.getString("endereco"),
            solicitacaoSelecionada.getString("email"),
            documentosStatus
        );
        TADetalhesArea.setText(detalhes);
            // 4. CONTROLE DA VISIBILIDADE DO BOTÃO
            btnDocumentos.setVisible(temDocumentos); 
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTFPesquisa = new javax.swing.JTextField();
        btnPesquisar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListAprovacoes = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        TADetalhesArea = new javax.swing.JTextArea();
        btnAprovar = new javax.swing.JButton();
        btnReprovar = new javax.swing.JButton();
        btnDocumentos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Pesquisar:");

        btnPesquisar.setText("Pesquisar");
        btnPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(297, 297, 297)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTFPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPesquisar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTFPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisar))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jListAprovacoes.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jListAprovacoes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAprovacoesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListAprovacoes);

        TADetalhesArea.setColumns(20);
        TADetalhesArea.setRows(5);
        jScrollPane2.setViewportView(TADetalhesArea);

        btnAprovar.setText("Aprovar");
        btnAprovar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAprovarActionPerformed(evt);
            }
        });

        btnReprovar.setText("Reprovar");
        btnReprovar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReprovarActionPerformed(evt);
            }
        });

        btnDocumentos.setText("Mostrar Documentos");
        btnDocumentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDocumentosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAprovar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnReprovar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDocumentos)))))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAprovar)
                    .addComponent(btnReprovar)
                    .addComponent(btnDocumentos))
                .addContainerGap(44, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jListAprovacoesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAprovacoesValueChanged
        // TODO add your handling code here:
        exibirDetalhes(evt);
    }//GEN-LAST:event_jListAprovacoesValueChanged

    private void btnAprovarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAprovarActionPerformed
        // TODO add your handling code here:
        if (solicitacaoSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione um registro para aprovar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final String idSolicitacao = solicitacaoSelecionada.getString("_id");

        if (controller.aprovar(idSolicitacao)) {
             JOptionPane.showMessageDialog(this, 
                "Solicitação de ID " + idSolicitacao + " APROVADA.", 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
             
             // Limpa e recarrega a lista para refletir o status atualizado
             carregarLista(controller.getAprovacoes());
        } else {
             JOptionPane.showMessageDialog(this, "Erro ao tentar aprovar no MongoDB.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAprovarActionPerformed

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
final String termo = jTFPesquisa.getText(); 
        carregarLista(controller.filtrarAprovacoes(termo));
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void btnReprovarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReprovarActionPerformed
    if (solicitacaoSelecionada == null) {
        JOptionPane.showMessageDialog(this, "Selecione um registro para aprovar.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
 // Obtém o _id da solicitação
    // ATENÇÃO: Se o _id na lista de aprovação for uma String, use getString("_id")
    final String idSolicitacao = solicitacaoSelecionada.getObjectId("_id").toString(); 
    
    // Chamada ao método atualizado do Controller
    if (controller.aprovarONG(idSolicitacao)) {
         JOptionPane.showMessageDialog(this, 
            "Solicitação APROVADA! Registro da ONG criado e perfil do usuário atualizado.", 
            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
         
         // Recarrega a lista
         carregarLista(controller.getAprovacoes());
         // Limpa a área de detalhes
         TADetalhesArea.setText(""); 
    } else {
         JOptionPane.showMessageDialog(this, "Erro FATAL ao aprovar/inserir a ONG.", "Erro", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnReprovarActionPerformed

    private void btnDocumentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDocumentosActionPerformed
        // TODO add your handling code here:
      if (solicitacaoSelecionada != null) {
            final String razaoSocial = solicitacaoSelecionada.getString("razaoSocial");
            
            // Chama a tela que simula a visualização dos PDFs
            new JFDocumentosFrame(razaoSocial).setVisible(true);
        }
    }//GEN-LAST:event_btnDocumentosActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new JFAprovacao().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TADetalhesArea;
    private javax.swing.JButton btnAprovar;
    private javax.swing.JButton btnDocumentos;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnReprovar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jListAprovacoes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTFPesquisa;
    // End of variables declaration//GEN-END:variables
}
