package utils;
import java.sql.*;

public class TestMySQL {
    public static void main(String[] args) {
        System.out.println("=== TEST MYSQL DIRECT ===");
        
        // Configuration
        String url = "jdbc:mysql://localhost:3300/pharmacie_db";
        String user = "root";
        String password = "kh99032027!";
        
        Connection conn = null;
        
        try {
            // 1. Charger le driver MANUELLEMENT
            System.out.println("1. Chargement du driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("   ✅ Driver chargé");
            
            // 2. Se connecter
            System.out.println("\n2. Connexion à MySQL...");
            System.out.println("   Port: 3300");
            System.out.println("   User: root");
            
            String fullUrl = url + "?useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(fullUrl, user, password);
            
            System.out.println("   ✅ Connecté !");
            System.out.println("   Base: " + conn.getCatalog());
            
            // 3. Tester
            System.out.println("\n3. Test des tables...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            
            System.out.println("   Tables trouvées:");
            while (rs.next()) {
                System.out.println("   - " + rs.getString(1));
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("\n🎉 SUCCÈS TOTAL !");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ DRIVER NON TROUVÉ");
            System.err.println("Le JAR MySQL n'est pas dans le classpath");
            System.err.println("Ajoutez: mysql-connector-j-9.5.0.jar");
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL: " + e.getMessage());
            System.err.println("Code: " + e.getErrorCode());
            
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("\n🔌 Connexion fermée");
                } catch (SQLException e) {
                    System.err.println("Erreur fermeture: " + e.getMessage());
                }
            }
        }
    }
} 