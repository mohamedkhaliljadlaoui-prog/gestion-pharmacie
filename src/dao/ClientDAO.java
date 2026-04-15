// [file name]: ClientDAO.java
package dao;

import models.Client;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des clients
 */
public class ClientDAO {
    
    /**
     * Ajoute un client
     */
    public boolean add(Client client) {
        String sql = "INSERT INTO client (nom, prenom, email, adresse, telephone) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, client.getNom());
            pstmt.setString(2, client.getPrenom());
            pstmt.setString(3, client.getEmail());
            pstmt.setString(4, client.getAdresse());
            pstmt.setString(5, client.getTelephone());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        client.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du client: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Récupère un client par son ID
     */
    public Client getById(int id) {
        String sql = "SELECT * FROM client WHERE id_client = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du client: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les clients
     */
    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des clients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return clients;
    }
    
    /**
     * Recherche des clients par nom ou prénom
     */
    public List<Client> search(String searchTerm) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            pstmt.setString(3, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des clients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return clients;
    }
    
    /**
     * Met à jour un client
     */
    public boolean update(Client client) {
        String sql = "UPDATE client SET nom = ?, prenom = ?, email = ?, adresse = ?, telephone = ? WHERE id_client = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getNom());
            pstmt.setString(2, client.getPrenom());
            pstmt.setString(3, client.getEmail());
            pstmt.setString(4, client.getAdresse());
            pstmt.setString(5, client.getTelephone());
            pstmt.setInt(6, client.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du client: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Supprime un client
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM client WHERE id_client = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Mappe un ResultSet vers un objet Client
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id_client"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setEmail(rs.getString("email"));
        client.setAdresse(rs.getString("adresse"));
        client.setTelephone(rs.getString("telephone"));
        return client;
    }
}