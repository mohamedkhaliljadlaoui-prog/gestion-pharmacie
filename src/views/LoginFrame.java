package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Fenêtre de connexion – Gestion de Pharmacie
 * Version professionnelle et moderne
 */
@SuppressWarnings({ "serial", "unused" })
public class LoginFrame extends JFrame {

    // ===== COULEURS =====
    private static final Color BG_MAIN   = new Color(245, 247, 250);
    private static final Color PRIMARY   = new Color(33, 150, 243);
    private static final Color TEXT_MAIN = new Color(55, 55, 55);
    private static final Color TEXT_SUB  = new Color(130, 130, 130);
    private static final Color ERROR     = new Color(211, 47, 47);
    private static final Color SUCCESS   = new Color(46, 125, 50);

    // ===== COMPOSANTS =====
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbUserType;
    private JCheckBox chkRemember;
    private JButton btnLogin;
    private JButton btnCancel;
    private JLabel lblMessage;
    
    // ===== CONTROLEUR =====
    private controllers.LoginController controller;

    public LoginFrame() {
        initFrame();
        initComponents();
        buildLayout();
        
        // Initialiser le contrôleur
        this.controller = new controllers.LoginController(this);
        
        // Configurer les listeners
        setupEventListeners();
        
        setVisible(true);
    }

    // ===== FRAME =====
    private void initFrame() {
        setTitle("Gestion de Pharmacie – Connexion");
        setSize(520, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_MAIN);
    }

    // ===== COMPOSANTS =====
    private void initComponents() {
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        cmbUserType = new JComboBox<>(new String[]{"Pharmacien", "Gestionnaire"});

        styleField(txtUsername);
        styleField(txtPassword);
        styleField(cmbUserType);

        chkRemember = new JCheckBox("Se souvenir de moi");
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkRemember.setForeground(TEXT_SUB);

        btnLogin = createPrimaryButton("Connexion");
        btnCancel = createSecondaryButton("Annuler");

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    // ===== LAYOUT =====
    private void buildLayout() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_MAIN);

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(25, 25));
        card.setBorder(BorderFactory.createEmptyBorder(40, 45, 40, 45));
        card.setOpaque(false);

        // ----- TITRE -----
        JLabel lblTitle = new JLabel("Connexion", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel("Gestion de Pharmacie", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(TEXT_SUB);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        // ----- FORMULAIRE -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        form.add(createLabel("Nom d'utilisateur"), gbc);
        gbc.gridy++;
        form.add(txtUsername, gbc);

        gbc.gridy++;
        form.add(createLabel("Mot de passe"), gbc);
        gbc.gridy++;
        form.add(txtPassword, gbc);

        gbc.gridy++;
        form.add(createLabel("Type d'utilisateur"), gbc);
        gbc.gridy++;
        form.add(cmbUserType, gbc);

        gbc.gridy++;
        form.add(chkRemember, gbc);

        // ----- BOUTONS -----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);
        buttons.add(btnLogin);
        buttons.add(btnCancel);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        container.add(card);
        add(container, BorderLayout.CENTER);

        // ----- BAS -----
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_MAIN);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottom.add(lblMessage, BorderLayout.CENTER);

        JLabel credit = new JLabel("© 2026 Gestion de Pharmacie", SwingConstants.CENTER);
        credit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        credit.setForeground(TEXT_SUB);
        bottom.add(credit, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    // ===== EVENT LISTENERS =====
    private void setupEventListeners() {
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String userType = (String) cmbUserType.getSelectedItem();
            
            controller.handleLogin(username, password, userType);
        });
        
        btnCancel.addActionListener(e -> {
            controller.handleCancel();
        });
        
        // Entrée sur le champ mot de passe pour login
        txtPassword.addActionListener(e -> {
            btnLogin.doClick();
        });
    }

    // ===== MÉTHODES PUBLIQUES POUR LE CONTROLEUR =====
    
    public void clearMessage() {
        lblMessage.setText(" ");
    }
    
    public void showMessage(String message, Color color) {
        lblMessage.setText(message);
        lblMessage.setForeground(color);
    }
    
    public void showError(String msg) {
        lblMessage.setText(msg);
        lblMessage.setForeground(ERROR);
    }
    
    public void showSuccess(String msg) {
        lblMessage.setText(msg);
        lblMessage.setForeground(SUCCESS);
    }
    
    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        cmbUserType.setSelectedIndex(0);
        chkRemember.setSelected(false);
        clearMessage();
    }
    
    public void setFieldsEnabled(boolean enabled) {
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        cmbUserType.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        
        // Changer l'apparence du bouton pendant le chargement
        if (enabled) {
            btnLogin.setText("Connexion");
            btnLogin.setBackground(PRIMARY);
        } else {
            btnLogin.setText("Connexion...");
            btnLogin.setBackground(new Color(180, 180, 180));
        }
    }
    
    // Ancienne méthode pour compatibilité (utilisée par le contrôleur)
    public void setLoading(boolean loading) {
        setFieldsEnabled(!loading);
        if (loading) {
            showMessage("Connexion en cours...", Color.GRAY);
        }
    }

    // ===== OUTILS UI =====
    private void styleField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(360, 48));
        field.setMinimumSize(new Dimension(360, 48));
        field.setMaximumSize(new Dimension(360, 48));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(170, 48));
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Effet hover
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(PRIMARY.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(PRIMARY);
            }
        });
        
        return b;
    }

    private JButton createSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(130, 48));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(240, 240, 240));
        b.setForeground(TEXT_MAIN);
        
        // Effet hover
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(230, 230, 230));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(240, 240, 240));
            }
        });
        
        return b;
    }

    // ===== CARTE AVEC OMBRE =====
    class CardPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadow = 10;
            int arc = 30;

            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(
                    shadow, shadow,
                    getWidth() - shadow * 2,
                    getHeight() - shadow * 2,
                    arc, arc
            );

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    0, 0,
                    getWidth() - shadow * 2,
                    getHeight() - shadow * 2,
                    arc, arc
            );
        }
    }

    // ===== MAIN (TEST) =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }
    
    // ===== GETTERS POUR TESTS =====
    public JTextField getTxtUsername() { return txtUsername; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JComboBox<String> getCmbUserType() { return cmbUserType; }
    public JButton getBtnLogin() { return btnLogin; }
    public JButton getBtnCancel() { return btnCancel; }
    public JLabel getLblMessage() { return lblMessage; }
}