package controller;

import model.SolicitacaoModel;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class AdminController {
    private final SolicitacaoModel model;

    public AdminController() {
        this.model = new SolicitacaoModel();
    }

    // --- Aprovação ---

    public List<Document> getAprovacoes() {
        return model.carregarAprovacoes();
    }
    
    public List<Document> filtrarAprovacoes(String termoBusca) {
        final String termo = termoBusca.toLowerCase(); 
        
        return model.carregarAprovacoes().stream()
                .filter(doc -> doc.getString("razaoSocial").toLowerCase().contains(termo) ||
                                doc.getString("_id").toLowerCase().contains(termo) ||
                                doc.getString("endereco").toLowerCase().contains(termo) ||
                                doc.getString("dataSolicitacao").contains(termo))
                .collect(Collectors.toList());
    }

    public boolean aprovar(String id) {
        return model.aprovarSolicitacao(id);
    }

    // --- Denúncia ---

    public List<Document> getDenuncias() {
        return model.carregarDenuncias();
    }

    public List<Document> filtrarDenuncias(String termoBusca) {
        final String termo = termoBusca.toLowerCase(); 
        
        return model.carregarDenuncias().stream()
                .filter(doc -> doc.getString("razaoSocial").toLowerCase().contains(termo) ||
                                doc.getString("_id").toLowerCase().contains(termo) ||
                                doc.getString("cnpj").contains(termo) ||
                                doc.getString("endereco").toLowerCase().contains(termo) ||
                                doc.getString("dataDenuncia").contains(termo))
                .collect(Collectors.toList());
    }
    
    public boolean removerPublicacao(String idDenuncia) {
        return model.removerPublicacao(idDenuncia);
    }

    // ===============================================
    // GESTÃO DE CONTAS E PERFIS
    // ===============================================
    

    public boolean setPerfil(String usuarioId, String perfil) {
        // Chama o método de atualização do Model com o novo perfil escolhido
        return model.atualizarPerfilUsuario(usuarioId, perfil);
    }
    
    /**
     * Lógica de alternância (opcional, mantida para compatibilidade).
     */
    public boolean alternarPerfil(String usuarioId, String perfilAtual) {
        String novoPerfil = perfilAtual.equalsIgnoreCase("ADMIN") ? "USER" : "ADMIN"; 
        return model.atualizarPerfilUsuario(usuarioId, novoPerfil);
    }
    
    // --- Consulta ---

    public List<Document> getContas() {
        return model.carregarContas();
    }
    
    public List<Document> getPublicacoes() {
        return model.carregarPublicacoes();
    }
}