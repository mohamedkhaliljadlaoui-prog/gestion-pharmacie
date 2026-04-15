package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private String url;
    private String user;
    private String password;
    private List<Connection> connectionPool;
    private static final int POOL_SIZE = 5;
    
    private DatabaseManager() {
        this.url = "jdbc:mysql://localhost:3300/pharmacie_db" +
                  "?useSSL=false" +
                  "&serverTimezone=UTC" +
                  "&allowPublicKeyRetrieval=true" +
                  "&useUnicode=true" +
                  "&characterEncoding=UTF-8";
        this.user = "root";
        this.password = "kh99032027!";
        this.connectionPool = new ArrayList<>();
        initializePool();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializePool() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            System.out.println("🔧 Initialisation du pool de connexions...");
            
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(url, user, password);
                connectionPool.add(conn);
            }
            
            System.out.println("✅ Pool de connexions initialisé (" + connectionPool.size() + " connexions)");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            System.err.println("   Ajoutez mysql-connector-java.jar au classpath");
            throw new RuntimeException("Driver MySQL introuvable", e);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'initialisation du pool: " + e.getMessage());
            System.err.println("   URL: " + url);
            System.err.println("   User: " + user);
            throw new RuntimeException("Erreur de connexion à la base", e);
        }
    }
    
    public synchronized Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            // Créer une nouvelle connexion si le pool est vide
            return DriverManager.getConnection(url, user, password);
        }
        
        // Prendre une connexion du pool
        Connection conn = connectionPool.remove(connectionPool.size() - 1);
        
        // Vérifier que la connexion est toujours valide
        if (conn.isClosed() || !conn.isValid(2)) {
            conn.close();
            return getConnection(); // Récursivité pour obtenir une connexion valide
        }
        
        return conn;
    }
    
    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (connectionPool.size() < POOL_SIZE) {
                        connectionPool.add(conn);
                    } else {
                        conn.close();
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠ Erreur lors de la libération de la connexion: " + e.getMessage());
            }
        }
    }
    
    public void closeAllConnections() {
        System.out.println("🔌 Fermeture de toutes les connexions...");
        
        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("⚠ Erreur fermeture connexion: " + e.getMessage());
            }
        }
        connectionPool.clear();
        
        System.out.println("✅ Toutes les connexions fermées");
    }
    
    public boolean testConnection() {
        try (Connection testConn = getConnection()) {
            boolean isValid = testConn != null && !testConn.isClosed();
            releaseConnection(testConn);
            
            if (isValid) {
                System.out.println("✅ Test de connexion réussi!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
        }
        return false;
    }
}