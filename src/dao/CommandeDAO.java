package dao;

import models.Commande;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAO {
    
    /**
     * Récupère toutes les commandes
     */
    public List<Commande> getAll() {
        List<Commande> commandes = new ArrayList<>();
        String sql = """
            SELECT c.*, m.nom as medicament_nom 
            FROM commande c 
            JOIN medicament m ON c.id_medicament = m.id_medicament 
            ORDER BY c.date_commande DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return commandes;
    }
    
    /**
     * Crée une nouvelle commande
     */
    public boolean create(Commande commande) {
        String sql = "INSERT INTO commande (id_gestionnaire, id_medicament, quantite, statut) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, commande.getIdGestionnaire());
            stmt.setInt(2, commande.getIdMedicament());
            stmt.setInt(3, commande.getQuantite());
            stmt.setString(4, commande.getStatut());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        commande.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la commande: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Valide une commande
     */
    public boolean valider(int id) {
        String sql = "UPDATE commande SET statut = 'validee' WHERE id_commande = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la validation de la commande: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Marque une commande comme reçue
     */
    public boolean recevoir(int id) {
        String sql = "UPDATE commande SET statut = 'recue', date_reception = NOW() WHERE id_commande = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réception de la commande: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Annule une commande
     */
    public boolean annuler(int id) {
        String sql = "UPDATE commande SET statut = 'annulee' WHERE id_commande = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'annulation de la commande: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Convertit un ResultSet en objet Commande
     */
    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id_commande"));
        commande.setIdGestionnaire(rs.getInt("id_gestionnaire"));
        commande.setIdMedicament(rs.getInt("id_medicament"));
        commande.setQuantite(rs.getInt("quantite"));
        commande.setDateCommande(rs.getTimestamp("date_commande"));
        commande.setStatut(rs.getString("statut"));
        commande.setDateReception(rs.getTimestamp("date_reception"));
        commande.setMedicamentNom(rs.getString("medicament_nom"));
        return commande;
    }
}