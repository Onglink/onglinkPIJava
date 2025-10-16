package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class SolicitacaoModel {
    private final MongoCollection<Document> aprovacaoCollection;
    private final MongoCollection<Document> denunciaCollection;
    private final MongoCollection<Document> publicacaoCollection;
    private final MongoCollection<Document> usuarioCollection;

    // Construtor: Inicializa as coleções e garante que dados de teste existam.
    public SolicitacaoModel() {
        MongoDatabase db = DBConfig.getDatabase();
        this.aprovacaoCollection = db.getCollection("solicitacoes_aprovacao");
        this.denunciaCollection = db.getCollection("solicitacoes_denuncia");
        this.publicacaoCollection = db.getCollection("publicacoes");
        this.usuarioCollection = db.getCollection("usuarios");

        // Garante que o banco de dados tenha dados iniciais para as tabelas.
        if (aprovacaoCollection.countDocuments() == 0) {
            popularAprovacoes();
        }
        if (denunciaCollection.countDocuments() == 0) {
            popularDenuncias();
        }
        if (publicacaoCollection.countDocuments() == 0) {
            popularPublicacoes();
        }
    }

    // ===============================================
    // MÉTODOS DE ADMINISTRAÇÃO (Aprovação)
    // ===============================================

    /**
     * Carrega todas as solicitações de aprovação do banco.
     */
    public List<Document> carregarAprovacoes() {
        List<Document> list = new ArrayList<>();
        aprovacaoCollection.find().into(list);
        return list;
    }

    /**
     * Marca uma solicitação de aprovação como aprovada no banco.
     */
    public boolean aprovarSolicitacao(String id) {
        try {
            aprovacaoCollection.updateOne(
                eq("_id", id),
                new Document("$set", new Document("dataAprovacao", LocalDate.now().toString()))
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao aprovar solicitação: " + e.getMessage());
            return false;
        }
    }

    // ===============================================
    // MÉTODOS DE ADMINISTRAÇÃO (Denúncia)
    // ===============================================

    /**
     * Carrega todas as solicitações de denúncia do banco.
     */
    public List<Document> carregarDenuncias() {
        List<Document> list = new ArrayList<>();
        denunciaCollection.find().into(list);
        return list;
    }

    /**
     * Simula a remoção de uma publicação após a aprovação da denúncia.
     */
    public boolean removerPublicacao(String idDenuncia) {
        try {
            // Marca a denúncia como resolvida/publicação removida
            denunciaCollection.updateOne(
                eq("_id", idDenuncia),
                new Document("$set", new Document("status", "RESOLVIDO - Publicação Removida"))
            );
            // Em um sistema real, aqui chamaria a lógica para remover o item da coleção 'publicacoes'
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao remover publicação: " + e.getMessage());
            return false;
        }
    }

    // ===============================================
    // MÉTODOS DE ADMINISTRAÇÃO (Consulta e Gestão de Contas)
    // ===============================================

    /**
     * Carrega todas as contas (usuários) cadastradas.
     */
    public List<Document> carregarContas() {
        List<Document> list = new ArrayList<>();
        usuarioCollection.find().into(list);
        return list;
    }

    /**
     * Carrega todas as publicações cadastradas.
     */
    public List<Document> carregarPublicacoes() {
        List<Document> list = new ArrayList<>();
        publicacaoCollection.find().into(list);
        return list;
    }

    /**
     * Atualiza o campo 'perfil' de um usuário no banco (ADMIN, USER, ONG).
     */
    public boolean atualizarPerfilUsuario(String usuarioId, String novoPerfil) {
        try {
            // Converte a String ID para o tipo ObjectId do MongoDB
            ObjectId objectId = new ObjectId(usuarioId);
            
            usuarioCollection.updateOne(
                eq("_id", objectId), 
                new Document("$set", new Document("perfil", novoPerfil)) 
            );
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar o perfil do usuário: " + e.getMessage());
            return false;
        }
    }

    // ===============================================
    // MÉTODOS DE POPULAÇÃO (Apenas para Teste Inicial)
    // ===============================================

    private void popularAprovacoes() {
        aprovacaoCollection.insertOne(new Document("_id", "A001")
                .append("razaoSocial", "Empresa Alpha Ltda")
                .append("endereco", "Rua A, 100")
                .append("dataSolicitacao", "2025-09-01")
                .append("email", "alpha@email.com")
                .append("documentosEnviados", true));
        aprovacaoCollection.insertOne(new Document("_id", "A002")
                .append("razaoSocial", "Comércio Beta ME")
                .append("endereco", "Av B, 50")
                .append("dataSolicitacao", "2025-09-15")
                .append("email", "beta@email.com")
                .append("documentosEnviados", false));
    }

    private void popularDenuncias() {
        denunciaCollection.insertOne(new Document("_id", "D001")
                .append("razaoSocial", "Loja Delta")
                .append("cnpj", "11.111.111/0001-11")
                .append("endereco", "Rua X")
                .append("dataDenuncia", "2025-09-20")
                .append("linkPublicacao", "http://link.pub/123")
                .append("email", "delta@email.com"));
    }

    private void popularPublicacoes() {
        publicacaoCollection.insertOne(
                new Document("id", 101).append("razaoSocial", "Alpha Ltda").append("cnpj", "00.000.000/0001-00")
            );
        publicacaoCollection.insertOne(
            new Document("id", 102).append("razaoSocial", "Comércio Beta").append("cnpj", "11.111.111/0001-11")
        );
    }
}