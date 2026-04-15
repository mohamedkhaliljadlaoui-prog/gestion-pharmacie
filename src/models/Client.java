// [file name]: Client.java
package models;

/**
 * Classe représentant un client
 */
public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    
    /**
     * Constructeur par défaut
     */
    public Client() {
    }
    
    /**
     * Constructeur avec paramètres essentiels
     */
    public Client(String nom, String prenom, String email, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }
    
    /**
     * Constructeur complet
     */
    public Client(int id, String nom, String prenom, String email, String adresse, String telephone) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
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
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    /**
     * Retourne le nom complet du client
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    /**
     * Vérifie si l'email est valide
     */
    public boolean isEmailValide() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (" + email + ")";
    }
}