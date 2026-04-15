package services;

import dao.CommandeDAO;
import dao.MedicamentDAO;
import dao.StockHistoriqueDAO;
import models.Commande;
import models.Medicament;
import models.StockHistorique;
import java.util.List;

/**
 * Service pour la gestion des commandes
 */
public class CommandeService {
    
    private CommandeDAO commandeDAO;
    private MedicamentDAO medicamentDAO;
    private StockHistoriqueDAO historiqueDAO;
    
    public CommandeService() {
        this.commandeDAO = new CommandeDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.historiqueDAO = new StockHistoriqueDAO();
    }
    
    /**
     * Récupère toutes les commandes
     */
    public List<Commande> getAllCommandes() {
        return commandeDAO.getAll();
    }
    
    /**
     * Crée une nouvelle commande
     */
    public boolean creerCommande(int idGestionnaire, int idMedicament, int quantite) {
        Commande commande = new Commande();
        commande.setIdGestionnaire(idGestionnaire);
        commande.setIdMedicament(idMedicament);
        commande.setQuantite(quantite);
        commande.setStatut("en_attente");
        
        return commandeDAO.create(commande);
    }
    
    /**
     * Valide une commande
     */
    public boolean validerCommande(int idCommande) {
        return commandeDAO.valider(idCommande);
    }
    
    /**
     * Marque une commande comme reçue et met à jour le stock
     */
    public boolean recevoirCommande(int idCommande) {
        // Récupérer la commande
        List<Commande> commandes = getAllCommandes();
        Commande commande = null;
        
        for (Commande cmd : commandes) {
            if (cmd.getId() == idCommande) {
                commande = cmd;
                break;
            }
        }
        
        if (commande == null) {
            return false;
        }
        
        // Mettre à jour le stock du médicament
        Medicament medicament = medicamentDAO.getById(commande.getIdMedicament());
        if (medicament == null) {
            return false;
        }
        
        int nouveauStock = medicament.getStock() + commande.getQuantite();
        
        // Mettre à jour dans la base de données
        boolean stockUpdated = medicamentDAO.updateStock(commande.getIdMedicament(), nouveauStock);
        boolean commandeUpdated = commandeDAO.recevoir(idCommande);
        
        if (stockUpdated && commandeUpdated) {
            // Enregistrer dans l'historique
            StockHistorique historique = new StockHistorique();
            historique.setIdMedicament(commande.getIdMedicament());
            historique.setQuantiteAvant(medicament.getStock());
            historique.setQuantiteApres(nouveauStock);
            historique.setTypeOperation("commande");
            historique.setIdOperation(idCommande);
            
            historiqueDAO.add(historique);
            return true;
        }
        
        return false;
    }
    
    /**
     * Annule une commande
     */
    public boolean annulerCommande(int idCommande) {
        return commandeDAO.annuler(idCommande);
    }
    
    /**
     * Compte les commandes en attente
     */
    public int countCommandesEnAttente() {
        List<Commande> commandes = getAllCommandes();
        int count = 0;
        
        for (Commande cmd : commandes) {
            if ("en_attente".equals(cmd.getStatut())) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Crée des commandes automatiques pour les stocks critiques
     */
    public int creerCommandesAutomatiques(int idGestionnaire) {
        List<Medicament> medicaments = medicamentDAO.getAll();
        int commandesCreees = 0;
        
        for (Medicament med : medicaments) {
            if (med.isStockCritique()) {
                // Quantité à commander = seuil alerte * 3
                int quantite = med.getSeuilAlerte() * 3;
                
                if (creerCommande(idGestionnaire, med.getId(), quantite)) {
                    commandesCreees++;
                }
            }
        }
        
        return commandesCreees;
    }
}