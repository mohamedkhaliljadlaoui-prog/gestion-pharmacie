// [file name]: ReportService.java
package services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.sql.*;
import utils.DatabaseConnection;

/**
 * Service pour générer des rapports statistiques
 */
public class ReportService {
    
    /**
     * Génère un rapport des ventes par période
     */
    public Map<String, Object> getRapportVentes(Date dateDebut, Date dateFin) {
        Map<String, Object> rapport = new LinkedHashMap<>();
        
        String sql = """
            SELECT 
                DATE(v.date_vente) as date,
                COUNT(*) as nb_ventes,
                SUM(v.prix_total) as chiffre_affaires,
                SUM(v.quantite) as total_quantite,
                AVG(v.prix_total) as panier_moyen
            FROM vente v
            WHERE DATE(v.date_vente) BETWEEN ? AND ? 
            AND v.statut = 'valide'
            GROUP BY DATE(v.date_vente)
            ORDER BY date
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(dateDebut.getTime()));
            pstmt.setDate(2, new java.sql.Date(dateFin.getTime()));
            
            ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> ventesParJour = new java.util.ArrayList<>();
            double totalCA = 0;
            int totalVentes = 0;
            
            while (rs.next()) {
                Map<String, Object> jour = new HashMap<>();
                jour.put("date", rs.getDate("date"));
                jour.put("nb_ventes", rs.getInt("nb_ventes"));
                jour.put("chiffre_affaires", rs.getDouble("chiffre_affaires"));
                jour.put("total_quantite", rs.getInt("total_quantite"));
                jour.put("panier_moyen", rs.getDouble("panier_moyen"));
                
                ventesParJour.add(jour);
                totalCA += rs.getDouble("chiffre_affaires");
                totalVentes += rs.getInt("nb_ventes");
            }
            
            rapport.put("periode_debut", dateDebut);
            rapport.put("periode_fin", dateFin);
            rapport.put("ventes_par_jour", ventesParJour);
            rapport.put("total_chiffre_affaires", totalCA);
            rapport.put("total_ventes", totalVentes);
            rapport.put("panier_moyen_periode", totalVentes > 0 ? totalCA / totalVentes : 0);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du rapport ventes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rapport;
    }
    
    /**
     * Génère un rapport des médicaments les plus vendus
     */
    public Map<String, Object> getTopMedicaments(int limit) {
        Map<String, Object> rapport = new LinkedHashMap<>();
        
        String sql = """
            SELECT 
                m.nom,
                m.dosage,
                COUNT(v.id_vente) as nb_ventes,
                SUM(v.quantite) as total_quantite,
                SUM(v.prix_total) as chiffre_affaires
            FROM medicament m
            LEFT JOIN vente v ON m.id_medicament = v.id_medicament
            WHERE v.statut = 'valide'
            GROUP BY m.id_medicament, m.nom, m.dosage
            ORDER BY total_quantite DESC
            LIMIT ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> topMedicaments = new java.util.ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> medicament = new HashMap<>();
                medicament.put("nom", rs.getString("nom"));
                medicament.put("dosage", rs.getString("dosage"));
                medicament.put("nb_ventes", rs.getInt("nb_ventes"));
                medicament.put("total_quantite", rs.getInt("total_quantite"));
                medicament.put("chiffre_affaires", rs.getDouble("chiffre_affaires"));
                
                topMedicaments.add(medicament);
            }
            
            rapport.put("top_medicaments", topMedicaments);
            rapport.put("limit", limit);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du rapport top médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rapport;
    }
    
    /**
     * Génère un rapport du stock
     */
    public Map<String, Object> getRapportStock() {
        Map<String, Object> rapport = new LinkedHashMap<>();
        
        String sql = """
            SELECT 
                m.nom,
                m.dosage,
                m.stock,
                m.prix_unitaire,
                m.seuil_alerte,
                (m.stock * m.prix_unitaire) as valeur_stock,
                CASE 
                    WHEN m.stock <= m.seuil_alerte THEN 'CRITIQUE'
                    WHEN m.stock <= m.seuil_alerte * 2 THEN 'FAIBLE'
                    ELSE 'NORMAL'
                END as statut_stock
            FROM medicament m
            ORDER BY m.stock ASC, m.nom
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Map<String, Object>> stockDetail = new java.util.ArrayList<>();
            double valeurTotaleStock = 0;
            int medicamentsCritiques = 0;
            int medicamentsFaibles = 0;
            
            while (rs.next()) {
                Map<String, Object> medicament = new HashMap<>();
                medicament.put("nom", rs.getString("nom"));
                medicament.put("dosage", rs.getString("dosage"));
                medicament.put("stock", rs.getInt("stock"));
                medicament.put("prix_unitaire", rs.getDouble("prix_unitaire"));
                medicament.put("seuil_alerte", rs.getInt("seuil_alerte"));
                medicament.put("valeur_stock", rs.getDouble("valeur_stock"));
                medicament.put("statut_stock", rs.getString("statut_stock"));
                
                stockDetail.add(medicament);
                valeurTotaleStock += rs.getDouble("valeur_stock");
                
                String statut = rs.getString("statut_stock");
                if ("CRITIQUE".equals(statut)) {
                    medicamentsCritiques++;
                } else if ("FAIBLE".equals(statut)) {
                    medicamentsFaibles++;
                }
            }
            
            rapport.put("stock_detail", stockDetail);
            rapport.put("valeur_totale_stock", valeurTotaleStock);
            rapport.put("medicaments_critiques", medicamentsCritiques);
            rapport.put("medicaments_faibles", medicamentsFaibles);
            rapport.put("total_medicaments", stockDetail.size());
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du rapport stock: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rapport;
    }
    
    /**
     * Génère un rapport des clients fidèles
     */
    public Map<String, Object> getClientsFideles(int limit) {
        Map<String, Object> rapport = new LinkedHashMap<>();
        
        String sql = """
            SELECT 
                c.id_client,
                c.nom,
                c.prenom,
                c.email,
                COUNT(v.id_vente) as nb_achats,
                SUM(v.prix_total) as total_depense,
                MAX(v.date_vente) as dernier_achat
            FROM client c
            LEFT JOIN vente v ON c.id_client = v.id_client
            WHERE v.statut = 'valide'
            GROUP BY c.id_client, c.nom, c.prenom, c.email
            ORDER BY total_depense DESC
            LIMIT ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> clientsFideles = new java.util.ArrayList<>();
            double totalDepenses = 0;
            
            while (rs.next()) {
                Map<String, Object> client = new HashMap<>();
                client.put("id", rs.getInt("id_client"));
                client.put("nom", rs.getString("nom"));
                client.put("prenom", rs.getString("prenom"));
                client.put("email", rs.getString("email"));
                client.put("nb_achats", rs.getInt("nb_achats"));
                client.put("total_depense", rs.getDouble("total_depense"));
                client.put("dernier_achat", rs.getTimestamp("dernier_achat"));
                
                clientsFideles.add(client);
                totalDepenses += rs.getDouble("total_depense");
            }
            
            rapport.put("clients_fideles", clientsFideles);
            rapport.put("total_depenses_clients", totalDepenses);
            rapport.put("moyenne_depense_client", clientsFideles.size() > 0 ? totalDepenses / clientsFideles.size() : 0);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du rapport clients fidèles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rapport;
    }
}