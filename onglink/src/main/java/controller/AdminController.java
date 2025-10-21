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
    
    public List<Document> filtrarContas(String termoBusca) {
        final String termo = termoBusca.toLowerCase(); 
        
        return model.carregarContas().stream()
            .filter(doc -> {
                
                // 1. FILTRO PRINCIPAL: Código do Usuário (CodUsuario)

                if (doc.getInteger("CodUsuario") != null && String.valueOf(doc.getInteger("CodUsuario")).contains(termo)) {
                    return true;
                }
                
                // 2. Filtro por Nome de Usuário
                if (doc.getString("usuario") != null && doc.getString("usuario").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 3. Filtro por CNPJ
                if (doc.getString("cnpj") != null && doc.getString("cnpj").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 4. Filtro por Perfil
                if (doc.getString("perfil") != null && doc.getString("perfil").toLowerCase().contains(termo)) {
                    return true;
                }
                
                return false;
            })
            .collect(Collectors.toList());
    }
    
    
    public List<Document> filtrarPublicacoes(String termoBusca) {
        final String termo = termoBusca.toLowerCase(); 
        
        return model.carregarPublicacoes().stream()
            .filter(doc -> {
                // 1. Filtro por Título e Texto (Strings)
                if (doc.getString("Titulo") != null && doc.getString("Titulo").toLowerCase().contains(termo)) {
                    return true;
                }
                if (doc.getString("Texto") != null && doc.getString("Texto").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 2. Filtro por Códigos (Integer -> String)
                if (doc.getInteger("CodPubli") != null && String.valueOf(doc.getInteger("CodPubli")).contains(termo)) {
                    return true;
                }
                if (doc.getInteger("CodUsuario") != null && String.valueOf(doc.getInteger("CodUsuario")).contains(termo)) {
                    return true;
                }
                
                return false;
            })
            .collect(Collectors.toList());
    }
}