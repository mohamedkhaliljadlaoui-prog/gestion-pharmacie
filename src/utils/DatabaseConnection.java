// [file name]: DatabaseConnection.java
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe pour gérer la connexion à la base de données MySQL
 */
public class DatabaseConnection {
    
    // Modifier ces paramètres selon votre configuration
    private static final String URL = "jdbc:mysql://localhost:3300/pharmacie_db";
    private static final String USER = "root";
    private static final String PASSWORD = "kh99032027!";
    
    private static final String URL_WITH_OPTIONS = URL + 
        "?useSSL=false" +
        "&serverTimezone=UTC" +
        "&allowPublicKeyRetrieval=true" +
        "&useUnicode=true" +
        "&characterEncoding=utf8" +
        "&autoReconnect=true";
    
    /**
     * Obtient la connexion à la base de données
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver JDBC MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Créer la connexion
            Connection connection = DriverManager.getConnection(URL_WITH_OPTIONS, USER, PASSWORD);
            
            System.out.println("✅ Connexion à la base de données établie avec succès");
            return connection;
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            System.err.println("  Vérifiez que mysql-connector-java.jar est dans le Build Path");
            throw new SQLException("Driver MySQL introuvable", e);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données:");
            System.err.println("  URL: " + URL);
            System.err.println("  User: " + USER);
            System.err.println("  Message: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("✅ Connexion à la base de données fermée");
            }
        } catch (SQLException e) {
            System.err.println("⚠ Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
    
    /**
     * Teste la connexion à la base de données
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            boolean isValid = conn != null && !conn.isClosed();
            
            if (isValid) {
                System.out.println("✅ Test de connexion réussi!");
                System.out.println("  Base de données: " + conn.getCatalog());
            }
            
            return isValid;
            
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
            return false;
        } finally {
            closeConnection(conn);
        }
    }
}