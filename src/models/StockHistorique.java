package models;

import java.sql.Timestamp;

public class StockHistorique {
    private int id;                  // id_historique
    private int idMedicament;        // id_medicament
    private int quantiteAvant;       // quantite_avant
    private int quantiteApres;       // quantite_apres
    private String typeOperation;    // type_operation
    private int idOperation;         // id_operation
    private Timestamp dateOperation; // date_operation
    private String medicamentNom;    // Pour l'affichage
    
    // Constructeurs
    public StockHistorique() {}
    
    public StockHistorique(int idMedicament, int quantiteAvant, int quantiteApres, 
                          String typeOperation, int idOperation) {
        this.idMedicament = idMedicament;
        this.quantiteAvant = quantiteAvant;
        this.quantiteApres = quantiteApres;
        this.typeOperation = typeOperation;
        this.idOperation = idOperation;
        this.dateOperation = new Timestamp(System.currentTimeMillis());
    }
    
    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdMedicament() { return idMedicament; }
    public void setIdMedicament(int idMedicament) { this.idMedicament = idMedicament; }
    
    public int getQuantiteAvant() { return quantiteAvant; }
    public void setQuantiteAvant(int quantiteAvant) { this.quantiteAvant = quantiteAvant; }
    
    public int getQuantiteApres() { return quantiteApres; }
    public void setQuantiteApres(int quantiteApres) { this.quantiteApres = quantiteApres; }
    
    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }
    
    public int getIdOperation() { return idOperation; }
    public void setIdOperation(int idOperation) { this.idOperation = idOperation; }
    
    public Timestamp getDateOperation() { return dateOperation; }
    public void setDateOperation(Timestamp dateOperation) { this.dateOperation = dateOperation; }
    
    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }
    
    // Méthode utilitaire pour calculer la différence
    public int getDifference() {
        return this.quantiteApres - this.quantiteAvant;
    }
    
    @Override
    public String toString() {
        return "StockHistorique{" +
                "id=" + id +
                ", idMedicament=" + idMedicament +
                ", quantiteAvant=" + quantiteAvant +
                ", quantiteApres=" + quantiteApres +
                ", typeOperation='" + typeOperation + '\'' +
                ", idOperation=" + idOperation +
                ", dateOperation=" + dateOperation +
                '}';
    }
}