package view;

import javax.swing.*;
import model.DBConfig;
import java.awt.Dimension;
import java.awt.Toolkit;

public class MainApp {
    public static void main(String[] args) {
        // Inicializa a conexão com o banco de dados MongoDB 
// 1. Conexão MongoDB (Sempre deve ocorrer primeiro)
        DBConfig.getDatabase(); 
        Runtime.getRuntime().addShutdownHook(new Thread(model.DBConfig::closeConnection));

        // 2. Inicia o fluxo de interface gráfica na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            
            // --- CÓDIGO DA SPLASH SCREEN (SIMPLIFICADO) ---
            JFrame splashFrame = new JFrame();
            
            // CRÍTICO: Usa o JPanel Form que você desenhou (SplashScreen)
            try {
                splashFrame.setContentPane(new SplashScreen()); 
                splashFrame.setUndecorated(true); // Remove bordas e barra de título
                splashFrame.pack(); // Ajusta ao tamanho do JPanel
            } catch (Exception e) {
                // Fallback para caso o JPanel Form não carregue
                splashFrame.setTitle("Carregando...");
                splashFrame.setSize(700, 600);
            }
            
            // Centraliza o Frame na tela (Método clássico do Java)
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            splashFrame.setLocation(dim.width/2 - splashFrame.getSize().width/2, dim.height/2 - splashFrame.getSize().height/2);

            splashFrame.setVisible(true);

            // 3. Timer para Fechar a Splash e Abrir o Login (3 Segundos)
            Timer timer = new Timer(3000, evt -> {
                splashFrame.dispose(); // Fecha o JFrame Splash
                new JFLogin().setVisible(true); // Abre a tela de Login
            });
            timer.setRepeats(false);
            timer.start();
            // ----------------------------------------------------
        });
    }
}