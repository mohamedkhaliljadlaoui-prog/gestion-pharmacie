package test;

import dao.ClientDAO;
import models.Client;

public class TestClientDAO {
    public static void main(String[] args) {
        System.out.println("=== TEST CLIENT DAO ===");
        
        ClientDAO clientDAO = new ClientDAO();
        
        // Test 1: Ajouter un client
        Client nouveauClient = new Client();
        nouveauClient.setNom("Test");
        nouveauClient.setPrenom("Jean");
        nouveauClient.setEmail("jean.test@email.com");
        nouveauClient.setTelephone("0612345678");
        nouveauClient.setAdresse("123 Rue de Test, Paris");
        
        boolean ajoutReussi = clientDAO.add(nouveauClient);
        
        if (ajoutReussi) {
            System.out.println("✅ Client ajouté avec succès!");
            System.out.println("ID attribué: " + nouveauClient.getId());
        } else {
            System.out.println("❌ Échec de l'ajout du client");
        }
        
        // Test 2: Récupérer tous les clients
        System.out.println("\n=== LISTE DES CLIENTS ===");
        java.util.List<Client> clients = clientDAO.getAll();
        
        if (clients.isEmpty()) {
            System.out.println("Aucun client dans la base de données");
        } else {
            for (Client client : clients) {
                System.out.println("ID: " + client.getId() + 
                                 " - " + client.getNom() + " " + client.getPrenom() +
                                 " - Tel: " + client.getTelephone());
            }
        }
    }
}