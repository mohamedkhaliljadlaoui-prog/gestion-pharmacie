// [file name]: EmailService.java - VERSION SIMPLIFIÉE (sans JavaMail)
package services;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import models.Medicament;

/**
 * Service d'email simplifié - Pas besoin de JavaMail
 * Crée des fichiers HTML et les ouvre dans le navigateur
 */
public class EmailService {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    @SuppressWarnings("unused")
	private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final String EMAIL_DIR = "emails_sent/";
    private boolean enabled = true;
    
    public EmailService() {
        // Créer le dossier pour les emails si nécessaire
        File dir = new File(EMAIL_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("✅ Dossier emails créé: " + EMAIL_DIR);
            }
        }
        
        System.out.println("📧 Service Email activé (Mode Simulation)");
        System.out.println("   Les emails seront sauvegardés dans: " + EMAIL_DIR);
    }
    
    /**
     * Envoie un email d'alerte de stock critique
     */
    public boolean sendStockAlert(String destinataire, Medicament medicament) {
        String subject = "ALERTE STOCK CRITIQUE - " + medicament.getNom();
        
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background: #ffc107; color: #856404; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
                    .alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; }
                    .info { background: #f8f9fa; padding: 15px; margin: 15px 0; border-left: 4px solid #dc3545; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                    strong { color: #333; }
                    .critical { color: #dc3545; font-weight: bold; font-size: 18px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>⚠️ ALERTE STOCK CRITIQUE</h2>
                    </div>
                    
                    <div class="alert">
                        <p>Le médicament suivant nécessite un réapprovisionnement urgent:</p>
                    </div>
                    
                    <div class="info">
                        <p><strong>Médicament:</strong> %s</p>
                        <p><strong>Dosage:</strong> %s</p>
                        <p><strong>Stock actuel:</strong> <span class="critical">%d unités</span></p>
                        <p><strong>Seuil d'alerte:</strong> %d unités</p>
                        <p><strong>État:</strong> <span class="critical">CRITIQUE - ACTION REQUISE</span></p>
                    </div>
                    
                    <p style="margin: 20px 0; padding: 15px; background: #d4edda; border-left: 4px solid #28a745;">
                        <strong>Action recommandée:</strong> Veuillez passer une commande de réapprovisionnement dans les plus brefs délais.
                    </p>
                    
                    <div class="footer">
                        <p><strong>Destinataire:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><em>Cet email a été généré automatiquement par Pharmacy Manager.</em></p>
                    </div>
                </div>
            </body>
            </html>
            """,
            subject,
            medicament.getNom(),
            medicament.getDosage(),
            medicament.getStock(),
            medicament.getSeuilAlerte(),
            destinataire,
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
        );
        
        return saveEmailAsFile(destinataire, subject, htmlContent);
    }
    
    /**
     * Envoie un rapport hebdomadaire de stock critique
     */
    public boolean sendWeeklyStockReport(String destinataire, List<Medicament> stockCritique) {
        String subject = "Rapport Hebdomadaire - Stock Critique";
        
        StringBuilder tableRows = new StringBuilder();
        for (Medicament med : stockCritique) {
            tableRows.append(String.format("""
                <tr style="border-bottom: 1px solid #ddd;">
                    <td style="padding: 10px;">%s</td>
                    <td style="padding: 10px;">%s</td>
                    <td style="padding: 10px; color: #dc3545; font-weight: bold;">%d</td>
                    <td style="padding: 10px;">%d</td>
                    <td style="padding: 10px;">%.2f DT</td>
                </tr>
                """,
                med.getNom(),
                med.getDosage(),
                med.getStock(),
                med.getSeuilAlerte(),
                med.getStock() * med.getPrixUnitaire()
            ));
        }
        
        double valeurTotale = stockCritique.stream()
            .mapToDouble(m -> m.getStock() * m.getPrixUnitaire())
            .sum();
        
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
                    .container { max-width: 900px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                    .header { background: #2196F3; color: white; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
                    table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    th { background: #2196F3; color: white; padding: 12px; text-align: left; }
                    td { padding: 10px; }
                    tr:hover { background: #f5f5f5; }
                    .summary { background: #e3f2fd; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>📊 Rapport Hebdomadaire de Stock</h2>
                        <p>Médicaments en stock critique</p>
                    </div>
                    
                    <div class="summary">
                        <h3>Résumé</h3>
                        <p><strong>Total de médicaments en stock critique:</strong> <span style="color: #dc3545; font-size: 24px;">%d</span></p>
                        <p><strong>Valeur totale du stock critique:</strong> %.2f DT</p>
                        <p><strong>Date du rapport:</strong> %s</p>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>Médicament</th>
                                <th>Dosage</th>
                                <th>Stock Actuel</th>
                                <th>Seuil Alerte</th>
                                <th>Valeur Stock</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    
                    <div style="margin-top: 30px; padding: 15px; background: #fff3cd; border-left: 4px solid #ffc107;">
                        <p><strong>⚠️ Action requise:</strong> Veuillez passer des commandes pour les médicaments listés ci-dessus.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Destinataire:</strong> %s</p>
                        <p><em>Rapport généré automatiquement par Pharmacy Manager.</em></p>
                    </div>
                </div>
            </body>
            </html>
            """,
            subject,
            stockCritique.size(),
            valeurTotale,
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
            tableRows.toString(),
            destinataire
        );
        
        return saveEmailAsFile(destinataire, subject, htmlContent);
    }
    
    /**
     * Envoie une notification de commande validée
     */
    public boolean sendOrderConfirmation(String destinataire, String medicament, int quantite) {
        String subject = "Commande Validée - " + medicament;
        
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                    .header { background: #28a745; color: white; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
                    .info { background: #d4edda; padding: 15px; margin: 15px 0; border-left: 4px solid #28a745; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>✅ Commande Validée</h2>
                    </div>
                    
                    <p>Votre commande a été validée avec succès:</p>
                    
                    <div class="info">
                        <p><strong>Médicament:</strong> %s</p>
                        <p><strong>Quantité:</strong> %d unités</p>
                        <p><strong>Statut:</strong> <span style="color: #28a745; font-weight: bold;">VALIDÉE</span></p>
                    </div>
                    
                    <p style="margin: 20px 0;">La commande sera livrée prochainement.</p>
                    
                    <div class="footer">
                        <p><strong>Destinataire:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><em>Email généré automatiquement par Pharmacy Manager.</em></p>
                    </div>
                </div>
            </body>
            </html>
            """,
            subject,
            medicament,
            quantite,
            destinataire,
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
        );
        
        return saveEmailAsFile(destinataire, subject, htmlContent);
    }
    
    /**
     * Sauvegarde l'email comme fichier HTML local
     */
    private boolean saveEmailAsFile(String to, String subject, String htmlContent) {
        if (!enabled) {
            System.out.println("⚠️ Service email désactivé");
            return false;
        }
        
        try {
            String timestamp = DATE_FORMAT.format(new Date());
            String safeSubject = subject.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = EMAIL_DIR + timestamp + "_" + safeSubject + ".html";
            
            // S'assurer que le dossier existe
            File dir = new File(EMAIL_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(htmlContent);
            }
            
            System.out.println("📧 EMAIL SAUVEGARDÉ (Mode Simulation)");
            System.out.println("   Destinataire: " + to);
            System.out.println("   Sujet: " + subject);
            System.out.println("   Fichier: " + fileName);
            System.out.println("   ✅ Ouverture automatique...");
            
            // Essayer d'ouvrir l'email dans le navigateur
            openEmailInBrowser(fileName);
            
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur de sauvegarde de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ouvre le fichier email dans le navigateur par défaut
     */
    @SuppressWarnings("deprecation")
    private void openEmailInBrowser(String filePath) {
        try {
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.err.println("❌ Fichier non trouvé: " + filePath);
                return;
            }
            
            // Détection du système d'exploitation
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + file.getAbsolutePath());
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + file.getAbsolutePath());
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                Runtime.getRuntime().exec("xdg-open " + file.getAbsolutePath());
            } else {
                System.out.println("⚠️  Système d'exploitation non reconnu pour l'ouverture automatique");
            }
            
            System.out.println("   🌐 Email ouvert dans le navigateur");
            
        } catch (IOException e) {
            System.out.println("   ℹ️ Impossible d'ouvrir automatiquement");
            System.out.println("   Ouvrez manuellement: " + filePath);
        }
    }
    
    /**
     * Active ou désactive le service email
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println(enabled ? "✅ Service email activé" : "⚠️ Service email désactivé");
    }
    
    /**
     * Vérifie si le service est activé
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Test du service email
     */
    public boolean testConnection() {
        System.out.println("🧪 Test du service email (Mode Simulation)");
        
        Medicament testMed = new Medicament();
        testMed.setNom("Test Medicament");
        testMed.setDosage("100mg");
        testMed.setStock(5);
        testMed.setSeuilAlerte(10);
        testMed.setPrixUnitaire(10.0);
        
        boolean result = sendStockAlert("test@pharmacie.com", testMed);
        
        if (result) {
            System.out.println("✅ Test réussi - Email de test sauvegardé");
        } else {
            System.out.println("❌ Test échoué");
        }
        
        return result;
    }
    
    /**
     * Nettoie les anciens emails (plus de 30 jours)
     */
    public void cleanOldEmails(int daysToKeep) {
        File dir = new File(EMAIL_DIR);
        if (!dir.exists()) return;
        
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        int deletedCount = 0;
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
        }
        
        System.out.println("🗑️ Nettoyage: " + deletedCount + " ancien(s) email(s) supprimé(s)");
    }
    
    /**
     * Compte le nombre d'emails envoyés
     */
    public int getEmailCount() {
        File dir = new File(EMAIL_DIR);
        if (!dir.exists()) return 0;
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".html"));
        return files != null ? files.length : 0;
    }
}