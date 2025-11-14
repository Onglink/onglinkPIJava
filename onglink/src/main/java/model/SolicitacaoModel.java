package model;

import static com.mongodb.client.model.Filters.in;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; // Necessário para a popularPublicacoes

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Updates;

public class SolicitacaoModel {
    // VARIÁVEIS DE COLEÇÃO CONFORME AS ÚLTIMAS ALTERAÇÕES
    private final MongoCollection<Document> solicitacoes_aprovacaoCollection;
    private final MongoCollection<Document> solicitacoes_denunciaCollection;
    private final MongoCollection<Document> denunciasCollection;
    private final MongoCollection<Document> publicacoesCollection;
    private final MongoCollection<Document> usuariosCollection;
    private final MongoCollection<Document> ongsCollection;

    public SolicitacaoModel() {
        MongoDatabase db = DBConfig.getDatabase();
        
        this.solicitacoes_aprovacaoCollection = db.getCollection("solicitacoes_aprovacao");
        this.solicitacoes_denunciaCollection = db.getCollection("solicitacoes_denuncia");
        this.denunciasCollection = db.getCollection("denuncias");
        this.publicacoesCollection = db.getCollection("publicacoes");
        this.usuariosCollection = db.getCollection("usuarios");
        this.ongsCollection = db.getCollection("ongs");

        
    }

    // ... [MÉTODOS DE ADMINISTRAÇÃO (Aprovação, Denúncia)] ...

    public List<Document> carregarAprovacoes() {
        List<Document> list = new ArrayList<>();
        solicitacoes_aprovacaoCollection.find().into(list);
        return list;
    }

    public boolean aprovarSolicitacao(String id) {
        try {
            solicitacoes_aprovacaoCollection.updateOne(
                eq("_id", id),
                new Document("$set", new Document("dataAprovacao", LocalDate.now().toString()))
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao aprovar solicitação: " + e.getMessage());
            return false;
        }
    }
    
    public boolean reprovarSolicitacao(String id) {
        try {
            ObjectId objId = new ObjectId(id);

            // A reprovação geralmente significa apenas REMOVER a solicitação da fila
            long deletedCount = solicitacoes_aprovacaoCollection.deleteOne(eq("_id", objId)).getDeletedCount();

            return deletedCount > 0;
        } catch (Exception e) {
            // Logar o erro
            return false;
        }
    }
    
    

    public List<Document> carregarDenuncias() {
        List<Document> list = new ArrayList<>();
        solicitacoes_denunciaCollection.find().into(list);
        return list;
    }

    public boolean removerPublicacao(String idDenuncia) {
        try {
            solicitacoes_denunciaCollection.updateOne(
                eq("_id", idDenuncia),
                new Document("$set", new Document("status", "RESOLVIDO - Publicação Removida"))
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao remover publicação: " + e.getMessage());
            return false;
        }
    }

    // ===============================================
    // GESTÃO DE CONTAS  
    // ===============================================
    
    public List<Document> carregarContas() {
        List<Document> list = new ArrayList<>();
        usuariosCollection.find().into(list);
        return list;
    }

    public List<Document> carregarPublicacoes() {
        List<Document> list = new ArrayList<>();
        publicacoesCollection.find().into(list);
        return list;
    }

    public List<Document> carregarOngs(){
        List<Document> list = new ArrayList<>();
        ongsCollection.find().into(list);
        return list;
    }    
    
    /**
    * Busca um Documento de usuário na coleção 'usuarios' pelo seu _id.
    */
    public Document getUsuarioById(ObjectId userId) {
       if (userId == null) {
           return null;
       }
       // Assume que 'usuariosCollection' já foi inicializada no construtor
       return usuariosCollection.find(eq("_id", userId)).first();
    }    
    
    
    //Ver depois para arrumar o de cima
    public List<Document> getUsersByIds(List<ObjectId> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        return new ArrayList<>();
    }
    
    List<Document> users = new ArrayList<>();
    
    // Filtra na coleção 'usuarios' onde o _id está CONTIDO na lista userIds
    usuariosCollection.find(in("_id", userIds)) // Usa o filtro $in
                      .into(users);
    
    return users;
}
    
    /**
     * Atualiza o campo 'status' de um usuário no banco (ADMIN, USER, ONG).
     */
    public boolean atualizarStatusUsuario(String usuarioId, String novoStatus) {
        try {
            // Conversão de ID e uso da coleção correta
            ObjectId objectId = new ObjectId(usuarioId);
            
            usuariosCollection.updateOne(
                eq("_id", objectId), 
                new Document("$set", new Document("status", novoStatus)) // Campo 'status' minúsculo
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar o status do usuário: " + e.getMessage());
            return false;
        }
    }

    
    
    public boolean aprovarEInserirONG(String solicitacaoId) {
    try {
        // 1. Localiza e converte o ID da solicitação
        ObjectId id = new ObjectId(solicitacaoId);
        Document solicitacaoDoc = solicitacoes_aprovacaoCollection.find(eq("_id", id)).first();
        
        if (solicitacaoDoc == null) {
            System.err.println("Solicitação não encontrada: " + solicitacaoId);
            return false;
        }

        // 2. Localiza o ID do Usuário e busca o Documento do usuário
        ObjectId userId = solicitacaoDoc.getObjectId("usuarioId"); // *** PRESSUPOSTO: campo existe! ***
        Document userDoc = getUsuarioById(userId); // Reutiliza o método getUsuarioById
        
        if (userDoc == null) {
            System.err.println("Usuário associado à solicitação não encontrado.");
            return false;
        }

        // 3. Monta o Documento da Nova ONG (Usando dados da Solicitacao E do Usuário)
        Document novaOng = new Document();
        
        // Copia campos da SOLICITAÇÃO (Razão Social, CNPJ, Causa Social, etc.)
        novaOng.append("razaoSocial", solicitacaoDoc.getString("razaoSocial"))
               .append("nomeFantasia", solicitacaoDoc.getString("nomeFantasia"))
               .append("cnpj", solicitacaoDoc.getString("cnpj"))
               .append("causaSocial", solicitacaoDoc.getString("causaSocial"))
               .append("telefone", solicitacaoDoc.getString("telefone"))
               .append("endereco", solicitacaoDoc.get("endereco")) // Mantém o objeto aninhado
               .append("redeSocial", solicitacaoDoc.get("redeSocial")) // Mantém o objeto aninhado
               .append("descricao", solicitacaoDoc.getString("descricao"))
               .append("dataFund", solicitacaoDoc.getString("dataFund")); // Mantém o valor original

        // SOBRESCREVE/INSERE CAMPOS DO USUÁRIO (CPF, Rep. Legal, Email)
        novaOng.append("repLegal", userDoc.getString("nome")) // Nome do usuário
               .append("cpf", userDoc.getString("cpf"))       // CPF do usuário
               .append("email", userDoc.getString("email"))   // Email do usuário
               .append("usuarioId", userId)                   // Mantém o link para o usuário
               .append("statusRegistro", "ATIVA") 
               .append("dataAprovacao", LocalDate.now().toString());

        // 4. Insere o novo documento na coleção de ONGs cadastradas
        ongsCollection.insertOne(novaOng);
        
        // 5. CRÍTICO: ATUALIZA O STATUS DO USUÁRIO para "ONG"
        usuariosCollection.updateOne(
            eq("_id", userId),
            Updates.set("status", "ONG")
        );

        // 6. Remove a solicitação original da fila
        solicitacoes_aprovacaoCollection.deleteOne(eq("_id", id));

        return true;

    } catch (Exception e) {
        System.err.println("Erro FATAL durante a aprovação e inserção da ONG: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
    
    
   
    
    
    // Método para atualizar campos de uma solicitação PENDENTE
    public boolean atualizarDadosSolicitacao(String solicitacaoId, Document updates) {
        try {
            ObjectId objectId = new ObjectId(solicitacaoId);

            solicitacoes_aprovacaoCollection.updateOne(
                eq("_id", objectId), // Usa ObjectId para buscar
                new Document("$set", updates)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados da solicitação: " + e.getMessage());
            return false;
        }
    }
    
    
    public boolean atualizarDadosONG(String ongId, Document updates) {
        try {
            // Converte a String ID para o tipo ObjectId do MongoDB
            ObjectId objectId = new ObjectId(ongId);

            // Assume que 'ongsCollection' está declarada e inicializada no construtor
            ongsCollection.updateOne(
                eq("_id", objectId), 
                new Document("$set", updates) // Aplica o Documento de updates
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados da ONG: " + e.getMessage());
            return false;
        }
    }
}