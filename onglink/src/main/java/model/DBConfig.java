package model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DBConfig {
     
    //private static final String CONNECTION_STRING = "mongodb+srv://felipe_paes:onglink_fatec@onglinkdb.9kxqdci.mongodb.net/?retryWrites=true&w=majority&appName=onglinkDb";
    private static final String CONNECTION_STRING = "mongodb+srv://felipe_paes:onglink_fatec@onglinkdb.ljvrr5b.mongodb.net/onglinkDb";
    private static final String DATABASE_NAME = "onglinkDb";

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Conectado ao MongoDB: " + DATABASE_NAME);
            } catch (Exception e) {
                System.err.println("Erro ao conectar ao MongoDB. Verifique a string de conexão e o acesso à rede.");
                e.printStackTrace();
            }
        }
        return database;
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Conexão MongoDB fechada.");
        }
    }
}