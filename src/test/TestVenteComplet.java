package test;

import java.util.List;
import models.Vente;
import services.VenteService;
import dao.MedicamentDAO;
import models.Medicament;
import dao.ClientDAO;
import models.Client;

public class TestVenteComplet {
    public static void main(String[] args) {
        System.out.println("=== TEST COMPLET DU SYSTÈME DE VENTE ===");
        
        VenteService venteService = new VenteService();
        MedicamentDAO medicamentDAO = new MedicamentDAO();
        ClientDAO clientDAO = new ClientDAO();
        
        // 1. Vérifier la connexion BD
        System.out.println("\n1. VÉRIFICATION DE LA BASE DE DONNÉES:");
        
        // Médicaments
        List<Medicament> medicaments = medicamentDAO.getAll();
        System.out.println("  Médicaments trouvés: " + medicaments.size());
        for (Medicament med : medicaments) {
            System.out.println("    - ID: " + med.getId() + " | " + med.getNom() + 
                             " | Stock: " + med.getStock() + " | Prix: " + med.getPrixUnitaire() + " DT");
        }
        
        // Clients
        List<Client> clients = clientDAO.getAll();
        System.out.println("  Clients trouvés: " + clients.size());
        for (Client client : clients) {
            System.out.println("    - ID: " + client.getId() + " | " + 
                             client.getNom() + " " + client.getPrenom());
        }
        
        // Ventes existantes
        List<Vente> ventes = venteService.getAllVentes();
        System.out.println("  Ventes existantes: " + ventes.size());
        
        if (medicaments.isEmpty()) {
            System.out.println("\n❌ ERREUR: Aucun médicament dans la base de données!");
            System.out.println("   Insérez d'abord des médicaments avec des stocks > 0");
            return;
        }
        
        if (clients.isEmpty()) {
            System.out.println("\n⚠ ATTENTION: Aucun client dans la base.");
            System.out.println("   Vous pouvez vendre à un client occasionnel (ID=0)");
        }
        
        // 2. Tester une vente directe
        System.out.println("\n2. TEST D'ENREGISTREMENT DIRECT:");
        
        // Prendre le premier médicament avec stock > 0
        Medicament medicamentAVendre = null;
        for (Medicament med : medicaments) {
            if (med.getStock() > 0) {
                medicamentAVendre = med;
                break;
            }
        }
        
        if (medicamentAVendre == null) {
            System.out.println("❌ Aucun médicament avec stock disponible!");
            return;
        }
        
        int idMedicament = medicamentAVendre.getId();
        int quantite = 1;
        int idPharmacien = 1; // Premier pharmacien
        int idClient = clients.isEmpty() ? 0 : clients.get(0).getId(); // Premier client ou occasionnel
        
        System.out.println("  Détails de la vente:");
        System.out.println("    - Médicament: " + medicamentAVendre.getNom() + 
                         " (Stock: " + medicamentAVendre.getStock() + ")");
        System.out.println("    - Quantité: " + quantite);
        System.out.println("    - Prix unitaire: " + medicamentAVendre.getPrixUnitaire() + " DT");
        System.out.println("    - Prix total: " + (medicamentAVendre.getPrixUnitaire() * quantite) + " DT");
        System.out.println("    - Client: " + (idClient == 0 ? "Occasionnel" : "ID " + idClient));
        
        // Vérification
        String verification = venteService.verifierVentePossible(idMedicament, quantite);
        System.out.println("  Vérification: " + verification);
        
        if ("OK".equals(verification)) {
            // Enregistrement
            System.out.println("  Tentative d'enregistrement...");
            boolean succes = venteService.enregistrerVente(idPharmacien, idClient, idMedicament, quantite);
            
            if (succes) {
                System.out.println("  ✅ Vente enregistrée avec succès!");
                
                // Vérification post-vente
                System.out.println("\n3. VÉRIFICATION POST-VENTE:");
                
                // Vérifier le nouveau stock
                Medicament medApres = medicamentDAO.getById(idMedicament);
                System.out.println("  Stock avant: " + medicamentAVendre.getStock());
                System.out.println("  Stock après: " + medApres.getStock());
                System.out.println("  Différence: " + (medicamentAVendre.getStock() - medApres.getStock()));
                
                // Vérifier les ventes
                ventes = venteService.getAllVentes();
                System.out.println("  Total ventes maintenant: " + ventes.size());
                
                if (!ventes.isEmpty()) {
                    Vente derniereVente = ventes.get(0); // La plus récente
                    System.out.println("  Dernière vente:");
                    System.out.println("    - ID: " + derniereVente.getId());
                    System.out.println("    - Médicament: " + derniereVente.getMedicamentNom());
                    System.out.println("    - Quantité: " + derniereVente.getQuantite());
                    System.out.println("    - Prix: " + derniereVente.getPrixTotal() + " DT");
                    System.out.println("    - Statut: " + derniereVente.getStatut());
                }
                
            } else {
                System.out.println("  ❌ Échec de l'enregistrement");
                System.out.println("  Vérifiez les logs d'erreur ci-dessus");
            }
        } else {
            System.out.println("  ❌ Vente impossible: " + verification);
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}