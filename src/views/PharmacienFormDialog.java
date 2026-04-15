package views;

import javax.swing.*;
import java.awt.*;
import models.Pharmacien;
import models.Gestionnaire;
import dao.PharmacienDAO;
import dao.GestionnaireDAO;

/**
 * Dialog pour ajouter/modifier un pharmacien
 */
@SuppressWarnings("serial")
public class PharmacienFormDialog extends JDialog {
    
    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtLogin;
    private JPasswordField txtPassword;
    private JTextField txtMatricule;
    private JButton btnSave;
    private JButton btnCancel;
    
    private Pharmacien pharmacien;
    private boolean saved = false;
    
    public PharmacienFormDialog(@SuppressWarnings("exports") Frame parent, Pharmacien pharmacien) {
        super(parent, pharmacien == null ? "Nouveau Pharmacien" : "Modifier Pharmacien", true);
        this.pharmacien = pharmacien;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        if (pharmacien != null) {
            loadPharmacienData();
        }
        
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        txtNom = new JTextField(20);
        txtPrenom = new JTextField(20);
        txtLogin = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtMatricule = new JTextField(20);
        
        btnSave = new JButton(pharmacien == null ? "Ajouter" : "Modifier");
        btnCancel = new JButton("Annuler");
        
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        
        btnCancel.setBackground(new Color(244, 67, 54));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitle = new JLabel(pharmacien == null ? "➕ Nouveau Pharmacien" : "✏️ Modifier Pharmacien");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(33, 150, 243));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Nom *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtNom, gbc);
        row++;
        
        // Prénom
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Prénom *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtPrenom, gbc);
        row++;
        
        // Login
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Login *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtLogin, gbc);
        row++;
        
        // Mot de passe
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Mot de passe *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtPassword, gbc);
        row++;
        
        // Matricule
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Matricule:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtMatricule, gbc);
        row++;
        
        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("* Champs obligatoires");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        btnSave.addActionListener(e -> savePharmacien());
        btnCancel.addActionListener(e -> dispose());
    }
    
    private void loadPharmacienData() {
        txtNom.setText(pharmacien.getNom());
        txtPrenom.setText(pharmacien.getPrenom());
        txtLogin.setText(pharmacien.getLogin());
        txtMatricule.setText(pharmacien.getMatricule());
        // Ne pas charger le mot de passe pour des raisons de sécurité
    }
    
    private void savePharmacien() {
        // Validation
        if (txtNom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtNom.requestFocus();
            return;
        }
        
        if (txtPrenom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtPrenom.requestFocus();
            return;
        }
        
        if (txtLogin.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le login est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtLogin.requestFocus();
            return;
        }
        
        String password = new String(txtPassword.getPassword());
        if (pharmacien == null && password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le mot de passe est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        if (!password.isEmpty() && password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 6 caractères", "Erreur", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }
        
        try {
            PharmacienDAO dao = new PharmacienDAO();
            
            if (pharmacien == null) {
                // Nouveau pharmacien
                pharmacien = new Pharmacien();
            }
            
            pharmacien.setNom(txtNom.getText().trim());
            pharmacien.setPrenom(txtPrenom.getText().trim());
            pharmacien.setLogin(txtLogin.getText().trim());
            pharmacien.setMatricule(txtMatricule.getText().trim());
            
            // Mettre à jour le mot de passe seulement s'il est renseigné
            if (!password.isEmpty()) {
                pharmacien.setPassword(password);
            }
            
            boolean success;
            if (pharmacien.getId() == 0) {
                success = dao.add(pharmacien);
            } else {
                success = dao.update(pharmacien);
            }
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, 
                    "Pharmacien " + (pharmacien.getId() == 0 ? "ajouté" : "modifié") + " avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur: " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public Pharmacien getPharmacien() {
        return pharmacien;
    }
}

/**
 * Dialog pour ajouter/modifier un gestionnaire
 */
@SuppressWarnings("serial")
class GestionnaireFormDialog extends JDialog {
    
    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtLogin;
    private JPasswordField txtPassword;
    private JButton btnSave;
    private JButton btnCancel;
    
    private Gestionnaire gestionnaire;
    private boolean saved = false;
    
    public GestionnaireFormDialog(Frame parent, Gestionnaire gestionnaire) {
        super(parent, gestionnaire == null ? "Nouveau Gestionnaire" : "Modifier Gestionnaire", true);
        this.gestionnaire = gestionnaire;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        if (gestionnaire != null) {
            loadGestionnaireData();
        }
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        txtNom = new JTextField(20);
        txtPrenom = new JTextField(20);
        txtLogin = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        btnSave = new JButton(gestionnaire == null ? "Ajouter" : "Modifier");
        btnCancel = new JButton("Annuler");
        
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        
        btnCancel.setBackground(new Color(244, 67, 54));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitle = new JLabel(gestionnaire == null ? "➕ Nouveau Gestionnaire" : "✏️ Modifier Gestionnaire");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255, 152, 0));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Nom *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtNom, gbc);
        row++;
        
        // Prénom
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Prénom *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtPrenom, gbc);
        row++;
        
        // Login
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Login *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtLogin, gbc);
        row++;
        
        // Mot de passe
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(createLabel("Mot de passe *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtPassword, gbc);
        row++;
        
        // Note
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("* Champs obligatoires");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        btnSave.addActionListener(e -> saveGestionnaire());
        btnCancel.addActionListener(e -> dispose());
    }
    
    private void loadGestionnaireData() {
        txtNom.setText(gestionnaire.getNom());
        txtPrenom.setText(gestionnaire.getPrenom());
        txtLogin.setText(gestionnaire.getLogin());
    }
    
    private void saveGestionnaire() {
        // Validation
        if (txtNom.getText().trim().isEmpty() ||
            txtPrenom.getText().trim().isEmpty() ||
            txtLogin.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les champs sont obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String password = new String(txtPassword.getPassword());
        if (gestionnaire == null && password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le mot de passe est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            GestionnaireDAO dao = new GestionnaireDAO();
            
            if (gestionnaire == null) {
                gestionnaire = new Gestionnaire();
            }
            
            gestionnaire.setNom(txtNom.getText().trim());
            gestionnaire.setPrenom(txtPrenom.getText().trim());
            gestionnaire.setLogin(txtLogin.getText().trim());
            
            if (!password.isEmpty()) {
                gestionnaire.setPwd(password);
            }
            
            boolean success = dao.update(gestionnaire); // Utilise update car GestionnaireDAO l'a
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, 
                    "Gestionnaire enregistré avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur: " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public Gestionnaire getGestionnaire() {
        return gestionnaire;
    }
}