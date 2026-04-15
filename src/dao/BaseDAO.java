package dao;

import utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    protected DatabaseManager dbManager;
    
    public BaseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    protected List<T> executeQuery(String sql, Object... params) {
        List<T> results = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setParameters(pstmt, params);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
            
        } catch (SQLException e) {
            handleSQLException("Erreur lors de l'exécution de la requête", e, sql);
        }
        
        return results;
    }
    
    protected T executeQuerySingle(String sql, Object... params) {
        List<T> results = executeQuery(sql, params);
        return results.isEmpty() ? null : results.get(0);
    }
    
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setParameters(pstmt, params);
            int affectedRows = pstmt.executeUpdate();
            
            // Récupérer l'ID généré si INSERT
            if (affectedRows > 0 && sql.trim().toUpperCase().startsWith("INSERT")) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
            return affectedRows;
            
        } catch (SQLException e) {
            handleSQLException("Erreur lors de la mise à jour", e, sql);
            return 0;
        }
    }
    
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
    
    private void handleSQLException(String message, SQLException e, String sql) {
        System.err.println("❌ " + message);
        System.err.println("   Requête: " + sql);
        System.err.println("   Erreur SQL: " + e.getMessage());
        System.err.println("   Code SQL: " + e.getErrorCode());
        System.err.println("   État SQL: " + e.getSQLState());
    }
}