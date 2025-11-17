package controller;

import java.util.ArrayList;
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
        String novoStatus = statusAtual.equalsIgnoreCase("admin") ? "user" : "admin"; 
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
    
    public List<Document> getOngs() {
        return model.carregarOngs();
    }
    
    /**
    * Busca um Documento de usuário pelo seu _id.
    */
    public Document getUsuarioById(ObjectId userId) {
       return model.getUsuarioById(userId);
    }
    
    
    /**
 * Busca uma ONG pelo seu ObjectId, acessando o método do Model.
 */
    public Document getOngById(ObjectId ongId) {
        // Assume que o Model tem o método getOngById implementado
        return model.getOngById(ongId);
    }
    
    // --- Reprovação ---
    public boolean reprovar(String id) {
        // Chama o método no Model para reprovar a solicitação
        return model.reprovarSolicitacao(id);
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
                if (doc.getString("cpf") != null && doc.getString("cpf").toLowerCase().contains(termo)) {
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
    
    
    public List<Document> getAssignedUsersDetails(Document ongDoc) {
    if (ongDoc == null) {
        return new ArrayList<>();
    }

    // 1. Extração da Lista de ObjectIds (Assumed structure)
    // Garante que o campo assignedTo é lido como uma Lista de ObjectIds
    List<ObjectId> userIds = ongDoc.getList("assignedTo", ObjectId.class);

    if (userIds == null || userIds.isEmpty()) {
        return new ArrayList<>();
    }

    // 2. Chama o método OTIMIZADO que busca todos os IDs em uma única consulta
    return model.getUsersByIds(userIds); 
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


public List<Document> filtrarOngs(String termoBusca) {
        final String termo = termoBusca.toLowerCase(); 
        
        return model.carregarOngs().stream()
            .filter(doc -> {
                
                // 1. Filtro por Nome (campo 'nome' minúsculo)
                if (doc.getString("nomeFantasia") != null && doc.getString("nomeFantasia").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 2. Filtro por CNPJ (campo 'cnpj' minúsculo)
                if (doc.getString("cnpj") != null && doc.getString("cnpj").contains(termo)) {
                    return true;
                }
                
                // 3. Filtro por Causa Social (campo 'causaSocial' minúsculo)
                if (doc.getString("causaSocial") != null && doc.getString("causaSocial").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 4. Filtro por ID do MongoDB (_id)
                if (doc.getObjectId("_id") != null && doc.getObjectId("_id").toString().toLowerCase().contains(termo)) {
                    return true;
                }
                
                //5. Filtro por Descrição
                if (doc.getString("descricao") != null && doc.getString("descricao").toString().contains(termo)){
                        return true;
                }
                
                // 6. Filtro por Nome (campo 'razaoSocial' minúsculo)
                if (doc.getString("razaoSocial") != null && doc.getString("razaoSocial").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 7. Filtro por Nome (campo 'email' minúsculo)
                if (doc.getString("email") != null && doc.getString("email").toLowerCase().contains(termo)) {
                    return true;
                }
                
                // 1. Filtro por Nome (campo 'nome' minúsculo)
                if (doc.getString("cpf") != null && doc.getString("cpf").toLowerCase().contains(termo)) {
                    return true;
                }
                
                return false;
            })
            .collect(Collectors.toList());
    }


    // Método para atualizar campos de uma solicitação PENDENTE
    public boolean atualizarDadosSolicitacao(String solicitacaoId, Document updates) {
        return model.atualizarDadosSolicitacao(solicitacaoId, updates);
    }

    // Método para aprovar (move o documento)
    public boolean aprovarONG(String solicitacaoId) {
        // Aqui você pode chamar o método 'aprovar' existente se ele já faz a lógica de mover e deletar.
        return model.aprovarSolicitacao(solicitacaoId); 
    }

    // Método para reprovar (apenas deleta)
    public boolean reprovarONG(String solicitacaoId) {
        return model.reprovarSolicitacao(solicitacaoId); // Renomeei para maior clareza
    }
    
    public boolean atualizarDadosONG(String ongId, Document updates) {
        // Assume que o Model possui o método atualizarDadosONG para a coleção 'ongs'
        return model.atualizarDadosONG(ongId, updates); 
    }
    
}