package dao;

import models.Medicament;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
/**
 * DAO pour la gestion des médicaments
 */
public class MedicamentDAO {
    
    // ============ MÉTHODES EXISTANTES (gardées intactes) ============
    
    /**
     * Ajoute un médicament
     */
    public boolean add(Medicament medicament) {
        String sql = "INSERT INTO medicament (nom, dosage, stock, prix_unitaire, seuil_alerte, description) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, medicament.getNom());
            pstmt.setString(2, medicament.getDosage());
            pstmt.setInt(3, medicament.getStock());
            pstmt.setDouble(4, medicament.getPrixUnitaire());
            pstmt.setInt(5, medicament.getSeuilAlerte());
            pstmt.setString(6, medicament.getDescription());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medicament.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Récupère un médicament par son ID
     */
    public Medicament getById(int id) {
        String sql = "SELECT * FROM medicament WHERE id_medicament = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMedicament(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les médicaments
     */
    public List<Medicament> getAll() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Recherche des médicaments par nom
     */
    public List<Medicament> searchByName(String searchTerm) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE nom LIKE ? OR description LIKE ? ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments disponibles (stock > 0)
     */
    public List<Medicament> getMedicamentsDisponibles() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE stock > 0 ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médicaments disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments en stock critique
     */
    public List<Medicament> getStockCritique() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE stock <= seuil_alerte ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
            System.out.println("✅ " + medicaments.size() + " médicaments en stock critique trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du stock critique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Met à jour un médicament
     */
    public boolean update(Medicament medicament) {
        String sql = "UPDATE medicament SET nom = ?, dosage = ?, stock = ?, prix_unitaire = ?, seuil_alerte = ?, description = ? WHERE id_medicament = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, medicament.getNom());
            pstmt.setString(2, medicament.getDosage());
            pstmt.setInt(3, medicament.getStock());
            pstmt.setDouble(4, medicament.getPrixUnitaire());
            pstmt.setInt(5, medicament.getSeuilAlerte());
            pstmt.setString(6, medicament.getDescription());
            pstmt.setInt(7, medicament.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Met à jour le stock d'un médicament
     */
    public boolean updateStock(int idMedicament, int newStock) {
        String sql = "UPDATE medicament SET stock = ? WHERE id_medicament = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, idMedicament);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du stock: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Diminue le stock d'un médicament
     */
    public boolean diminuerStock(int idMedicament, int quantite) {
        String sql = "UPDATE medicament SET stock = stock - ? WHERE id_medicament = ? AND stock >= ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantite);
            pstmt.setInt(2, idMedicament);
            pstmt.setInt(3, quantite);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la diminution du stock: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Augmente le stock d'un médicament
     */
    public boolean augmenterStock(int idMedicament, int quantite) {
        String sql = "UPDATE medicament SET stock = stock + ? WHERE id_medicament = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantite);
            pstmt.setInt(2, idMedicament);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'augmentation du stock: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Supprime un médicament
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM medicament WHERE id_medicament = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ============ NOUVELLES MÉTHODES TRANSACTIONNELLES ============
    
    /**
     * Diminue le stock d'un médicament (version transactionnelle)
     * @param conn Connection déjà ouverte (pour transaction)
     */
    public boolean diminuerStockTransaction(int idMedicament, int quantite, Connection conn) throws SQLException {
        String sql = "UPDATE medicament SET stock = stock - ? WHERE id_medicament = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantite);
            pstmt.setInt(2, idMedicament);
            return pstmt.executeUpdate() > 0;
        }
    }
    /**
     * Augmente le stock d'un médicament (version transactionnelle)
     * @param conn Connection déjà ouverte (pour transaction)
     */
    public boolean augmenterStockTransaction(int idMedicament, int quantite, Connection conn) throws SQLException {
        String sql = "UPDATE medicament SET stock = stock + ? WHERE id_medicament = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantite);
            pstmt.setInt(2, idMedicament);
            return pstmt.executeUpdate() > 0;
        }
    }
    /**
     * Met à jour le stock d'un médicament (version transactionnelle)
     * @param conn Connection déjà ouverte (pour transaction)
     */
    public boolean updateStockTransaction(int idMedicament, int newStock, Connection conn) throws SQLException {
        String sql = "UPDATE medicament SET stock = ? WHERE id_medicament = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, idMedicament);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet Medicament
     */
    private Medicament mapResultSetToMedicament(ResultSet rs) throws SQLException {
        Medicament medicament = new Medicament();
        medicament.setId(rs.getInt("id_medicament"));
        medicament.setNom(rs.getString("nom"));
        medicament.setDosage(rs.getString("dosage"));
        medicament.setStock(rs.getInt("stock"));
        medicament.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        medicament.setSeuilAlerte(rs.getInt("seuil_alerte"));
        medicament.setDescription(rs.getString("description"));
        return medicament;
    }
 // Ajoutez ces méthodes à votre MedicamentDAO existant

    /**
     * Diminue le stock avec vérification et retourne le nouveau stock
     */
    public int diminuerStockAvecRetour(int idMedicament, int quantite) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Vérifier le stock actuel
            String checkSql = "SELECT stock FROM medicament WHERE id_medicament = ? FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idMedicament);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    int stockActuel = rs.getInt("stock");
                    
                    if (stockActuel < quantite) {
                        conn.rollback();
                        return -1; // Stock insuffisant
                    }
                    
                    // 2. Diminuer le stock
                    String updateSql = "UPDATE medicament SET stock = stock - ? WHERE id_medicament = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, quantite);
                        updateStmt.setInt(2, idMedicament);
                        
                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            // 3. Récupérer le nouveau stock
                            String getSql = "SELECT stock FROM medicament WHERE id_medicament = ?";
                            try (PreparedStatement getStmt = conn.prepareStatement(getSql)) {
                                getStmt.setInt(1, idMedicament);
                                ResultSet rs2 = getStmt.executeQuery();
                                if (rs2.next()) {
                                    int nouveauStock = rs2.getInt("stock");
                                    conn.commit();
                                    return nouveauStock;
                                }
                            }
                        }
                    }
                }
            }
            conn.rollback();
            return -1;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            System.err.println("❌ Erreur diminuerStockAvecRetour: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }

    /**
     * Augmente le stock et retourne le nouveau stock
     */
    public int augmenterStockAvecRetour(int idMedicament, int quantite) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Augmenter le stock
            String updateSql = "UPDATE medicament SET stock = stock + ? WHERE id_medicament = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, quantite);
                updateStmt.setInt(2, idMedicament);
                
                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    // 2. Récupérer le nouveau stock
                    String getSql = "SELECT stock FROM medicament WHERE id_medicament = ?";
                    try (PreparedStatement getStmt = conn.prepareStatement(getSql)) {
                        getStmt.setInt(1, idMedicament);
                        ResultSet rs = getStmt.executeQuery();
                        if (rs.next()) {
                            int nouveauStock = rs.getInt("stock");
                            conn.commit();
                            return nouveauStock;
                        }
                    }
                }
            }
            conn.rollback();
            return -1;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            System.err.println("❌ Erreur augmenterStockAvecRetour: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }
 // ============ NOUVELLES MÉTHODES POUR RÉAPPROVISIONNEMENT ============

    /**
     * Récupère les médicaments à réapprovisionner (stock <= seuil d'alerte)
     */
    public List<Medicament> getMedicamentsAReapprovisionner() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE stock <= seuil_alerte ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
            System.out.println("✅ " + medicaments.size() + " médicaments à réapprovisionner trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des médicaments à réapprovisionner: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }

    /**
     * Récupère les médicaments en stock critique (stock <= 30% du seuil)
     */
    public List<Medicament> getMedicamentsCritiques() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE stock <= ROUND(seuil_alerte * 0.3) ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
            System.out.println("⚠️ " + medicaments.size() + " médicaments critiques trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des médicaments critiques: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }

    /**
     * Récupère les médicaments urgents (stock <= 10% du seuil)
     */
    public List<Medicament> getMedicamentsUrgents() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE stock <= ROUND(seuil_alerte * 0.1) ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapResultSetToMedicament(rs));
            }
            
            System.out.println("🚨 " + medicaments.size() + " médicaments urgents trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des médicaments urgents: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }

    /**
     * Calcule les statistiques de réapprovisionnement
     */
    public Map<String, Object> getStatsReapprovisionnement() {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = "SELECT " +
                     "COUNT(CASE WHEN stock <= seuil_alerte THEN 1 END) as a_reapprovisionner, " +
                     "COUNT(CASE WHEN stock <= ROUND(seuil_alerte * 0.3) THEN 1 END) as critiques, " +
                     "COUNT(CASE WHEN stock <= ROUND(seuil_alerte * 0.1) THEN 1 END) as urgents, " +
                     "SUM(CASE WHEN stock <= seuil_alerte " +
                     "THEN (seuil_alerte * 2 - stock) * prix_unitaire ELSE 0 END) as valeur_totale " +
                     "FROM medicament";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("a_reapprovisionner", rs.getInt("a_reapprovisionner"));
                stats.put("critique", rs.getInt("critiques"));
                stats.put("urgent", rs.getInt("urgents"));
                stats.put("valeur_totale", rs.getDouble("valeur_totale"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du calcul des statistiques: " + e.getMessage());
            e.printStackTrace();
            
            // Valeurs par défaut en cas d'erreur
            stats.put("a_reapprovisionner", 0);
            stats.put("critique", 0);
            stats.put("urgent", 0);
            stats.put("valeur_totale", 0.0);
        }
        
        return stats;
    }

    /**
     * Récupère les détails pour le réapprovisionnement
     */
    public List<Map<String, Object>> getDetailsReapprovisionnement() {
        List<Map<String, Object>> details = new ArrayList<>();
        
        String sql = "SELECT *, " +
                     "(seuil_alerte * 2 - stock) as quantite_a_commander, " +
                     "(seuil_alerte * 2 - stock) * prix_unitaire as prix_estime, " +
                     "CASE " +
                     "  WHEN stock <= ROUND(seuil_alerte * 0.1) THEN 'URGENT' " +
                     "  WHEN stock <= ROUND(seuil_alerte * 0.3) THEN 'CRITIQUE' " +
                     "  WHEN stock <= seuil_alerte THEN 'A_REAPPRO' " +
                     "  ELSE 'NORMAL' " +
                     "END as statut " +
                     "FROM medicament " +
                     "WHERE stock <= seuil_alerte " +
                     "ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> detail = new HashMap<>();
                
                // Informations du médicament
                detail.put("id", rs.getInt("id_medicament"));
                detail.put("nom", rs.getString("nom"));
                detail.put("dosage", rs.getString("dosage"));
                detail.put("stock_actuel", rs.getInt("stock"));
                detail.put("seuil", rs.getInt("seuil_alerte"));
                detail.put("difference", rs.getInt("seuil_alerte") - rs.getInt("stock"));
                
                // Calculs pour réapprovisionnement
                int quantiteACommander = rs.getInt("quantite_a_commander");
                detail.put("quantite_a_commander", Math.max(quantiteACommander, 10)); // Minimum 10 unités
                
                double prixEstime = rs.getDouble("prix_estime");
                detail.put("prix_estime", Math.max(prixEstime, 0));
                
                detail.put("statut", rs.getString("statut"));
                
                details.add(detail);
            }
            
            System.out.println("📊 " + details.size() + " détails de réapprovisionnement chargés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des détails: " + e.getMessage());
            e.printStackTrace();
        }
        
        return details;
    }
    
}