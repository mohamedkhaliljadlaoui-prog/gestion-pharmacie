package dao;

import models.Vente;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class VenteDAO {

    // ======================
    // AJOUTER UNE VENTE
    // ======================
    public boolean add(Vente vente) {

        String sql = "INSERT INTO vente " +
                "(id_pharmacien, id_client, id_medicament, quantite, prix_total, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, vente.getIdPharmacien());
            pstmt.setInt(2, vente.getIdClient());
            pstmt.setInt(3, vente.getIdMedicament());
            pstmt.setInt(4, vente.getQuantite());
            pstmt.setDouble(5, vente.getPrixTotal());
            pstmt.setString(6, vente.getStatut());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    vente.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur insertion vente");
            e.printStackTrace();
        }

        return false;
    }

    // ======================
    // RÉCUPÉRER PAR ID
    // ======================
    public Vente getById(int id) {

        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE v.id_vente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ======================
    // TOUTES LES VENTES
    // ======================
    public List<Vente> getAll() {

        List<Vente> list = new ArrayList<>();

        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ======================
    // ANNULER UNE VENTE
    // ======================
    public boolean annuler(int idVente) {

        String sql = "UPDATE vente SET statut = 'annulee' WHERE id_vente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVente);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ======================
    // AJOUT DES MÉTHODES MANQUANTES
    // ======================

    /**
     * Récupère les ventes par pharmacien
     */
    public List<Vente> getByPharmacien(int idPharmacien) {
        List<Vente> list = new ArrayList<>();
        
        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE v.id_pharmacien = ? " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idPharmacien);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(map(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByPharmacien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Récupère les ventes par client
     */
    public List<Vente> getByClient(int idClient) {
        List<Vente> list = new ArrayList<>();
        
        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE v.id_client = ? " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idClient);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(map(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByClient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Récupère les ventes par médicament
     */
    public List<Vente> getByMedicament(int idMedicament) {
        List<Vente> list = new ArrayList<>();
        
        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE v.id_medicament = ? " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idMedicament);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(map(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByMedicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Récupère les ventes sur une période
     */
    public List<Vente> getByPeriode(java.sql.Date dateDebut, java.sql.Date dateFin) {
        List<Vente> list = new ArrayList<>();
        
        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE DATE(v.date_vente) BETWEEN ? AND ? " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, dateDebut);
            pstmt.setDate(2, dateFin);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                list.add(map(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByPeriode: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Récupère les ventes du jour
     */
    public List<Vente> getVentesAujourdhui() {
        List<Vente> list = new ArrayList<>();
        
        String sql = "SELECT v.*, p.nom AS pharmacien_nom, c.nom AS client_nom, m.nom AS medicament_nom " +
                "FROM vente v " +
                "LEFT JOIN pharmacien p ON v.id_pharmacien = p.id_pharmacien " +
                "LEFT JOIN client c ON v.id_client = c.id_client " +
                "LEFT JOIN medicament m ON v.id_medicament = m.id_medicament " +
                "WHERE DATE(v.date_vente) = CURDATE() " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(map(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getVentesAujourdhui: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Calcule le total des ventes sur une période
     */
    public double getTotalVentesPeriode(java.sql.Date dateDebut, java.sql.Date dateFin) {
        String sql = "SELECT SUM(prix_total) as total " +
                "FROM vente " +
                "WHERE DATE(date_vente) BETWEEN ? AND ? " +
                "AND statut = 'valide'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, dateDebut);
            pstmt.setDate(2, dateFin);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur getTotalVentesPeriode: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Compte le nombre de ventes sur une période
     */
    public int countVentesPeriode(java.sql.Date dateDebut, java.sql.Date dateFin) {
        String sql = "SELECT COUNT(*) as count " +
                "FROM vente " +
                "WHERE DATE(date_vente) BETWEEN ? AND ? " +
                "AND statut = 'valide'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, dateDebut);
            pstmt.setDate(2, dateFin);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur countVentesPeriode: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    // ======================
    // MAPPING (EXISTANT - NON MODIFIÉ)
    // ======================
    private Vente map(ResultSet rs) throws SQLException {

        Vente v = new Vente();
        v.setId(rs.getInt("id_vente"));
        v.setIdPharmacien(rs.getInt("id_pharmacien"));
        v.setIdClient(rs.getInt("id_client"));
        v.setIdMedicament(rs.getInt("id_medicament"));
        v.setQuantite(rs.getInt("quantite"));
        v.setPrixTotal(rs.getDouble("prix_total"));
        v.setDateVente(rs.getTimestamp("date_vente"));
        v.setStatut(rs.getString("statut"));

        v.setPharmacienNom(rs.getString("pharmacien_nom"));
        v.setClientNom(rs.getString("client_nom"));
        v.setMedicamentNom(rs.getString("medicament_nom"));

        return v;
    }
 // Ajoutez ces méthodes à votre VenteDAO existant

    /**
     * Ajoute une vente avec une connexion existante (pour transaction)
     */
    public boolean addAvecConnection(Vente vente, Connection conn) {
        String sql = "INSERT INTO vente " +
                "(id_pharmacien, id_client, id_medicament, quantite, prix_total, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, vente.getIdPharmacien());
            pstmt.setInt(2, vente.getIdClient());
            pstmt.setInt(3, vente.getIdMedicament());
            pstmt.setInt(4, vente.getQuantite());
            pstmt.setDouble(5, vente.getPrixTotal());
            pstmt.setString(6, vente.getStatut());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    vente.setId(rs.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur insertion vente avec connexion: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}