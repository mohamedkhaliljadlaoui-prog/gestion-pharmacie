package services;

import java.sql.Connection;
import java.util.List;

import dao.MedicamentDAO;
import dao.StockHistoriqueDAO;
import models.Medicament;
import models.StockHistorique;
import utils.TransactionManager;
//Ajoutez ces imports en haut de StockService.java
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
/**
 * Service pour la gestion du stock de médicaments avec transactions
 * CORRIGÉ: Ajout de toutes les méthodes nécessaires
 */
public class StockService {
    
    private MedicamentDAO medicamentDAO;
    private StockHistoriqueDAO historiqueDAO;
    
    public StockService() {
        this.medicamentDAO = new MedicamentDAO();
        this.historiqueDAO = new StockHistoriqueDAO();
    }
    
    /**
     * Récupère tous les médicaments
     */
    public List<Medicament> getAllMedicaments() {
        return medicamentDAO.getAll();
    }
 // Dans votre StockService existant, ajoutez cette méthode après getDetailsReapprovisionnement() :

    /**
     * Récupère les détails pour réapprovisionnement depuis le DAO
     */
    public List<Map<String, Object>> getDetailsReapprovisionnement() {
        return medicamentDAO.getDetailsReapprovisionnement();
    }
    /**
     * Récupère un médicament par son ID
     */
    public Medicament getMedicamentById(int id) {
        return medicamentDAO.getById(id);
    }
    
    /**
     * Recherche des médicaments par nom
     */
    public List<Medicament> searchMedicaments(String searchTerm) {
        return medicamentDAO.searchByName(searchTerm);
    }
    
    /**
     * Récupère les médicaments disponibles
     */
    public List<Medicament> getAvailableMedicaments() {
        return medicamentDAO.getMedicamentsDisponibles();
    }
    
    /**
     * Récupère les médicaments en stock critique
     */
    public List<Medicament> getCriticalStock() {
        return medicamentDAO.getStockCritique();
    }
    
    /**
     * Ajoute un nouveau médicament (avec transaction)
     */
    public boolean ajouterMedicament(Medicament medicament) {
        return TransactionManager.executeInTransaction((Connection conn) -> {
            // Ajouter le médicament
            boolean medicamentAjoute = medicamentDAO.add(medicament);
            
            if (medicamentAjoute && medicament.getStock() > 0) {
                // Ajouter l'historique
                StockHistorique historique = new StockHistorique();
                historique.setIdMedicament(medicament.getId());
                historique.setQuantiteAvant(0);
                historique.setQuantiteApres(medicament.getStock());
                historique.setTypeOperation("ajout");
                historique.setIdOperation(0);
                
                return historiqueDAO.add(historique);
            }
            
            return medicamentAjoute;
        });
    }
    
    /**
     * Modifie un médicament existant (avec transaction)
     */
    public boolean modifierMedicament(Medicament medicament) {
        return TransactionManager.executeInTransaction((Connection conn) -> {
            // Récupérer l'ancienne version
            Medicament ancien = medicamentDAO.getById(medicament.getId());
            if (ancien == null) {
                return false;
            }
            
            // Mettre à jour le médicament
            boolean medicamentModifie = medicamentDAO.update(medicament);
            
            if (medicamentModifie && ancien.getStock() != medicament.getStock()) {
                // Ajouter l'historique
                StockHistorique historique = new StockHistorique();
                historique.setIdMedicament(medicament.getId());
                historique.setQuantiteAvant(ancien.getStock());
                historique.setQuantiteApres(medicament.getStock());
                historique.setTypeOperation("ajustement");
                historique.setIdOperation(0);
                
                return historiqueDAO.add(historique);
            }
            
            return medicamentModifie;
        });
    }
    
    /**
     * Supprime un médicament
     */
    public boolean supprimerMedicament(int id) {
        return medicamentDAO.delete(id);
    }
    
    /**
     * Met à jour le stock d'un médicament
     */
    public boolean updateStock(int idMedicament, int newStock) {
        return medicamentDAO.updateStock(idMedicament, newStock);
    }
    
    /**
     * Diminue le stock d'un médicament (pour vente)
     * CORRECTION: Validation stricte du stock
     */
    public boolean diminuerStock(int idMedicament, int quantite, int idVente) {
        return TransactionManager.executeInTransaction((Connection conn) -> {
            Medicament med = medicamentDAO.getById(idMedicament);
            if (med == null || med.getStock() < quantite) {
                return false;
            }
            
            // Diminuer le stock
            boolean stockDiminue = medicamentDAO.diminuerStock(idMedicament, quantite);
            
            if (stockDiminue) {
                // Enregistrer dans l'historique
                StockHistorique historique = new StockHistorique();
                historique.setIdMedicament(idMedicament);
                historique.setQuantiteAvant(med.getStock());
                historique.setQuantiteApres(med.getStock() - quantite);
                historique.setTypeOperation("vente");
                historique.setIdOperation(idVente);
                
                return historiqueDAO.add(historique);
            }
            
            return false;
        });
    }
    
    /**
     * Augmente le stock d'un médicament (pour commande)
     */
    public boolean augmenterStock(int idMedicament, int quantite, int idCommande) {
        return TransactionManager.executeInTransaction((Connection conn) -> {
            Medicament med = medicamentDAO.getById(idMedicament);
            if (med == null) {
                return false;
            }
            
            // Augmenter le stock
            boolean stockAugmente = medicamentDAO.augmenterStock(idMedicament, quantite);
            
            if (stockAugmente) {
                // Enregistrer dans l'historique
                StockHistorique historique = new StockHistorique();
                historique.setIdMedicament(idMedicament);
                historique.setQuantiteAvant(med.getStock());
                historique.setQuantiteApres(med.getStock() + quantite);
                historique.setTypeOperation("commande");
                historique.setIdOperation(idCommande);
                
                return historiqueDAO.add(historique);
            }
            
            return false;
        });
    }
    
    /**
     * Calcule la valeur totale du stock
     */
    public double calculerValeurStock() {
        List<Medicament> medicaments = getAllMedicaments();
        double valeurTotale = 0.0;
        
        for (Medicament med : medicaments) {
            valeurTotale += med.getStock() * med.getPrixUnitaire();
        }
        
        return valeurTotale;
    }
    
    /**
     * Compte le nombre de médicaments en stock critique
     */
    public int countStockCritique() {
        List<Medicament> medicaments = getAllMedicaments();
        int count = 0;
        
        for (Medicament med : medicaments) {
            if (med.isStockCritique()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Compte le nombre total de médicaments
     */
    public int countTotalMedicaments() {
        List<Medicament> medicaments = getAllMedicaments();
        return medicaments.size();
    }
    
    /**
     * Récupère tout l'historique de stock
     */
    public List<StockHistorique> getAllStockHistorique() {
        return historiqueDAO.getAll();
    }
    
    /**
     * Récupère l'historique de stock par type d'opération
     */
    public List<StockHistorique> getHistoriqueByType(String typeOperation) {
        return historiqueDAO.getByTypeOperation(typeOperation);
    }
    
    /**
     * Récupère l'historique pour un médicament spécifique
     */
    public List<StockHistorique> getHistoriqueByMedicament(int idMedicament) {
        return historiqueDAO.getByMedicament(idMedicament);
    }
    
    /**
     * Vérifie si un médicament existe
     */
    public boolean medicamentExists(String nom, String dosage) {
        List<Medicament> medicaments = getAllMedicaments();
        for (Medicament med : medicaments) {
            if (med.getNom().equalsIgnoreCase(nom) && med.getDosage().equalsIgnoreCase(dosage)) {
                return true;
            }
        }
        return false;
    }
 // Ajoutez ces méthodes dans la classe StockService

    /**
     * Récupère les médicaments à réapprovisionner (stock <= seuil d'alerte)
     */
    public List<Medicament> getMedicamentsAReapprovisionner() {
        return medicamentDAO.getMedicamentsAReapprovisionner();
    }

    /**
     * Récupère les médicaments en stock critique (stock <= 30% du seuil d'alerte)
     */
    public List<Medicament> getMedicamentsCritiques() {
        return medicamentDAO.getMedicamentsCritiques();
    }

    /**
     * Récupère les médicaments urgents (stock <= 10% du seuil d'alerte)
     */
    public List<Medicament> getMedicamentsUrgents() {
        return medicamentDAO.getMedicamentsUrgents();
    }

    /**
     * Calcule les statistiques de réapprovisionnement
     */
    public Map<String, Object> getStatsReapprovisionnement() {
        List<Medicament> aReappro = getMedicamentsAReapprovisionner();
        List<Medicament> critiques = getMedicamentsCritiques();
        List<Medicament> urgents = getMedicamentsUrgents();
        
        // Calculer la valeur totale
        double valeurTotale = 0.0;
        for (Medicament med : aReappro) {
            double quantiteManquante = med.getSeuilAlerte() * 2 - med.getStock(); // Commande le double du seuil
            if (quantiteManquante > 0) {
                valeurTotale += quantiteManquante * med.getPrixUnitaire();
            }
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("a_reapprovisionner", aReappro.size());
        stats.put("critique", critiques.size());
        stats.put("urgent", urgents.size());
        stats.put("valeur_totale", valeurTotale);
        
        return stats;
    }

    /**
     * Calcule la quantité recommandée pour réapprovisionner un médicament
     */
    public int getQuantiteRecommandee(int idMedicament) {
        Medicament med = getMedicamentById(idMedicament);
        if (med == null) return 0;
        
        // Quantité recommandée = 2 x seuil d'alerte - stock actuel
        int quantiteRecommandee = med.getSeuilAlerte() * 2 - med.getStock();
        
        // Minimum de 10 unités si besoin de réappro
        return Math.max(quantiteRecommandee, 10);
    }

    /**
     * Génère une suggestion de commande pour réapprovisionnement
     */
    public Map<String, Object> genererSuggestionCommande(int idMedicament) {
        Medicament med = getMedicamentById(idMedicament);
        if (med == null) return null;
        
        int quantite = getQuantiteRecommandee(idMedicament);
        double prixTotal = quantite * med.getPrixUnitaire();
        
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("medicament", med);
        suggestion.put("quantite", quantite);
        suggestion.put("prix_total", prixTotal);
        suggestion.put("stock_actuel", med.getStock());
        suggestion.put("seuil_alerte", med.getSeuilAlerte());
        suggestion.put("difference", med.getSeuilAlerte() - med.getStock());
        
        return suggestion;
    }

    /**
     * Vérifie si un médicament a besoin d'être réapprovisionné
     */
    public boolean needsReapprovisionnement(int idMedicament) {
        Medicament med = getMedicamentById(idMedicament);
        if (med == null) return false;
        return med.getStock() <= med.getSeuilAlerte();
    }

    /**
     * Récupère tous les médicaments avec leur statut de réapprovisionnement
     */
    public List<Map<String, Object>> getMedicamentsAvecStatutReappro() {
        List<Medicament> allMedicaments = getAllMedicaments();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Medicament med : allMedicaments) {
            Map<String, Object> item = new HashMap<>();
            item.put("medicament", med);
            item.put("statut_reappro", getStatutReapprovisionnement(med));
            item.put("quantite_recommandee", getQuantiteRecommandee(med.getId()));
            item.put("prix_estime", getQuantiteRecommandee(med.getId()) * med.getPrixUnitaire());
            
            result.add(item);
        }
        
        return result;
    }

    /**
     * Détermine le statut de réapprovisionnement
     */
    private String getStatutReapprovisionnement(Medicament med) {
        if (med.getStock() <= med.getSeuilAlerte() * 0.3) {
            return "CRITIQUE";
        } else if (med.getStock() <= med.getSeuilAlerte() * 0.5) {
            return "URGENT";
        } else if (med.getStock() <= med.getSeuilAlerte()) {
            return "A_REAPPRO";
        } else {
            return "NORMAL";
        }
    }
    
}