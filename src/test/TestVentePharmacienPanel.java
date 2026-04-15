package test;

import javax.swing.*;
import views.VentePharmacienPanel;

public class TestVentePharmacienPanel {
    public static void main(String[] args) {
        System.out.println("=== TEST VENTE PHARMACIEN PANEL ===");
        
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Test VentePharmacienPanel");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(1000, 700);
            testFrame.setLocationRelativeTo(null);
            
            // Créer le panel avec ID pharmacien = 1
            VentePharmacienPanel panel = new VentePharmacienPanel(1);
            
            testFrame.add(panel);
            testFrame.setVisible(true);
            
            System.out.println("✅ VentePharmacienPanel affiché");
            System.out.println("Pharmacien ID: 1");
            System.out.println("\nInstructions:");
            System.out.println("1. Sélectionnez un médicament dans la liste");
            System.out.println("2. Sélectionnez un client");
            System.out.println("3. Entrez une quantité (ex: 1)");
            System.out.println("4. Vérifiez que le prix total est calculé");
            System.out.println("5. Cliquez sur 'Enregistrer Vente'");
            System.out.println("6. Confirmez dans la boîte de dialogue");
            System.out.println("7. Vérifiez la console pour les messages");
        });
    }
}