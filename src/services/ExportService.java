package services;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import models.*;
import dao.*;

/**
 * Service pour exporter les données au format CSV, TXT, HTML
 */
public class ExportService {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Exporte les médicaments au format CSV
     */
    public boolean exportMedicamentsCSV(String filePath) {
        MedicamentDAO dao = new MedicamentDAO();
        List<Medicament> medicaments = dao.getAll();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("ID,Nom,Dosage,Stock,Prix Unitaire,Seuil Alerte,Description,Statut");
            
            // Données
            for (Medicament med : medicaments) {
                String statut = med.isStockCritique() ? "CRITIQUE" : "NORMAL";
                writer.printf("%d,\"%s\",\"%s\",%d,%.2f,%d,\"%s\",%s%n",
                    med.getId(),
                    escapeCsv(med.getNom()),
                    escapeCsv(med.getDosage()),
                    med.getStock(),
                    med.getPrixUnitaire(),
                    med.getSeuilAlerte(),
                    escapeCsv(med.getDescription()),
                    statut
                );
            }
            
            System.out.println("✅ Médicaments exportés: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur d'export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporte les ventes au format CSV
     */
    public boolean exportVentesCSV(String filePath, java.util.Date dateDebut, java.util.Date dateFin) {
        VenteDAO dao = new VenteDAO();
        List<Vente> ventes;
        
        if (dateDebut != null && dateFin != null) {
            // Conversion de java.util.Date en java.sql.Date
            java.sql.Date sqlDateDebut = new java.sql.Date(dateDebut.getTime());
            java.sql.Date sqlDateFin = new java.sql.Date(dateFin.getTime());
            ventes = dao.getByPeriode(sqlDateDebut, sqlDateFin);
        } else {
            ventes = dao.getAll();
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("ID,Date,Pharmacien,Client,Médicament,Quantité,Prix Total,Statut");
            
            // Données
            for (Vente vente : ventes) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%.2f,%s%n",
                    vente.getId(),
                    vente.getDateVente() != null ? DISPLAY_FORMAT.format(vente.getDateVente()) : "",
                    escapeCsv(vente.getPharmacienNom()),
                    escapeCsv(vente.getClientNom()),
                    escapeCsv(vente.getMedicamentNom()),
                    vente.getQuantite(),
                    vente.getPrixTotal(),
                    vente.getStatut()
                );
            }
            
            System.out.println("✅ Ventes exportées: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur d'export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporte les clients au format CSV
     */
    public boolean exportClientsCSV(String filePath) {
        ClientDAO dao = new ClientDAO();
        List<Client> clients = dao.getAll();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("ID,Nom,Prénom,Email,Téléphone,Adresse");
            
            // Données
            for (Client client : clients) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    client.getId(),
                    escapeCsv(client.getNom()),
                    escapeCsv(client.getPrenom()),
                    escapeCsv(client.getEmail()),
                    escapeCsv(client.getTelephone()),
                    escapeCsv(client.getAdresse())
                );
            }
            
            System.out.println("✅ Clients exportés: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur d'export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporte l'historique de stock au format CSV
     */
    public boolean exportHistoriqueCSV(String filePath) {
        StockHistoriqueDAO dao = new StockHistoriqueDAO();
        List<StockHistorique> historique = dao.getAll();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("ID,Date,Médicament,Quantité Avant,Quantité Après,Différence,Type Opération,ID Opération");
            
            // Données
            for (StockHistorique h : historique) {
                int difference = h.getQuantiteApres() - h.getQuantiteAvant();
                writer.printf("%d,\"%s\",\"%s\",%d,%d,%d,\"%s\",%d%n",
                    h.getId(),
                    h.getDateOperation() != null ? DISPLAY_FORMAT.format(h.getDateOperation()) : "",
                    escapeCsv(h.getMedicamentNom()),
                    h.getQuantiteAvant(),
                    h.getQuantiteApres(),
                    difference,
                    h.getTypeOperation(),
                    h.getIdOperation()
                );
            }
            
            System.out.println("✅ Historique exporté: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur d'export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporte les ventes d'aujourd'hui au format TXT
     */
    public boolean exportVentesAujourdhuiTXT(String filePath) {
        VenteDAO dao = new VenteDAO();
        List<Vente> ventes = dao.getVentesAujourdhui();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=".repeat(60));
            writer.println("          RAPPORT DES VENTES DU JOUR");
            writer.println("=".repeat(60));
            writer.println();
            
            double totalCA = 0;
            int totalQuantite = 0;
            
            for (Vente vente : ventes) {
                if ("valide".equalsIgnoreCase(vente.getStatut())) {
                    writer.printf("Vente #%d%n", vente.getId());
                    writer.printf("  Date: %s%n", 
                        vente.getDateVente() != null ? DISPLAY_FORMAT.format(vente.getDateVente()) : "N/A");
                    writer.printf("  Client: %s%n", vente.getClientNom());
                    writer.printf("  Médicament: %s%n", vente.getMedicamentNom());
                    writer.printf("  Quantité: %d%n", vente.getQuantite());
                    writer.printf("  Prix total: %.2f DT%n", vente.getPrixTotal());
                    writer.println("-".repeat(40));
                    
                    totalCA += vente.getPrixTotal();
                    totalQuantite += vente.getQuantite();
                }
            }
            
            writer.println();
            writer.println("=".repeat(60));
            writer.printf("Nombre de ventes: %d%n", ventes.size());
            writer.printf("Quantité totale vendue: %d%n", totalQuantite);
            writer.printf("Chiffre d'affaires total: %.2f DT%n", totalCA);
            writer.println("=".repeat(60));
            
            System.out.println("✅ Rapport TXT généré: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur d'export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Génère un rapport HTML des ventes
     */
    public boolean exportRapportVentesHTML(String filePath, java.util.Date dateDebut, java.util.Date dateFin) {
        VenteDAO venteDAO = new VenteDAO();
        
        // Conversion de java.util.Date en java.sql.Date
        java.sql.Date sqlDateDebut = new java.sql.Date(dateDebut.getTime());
        java.sql.Date sqlDateFin = new java.sql.Date(dateFin.getTime());
        List<Vente> ventes = venteDAO.getByPeriode(sqlDateDebut, sqlDateFin);
        
        double totalCA = ventes.stream()
            .filter(v -> "valide".equalsIgnoreCase(v.getStatut()))
            .mapToDouble(Vente::getPrixTotal)
            .sum();
        
        int nbVentes = (int) ventes.stream()
            .filter(v -> "valide".equalsIgnoreCase(v.getStatut()))
            .count();
        
        int totalQuantite = ventes.stream()
            .filter(v -> "valide".equalsIgnoreCase(v.getStatut()))
            .mapToInt(Vente::getQuantite)
            .sum();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='fr'>");
            writer.println("<head>");
            writer.println("    <meta charset='UTF-8'>");
            writer.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("    <title>Rapport des Ventes - Pharmacie</title>");
            writer.println("    <style>");
            writer.println("        * { margin: 0; padding: 0; box-sizing: border-box; }");
            writer.println("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; color: #333; line-height: 1.6; }");
            writer.println("        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }");
            writer.println("        .header { background: linear-gradient(135deg, #2196F3, #1976D2); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
            writer.println("        h1 { font-size: 2.5rem; margin-bottom: 10px; }");
            writer.println("        .subtitle { font-size: 1.1rem; opacity: 0.9; }");
            writer.println("        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }");
            writer.println("        .summary-card { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.08); text-align: center; transition: transform 0.3s ease; }");
            writer.println("        .summary-card:hover { transform: translateY(-5px); }");
            writer.println("        .summary-card h3 { color: #555; font-size: 1rem; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px; }");
            writer.println("        .summary-card .value { font-size: 2rem; font-weight: bold; color: #2196F3; }");
            writer.println("        .summary-card.total .value { color: #4CAF50; }");
            writer.println("        .summary-card.avg .value { color: #FF9800; }");
            writer.println("        table { width: 100%; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.08); margin-bottom: 30px; }");
            writer.println("        thead { background: #f5f7fa; }");
            writer.println("        th { padding: 15px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #e0e0e0; }");
            writer.println("        td { padding: 12px 15px; border-bottom: 1px solid #eee; }");
            writer.println("        tbody tr:hover { background: #f8f9fa; }");
            writer.println("        .status { padding: 5px 12px; border-radius: 20px; font-size: 0.85rem; font-weight: 600; }");
            writer.println("        .status.valide { background: #e8f5e9; color: #2e7d32; }");
            writer.println("        .status.annulee { background: #ffebee; color: #c62828; }");
            writer.println("        .footer { text-align: center; margin-top: 40px; padding: 20px; color: #666; font-size: 0.9rem; border-top: 1px solid #e0e0e0; }");
            writer.println("        @media print { body { background: white; } .header, .summary-card, table { box-shadow: none; } }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class='container'>");
            writer.println("        <div class='header'>");
            writer.println("            <h1>📊 Rapport des Ventes</h1>");
            writer.println("            <div class='subtitle'>Pharmacie - Système de Gestion</div>");
            writer.printf("            <p style='margin-top: 15px;'>Période: du %s au %s</p>%n", 
                DISPLAY_FORMAT.format(dateDebut), 
                DISPLAY_FORMAT.format(dateFin));
            writer.println("        </div>");
            
            writer.println("        <div class='summary-grid'>");
            writer.println("            <div class='summary-card'>");
            writer.println("                <h3>Ventes Validées</h3>");
            writer.printf("                <div class='value'>%d</div>%n", nbVentes);
            writer.println("            </div>");
            writer.println("            <div class='summary-card'>");
            writer.println("                <h3>Quantité Totale</h3>");
            writer.printf("                <div class='value'>%d</div>%n", totalQuantite);
            writer.println("            </div>");
            writer.println("            <div class='summary-card total'>");
            writer.println("                <h3>Chiffre d'Affaires</h3>");
            writer.printf("                <div class='value'>%.2f DT</div>%n", totalCA);
            writer.println("            </div>");
            writer.println("            <div class='summary-card avg'>");
            writer.println("                <h3>Panier Moyen</h3>");
            writer.printf("                <div class='value'>%.2f DT</div>%n", nbVentes > 0 ? totalCA / nbVentes : 0);
            writer.println("            </div>");
            writer.println("        </div>");
            
            writer.println("        <table>");
            writer.println("            <thead>");
            writer.println("                <tr>");
            writer.println("                    <th>ID</th>");
            writer.println("                    <th>Date</th>");
            writer.println("                    <th>Client</th>");
            writer.println("                    <th>Médicament</th>");
            writer.println("                    <th>Quantité</th>");
            writer.println("                    <th>Prix Total</th>");
            writer.println("                    <th>Statut</th>");
            writer.println("                </tr>");
            writer.println("            </thead>");
            writer.println("            <tbody>");
            
            if (ventes.isEmpty()) {
                writer.println("                <tr>");
                writer.println("                    <td colspan='7' style='text-align: center; padding: 40px; color: #666;'>");
                writer.println("                        Aucune vente trouvée pour cette période.");
                writer.println("                    </td>");
                writer.println("                </tr>");
            } else {
                for (Vente vente : ventes) {
                    String statusClass = "valide".equalsIgnoreCase(vente.getStatut()) ? "valide" : "annulee";
                    writer.println("                <tr>");
                    writer.printf("                    <td>#%d</td>%n", vente.getId());
                    writer.printf("                    <td>%s</td>%n", 
                        vente.getDateVente() != null ? DISPLAY_FORMAT.format(vente.getDateVente()) : "N/A");
                    writer.printf("                    <td>%s</td>%n", escapeHtml(vente.getClientNom()));
                    writer.printf("                    <td>%s</td>%n", escapeHtml(vente.getMedicamentNom()));
                    writer.printf("                    <td>%d</td>%n", vente.getQuantite());
                    writer.printf("                    <td><strong>%.2f DT</strong></td>%n", vente.getPrixTotal());
                    writer.printf("                    <td><span class='status %s'>%s</span></td>%n", 
                        statusClass, vente.getStatut().toUpperCase());
                    writer.println("                </tr>");
                }
            }
            
            writer.println("            </tbody>");
            writer.println("        </table>");
            
            writer.println("        <div class='footer'>");
            writer.printf("            Rapport généré le %s | Pharmacie Management System%n", 
                DISPLAY_FORMAT.format(new Date()));
            writer.println("        </div>");
            writer.println("    </div>");
            writer.println("</body>");
            writer.println("</html>");
            
            System.out.println("✅ Rapport HTML généré: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur de génération du rapport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Génère un nom de fichier avec timestamp
     */
    public String generateFileName(String prefix, String extension) {
        String timestamp = DATE_FORMAT.format(new Date());
        return prefix + "_" + timestamp + "." + extension;
    }
    
    /**
     * Exporte un rapport complet (médicaments + ventes + historique)
     */
    public boolean exportRapportComplet(String directoryPath, java.util.Date dateDebut, java.util.Date dateFin) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String timestamp = DATE_FORMAT.format(new Date());
        String basePath = directoryPath + File.separator + "rapport_complet_" + timestamp;
        
        boolean success1 = exportMedicamentsCSV(basePath + "_medicaments.csv");
        boolean success2 = exportVentesCSV(basePath + "_ventes.csv", dateDebut, dateFin);
        boolean success3 = exportHistoriqueCSV(basePath + "_historique.csv");
        boolean success4 = exportRapportVentesHTML(basePath + "_rapport.html", dateDebut, dateFin);
        
        return success1 && success2 && success3 && success4;
    }
    
    /**
     * Échappe les caractères spéciaux CSV
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Remplacer les guillemets doubles par deux guillemets
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Échappe les caractères HTML
     */
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}