package dao;

import models.Pharmacien;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PharmacienDAO {
    
	public List<Pharmacien> getAll() {
	    List<Pharmacien> pharmaciens = new ArrayList<>();
	    String sql = "SELECT * FROM pharmacien";
	    
	    System.out.println("=== RÉCUPÉRATION PHARMACIENS ===");
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        
	        // Obtenir les métadonnées pour connaître les colonnes disponibles
	        ResultSetMetaData metaData = rs.getMetaData();
	        int columnCount = metaData.getColumnCount();
	        
	        System.out.println("Colonnes disponibles dans la table:");
	        for (int i = 1; i <= columnCount; i++) {
	            System.out.println("  " + i + ". " + metaData.getColumnName(i));
	        }
	        
	        boolean hasMatricule = false;
	        for (int i = 1; i <= columnCount; i++) {
	            if ("matricule".equalsIgnoreCase(metaData.getColumnName(i))) {
	                hasMatricule = true;
	                break;
	            }
	        }
	        
	        System.out.println("La table a la colonne 'matricule': " + hasMatricule);
	        
	        while (rs.next()) {
	            Pharmacien p = new Pharmacien();
	            p.setId(rs.getInt("id_pharmacien"));
	            p.setNom(rs.getString("nom"));
	            p.setPrenom(rs.getString("prenom"));
	            p.setLogin(rs.getString("login"));
	            p.setPwd(rs.getString("pwd"));
	            
	            if (hasMatricule) {
	                p.setMatricule(rs.getString("matricule"));
	            } else {
	                p.setMatricule("MAT" + p.getId()); // Valeur par défaut
	            }
	            
	            pharmaciens.add(p);
	            
	            System.out.println("Pharmacien #" + p.getId() + ": " + 
	                             p.getPrenom() + " " + p.getNom() + 
	                             " (Login: " + p.getLogin() + ")");
	        }
	        
	        System.out.println("Total: " + pharmaciens.size() + " pharmaciens");
	        
	    } catch (SQLException e) {
	        System.err.println("❌ Erreur PharmacienDAO.getAll: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return pharmaciens;
	}
    
    public boolean add(Pharmacien pharmacien) {
        String sql = "INSERT INTO pharmacien (nom, prenom, login, pwd, matricule) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, pharmacien.getNom());
            pstmt.setString(2, pharmacien.getPrenom());
            pstmt.setString(3, pharmacien.getLogin());
            pstmt.setString(4, pharmacien.getPwd());
            pstmt.setString(5, pharmacien.getMatricule());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pharmacien.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du pharmacien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean update(Pharmacien pharmacien) {
        String sql = "UPDATE pharmacien SET nom = ?, prenom = ?, login = ?, matricule = ? WHERE id_pharmacien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pharmacien.getNom());
            pstmt.setString(2, pharmacien.getPrenom());
            pstmt.setString(3, pharmacien.getLogin());
            pstmt.setString(4, pharmacien.getMatricule());
            pstmt.setInt(5, pharmacien.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du pharmacien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public Pharmacien getById(int id) {
        String sql = "SELECT * FROM pharmacien WHERE id_pharmacien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Pharmacien p = new Pharmacien();
                p.setId(rs.getInt("id_pharmacien"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setLogin(rs.getString("login"));
                p.setPwd(rs.getString("pwd"));
                p.setMatricule(rs.getString("matricule"));
                return p;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du pharmacien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM pharmacien WHERE id_pharmacien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du pharmacien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<Pharmacien> search(String searchTerm) {
        List<Pharmacien> pharmaciens = new ArrayList<>();
        String sql = "SELECT * FROM pharmacien WHERE nom LIKE ? OR prenom LIKE ? OR login LIKE ? OR matricule LIKE ? ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            pstmt.setString(3, "%" + searchTerm + "%");
            pstmt.setString(4, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Pharmacien p = new Pharmacien();
                p.setId(rs.getInt("id_pharmacien"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setLogin(rs.getString("login"));
                p.setPwd(rs.getString("pwd"));
                p.setMatricule(rs.getString("matricule"));
                pharmaciens.add(p);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche des pharmaciens: " + e.getMessage());
            e.printStackTrace();
        }
        
        return pharmaciens;
    }
}