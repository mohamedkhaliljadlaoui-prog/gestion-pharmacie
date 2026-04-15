package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import dao.MedicamentDAO;
import dao.VenteDAO;
import models.Medicament;
import models.Vente;
import utils.DatabaseConnection;

public class VenteService {
    
    private VenteDAO venteDAO;
    private MedicamentDAO medicamentDAO;
    
    public VenteService() {
        this.venteDAO = new VenteDAO();
        this.medicamentDAO = new MedicamentDAO();
    }
    
    /**
     * Enregistre une nouvelle vente AVEC TRANSACTION
     */
    public boolean enregistrerVente(int idPharmacien, int idClient, int idMedicament, int quantite) {
        Connection conn = null;
        
        try {
            // Vérifications préalables
            if (quantite <= 0) {
                System.err.println("✗ La quantité doit être positive");
                return false;
            }
            
            // Récupérer le médicament pour vérification
            Medicament medicament = medicamentDAO.getById(idMedicament);
            if (medicament == null) {
                System.err.println("✗ Médicament introuvable");
                return false;
            }
            
            // Vérifier le stock disponible
            if (medicament.getStock() < quantite) {
                System.err.println("✗ Stock insuffisant!");
                System.err.println("  Disponible: " + medicament.getStock() + ", Demandé: " + quantite);
                return false;
            }
            
            // Calculer le prix total
            double prixTotal = medicament.getPrixUnitaire() * quantite;
            
            // Créer l'objet vente
            Vente vente = new Vente(idPharmacien, idClient, idMedicament, quantite, prixTotal);
            
            // Démarrer la transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                System.out.println("=== DÉBUT TRANSACTION ===");
                System.out.println("Médicament: " + medicament.getNom());
                System.out.println("Quantité: " + quantite);
                System.out.println("Stock avant: " + medicament.getStock());
                
                // ÉTAPE 1: Diminuer le stock AVEC TRANSACTION
                int nouveauStock = diminuerStockTransaction(idMedicament, quantite, conn);
                
                if (nouveauStock < 0) {
                    conn.rollback();
                    System.err.println("✗ Échec de la mise à jour du stock");
                    return false;
                }
                
                System.out.println("Stock après: " + nouveauStock);
                
                // ÉTAPE 2: Enregistrer la vente AVEC LA MÊME CONNEXION
                boolean venteEnregistree = ajouterVenteTransaction(vente, conn);
                
                if (!venteEnregistree) {
                    conn.rollback();
                    System.err.println("✗ Échec de l'enregistrement de la vente");
                    // Remettre le stock
                    augmenterStockTransaction(idMedicament, quantite, conn);
                    conn.rollback();
                    return false;
                }
                
                // TOUT EST BON - COMMIT
                conn.commit();
                
                System.out.println("✅ Vente enregistrée avec succès!");
                System.out.println("  ID Vente: " + vente.getId());
                System.out.println("  Médicament: " + medicament.getNom());
                System.out.println("  Quantité: " + quantite);
                System.out.println("  Prix total: " + prixTotal + " DT");
                System.out.println("  Nouveau stock: " + nouveauStock);
                System.out.println("=== FIN TRANSACTION ===");
                
                return true;
                
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.err.println("✗ Rollback effectué");
                    } catch (SQLException ex) {}
                }
                System.err.println("✗ Erreur transaction: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Erreur générale: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Diminue le stock avec transaction
     */
    private int diminuerStockTransaction(int idMedicament, int quantite, Connection conn) throws SQLException {
        // 1. Vérifier le stock actuel
        String checkSql = "SELECT stock FROM medicament WHERE id_medicament = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, idMedicament);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int stockActuel = rs.getInt("stock");
                System.out.println("  Vérification stock: " + stockActuel);
                
                if (stockActuel < quantite) {
                    System.err.println("  Stock insuffisant: " + stockActuel + " < " + quantite);
                    return -1;
                }
                
                // 2. Diminuer le stock
                String updateSql = "UPDATE medicament SET stock = stock - ? WHERE id_medicament = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantite);
                    updateStmt.setInt(2, idMedicament);
                    
                    int rows = updateStmt.executeUpdate();
                    if (rows == 0) {
                        System.err.println("  Aucune ligne mise à jour");
                        return -1;
                    }
                    
                    // 3. Récupérer le nouveau stock
                    try (PreparedStatement getStmt = conn.prepareStatement(checkSql)) {
                        getStmt.setInt(1, idMedicament);
                        ResultSet rs2 = getStmt.executeQuery();
                        if (rs2.next()) {
                            int nouveauStock = rs2.getInt("stock");
                            System.out.println("  Stock mis à jour: " + nouveauStock);
                            return nouveauStock;
                        }
                    }
                }
            } else {
                System.err.println("  Médicament non trouvé: " + idMedicament);
            }
        }
        return -1;
    }
    
    /**
     * Augmente le stock avec transaction
     */
    private void augmenterStockTransaction(int idMedicament, int quantite, Connection conn) throws SQLException {
        String sql = "UPDATE medicament SET stock = stock + ? WHERE id_medicament = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantite);
            pstmt.setInt(2, idMedicament);
            pstmt.executeUpdate();
            System.out.println("  Stock remis: +" + quantite);
        }
    }
    
    /**
     * Ajoute une vente avec transaction
     */
    private boolean ajouterVenteTransaction(Vente vente, Connection conn) throws SQLException {
        // Si votre table a date_vente, ajoutez-la
        String sql = "INSERT INTO vente " +
                "(id_pharmacien, id_client, id_medicament, quantite, prix_total, statut, date_vente) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, vente.getIdPharmacien());
            pstmt.setInt(2, vente.getIdClient());
            pstmt.setInt(3, vente.getIdMedicament());
            pstmt.setInt(4, vente.getQuantite());
            pstmt.setDouble(5, vente.getPrixTotal());
            pstmt.setString(6, vente.getStatut());
            // date_vente est gérée par NOW()
            
            int rows = pstmt.executeUpdate();
            System.out.println("Lignes insérées: " + rows); // Log ajouté
            
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    vente.setId(rs.getInt(1));
                    System.out.println("  Vente enregistrée ID: " + vente.getId());
                    return true;
                }
            }
            System.err.println("  Aucune ligne insérée");
            return false;
            
        } catch (SQLException e) {
            System.err.println("  Erreur insertion: " + e.getMessage());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            throw e;
        }
    }
    
    /**
     * Annule une vente AVEC TRANSACTION
     */
    public boolean annulerVente(int idVente) {
        Connection conn = null;
        
        try {
            // Récupérer la vente
            Vente vente = venteDAO.getById(idVente);
            if (vente == null) {
                System.err.println("✗ Vente introuvable");
                return false;
            }
            
            // Vérifier le statut
            if ("annulee".equalsIgnoreCase(vente.getStatut())) {
                System.err.println("✗ La vente est déjà annulée");
                return false;
            }
            
            // Récupérer le médicament
            Medicament medicament = medicamentDAO.getById(vente.getIdMedicament());
            if (medicament == null) {
                System.err.println("✗ Médicament introuvable");
                return false;
            }
            
            // Démarrer la transaction
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                System.out.println("=== ANNULATION VENTE ===");
                System.out.println("Vente ID: " + idVente);
                System.out.println("Médicament: " + medicament.getNom());
                System.out.println("Quantité à restituer: " + vente.getQuantite());
                System.out.println("Stock avant: " + medicament.getStock());
                
                // 1. Remettre le stock
                augmenterStockTransaction(vente.getIdMedicament(), vente.getQuantite(), conn);
                
                // 2. Récupérer le nouveau stock
                String checkSql = "SELECT stock FROM medicament WHERE id_medicament = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, vente.getIdMedicament());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int nouveauStock = rs.getInt("stock");
                        System.out.println("Stock après: " + nouveauStock);
                    }
                }
                
                // 3. Annuler la vente
                String updateSql = "UPDATE vente SET statut = 'annulee' WHERE id_vente = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, idVente);
                    int rows = pstmt.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        System.err.println("✗ Échec de l'annulation de la vente");
                        return false;
                    }
                }
                
                // COMMIT
                conn.commit();
                
                System.out.println("✅ Vente annulée avec succès!");
                System.out.println("=== FIN ANNULATION ===");
                
                return true;
                
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {}
                }
                System.err.println("✗ Erreur transaction annulation: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Erreur générale annulation: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ======================
    // MÉTHODES DE REQUÊTE SIMPLIFIÉES
    // ======================
    
    /**
     * Vérifie si une vente est possible
     */
    public String verifierVentePossible(int idMedicament, int quantite) {
        try {
            if (quantite <= 0) {
                return "La quantité doit être positive";
            }
            
            Medicament medicament = medicamentDAO.getById(idMedicament);
            if (medicament == null) {
                return "Médicament introuvable";
            }
            
            if (medicament.getStock() < quantite) {
                return "Stock insuffisant! Disponible: " + medicament.getStock();
            }
            
            return "OK";
            
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }
    
    /**
     * Récupère toutes les ventes
     */
    public List<Vente> getAllVentes() {
        return venteDAO.getAll();
    }
    
    /**
     * Récupère les ventes sur une période
     */
    public List<Vente> getVentesByPeriode(java.util.Date dateDebut, java.util.Date dateFin) {
        // Convertir java.util.Date en java.sql.Date
        java.sql.Date sqlDateDebut = new java.sql.Date(dateDebut.getTime());
        java.sql.Date sqlDateFin = new java.sql.Date(dateFin.getTime());
        return venteDAO.getByPeriode(sqlDateDebut, sqlDateFin);
    }
    
    /**
     * Récupère les ventes du jour
     */
    public List<Vente> getVentesAujourdhui() {
        return venteDAO.getVentesAujourdhui();
    }
    
    /**
     * Calcule le chiffre d'affaires sur une période
     */
    public double getChiffreAffaires(java.util.Date dateDebut, java.util.Date dateFin) {
        // Convertir java.util.Date en java.sql.Date
        java.sql.Date sqlDateDebut = new java.sql.Date(dateDebut.getTime());
        java.sql.Date sqlDateFin = new java.sql.Date(dateFin.getTime());
        double total = venteDAO.getTotalVentesPeriode(sqlDateDebut, sqlDateFin);
        return total;
    }
    
    /**
     * Compte le nombre de ventes sur une période
     */
    public int countVentes(java.util.Date dateDebut, java.util.Date dateFin) {
        // Convertir java.util.Date en java.sql.Date
        java.sql.Date sqlDateDebut = new java.sql.Date(dateDebut.getTime());
        java.sql.Date sqlDateFin = new java.sql.Date(dateFin.getTime());
        int count = venteDAO.countVentesPeriode(sqlDateDebut, sqlDateFin);
        return count;
    }
    
    /**
     * Calcule le chiffre d'affaires du jour
     */
    public double getChiffreAffairesAujourdhui() {
        java.util.Date today = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(today.getTime());
        double total = venteDAO.getTotalVentesPeriode(sqlDate, sqlDate);
        return total;
    }
    
    /**
     * Récupère une vente par ID
     */
    public Vente getVenteById(int idVente) {
        return venteDAO.getById(idVente);
    }
    
    /**
     * Récupère les ventes d'un pharmacien
     */
    public List<Vente> getVentesByPharmacien(int idPharmacien) {
        return venteDAO.getByPharmacien(idPharmacien);
    }
    
    /**
     * Récupère les ventes d'un client
     */
    public List<Vente> getVentesByClient(int idClient) {
        return venteDAO.getByClient(idClient);
    }
    
    /**
     * Récupère les ventes d'un médicament
     */
    public List<Vente> getVentesByMedicament(int idMedicament) {
        return venteDAO.getByMedicament(idMedicament);
    }
    
    /**
     * Récupère le stock disponible
     */
    public int getStockDisponible(int idMedicament) {
        try {
            Medicament medicament = medicamentDAO.getById(idMedicament);
            return medicament != null ? medicament.getStock() : -1;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du stock: " + e.getMessage());
            return -1;
        }
    }
    
    
}