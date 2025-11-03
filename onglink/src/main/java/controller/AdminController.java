package controller;

import model.SolicitacaoModel;
import org.bson.Document;
import org.bson.types.ObjectId;

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
    // GESTÃO DE CONTAS E STATUS (CORRIGIDO)
    // ===============================================
    
    /**
     * Define o Status de um usuário com um valor específico (ADMIN, USER, ONG).
     */
    public boolean setStatus(String _id, String status) {
        // CORREÇÃO: Chamando o método renomeado no Model
        return model.atualizarStatusUsuario(_id, status); 
    }
    
    /**
     * Lógica de alternância (opcional, mantida para compatibilidade).
     */
    public boolean alternarStatus(String usuarioId, String statusAtual) {
        String novoStatus = statusAtual.equalsIgnoreCase("ADMIN") ? "USER" : "ADMIN"; 
        // CORREÇÃO: Chamando o método renomeado no Model
        return model.atualizarStatusUsuario(usuarioId, novoStatus); 
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
                // Filtro por _id (ID)
                if (doc.getObjectId("_id") != null && doc.getObjectId("_id").toString().toLowerCase().contains(termo)) {
                    return true;
                }
                
                // Filtro por Nome (campo 'nome' minúsculo)
                if (doc.getString("nome") != null && doc.getString("nome").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // Filtro por CNPJ (campo 'cnpj' minúsculo)
                if (doc.getString("cnpj") != null && doc.getString("cpf").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // Filtro por Status (campo 'status' minúsculo)
                if (doc.getString("status") != null && doc.getString("status").toLowerCase().contains(termo)) {
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
                
                // --- 1. Filtro por Título e Descrição (Minúsculo) ---
                if (doc.getString("titulo") != null && doc.getString("titulo").toLowerCase().contains(termo)) {
                    return true;
                }
                // O campo 'Texto' no seu código Java corresponde a 'descricao' no seu MongoDB
                if (doc.getString("descricao") != null && doc.getString("descricao").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // --- 2. Filtro por IDs (ObjectIds) ---
                
                // Filtro pelo ID da Publicação (_id)
                if (doc.getObjectId("_id") != null && doc.getObjectId("_id").toString().toLowerCase().contains(termo)) {
                    return true;
                }
                
                // Filtro pelo ID do Criador (criadoPor)
                if (doc.get("criadoPor") instanceof ObjectId) {
                    if (doc.getObjectId("criadoPor").toString().toLowerCase().contains(termo)) {
                        return true;
                    }
                }
                
                return false;
            })
            .collect(Collectors.toList());
    }
}