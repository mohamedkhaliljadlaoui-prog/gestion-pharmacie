package models;

public class Pharmacien {
    private int id;
    private String nom;
    private String prenom;
    private String login;
    private String pwd;
    private String matricule;
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    
    // Méthode pratique
    public void setPassword(String password) {
        this.pwd = password;
    }
    
    @Override
    public String toString() {
        return nom + " " + prenom + " (" + matricule + ")";
    }
}