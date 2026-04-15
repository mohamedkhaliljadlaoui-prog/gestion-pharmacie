package models;

/**
 * Classe reprÃ©sentant un mÃ©dicament
 */
public class Medicament {
    private int id;
    private String nom;
    private String dosage;
    private int stock;
    private double prixUnitaire;
    private int seuilAlerte;
    private String description;
    
    /**
     * Constructeur par dÃ©faut
     */
    public Medicament() {
    }
    
    /**
     * Constructeur avec paramÃ¨tres essentiels
     */
    public Medicament(int id, String nom, String dosage, int stock, double prixUnitaire) {
        this.id = id;
        this.nom = nom;
        this.dosage = dosage;
        this.stock = stock;
        this.prixUnitaire = prixUnitaire;
        this.seuilAlerte = 10; // Valeur par dÃ©faut
    }
    
    /**
     * Constructeur complet
     */
    public Medicament(int id, String nom, String dosage, int stock, double prixUnitaire, 
                     int seuilAlerte, String description) {
        this.id = id;
        this.nom = nom;
        this.dosage = dosage;
        this.stock = stock;
        this.prixUnitaire = prixUnitaire;
        this.seuilAlerte = seuilAlerte;
        this.description = description;
    }
    
    // ============================================
    // GETTERS ET SETTERS
    // ============================================
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public double getPrixUnitaire() {
        return prixUnitaire;
    }
    
    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
    
    public int getSeuilAlerte() {
        return seuilAlerte;
    }
    
    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // ============================================
    // MÃ‰THODES UTILITAIRES
    // ============================================
    
    /**
     * VÃ©rifie si le stock est critique
     */
    public boolean isStockCritique() {
        return stock <= seuilAlerte;
    }
    
    /**
     * Retourne le nom complet du mÃ©dicament (nom + dosage)
     */
    public String getNomComplet() {
        return nom + " " + dosage;
    }
    
    /**
     * VÃ©rifie si le mÃ©dicament est disponible pour une quantitÃ© donnÃ©e
     */
    public boolean isDisponible(int quantite) {
        return stock >= quantite;
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (Stock: " + stock + ")";
    }
    
}