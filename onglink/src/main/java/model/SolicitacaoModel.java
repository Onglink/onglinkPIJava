package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; // Necessário para a popularPublicacoes

import static com.mongodb.client.model.Filters.eq;

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

        // Garante que o banco de dados tenha dados iniciais para as tabelas.
        if (solicitacoes_aprovacaoCollection.countDocuments() == 0) {
            popularAprovacoes();
        }
        if (solicitacoes_denunciaCollection.countDocuments() == 0) {
            popularDenuncias();
        }
        if (publicacoesCollection.countDocuments() == 0) {
            popularPublicacoes();
        }
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
    // GESTÃO DE CONTAS (USANDO O NOVO NOME DE FUNÇÃO)
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

    // ===============================================
    // MÉTODOS DE POPULAÇÃO (AJUSTADOS PARA NOVOS NOMES DE COLEÇÃO)
    // ===============================================

    private void popularAprovacoes() {
        // Limpa a coleção antes de inserir novos dados de teste
    solicitacoes_aprovacaoCollection.deleteMany(new Document()); 
    
    // --- Exemplo 1: Solicitação Completa com Documentos ---
    solicitacoes_aprovacaoCollection.insertOne(new Document("_id", "A001")
            .append("razaoSocial", "Instituto Verde Esperança Ltda")
            .append("nomeFantasia", "Verde Esperança")
            .append("cnpj", "12.345.678/0001-99")
            .append("repLegal", "Ana Paula Souza")
            .append("causaSocial", "Meio Ambiente e Reflorestamento")
            .append("descricao", "Focados na recuperação de áreas degradadas e educação ambiental.")
            .append("endereco", "Rua Paes Leme, 50, Pinheiros - SP")
            .append("email", "contato@verdeesperanca.org")
            .append("documentosEnviados", true)); // DOCUMENTOS PRESENTES

    // --- Exemplo 2: Solicitação Simples, Sem Documentos ---
    solicitacoes_aprovacaoCollection.insertOne(new Document("_id", "A002")
            .append("razaoSocial", "Ação Social do Bairro")
            .append("nomeFantasia", "ASB")
            .append("cnpj", "99.887.766/0001-01")
            .append("repLegal", "Roberto Carlos Lima")
            .append("causaSocial", "Assistência Social e Moradia")
            .append("descricao", "Oferece apoio a famílias de baixa renda e cursos profissionalizantes.")
            .append("endereco", "Av. Brasil, 1200, Centro - RJ")
            .append("email", "social@asb.org")
            .append("documentosEnviados", false)); // DOCUMENTOS AUSENTES
    }

    private void popularDenuncias() {
        solicitacoes_denunciaCollection.insertOne(new Document("_id", "D001")
                .append("razaoSocial", "Loja Delta")
                .append("cnpj", "11.111.111/0001-11")
                .append("endereco", "Rua X")
                .append("dataDenuncia", "2025-09-20")
                .append("linkPublicacao", "http://link.pub/123")
                .append("email", "delta@email.com"));
    }

private void popularPublicacoes() {
    // Limpa a coleção para inserir novos exemplos estruturados
    publicacoesCollection.deleteMany(new Document()); 

    java.util.Date dataExemplo = new java.util.Date(); 
    
    // Supondo IDs de usuário/ONG criados no AuthModel (exemplo de ObjectId)
    ObjectId idCriadorExemplo1 = new ObjectId("6901539530ab44fc0e2f56d7"); 
    ObjectId idCriadorExemplo2 = new ObjectId("6901539530ab44fc0e2f56d8"); 

    // Exemplo 1: Publicação completa - Usando a capitalização minúscula
    Document pub1 = new Document("titulo", "Limpeza de Rio: Junte-se à Nossa Causa Ambiental!")
        .append("descricao", "Neste domingo, faremos a limpeza do Rio Tietê... Sua participação é fundamental.")
        .append("imagem", java.util.Arrays.asList(
            "https://seuservidor.com/imagens/rio_limpo_antes.jpg",
            "https://seuservidor.com/imagens/rio_limpo_mapa_encontro.jpg"
        ))
        .append("criadoPor", idCriadorExemplo1) // Usando o campo de referência correto
        .append("createdAt", dataExemplo);       // Usando o campo de data correto
    publicacoesCollection.insertOne(pub1);

    // Exemplo 2: Publicação simples
    Document pub2 = new Document("titulo", "Chamada Urgente para Doações")
        .append("descricao", "Precisamos de cobertores e alimentos não perecíveis para a campanha de inverno.")
        .append("imagem", java.util.Arrays.asList("url_simples.jpg"))
        .append("criadoPor", idCriadorExemplo2)
        .append("createdAt", dataExemplo);
    publicacoesCollection.insertOne(pub2);
}
}