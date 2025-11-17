package model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class AuthModel {
    private final MongoCollection<Document> usuariosCollection;

    public AuthModel() {
        MongoDatabase db = DBConfig.getDatabase();
        this.usuariosCollection = db.getCollection("usuarios"); 
        
        // Criando usuários de teste para os 3 perfis se a coleção estiver vazia
        if (usuariosCollection.countDocuments() == 0) {
            System.out.println("Criando usuários de teste (admin, ong, user)...");
            
            // ADMIN (Pode logar no sistema)
            usuariosCollection.insertOne(new Document("email", "admin@admin.com").append("senha", "12345").append("status", "admin"));
            
            // ONG (Não pode logar no sistema)
            usuariosCollection.insertOne(new Document("email", "ong_teste@teste.com").append("senha", "ong123").append("status", "ong"));
            
            // USER (Não pode logar no sistema)
            usuariosCollection.insertOne(new Document("email", "user_teste@teste.com").append("senha", "user123").append("status", "user"));
        }
    }

    public boolean autenticar(String email, String senha) {
        Document userDoc = usuariosCollection.find(and(
            eq("email", email),
            eq("senha", senha)
        )).first();

        return userDoc != null;
    }

    /**
     * Verifica se o usuário tem o status de ADMIN. 
     */
    public boolean isAdmin(String email) {
        Document userDoc = usuariosCollection.find(eq("email", email)).first();
        if (userDoc != null) {
            String status = userDoc.getString("status"); // Lendo campo 'status' minúsculo
            return "admin".equalsIgnoreCase(status); 
        }
        return false;
    }
    
    /**
     * Retorna o status de um usuário autenticado.
     */
    public String getStatus(String email) { // RENOMEADO para refletir 'status'
         Document userDoc = usuariosCollection.find(eq("email", email)).first();
         return userDoc != null ? userDoc.getString("status") : null;
    }
}