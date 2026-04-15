package services;

import dao.PharmacienDAO;
import dao.GestionnaireDAO;
import models.User;
import models.Pharmacien;
import models.Gestionnaire;
import java.util.List;

/**
 * Service d'authentification
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private final PharmacienDAO pharmacienDAO;
    private final GestionnaireDAO gestionnaireDAO;

    private AuthenticationService() {
        this.pharmacienDAO = new PharmacienDAO();
        this.gestionnaireDAO = new GestionnaireDAO();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    // ===================== LOGIN =====================
    public User login(String login, String password, String role) {
        System.out.println("=== AUTHENTIFICATION ===");
        System.out.println("Login: " + login);
        System.out.println("Rôle: " + role);
        
        try {
            if ("GESTIONNAIRE".equals(role)) {
                System.out.println("Recherche dans la table GESTIONNAIRE...");
                
                // Méthode 1: Récupérer tous les gestionnaires
                List<Gestionnaire> gestionnaires = gestionnaireDAO.getAll();
                System.out.println("Nombre de gestionnaires dans BD: " + gestionnaires.size());
                
                for (Gestionnaire g : gestionnaires) {
                    System.out.println("  - Login BD: " + g.getLogin() + ", Password BD: " + g.getPwd());
                    
                    // Comparer login et mot de passe
                    if (login.equals(g.getLogin()) && password.equals(g.getPwd())) {
                        System.out.println("✅ Gestionnaire trouvé: " + g.getNom());
                        
                        // Créer l'objet User
                        User user = new User();
                        user.setId(g.getId());
                        user.setNom(g.getNom());
                        user.setPrenom(g.getPrenom());
                        user.setLogin(g.getLogin());
                        user.setRole("GESTIONNAIRE");
                        return user;
                    }
                }
                
                System.out.println("❌ Aucun gestionnaire trouvé avec ces identifiants");
                
            } else if ("PHARMACIEN".equals(role)) {
                System.out.println("Recherche dans la table PHARMACIEN...");
                
                // Récupérer tous les pharmaciens
                List<Pharmacien> pharmaciens = pharmacienDAO.getAll();
                System.out.println("Nombre de pharmaciens dans BD: " + pharmaciens.size());
                
                for (Pharmacien p : pharmaciens) {
                    System.out.println("  - Login BD: " + p.getLogin() + ", Password BD: " + p.getPwd());
                    
                    if (login.equals(p.getLogin()) && password.equals(p.getPwd())) {
                        System.out.println("✅ Pharmacien trouvé: " + p.getNom());
                        
                        User user = new User();
                        user.setId(p.getId());
                        user.setNom(p.getNom());
                        user.setPrenom(p.getPrenom());
                        user.setLogin(p.getLogin());
                        user.setRole("PHARMACIEN");
                        return user;
                    }
                }
                
                System.out.println("❌ Aucun pharmacien trouvé avec ces identifiants");
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===================== LOGOUT =====================
    public void logout() {
        // Vide pour l'instant
    }

    // ===================== GETTERS =====================
    public User getCurrentUser() {
        return null; // À implémenter si nécessaire
    }

    public boolean isLoggedIn() {
        return false; // À implémenter si nécessaire
    }
}