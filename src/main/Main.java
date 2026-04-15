package main;

import javax.swing.*;
import views.LoginFrame;
import utils.DatabaseManager;
import utils.LoggerUtil;
import java.sql.Connection;

/**
 * Classe principale de l'application Pharmacy Manager
 * Point d'entrée de l'application avec initialisation de la BD et de l'interface
 */
public class Main {

    public static void main(String[] args) {
        // Configuration du look and feel
        setLookAndFeel();

        // Initialisation de la base de données
        if (!initializeDatabase()) {
            System.err.println("❌ Impossible de démarrer l'application sans connexion BD");
            System.exit(1);
            return;
        }

        // Lancement de l'application
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                LoggerUtil.info("Application démarrée avec succès");
            } catch (Exception e) {
                System.err.println("❌ Erreur au démarrage de l'application: " + e.getMessage());
                e.printStackTrace();
                showStartupError(e);
            }
        });
    }

    /**
     * Configure le Look and Feel de l'application
     */
    private static void setLookAndFeel() {
        try {
            // Utiliser le look natif du système
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Configuration des polices modernes
            java.awt.Font segoeUI = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
            UIManager.put("Button.font", segoeUI);
            UIManager.put("Label.font", segoeUI);
            UIManager.put("TextField.font", segoeUI);
            UIManager.put("ComboBox.font", segoeUI);
            UIManager.put("Table.font", segoeUI);
            UIManager.put("TableHeader.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            
            LoggerUtil.info("Look and Feel configuré avec succès");
            
        } catch (ClassNotFoundException e) {
            System.err.println("⚠ Classe Look and Feel non trouvée: " + e.getMessage());
        } catch (InstantiationException e) {
            System.err.println("⚠ Impossible d'instancier le Look and Feel: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("⚠ Accès interdit au Look and Feel: " + e.getMessage());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("⚠ Look and Feel non supporté: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("⚠ Erreur inattendue lors de la configuration du Look and Feel: " + e.getMessage());
        }
    }

    /**
     * Initialise et teste la connexion à la base de données
     * @return true si la connexion réussit, false sinon
     */
    private static boolean initializeDatabase() {
        Connection conn = null;
        try {
            System.out.println("🔧 Initialisation de la base de données...");
            DatabaseManager dbManager = DatabaseManager.getInstance();

            // Test de connexion
            conn = dbManager.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                // Récupération des informations de la BD
                String dbName = conn.getCatalog();
                String dbProduct = conn.getMetaData().getDatabaseProductName();
                String dbVersion = conn.getMetaData().getDatabaseProductVersion();
                String dbUrl = conn.getMetaData().getURL();
                
                System.out.println("✅ Base de données connectée:");
                System.out.println("   - Nom: " + dbName);
                System.out.println("   - Type: " + dbProduct);
                System.out.println("   - Version: " + dbVersion);
                System.out.println("   - URL: " + dbUrl);
                
                LoggerUtil.info("Connexion à la base de données réussie: " + dbName);
                return true;
                
            } else {
                System.err.println("❌ Connexion à la base de données fermée ou nulle");
                showDatabaseError(new Exception("Connexion invalide"));
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ ERREUR DE CONNEXION BASE DE DONNÉES:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            
            LoggerUtil.error("Erreur de connexion à la base de données: " + e.getMessage());
            showDatabaseError(e);
            return false;
            
        } finally {
            // Fermeture propre de la connexion de test
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    System.err.println("⚠ Erreur lors de la fermeture de la connexion test: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Affiche un message d'erreur de connexion BD à l'utilisateur
     */
    private static void showDatabaseError(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        
        JOptionPane.showMessageDialog(null,
            "<html><div style='width: 400px;'>" +
            "<b style='color: red; font-size: 14px;'>Impossible de se connecter à la base de données</b><br><br>" +
            "<b>Vérifiez les points suivants:</b><br>" +
            "✓ MySQL Server est démarré<br>" +
            "✓ Le port 3306 (ou 3300) est accessible<br>" +
            "✓ La base 'pharmacie_db' existe<br>" +
            "✓ Les identifiants sont corrects (user/password)<br>" +
            "✓ Le pare-feu n'est pas bloqué<br><br>" +
            "<b>Erreur technique:</b><br>" +
            "<span style='color: #666;'>" + errorMessage + "</span>" +
            "</div></html>",
            "Erreur de Connexion - Pharmacy Manager",
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Affiche un message d'erreur de démarrage à l'utilisateur
     */
    private static void showStartupError(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        
        JOptionPane.showMessageDialog(null,
            "<html><div style='width: 400px;'>" +
            "<b style='color: red;'>Erreur au démarrage de l'application</b><br><br>" +
            errorMessage +
            "</div></html>",
            "Erreur - Pharmacy Manager",
            JOptionPane.ERROR_MESSAGE);
    }
}