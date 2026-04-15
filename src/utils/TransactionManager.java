// [file name]: TransactionManager.java
package utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestionnaire de transactions pour assurer l'intégrité des données
 */
public class TransactionManager {
    
    /**
     * Exécute une opération dans une transaction
     */
    public static boolean executeInTransaction(TransactionOperation operation) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Désactiver l'auto-commit
            
            boolean success = operation.execute(conn);
            
            if (success) {
                conn.commit();
                System.out.println("✅ Transaction commitée avec succès");
                return true;
            } else {
                conn.rollback();
                System.out.println("⚠ Transaction annulée");
                return false;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Erreur dans la transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Réactiver l'auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Interface pour les opérations de transaction
     */
    public interface TransactionOperation {
        boolean execute(Connection connection) throws SQLException;
    }
}