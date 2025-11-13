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
        if (ongsCollection.countDocuments() == 0){
            popularOngs();
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



    private void popularOngs(){
    ongsCollection.deleteMany(new Document()); 
    
    // Usamos IDs de exemplo para garantir ObjectIds válidos
    ObjectId idOng1 = new ObjectId("6901539530ab44fc0e2f56d7"); 
    ObjectId idOng2 = new ObjectId("6901539530ab44fc0e2f56d8"); 
    ObjectId idUsuarioAdmin = new ObjectId("65c345a98d02d0001f3b7d1c"); // Exemplo de ID de usuário

    // --- Endereço e Rede Social Aninhados ---
    Document endereco1 = new Document("rua", "Rua das Flores")
        .append("numeroEnd", "100")
        .append("cidade", "São Paulo")
        .append("cep", "05422000");
    
    Document redes1 = new Document("instagram", "@VerdeEsperanca")
        .append("site", "https://www.verdeesperanca.org");

    // --- ONG 1: Exemplo Detalhado ---
    ongsCollection.insertOne(new Document("_id", idOng1) 
            .append("razaoSocial", "Instituto Verde Esperança Ltda") // Novo
            .append("nomeFantasia", "Verde Esperança") // Novo
            .append("cnpj", "11.123.456/0001-00")
            .append("cpf", "111.222.333-44") // Novo
            .append("repLegal", "Roberto Fernandes Lima") // Novo
            .append("telefone", "15 3322-3322")
            .append("email", "contato@verde.org")
            .append("endereco", endereco1) // Objeto aninhado
            .append("redeSocial", redes1) // Objeto aninhado
            .append("descricao", "Focados na recuperação de áreas degradadas e educação ambiental.")
            .append("dataFund", "2008-10-15") // Novo formato
            .append("causaSocial", "Meio Ambiente") // Novo
            .append("assignedTo", Arrays.asList(idUsuarioAdmin))); // Novo Array
    
    // --- ONG 2: Exemplo Simples ---
    ongsCollection.insertOne(new Document("_id", idOng2)
            .append("razaoSocial", "Ação Social do Bairro Ltda") 
            .append("nomeFantasia", "ASB")
            .append("cnpj", "22.345.678/0001-11")
            .append("cpf", "555.666.777-88")
            .append("repLegal", "Maria da Silva")
            .append("telefone", "21 9988-7766")
            .append("email", "asb@social.org")
            .append("endereco", new Document("cidade", "Rio de Janeiro"))
            .append("descricao", "Oferece apoio a famílias de baixa renda.")
            .append("dataFund", "2015-05-20")
            .append("causaSocial", "Assistência Social"));
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
}