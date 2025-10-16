package view;

import javax.swing.SwingUtilities;
import model.DBConfig;

public class MainApp {
    public static void main(String[] args) {
        // Inicializa a conexão com o banco de dados MongoDB 
        DBConfig.getDatabase(); 

        // Inicia a aplicação na Thread de Eventos
        SwingUtilities.invokeLater(() -> {
            // Chama a tela de Login
            new JFLogin().setVisible(true); 
        });
    }
}