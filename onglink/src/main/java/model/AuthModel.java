package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class AuthModel {
    private final MongoCollection<Document> collection;

    public AuthModel() {
        MongoDatabase db = DBConfig.getDatabase();
        this.collection = db.getCollection("usuarios"); 
        
        // Criando usuários de teste para os 3 perfis se a coleção estiver vazia
        if (collection.countDocuments() == 0) {
            System.out.println("Criando usuários de teste (ADMIN, ONG, USER)...");
            
            // ADMIN (Pode logar no sistema)
            collection.insertOne(new Document("usuario", "admin").append("senha", "12345").append("perfil", "ADMIN"));
            
            // ONG (Não pode logar no sistema)
            collection.insertOne(new Document("usuario", "ong_teste").append("senha", "ong123").append("perfil", "ONG"));
            
            // USER (Não pode logar no sistema)
            collection.insertOne(new Document("usuario", "user_teste").append("senha", "user123").append("perfil", "USER"));
        }
    }

    public boolean autenticar(String usuario, String senha) {
        Document userDoc = collection.find(and(
            eq("usuario", usuario),
            eq("senha", senha)
        )).first();

        return userDoc != null;
    }

    /**
     * Verifica se o usuário tem o perfil de ADMIN. 
     * Somente ADMIN pode acessar o painel de gestão.
     */
    public boolean isAdmin(String usuario) {
        Document userDoc = collection.find(eq("usuario", usuario)).first();
        if (userDoc != null) {
            // A regra é simples: o campo 'perfil' deve ser "ADMIN"
            String perfil = userDoc.getString("perfil");
            return "ADMIN".equalsIgnoreCase(perfil); 
        }
        return false;
    }
    
    /**
     * Retorna o perfil de um usuário autenticado.
     */
    public String getPerfil(String usuario) {
         Document userDoc = collection.find(eq("usuario", usuario)).first();
         return userDoc != null ? userDoc.getString("perfil") : null;
    }
}