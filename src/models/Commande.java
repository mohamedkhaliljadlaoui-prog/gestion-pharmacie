package models;

import java.sql.Timestamp;

/**
 * Classe représentant une commande de réapprovisionnement
 */
public class Commande {
    private int id;
    private int idGestionnaire;
    private int idMedicament;
    private int quantite;
    private Timestamp dateCommande;
    private String statut; // "en_attente", "validee", "recue", "annulee"
    private Timestamp dateReception;
    
    // Attributs supplémentaires pour l'affichage
    private String gestionnaireNom;
    private String medicamentNom;
    
    /**
     * Constructeur par défaut
     */
    public Commande() {
        this.statut = "en_attente";
    }
    
    /**
     * Constructeur avec paramètres essentiels
     */
    public Commande(int idGestionnaire, int idMedicament, int quantite) {
        this.idGestionnaire = idGestionnaire;
        this.idMedicament = idMedicament;
        this.quantite = quantite;
        this.statut = "en_attente";
    }
    
    /**
     * Constructeur complet
     */
    public Commande(int id, int idGestionnaire, int idMedicament, int quantite,
                   Timestamp dateCommande, String statut, Timestamp dateReception) {
        this.id = id;
        this.idGestionnaire = idGestionnaire;
        this.idMedicament = idMedicament;
        this.quantite = quantite;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.dateReception = dateReception;
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
    
    public int getIdGestionnaire() {
        return idGestionnaire;
    }
    
    public void setIdGestionnaire(int idGestionnaire) {
        this.idGestionnaire = idGestionnaire;
    }
    
    public int getIdMedicament() {
        return idMedicament;
    }
    
    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }
    
    public int getQuantite() {
        return quantite;
    }
    
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    
    public Timestamp getDateCommande() {
        return dateCommande;
    }
    
    public void setDateCommande(Timestamp dateCommande) {
        this.dateCommande = dateCommande;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public Timestamp getDateReception() {
        return dateReception;
    }
    
    public void setDateReception(Timestamp dateReception) {
        this.dateReception = dateReception;
    }
    
    public String getGestionnaireNom() {
        return gestionnaireNom;
    }
    
    public void setGestionnaireNom(String gestionnaireNom) {
        this.gestionnaireNom = gestionnaireNom;
    }
    
    public String getMedicamentNom() {
        return medicamentNom;
    }
    
    public void setMedicamentNom(String medicamentNom) {
        this.medicamentNom = medicamentNom;
    }
    
    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================
    
    /**
     * Vérifie si la commande est en attente
     */
    public boolean isEnAttente() {
        return "en_attente".equalsIgnoreCase(statut);
    }
    
    /**
     * Vérifie si la commande est validée
     */
    public boolean isValidee() {
        return "validee".equalsIgnoreCase(statut);
    }
    
    /**
     * Vérifie si la commande est reçue
     */
    public boolean isRecue() {
        return "recue".equalsIgnoreCase(statut);
    }
    
    /**
     * Valide la commande
     */
    public void valider() {
        this.statut = "validee";
    }
    
    /**
     * Marque la commande comme reçue
     */
    public void marquerRecue() {
        this.statut = "recue";
        this.dateReception = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Annule la commande
     */
    public void annuler() {
        this.statut = "annulee";
    }
    
    @Override
    public String toString() {
        return "Commande #" + id + " - " + medicamentNom + " x" + quantite + 
               " [" + statut + "]";
    }
}