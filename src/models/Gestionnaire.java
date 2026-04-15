package models;

public class Gestionnaire {
    private int id;
    private String nom;
    private String prenom;
    private String login;
    private String pwd;
    
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
}