package controllers;

import javax.swing.JOptionPane;

import models.User;
import services.AuthenticationService;
import views.GestionnaireDashboard;
import views.LoginFrame;
import views.PharmacienDashboard;

/**
 * Contrôleur de connexion - Version ULTRA simple
 */
public class LoginController {

    private final LoginFrame view;
    private final AuthenticationService authService;

    public LoginController(LoginFrame view) {
        this.view = view;
        this.authService = AuthenticationService.getInstance();
    }

    public void handleLogin(String login, String password, String role) {
        System.out.println("DEBUG - Login: " + login + ", Role: " + role);
        
        // Validation basique
        if (login.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Normalisation du rôle
        String roleNormalized;
        if ("Pharmacien".equals(role)) {
            roleNormalized = "PHARMACIEN";
        } else {
            roleNormalized = "GESTIONNAIRE";
        }
        
        // SIMPLE - Appel direct
        try {
            // Afficher chargement
            view.setLoading(true);
            
            // Appel DIRECT
            User user = authService.login(login, password, roleNormalized);
            
            // Arrêter chargement
            view.setLoading(false);
            
            if (user != null) {
                System.out.println("SUCCÈS - Utilisateur: " + user.getNom());
                view.showSuccess("Connexion réussie!");
                
                // Fermer login IMMÉDIATEMENT
                view.dispose();
                
                // Ouvrir dashboard
                if ("PHARMACIEN".equals(roleNormalized)) {
                    new PharmacienDashboard(user).setVisible(true);
                } else {
                    new GestionnaireDashboard(user).setVisible(true);
                }
            } else {
                System.err.println("ÉCHEC - Identifiants invalides");
                JOptionPane.showMessageDialog(view, 
                    "Identifiants incorrects!\nLogin: " + login + "\nRôle: " + roleNormalized, 
                    "Échec", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            view.setLoading(false);
            System.err.println("EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, 
                "Erreur: " + e.getMessage(), 
                "Exception", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleCancel() {
        view.clearFields();
    }
}