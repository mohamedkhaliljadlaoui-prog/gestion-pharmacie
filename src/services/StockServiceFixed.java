// [file name]: StockServiceFixed.java
package services;

import dao.MedicamentDAO;
import dao.StockHistoriqueDAO;
import models.Medicament;
import models.StockHistorique;
import utils.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Service amélioré pour la gestion du stock de médicaments avec transactions
 */
@SuppressWarnings("unused")
public class StockServiceFixed {
    
    private MedicamentDAO medicamentDAO;
    private StockHistoriqueDAO historiqueDAO;
    
    public StockServiceFixed() {
        this.medicamentDAO = new MedicamentDAO();
        this.historiqueDAO = new StockHistoriqueDAO();
    }
    
    /**
     * Ajoute un médicament avec historique (transaction)
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
     * Met à jour le stock d'un médicament (pour vente)
     */
    public boolean vendreMedicament(int idMedicament, int quantite, int idVente) {
        return TransactionManager.executeInTransaction((Connection conn) -> {
            // Récupérer le médicament
            Medicament medicament = medicamentDAO.getById(idMedicament);
            if (medicament == null || medicament.getStock() < quantite) {
                return false;
            }
            
            // Mettre à jour le stock
            boolean stockMisAJour = medicamentDAO.diminuerStock(idMedicament, quantite);
            
            if (stockMisAJour) {
                // Enregistrer dans l'historique
                StockHistorique historique = new StockHistorique();
                historique.setIdMedicament(idMedicament);
                historique.setQuantiteAvant(medicament.getStock());
                historique.setQuantiteApres(medicament.getStock() - quantite);
                historique.setTypeOperation("vente");
                historique.setIdOperation(idVente);
                
                return historiqueDAO.add(historique);
            }
            
            return false;
        });
    }
    
    // ... autres méthodes ...
}