// [file name]: StockHistoriqueDAO.java - Version corrigée
package dao;

import models.StockHistorique;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * DAO pour la gestion de l'historique de stock
 */
public class StockHistoriqueDAO {
    
    /**
     * Récupère tout l'historique
     */
    public List<StockHistorique> getAll() {
        List<StockHistorique> historique = new ArrayList<>();
        String sql = """
            SELECT 
                sh.id_historique,
                sh.id_medicament,
                sh.quantite_avant,
                sh.quantite_apres,
                sh.type_operation,
                sh.id_operation,
                sh.date_operation,
                m.nom as medicament_nom
            FROM stock_historique sh
            LEFT JOIN medicament m ON sh.id_medicament = m.id_medicament
            ORDER BY sh.date_operation DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                historique.add(mapResultSetToHistorique(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historique;
    }
    
    /**
     * Récupère l'historique pour un médicament
     */
    public List<StockHistorique> getByMedicament(int idMedicament) {
        List<StockHistorique> historique = new ArrayList<>();
        String sql = """
            SELECT 
                sh.id_historique,
                sh.id_medicament,
                sh.quantite_avant,
                sh.quantite_apres,
                sh.type_operation,
                sh.id_operation,
                sh.date_operation,
                m.nom as medicament_nom
            FROM stock_historique sh
            LEFT JOIN medicament m ON sh.id_medicament = m.id_medicament
            WHERE sh.id_medicament = ?
            ORDER BY sh.date_operation DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idMedicament);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                historique.add(mapResultSetToHistorique(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historique;
    }
    
    /**
     * Récupère l'historique par type d'opération
     */
    public List<StockHistorique> getByTypeOperation(String typeOperation) {
        List<StockHistorique> historique = new ArrayList<>();
        String sql = """
            SELECT 
                sh.id_historique,
                sh.id_medicament,
                sh.quantite_avant,
                sh.quantite_apres,
                sh.type_operation,
                sh.id_operation,
                sh.date_operation,
                m.nom as medicament_nom
            FROM stock_historique sh
            LEFT JOIN medicament m ON sh.id_medicament = m.id_medicament
            WHERE sh.type_operation = ?
            ORDER BY sh.date_operation DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, typeOperation);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                historique.add(mapResultSetToHistorique(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique par type: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historique;
    }
    
    /**
     * Récupère l'historique sur une période
     */
    public List<StockHistorique> getByPeriode(Date dateDebut, Date dateFin) {
        List<StockHistorique> historique = new ArrayList<>();
        String sql = """
            SELECT 
                sh.id_historique,
                sh.id_medicament,
                sh.quantite_avant,
                sh.quantite_apres,
                sh.type_operation,
                sh.id_operation,
                sh.date_operation,
                m.nom as medicament_nom
            FROM stock_historique sh
            LEFT JOIN medicament m ON sh.id_medicament = m.id_medicament
            WHERE DATE(sh.date_operation) BETWEEN ? AND ?
            ORDER BY sh.date_operation DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(dateDebut.getTime()));
            pstmt.setDate(2, new java.sql.Date(dateFin.getTime()));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                historique.add(mapResultSetToHistorique(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return historique;
    }
    
    /**
     * Ajoute un enregistrement d'historique
     */
    public boolean add(StockHistorique historique) {
        String sql = """
            INSERT INTO stock_historique 
            (id_medicament, quantite_avant, quantite_apres, type_operation, id_operation)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, historique.getIdMedicament());
            pstmt.setInt(2, historique.getQuantiteAvant());
            pstmt.setInt(3, historique.getQuantiteApres());
            pstmt.setString(4, historique.getTypeOperation());
            pstmt.setInt(5, historique.getIdOperation());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        historique.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Mappe un ResultSet vers un objet StockHistorique
     */
    private StockHistorique mapResultSetToHistorique(ResultSet rs) throws SQLException {
        StockHistorique historique = new StockHistorique();
        historique.setId(rs.getInt("id_historique"));
        historique.setIdMedicament(rs.getInt("id_medicament"));
        historique.setQuantiteAvant(rs.getInt("quantite_avant"));
        historique.setQuantiteApres(rs.getInt("quantite_apres"));
        historique.setTypeOperation(rs.getString("type_operation"));
        historique.setIdOperation(rs.getInt("id_operation"));
        historique.setDateOperation(rs.getTimestamp("date_operation"));
        historique.setMedicamentNom(rs.getString("medicament_nom"));
        return historique;
    }
}