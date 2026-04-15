package services;

import java.util.ArrayList;
import java.util.List;
import models.Medicament;

/**
 * Classe de test pour le service email
 * Exécutez cette classe pour tester le service
 */
public class TestEmailService {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("TEST DU SERVICE EMAIL");
        System.out.println("=================================\n");
        
        EmailService emailService = new EmailService();
        
        // Test 1: Alerte stock critique
        System.out.println("\n--- TEST 1: Alerte Stock Critique ---");
        testStockAlert(emailService);
        
        // Test 2: Rapport hebdomadaire
        System.out.println("\n--- TEST 2: Rapport Hebdomadaire ---");
        testWeeklyReport(emailService);
        
        // Test 3: Confirmation de commande
        System.out.println("\n--- TEST 3: Confirmation de Commande ---");
        testOrderConfirmation(emailService);
        
        // Statistiques
        System.out.println("\n=================================");
        System.out.println("RÉSUMÉ");
        System.out.println("=================================");
        System.out.println("Nombre d'emails générés: " + emailService.getEmailCount());
        System.out.println("\n✅ Tous les tests sont terminés!");
        System.out.println("📁 Vérifiez le dossier: emails_sent/");
        System.out.println("🌐 Les emails devraient s'ouvrir automatiquement dans votre navigateur");
    }
    
    private static void testStockAlert(EmailService emailService) {
        Medicament medicament = new Medicament();
        medicament.setId(1);
        medicament.setNom("Ventoline");
        medicament.setDosage("100µg");
        medicament.setStock(3);
        medicament.setSeuilAlerte(10);
        medicament.setPrixUnitaire(25.00);
        
        boolean result = emailService.sendStockAlert("gestionnaire@pharmacie.com", medicament);
        
        if (result) {
            System.out.println("✅ Alerte stock critique envoyée avec succès");
        } else {
            System.out.println("❌ Échec de l'envoi de l'alerte");
        }
    }
    
    private static void testWeeklyReport(EmailService emailService) {
        List<Medicament> stockCritique = new ArrayList<>();
        
        // Médicament 1
        Medicament med1 = new Medicament();
        med1.setId(1);
        med1.setNom("Ventoline");
        med1.setDosage("100µg");
        med1.setStock(3);
        med1.setSeuilAlerte(10);
        med1.setPrixUnitaire(25.00);
        stockCritique.add(med1);
        
        // Médicament 2
        Medicament med2 = new Medicament();
        med2.setId(2);
        med2.setNom("Ciprofloxacine");
        med2.setDosage("500mg");
        med2.setStock(5);
        med2.setSeuilAlerte(10);
        med2.setPrixUnitaire(18.00);
        stockCritique.add(med2);
        
        // Médicament 3
        Medicament med3 = new Medicament();
        med3.setId(3);
        med3.setNom("Insuline");
        med3.setDosage("100UI/ml");
        med3.setStock(2);
        med3.setSeuilAlerte(8);
        med3.setPrixUnitaire(45.00);
        stockCritique.add(med3);
        
        boolean result = emailService.sendWeeklyStockReport("gestionnaire@pharmacie.com", stockCritique);
        
        if (result) {
            System.out.println("✅ Rapport hebdomadaire envoyé avec succès");
        } else {
            System.out.println("❌ Échec de l'envoi du rapport");
        }
    }
    
    private static void testOrderConfirmation(EmailService emailService) {
        boolean result = emailService.sendOrderConfirmation(
            "fournisseur@pharma.com",
            "Doliprane 1000mg",
            100
        );
        
        if (result) {
            System.out.println("✅ Confirmation de commande envoyée avec succès");
        } else {
            System.out.println("❌ Échec de l'envoi de la confirmation");
        }
    }
}