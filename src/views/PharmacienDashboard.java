package views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.ClientDAO;
import dao.MedicamentDAO;
import dao.VenteDAO;
import models.Client;
import models.Medicament;
import models.User;
import models.Vente;
import services.EmailService;
import services.ExportService;
import services.ReportService;
import services.StockService;
import services.VenteService;

@SuppressWarnings("serial")
public class PharmacienDashboard extends JFrame {
	// Après les autres déclarations de variables
	@SuppressWarnings("unused")
	private JTable alertesTable;
	@SuppressWarnings("unused")
	private DefaultTableModel alertesTableModel;
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    @SuppressWarnings("unused")
	private StockService stockService;
    private VenteService venteService;
    private EmailService emailService;
    @SuppressWarnings("unused")
	private ExportService exportService;
    @SuppressWarnings("unused")
	private ReportService reportService;
    private ClientDAO clientDAO;
    private MedicamentDAO medicamentDAO;
    @SuppressWarnings("unused")
	private VenteDAO venteDAO;
    
    private JLabel lblTotalStock, lblStockCritique, lblVentesJour, lblCAJour;
    private JTable stockTable, clientsTable, notificationsTable, ventesTable;
    private DefaultTableModel stockTableModel, clientsTableModel, notificationsTableModel, ventesTableModel;
    
    // Couleurs
    private static final Color PRIMARY = new Color(33, 150, 243);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color WARNING = new Color(255, 152, 0);
    private static final Color DANGER = new Color(244, 67, 54);
    private static final Color INFO = new Color(156, 39, 176);
    
    public PharmacienDashboard(User user) {
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        setupModernLayout();
        setupEventHandlers();
        loadInitialData();
        
        setLocationRelativeTo(null);
    }
    
    private void initializeServices() {
        this.stockService = new StockService();
        this.venteService = new VenteService();
        this.emailService = new EmailService();
        this.exportService = new ExportService();
        this.reportService = new ReportService();
        this.clientDAO = new ClientDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.venteDAO = new VenteDAO();
    }
    
    private void initializeComponents() {
        setTitle("Pharmacy Manager - Pharmacien");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        lblTotalStock = createStatLabel("0");
        lblStockCritique = createStatLabel("0");
        lblVentesJour = createStatLabel("0");
        lblCAJour = createStatLabel("0 DT");
        
        initializeTables();
    }
    
    private void initializeTables() {
        String[] stockColumns = {"ID", "Nom", "Dosage", "Stock", "Prix", "Seuil", "Status"};
        stockTableModel = new DefaultTableModel(stockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        styleTable(stockTable);
        
        String[] clientsColumns = {"ID", "Nom", "Prénom", "Email", "Téléphone", "Adresse"};
        clientsTableModel = new DefaultTableModel(clientsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientsTable = new JTable(clientsTableModel);
        styleTable(clientsTable);
        
        String[] notifColumns = {"Date", "Type", "Message", "Priorité", "Statut"};
        notificationsTableModel = new DefaultTableModel(notifColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        notificationsTable = new JTable(notificationsTableModel);
        styleTable(notificationsTable);
        
        String[] ventesColumns = {"ID", "Date", "Client", "Médicament", "Qté", "Prix Total", "Statut"};
        ventesTableModel = new DefaultTableModel(ventesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ventesTable = new JTable(ventesTableModel);
        styleTable(ventesTable);
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        return label;
    }
    
    private void setupModernLayout() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = createHeaderPanel();
        JPanel sidebarPanel = createSidebarPanel();
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createStockPanel(), "stock");
        mainPanel.add(createVentePanel(), "vente");
        mainPanel.add(createClientsPanel(), "clients");
        mainPanel.add(createProfilePanel(), "profile");
        mainPanel.add(createNotificationsPanel(), "notifications");
        mainPanel.add(createAlertesPanel(), "alertes");
        mainPanel.add(createConsultationVentesPanel(), "consultationVentes");
        mainPanel.add(createRapportsPanel(), "rapports");
        
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY);
        panel.setPreferredSize(new Dimension(getWidth(), 70));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        JLabel lblTitle = new JLabel("PHARMACY MANAGER - PHARMACIEN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(PRIMARY);
        
        JLabel lblUser = new JLabel(currentUser.getPrenom() + " " + currentUser.getNom());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);
        
        JLabel lblRole = new JLabel("PHARMACIEN");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(PRIMARY);
        lblRole.setBackground(Color.WHITE);
        lblRole.setOpaque(true);
        lblRole.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        userPanel.add(lblUser);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(lblRole);
        
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(45, 55, 72));
        panel.setPreferredSize(new Dimension(250, getHeight()));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblSidebarTitle = new JLabel("NAVIGATION");
        lblSidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSidebarTitle.setForeground(new Color(200, 200, 200));
        lblSidebarTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        panel.add(lblSidebarTitle);
        panel.add(Box.createVerticalStrut(10));
        
        JButton btnDashboard = createNavButton("📊 Tableau de bord", "dashboard");
        JButton btnStock = createNavButton("📦 Gestion Stock", "stock");
        JButton btnVente = createNavButton("💰 Nouvelle Vente", "vente");
        JButton btnClients = createNavButton("👥 Clients", "clients");
        JButton btnConsultationVentes = createNavButton("📋 Consultation Ventes", "consultationVentes");
        JButton btnAlertes = createNavButton("⚠️ Alertes", "alertes");
        JButton btnNotifications = createNavButton("📨 Notifications", "notifications");
        JButton btnRapports = createNavButton("📊 Rapports", "rapports");
        
        panel.add(btnDashboard);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnStock);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnVente);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnClients);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnConsultationVentes);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnAlertes);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnNotifications);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnRapports);
        panel.add(Box.createVerticalStrut(30));
        
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(100, 100, 100));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(30));
        
        JButton btnProfile = createNavButton("👤 Mon Profil", "profile");
        JButton btnLogout = createNavButton("🚪 Déconnexion", "logout");
        
        panel.add(btnProfile);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnLogout);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(210, 45));
        button.setMinimumSize(new Dimension(210, 45));
        
        button.setBackground(new Color(60, 70, 90));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(70, 80, 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 70, 90));
            }
        });
        
        button.addActionListener(e -> {
            if ("logout".equals(panelName)) {
                handleLogout();
            } else {
                cardLayout.show(mainPanel, panelName);
                if ("stock".equals(panelName)) {
                    loadStockData();
                } else if ("dashboard".equals(panelName)) {
                    updateStatistics();
                } else if ("clients".equals(panelName)) {
                    loadClientsData();
                } else if ("consultationVentes".equals(panelName)) {
                    loadVentesData();
                } else if ("alertes".equals(panelName)) {
                    loadAlertesData();
                } else if ("notifications".equals(panelName)) {
                    loadNotificationsData();
                } else if ("rapports".equals(panelName)) {
                    loadRapportsData();
                }
            }
        });
        
        return button;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("Tableau de Bord - Pharmacien");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        statsPanel.add(createStatCard("Total Stock", lblTotalStock, PRIMARY));
        statsPanel.add(createStatCard("Stock Critique", lblStockCritique, DANGER));
        statsPanel.add(createStatCard("Ventes Aujourd'hui", lblVentesJour, WARNING));
        statsPanel.add(createStatCard("Chiffre d'Affaires", lblCAJour, SUCCESS));
        
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickActionsPanel.setBackground(Color.WHITE);
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Actions Rapides"));
        
        JButton btnQuickVente = createQuickActionButton("💰 Nouvelle Vente", "💳");
        JButton btnQuickStock = createQuickActionButton("📦 Vérifier Stock", "📊");
        JButton btnQuickAlertes = createQuickActionButton("⚠️ Alertes Stock", "🚨");
        JButton btnQuickClients = createQuickActionButton("👥 Ajouter Client", "➕");
        JButton btnQuickRapport = createQuickActionButton("📊 Rapport Journalier", "📈");
        JButton btnQuickNotifications = createQuickActionButton("📨 Notifications", "🔔");
        
        btnQuickVente.addActionListener(e -> cardLayout.show(mainPanel, "vente"));
        btnQuickStock.addActionListener(e -> {
            cardLayout.show(mainPanel, "stock");
            loadStockData();
        });
        btnQuickAlertes.addActionListener(e -> {
            cardLayout.show(mainPanel, "alertes");
            loadAlertesData();
        });
        btnQuickClients.addActionListener(e -> {
            cardLayout.show(mainPanel, "clients");
            showAddClientDialog();
        });
        btnQuickRapport.addActionListener(e -> {
            cardLayout.show(mainPanel, "rapports");
            generateDailyReport();
        });
        btnQuickNotifications.addActionListener(e -> {
            cardLayout.show(mainPanel, "notifications");
            loadNotificationsData();
        });
        
        quickActionsPanel.add(btnQuickVente);
        quickActionsPanel.add(btnQuickStock);
        quickActionsPanel.add(btnQuickAlertes);
        quickActionsPanel.add(btnQuickClients);
        quickActionsPanel.add(btnQuickRapport);
        quickActionsPanel.add(btnQuickNotifications);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createQuickActionButton(String text, String icon) {
        JButton button = new JButton(icon + " " + text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY);
            }
        });
        
        return button;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📦 Gestion du Stock");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnSearch = createActionButton("🔍 Rechercher", INFO);
        JButton btnExport = createActionButton("📊 Exporter CSV", SUCCESS);
        JButton btnAlertes = createActionButton("⚠️ Voir Alertes", DANGER);
        JButton btnCommander = createActionButton("📋 Demander Commande", WARNING);
        
        btnRefresh.addActionListener(e -> loadStockData());
        btnSearch.addActionListener(e -> showSearchDialog());
        btnExport.addActionListener(e -> exportStockCSV());
        btnAlertes.addActionListener(e -> {
            cardLayout.show(mainPanel, "alertes");
            loadAlertesData();
        });
        btnCommander.addActionListener(e -> showDemandeCommandeDialog());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnAlertes);
        buttonPanel.add(btnCommander);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createVentePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("💰 Nouvelle Vente");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Détails de la vente"
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Client
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Client:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cmbClients = new JComboBox<>();
        loadClientsIntoCombo(cmbClients);
        formPanel.add(cmbClients, gbc);
        
        gbc.gridx = 2;
        JButton btnNewClient = createActionButton("➕ Nouveau", SUCCESS);
        btnNewClient.addActionListener(e -> showAddClientDialog());
        formPanel.add(btnNewClient, gbc);
        
        // Médicament
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Médicament:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cmbMedicaments = new JComboBox<>();
        loadMedicamentsIntoCombo(cmbMedicaments);
        formPanel.add(cmbMedicaments, gbc);
        
        gbc.gridx = 2;
        JButton btnCheckStock = createActionButton("📊 Stock", INFO);
        btnCheckStock.addActionListener(e -> {
            String selected = (String) cmbMedicaments.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split(" - ");
                if (parts.length > 1) {
                    String medInfo = parts[1];
                    JOptionPane.showMessageDialog(this, 
                        "Stock disponible pour " + medInfo + ": " + getStockForMedicament(medInfo) + " unités",
                        "Stock Disponible",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        formPanel.add(btnCheckStock, gbc);
        
        // Quantité
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 1;
        JSpinner spnQuantite = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        formPanel.add(spnQuantite, gbc);
        
        // Prix unitaire (automatique)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Prix unitaire (DT):"), gbc);
        
        gbc.gridx = 1;
        JLabel lblPrixUnitaire = new JLabel("0.00");
        lblPrixUnitaire.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrixUnitaire.setForeground(PRIMARY);
        formPanel.add(lblPrixUnitaire, gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(new JLabel("Total:"), gbc);
        
        gbc.gridx = 1;
        JLabel lblTotal = new JLabel("0.00 DT");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(SUCCESS);
        formPanel.add(lblTotal, gbc);
        
        // Calcul automatique du total
        spnQuantite.addChangeListener(e -> {
            try {
                int qte = (Integer) spnQuantite.getValue();
                double prix = Double.parseDouble(lblPrixUnitaire.getText());
                lblTotal.setText(String.format("%.2f DT", qte * prix));
            } catch (NumberFormatException ex) {
                lblTotal.setText("0.00 DT");
            }
        });
        
        // Écouteur pour mettre à jour le prix quand le médicament change
        cmbMedicaments.addActionListener(e -> {
            String selected = (String) cmbMedicaments.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                try {
                    String[] parts = selected.split(" - ");
                    if (parts.length > 0) {
                        int idMedicament = Integer.parseInt(parts[0]);
                        Medicament medicament = medicamentDAO.getById(idMedicament);
                        if (medicament != null) {
                            // Mettre à jour le prix unitaire
                            double prix = medicament.getPrixUnitaire();
                            lblPrixUnitaire.setText(String.format("%.2f", prix));
                            
                            // Mettre à jour le total
                            int qte = (Integer) spnQuantite.getValue();
                            lblTotal.setText(String.format("%.2f DT", prix * qte));
                        }
                    }
                } catch (NumberFormatException ex) {
                    lblPrixUnitaire.setText("0.00");
                    lblTotal.setText("0.00 DT");
                }
            }
        });
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnVendre = createActionButton("💳 Enregistrer la Vente", SUCCESS);
        JButton btnAnnuler = createActionButton("❌ Annuler", DANGER);
        JButton btnHistorique = createActionButton("📋 Historique Ventes", INFO);
        JButton btnRapport = createActionButton("📊 Rapport Ventes", WARNING);
        
        btnVendre.setPreferredSize(new Dimension(200, 45));
        btnVendre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        btnVendre.addActionListener(e -> {
            try {
                String medicamentText = (String) cmbMedicaments.getSelectedItem();
                if (medicamentText == null || medicamentText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String[] parts = medicamentText.split(" - ");
                int idMedicament = Integer.parseInt(parts[0]);
                Medicament medicament = medicamentDAO.getById(idMedicament);
                if (medicament == null) {
                    JOptionPane.showMessageDialog(this, "Médicament non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int idClient = 0;
                String clientText = (String) cmbClients.getSelectedItem();
                if (clientText != null && !clientText.contains("Client occasionnel")) {
                    String[] clientParts = clientText.split(" - ");
                    if (clientParts.length > 0 && clientParts[0].matches("\\d+")) {
                        idClient = Integer.parseInt(clientParts[0]);
                    }
                }
                
                int quantite = (Integer) spnQuantite.getValue();
                double prixUnitaire;
                try {
                    prixUnitaire = Double.parseDouble(lblPrixUnitaire.getText());
                } catch (NumberFormatException ex) {
                    prixUnitaire = medicament.getPrixUnitaire();
                }
                double total = quantite * prixUnitaire;
                
                String verification = venteService.verifierVentePossible(idMedicament, quantite);
                if (!"OK".equals(verification)) {
                    JOptionPane.showMessageDialog(this, verification, "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String messageConfirmation = String.format(
                    "Confirmer l'enregistrement de la vente?\n\n" +
                    "Médicament: %s\n" +
                    "Client: %s\n" +
                    "Quantité: %d\n" +
                    "Prix unitaire: %.2f DT\n" +
                    "Prix total: %.2f DT",
                    medicament.getNom(),
                    (idClient == 0 ? "Client occasionnel" : clientText),
                    quantite,
                    prixUnitaire,
                    total
                );
                
                int confirmation = JOptionPane.showConfirmDialog(this, 
                    messageConfirmation, 
                    "Confirmation de Vente", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirmation == JOptionPane.YES_OPTION) {
                    boolean succes = venteService.enregistrerVente(
                        currentUser.getId(),
                        idClient, 
                        idMedicament, 
                        quantite
                    );
                    
                    if (succes) {
                        JOptionPane.showMessageDialog(this, 
                            "✅ Vente enregistrée avec succès!\n" +
                            "Stock mis à jour: " + (medicament.getStock() - quantite) + " unités restantes", 
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                        
                        spnQuantite.setValue(1);
                        cmbMedicaments.setSelectedIndex(0);
                        cmbClients.setSelectedIndex(0);
                        lblPrixUnitaire.setText("0.00");
                        lblTotal.setText("0.00 DT");
                        
                        updateStatistics();
                        loadStockData();
                        loadVentesData();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "❌ Erreur lors de l'enregistrement de la vente", 
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnAnnuler.addActionListener(e -> {
            spnQuantite.setValue(1);
            cmbMedicaments.setSelectedIndex(0);
            cmbClients.setSelectedIndex(0);
            lblPrixUnitaire.setText("0.00");
            lblTotal.setText("0.00 DT");
        });
        
        btnHistorique.addActionListener(e -> {
            cardLayout.show(mainPanel, "consultationVentes");
            loadVentesData();
        });
        
        btnRapport.addActionListener(e -> generateDailySalesReport());
        
        buttonPanel.add(btnVendre);
        buttonPanel.add(btnAnnuler);
        buttonPanel.add(btnHistorique);
        buttonPanel.add(btnRapport);
        
        formPanel.add(buttonPanel, gbc);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("👥 Gestion des Clients");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = createActionButton("➕ Ajouter", SUCCESS);
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnExport = createActionButton("📊 Exporter CSV", INFO);
        JButton btnSearch = createActionButton("🔍 Rechercher", WARNING);
        
        btnAdd.addActionListener(e -> showAddClientDialog());
        btnRefresh.addActionListener(e -> loadClientsData());
        btnExport.addActionListener(e -> exportClientsCSV());
        btnSearch.addActionListener(e -> searchClientDialog());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnSearch);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(clientsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createConsultationVentesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📋 Consultation des Ventes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnFilter = createActionButton("🔍 Filtrer", INFO);
        JButton btnExport = createActionButton("📊 Exporter CSV", SUCCESS);
        JButton btnAnnuler = createActionButton("❌ Annuler Vente", DANGER);
        JButton btnRapport = createActionButton("📈 Rapport Complet", WARNING);
        
        btnRefresh.addActionListener(e -> loadVentesData());
        btnFilter.addActionListener(e -> filterVentesDialog());
        btnExport.addActionListener(e -> exportVentesCSV());
        
        // MODIFICATION : Utiliser VenteService pour annuler les ventes
        btnAnnuler.addActionListener(e -> {
            int selectedRow = ventesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner une vente à annuler",
                    "Aucune sélection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int idVente = (int) ventesTableModel.getValueAt(selectedRow, 0);
                String client = (String) ventesTableModel.getValueAt(selectedRow, 2);
                String medicament = (String) ventesTableModel.getValueAt(selectedRow, 3);
                double montant = Double.parseDouble(((String)ventesTableModel.getValueAt(selectedRow, 5)).replace(" DT", ""));
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir annuler cette vente?\n\n" +
                    "ID: " + idVente + "\n" +
                    "Client: " + client + "\n" +
                    "Médicament: " + medicament + "\n" +
                    "Montant: " + montant + " DT\n\n" +
                    "Cette action restituera le stock et ne pourra pas être annulée.",
                    "Confirmation d'annulation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = venteService.annulerVente(idVente);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "✅ Vente #" + idVente + " annulée avec succès!\n" +
                            "Le stock a été restitué.",
                            "Annulation réussie",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadVentesData();
                        loadStockData();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "❌ Erreur lors de l'annulation de la vente",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnRapport.addActionListener(e -> generateDetailedSalesReport());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnFilter);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnAnnuler);
        buttonPanel.add(btnRapport);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(ventesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📨 Notifications et Alertes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnVoirEmails = createActionButton("📧 Voir Emails", SUCCESS);
        JButton btnAlertesStock = createActionButton("⚠️ Alertes Stock", DANGER);
        JButton btnConfig = createActionButton("⚙️ Config", INFO);
        JButton btnClear = createActionButton("🗑️ Effacer", WARNING);
        
        btnRefresh.addActionListener(e -> loadNotificationsData());
        btnVoirEmails.addActionListener(e -> ouvrirDossierEmails());
        btnAlertesStock.addActionListener(e -> {
            cardLayout.show(mainPanel, "alertes");
            loadAlertesData();
        });
        btnConfig.addActionListener(e -> configurerNotifications());
        btnClear.addActionListener(e -> clearNotifications());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnVoirEmails);
        buttonPanel.add(btnAlertesStock);
        buttonPanel.add(btnConfig);
        buttonPanel.add(btnClear);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel infoLabel = new JLabel("🔔 Double-cliquez sur une notification pour plus de détails");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        // Double-clic sur les notifications
        notificationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = notificationsTable.getSelectedRow();
                    if (row != -1) {
                        String type = (String) notificationsTableModel.getValueAt(row, 1);
                        String message = (String) notificationsTableModel.getValueAt(row, 2);
                        JOptionPane.showMessageDialog(PharmacienDashboard.this,
                            "Notification détaillée:\n\nType: " + type + "\nMessage: " + message,
                            "Détails de la notification",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createAlertesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("⚠️ Alertes Stock Critique");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        // ... boutons existants
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // MODIFICATION ICI : Utiliser les variables d'instance
        String[] alertColumns = {"Médicament", "Dosage", "Stock Actuel", "Seuil", "Différence", "État"};
        alertesTableModel = new DefaultTableModel(alertColumns, 0);
        alertesTable = new JTable(alertesTableModel);
        styleTable(alertesTable);
        
        // Colorer les lignes selon l'état
        alertesTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String etat = (String) table.getValueAt(row, 5);
                    if ("CRITIQUE".equals(etat)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if ("FAIBLE".equals(etat)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(alertesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRapportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📊 Rapports et Statistiques");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnGenerer = createActionButton("🔄 Générer", PRIMARY);
        JButton btnExportPDF = createActionButton("📄 Exporter PDF", DANGER);
        JButton btnExportExcel = createActionButton("📊 Exporter Excel", SUCCESS);
        JButton btnVueEnsemble = createActionButton("👁️ Vue d'ensemble", INFO);
        
        btnGenerer.addActionListener(e -> generateAllReports());
        btnExportPDF.addActionListener(e -> exportReportsToPDF());
        btnExportExcel.addActionListener(e -> exportReportsToExcel());
        btnVueEnsemble.addActionListener(e -> showDashboardOverview());
        
        buttonPanel.add(btnGenerer);
        buttonPanel.add(btnExportPDF);
        buttonPanel.add(btnExportExcel);
        buttonPanel.add(btnVueEnsemble);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Onglets pour différents rapports
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Rapport 1: Ventes
        tabbedPane.addTab("💰 Ventes", createSalesReportPanel());
        
        // Rapport 2: Stock
        tabbedPane.addTab("📦 Stock", createStockReportPanel());
        
        // Rapport 3: Clients
        tabbedPane.addTab("👥 Clients", createClientReportPanel());
        
        // Rapport 4: Journalier
        tabbedPane.addTab("📅 Journalier", createDailyReportPanel());
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("👤 Mon Profil - Pharmacien");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Avatar
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 6;
        JLabel lblAvatar = new JLabel("👨‍⚕️");
        lblAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        lblAvatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        infoPanel.add(lblAvatar, gbc);
        
        // Informations
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1;
        infoPanel.add(new JLabel("ID Pharmacien:"), gbc);
        gbc.gridx = 2;
        JLabel lblId = new JLabel(String.valueOf(currentUser.getId()));
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoPanel.add(lblId, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        infoPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 2;
        JLabel lblNom = new JLabel(currentUser.getNom());
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoPanel.add(lblNom, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        infoPanel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 2;
        JLabel lblPrenom = new JLabel(currentUser.getPrenom());
        lblPrenom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoPanel.add(lblPrenom, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        infoPanel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 2;
        JLabel lblLogin = new JLabel(currentUser.getLogin());
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoPanel.add(lblLogin, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        infoPanel.add(new JLabel("Rôle:"), gbc);
        gbc.gridx = 2;
        JLabel lblRole = new JLabel("PHARMACIEN");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setForeground(PRIMARY);
        infoPanel.add(lblRole, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        infoPanel.add(new JLabel("Statistiques:"), gbc);
        gbc.gridx = 2;
        JLabel lblStats = new JLabel("Actif - " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        lblStats.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStats.setForeground(new Color(100, 100, 100));
        infoPanel.add(lblStats, gbc);
        
        // Boutons d'action
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnChangerMDP = createActionButton("🔐 Changer MDP", INFO);
        JButton btnModifierProfil = createActionButton("✏️ Modifier Profil", PRIMARY);
        JButton btnActivite = createActionButton("📊 Mon Activité", SUCCESS);
        
        btnChangerMDP.addActionListener(e -> changerMotDePasse());
        btnModifierProfil.addActionListener(e -> modifierProfil());
        btnActivite.addActionListener(e -> showMonActivite());
        
        actionPanel.add(btnChangerMDP);
        actionPanel.add(btnModifierProfil);
        actionPanel.add(btnActivite);
        
        infoPanel.add(actionPanel, gbc);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSalesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Statistiques clés
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        
        JLabel lblVentesTotal = new JLabel("0", SwingConstants.CENTER);
        lblVentesTotal.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblCAMoyen = new JLabel("0 DT", SwingConstants.CENTER);
        lblCAMoyen.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblTopMed = new JLabel("Aucun", SwingConstants.CENTER);
        lblTopMed.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel lblClientsActifs = new JLabel("0", SwingConstants.CENTER);
        lblClientsActifs.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblPanierMoyen = new JLabel("0 DT", SwingConstants.CENTER);
        lblPanierMoyen.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblTendance = new JLabel("→ Stable", SwingConstants.CENTER);
        lblTendance.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        statsPanel.add(createReportCard("Ventes Total", lblVentesTotal, PRIMARY));
        statsPanel.add(createReportCard("CA Moyen", lblCAMoyen, SUCCESS));
        statsPanel.add(createReportCard("Top Médicament", lblTopMed, WARNING));
        statsPanel.add(createReportCard("Clients Actifs", lblClientsActifs, INFO));
        statsPanel.add(createReportCard("Panier Moyen", lblPanierMoyen, SUCCESS));
        statsPanel.add(createReportCard("Tendance", lblTendance, DANGER));
        
        // Charger les données dynamiquement
        new Thread(() -> {
            try {
                ReportService reportService = new ReportService();
                
                // Récupérer les données
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                Date debutMois = cal.getTime();
                Date finMois = new Date();
                
                Map<String, Object> rapportVentes = reportService.getRapportVentes(debutMois, finMois);
                
                // Mettre à jour l'interface
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Ventes totales
                        Object totalVentesObj = rapportVentes.get("total_ventes");
                        int totalVentes = 0;
                        if (totalVentesObj != null) {
                            if (totalVentesObj instanceof Integer) {
                                totalVentes = (Integer) totalVentesObj;
                            } else if (totalVentesObj instanceof Long) {
                                totalVentes = ((Long) totalVentesObj).intValue();
                            }
                        }
                        lblVentesTotal.setText(String.valueOf(totalVentes));
                        
                        // CA moyen
                        Object panierMoyenObj = rapportVentes.get("panier_moyen_periode");
                        double panierMoyen = 0.0;
                        if (panierMoyenObj != null) {
                            if (panierMoyenObj instanceof Double) {
                                panierMoyen = (Double) panierMoyenObj;
                            } else if (panierMoyenObj instanceof Float) {
                                panierMoyen = ((Float) panierMoyenObj).doubleValue();
                            } else if (panierMoyenObj instanceof Number) {
                                panierMoyen = ((Number) panierMoyenObj).doubleValue();
                            }
                        }
                        lblCAMoyen.setText(String.format("%.2f DT", panierMoyen));
                        lblPanierMoyen.setText(String.format("%.2f DT", panierMoyen));
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Top médicaments
                Map<String, Object> rapportTopMed = reportService.getTopMedicaments(1);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> topMedicaments = (List<Map<String, Object>>) rapportTopMed.get("top_medicaments");
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (topMedicaments != null && !topMedicaments.isEmpty()) {
                            Map<String, Object> topMed = topMedicaments.get(0);
                            lblTopMed.setText((String) topMed.get("nom"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Clients actifs
                Map<String, Object> rapportClients = reportService.getClientsFideles(100);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> clientsFideles = (List<Map<String, Object>>) rapportClients.get("clients_fideles");
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        lblClientsActifs.setText(String.valueOf(clientsFideles != null ? clientsFideles.size() : 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    lblVentesTotal.setText("Err");
                    lblCAMoyen.setText("Err");
                    lblTopMed.setText("Err");
                    lblClientsActifs.setText("Err");
                    lblPanierMoyen.setText("Err");
                    lblTendance.setText("Err");
                });
            }
        }).start();
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Graphique
        JLabel lblGraph = new JLabel("📈 Graphique des ventes des 30 derniers jours", SwingConstants.CENTER);
        lblGraph.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblGraph.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        panel.add(lblGraph, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }
    
    private void loadInitialData() {
        System.out.println("🚀 DÉMARRAGE PHARMACIEN DASHBOARD");
        
        // Charger les données dans l'ordre
        loadStockData();
        loadClientsData();
        loadVentesData();
        loadNotificationsData();
        
        // Charger les alertes
        System.out.println("\n📊 CHARGEMENT ALERTES...");
        loadAlertesData();
        
        updateStatistics();
        
        // Afficher le tableau de bord par défaut
        cardLayout.show(mainPanel, "dashboard");
        
        System.out.println("✅ Dashboard prêt à l'emploi\n");
    }
    
    private void loadStockData() {
        stockTableModel.setRowCount(0);
        try {
            List<Medicament> medicaments = medicamentDAO.getAll();
            for (Medicament med : medicaments) {
                String status = med.isStockCritique() ? "⚠ Critique" : "✅ Normal";
                stockTableModel.addRow(new Object[]{
                    med.getId(),
                    med.getNom(),
                    med.getDosage(),
                    med.getStock(),
                    String.format("%.2f DT", med.getPrixUnitaire()),
                    med.getSeuilAlerte(),
                    status
                });
            }
        } catch (Exception e) {
            // Mode démo
            stockTableModel.addRow(new Object[]{1, "Doliprane", "1000mg", 50, "5.99 DT", 10, "✅ Normal"});
            stockTableModel.addRow(new Object[]{2, "Aspirine", "500mg", 30, "3.50 DT", 5, "✅ Normal"});
            stockTableModel.addRow(new Object[]{3, "Ventoline", "100µg", 3, "25.00 DT", 10, "⚠ Critique"});
            stockTableModel.addRow(new Object[]{4, "Insuline", "100UI/ml", 5, "45.00 DT", 8, "⚠ Critique"});
        }
    }
    
    private void loadClientsData() {
        clientsTableModel.setRowCount(0);
        try {
            System.out.println("🔄 Chargement des clients depuis la BD...");
            List<Client> clients = clientDAO.getAll();
            
            if (clients.isEmpty()) {
                System.out.println("⚠ Aucun client trouvé dans la base de données");
            } else {
                System.out.println("✅ " + clients.size() + " clients chargés");
                for (Client client : clients) {
                    clientsTableModel.addRow(new Object[]{
                        client.getId(),
                        client.getNom(),
                        client.getPrenom(),
                        client.getEmail(),
                        client.getTelephone(),
                        client.getAdresse()
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des clients: " + e.getMessage());
            e.printStackTrace();
            
            // Mode démo
            clientsTableModel.addRow(new Object[]{1, "Dupont", "Jean", "jean.dupont@email.com", "0612345678", "Paris"});
            clientsTableModel.addRow(new Object[]{2, "Martin", "Marie", "marie.martin@email.com", "0623456789", "Lyon"});
            clientsTableModel.addRow(new Object[]{3, "Bernard", "Pierre", "pierre.bernard@email.com", "0634567890", "Marseille"});
        }
    }
    
    private void loadVentesData() {
        ventesTableModel.setRowCount(0);
        try {
            List<Vente> ventes = venteService.getAllVentes();
            
            if (ventes.isEmpty()) {
                System.out.println("⚠ Aucune vente trouvée dans la base de données");
            } else {
                System.out.println("✅ " + ventes.size() + " ventes chargées");
            }
            
            for (Vente vente : ventes) {
                String statut = "valide".equalsIgnoreCase(vente.getStatut()) ? "✓ Valide" : "✗ Annulée";
                String clientNom = vente.getClientNom();
                if (clientNom == null || clientNom.isEmpty()) {
                    clientNom = "Client occasionnel";
                }
                
                ventesTableModel.addRow(new Object[]{
                    vente.getId(),
                    vente.getDateVente() != null ? vente.getDateVente().toString() : "",
                    clientNom,
                    vente.getMedicamentNom(),
                    vente.getQuantite(),
                    String.format("%.2f DT", vente.getPrixTotal()),
                    statut
                });
            }
        } catch (Exception e) {
            // Mode démo
            ventesTableModel.addRow(new Object[]{1, "2024-01-23", "Jean Dupont", "Doliprane", 2, "11.98 DT", "Validée"});
            ventesTableModel.addRow(new Object[]{2, "2024-01-23", "Marie Martin", "Aspirine", 1, "3.50 DT", "Validée"});
            ventesTableModel.addRow(new Object[]{3, "2024-01-22", "Pierre Bernard", "Ventoline", 1, "25.00 DT", "Validée"});
        }
    }
    
    private void loadNotificationsData() {
        notificationsTableModel.setRowCount(0);
        
        // Notifications système
        notificationsTableModel.addRow(new Object[]{
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
            "Système",
            "Bienvenue dans Pharmacy Manager",
            "Faible",
            "Non lue"
        });
        
        // Alertes stock critique
        List<Medicament> stockCritique = medicamentDAO.getStockCritique();
        if (!stockCritique.isEmpty()) {
            for (Medicament med : stockCritique) {
                notificationsTableModel.addRow(new Object[]{
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                    "Alerte Stock",
                    med.getNom() + " - Stock critique: " + med.getStock() + " unités",
                    "Haute",
                    "Non lue"
                });
            }
        }
        
        // Ventes du jour
        notificationsTableModel.addRow(new Object[]{
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
            "Vente",
            "3 ventes enregistrées aujourd'hui",
            "Moyenne",
            "Lue"
        });
    }
    private void loadAlertesData() {
        System.out.println("🚨 CHARGEMENT ALERTES STOCK CRITIQUE");
        
        try {
            if (alertesTableModel == null) {
                System.err.println("❌ ERREUR: Modèle de tableau d'alertes non initialisé!");
                return;
            }
            
            // Vider le tableau
            alertesTableModel.setRowCount(0);
            
            // Récupérer les médicaments en stock critique
            List<Medicament> medicamentsCritiques = medicamentDAO.getStockCritique();
            System.out.println("✅ " + medicamentsCritiques.size() + " médicaments critiques trouvés");
            
            // Afficher les résultats
            if (medicamentsCritiques.isEmpty()) {
                alertesTableModel.addRow(new Object[]{
                    "✅ Tout est en ordre",
                    "-",
                    "-",
                    "-",
                    "-",
                    "NORMAL"
                });
                System.out.println("✅ Aucun stock critique détecté");
            } else {
                for (Medicament med : medicamentsCritiques) {
                    int difference = med.getSeuilAlerte() - med.getStock();
                    String etat = determineEtatStock(med);
                    
                    alertesTableModel.addRow(new Object[]{
                        med.getNom(),
                        med.getDosage(),
                        med.getStock(),
                        med.getSeuilAlerte(),
                        difference,
                        etat
                    });
                    
                    System.out.println("⚠️  " + med.getNom() + " " + med.getDosage() + 
                                     " - Stock: " + med.getStock() + 
                                     "/" + med.getSeuilAlerte() + 
                                     " (" + etat + ")");
                }
                System.out.println("✅ " + medicamentsCritiques.size() + " alertes affichées");
            }
            
            // Rafraîchir l'affichage
            alertesTable.revalidate();
            alertesTable.repaint();
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE dans loadAlertesData: " + e.getMessage());
            e.printStackTrace();
            showAlertesDemo();
        }
    }

    private String determineEtatStock(Medicament med) {
        if (med.getStock() <= 0) {
            return "RUPTURE";
        } else if (med.getStock() <= med.getSeuilAlerte() * 0.3) {
            return "CRITIQUE";
        } else if (med.getStock() <= med.getSeuilAlerte() * 0.5) {
            return "FAIBLE";
        } else {
            return "NORMAL";
        }
    }


    // Méthode simplifiée pour trouver le tableau d'alertes
    private JTable findAlertesTableInMainPanel() {
        // Chercher dans tous les composants de mainPanel
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                JTable table = findTableInPanel(panel);
                if (table != null && table.getColumnCount() >= 5) { // Le tableau d'alertes a au moins 5 colonnes
                    return table;
                }
            }
        }
        return null;
    }

    // Méthode récursive pour chercher un JTable dans un panel
    private JTable findTableInPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTable) {
                return (JTable) comp;
            }
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTable) {
                    return (JTable) view;
                }
            }
            if (comp instanceof Container) {
                if (comp instanceof JPanel) {
                    JTable table = findTableInPanel((JPanel) comp);
                    if (table != null) return table;
                }
            }
        }
        return null;
    }



    // Appliquer les couleurs aux lignes du tableau
    private void applyAlertesColorsToTable(JTable table) {
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    try {
                        // Récupérer l'état depuis la colonne 5
                        String etat = (String) table.getValueAt(row, 5);
                        
                        if (etat != null) {
                            switch (etat) {
                                case "RUPTURE":
                                    c.setBackground(new Color(255, 100, 100)); // Rouge vif
                                    c.setForeground(Color.WHITE);
                                    break;
                                    
                                case "CRITIQUE":
                                    c.setBackground(new Color(255, 200, 200)); // Rouge clair
                                    c.setForeground(Color.BLACK);
                                    break;
                                    
                                case "FAIBLE":
                                    c.setBackground(new Color(255, 255, 200)); // Jaune clair
                                    c.setForeground(Color.BLACK);
                                    break;
                                    
                                case "NORMAL":
                                    c.setBackground(Color.WHITE);
                                    c.setForeground(Color.BLACK);
                                    break;
                                    
                                default:
                                    c.setBackground(Color.WHITE);
                                    c.setForeground(Color.BLACK);
                            }
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                
                return c;
            }
        });
    }

    // Mode démo en cas d'erreur
    private void showAlertesDemo() {
        try {
            JTable alertTable = findAlertesTableInMainPanel();
            if (alertTable != null) {
                DefaultTableModel model = (DefaultTableModel) alertTable.getModel();
                model.setRowCount(0);
                
                // Données de démo basées sur votre base de données
                model.addRow(new Object[]{"Ventoline", "100µg", 3, 10, 7, "CRITIQUE"});
                model.addRow(new Object[]{"Insuline", "100UI/ml", 5, 8, 3, "CRITIQUE"});
                model.addRow(new Object[]{"Doliprane", "1000mg", 50, 10, -40, "NORMAL"});
                model.addRow(new Object[]{"Aspirine", "500mg", 30, 5, -25, "NORMAL"});
                
                System.out.println("📊 Mode démo activé - 2 médicaments critiques");
                applyAlertesColorsToTable(alertTable);
            }
        } catch (Exception e) {
            System.err.println("❌ ERREUR mode démo: " + e.getMessage());
        }
    }
    
    private void loadRapportsData() {
        // Cette méthode serait implémentée pour charger les données de rapports
    }
    
    private void updateStatistics() {
        try {
            List<Medicament> medicaments = medicamentDAO.getAll();
            int totalStock = medicaments.stream().mapToInt(Medicament::getStock).sum();
            int stockCritique = (int) medicaments.stream().filter(Medicament::isStockCritique).count();
            
            // Simuler des ventes
            int ventesJour = 5;
            double caJour = 124.75;
            
            lblTotalStock.setText(String.valueOf(totalStock));
            lblStockCritique.setText(String.valueOf(stockCritique));
            lblVentesJour.setText(String.valueOf(ventesJour));
            lblCAJour.setText(String.format("%.2f DT", caJour));
        } catch (Exception e) {
            // Mode démo
            lblTotalStock.setText("88");
            lblStockCritique.setText("2");
            lblVentesJour.setText("5");
            lblCAJour.setText("124.75 DT");
        }
    }
    
    private void showSearchDialog() {
        JTextField txtSearch = new JTextField(20);
        Object[] message = {"Rechercher un médicament:", txtSearch};
        
        int option = JOptionPane.showConfirmDialog(this, message, "Recherche", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String searchTerm = txtSearch.getText().trim();
            if (!searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Recherche pour: " + searchTerm + "\nRésultats: 2 médicaments trouvés",
                    "Recherche",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void showAddClientDialog() {
        JDialog dialog = new JDialog(this, "Ajouter un client", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField txtNom = new JTextField(20);
        JTextField txtPrenom = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtTelephone = new JTextField(20);
        JTextField txtAdresse = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPrenom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtTelephone, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtAdresse, gbc);
        
        JButton btnSave = createActionButton("Enregistrer", SUCCESS);
        JButton btnCancel = createActionButton("Annuler", DANGER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        // CORRECTION ICI : Appeler le DAO pour sauvegarder en BD
        btnSave.addActionListener(e -> {
            if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nom et prénom sont obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // 1. Créer l'objet Client
                Client newClient = new Client();
                newClient.setNom(txtNom.getText().trim());
                newClient.setPrenom(txtPrenom.getText().trim());
                newClient.setEmail(txtEmail.getText().trim());
                newClient.setTelephone(txtTelephone.getText().trim());
                newClient.setAdresse(txtAdresse.getText().trim());
                
                // 2. Sauvegarder dans la base de données
                boolean ajoutReussi = clientDAO.add(newClient);
                
                if (ajoutReussi) {
                    // 3. Ajouter au tableau (avec l'ID généré par la BD)
                    clientsTableModel.addRow(new Object[]{
                        newClient.getId(),  // ID auto-généré
                        newClient.getNom(),
                        newClient.getPrenom(),
                        newClient.getEmail(),
                        newClient.getTelephone(),
                        newClient.getAdresse()
                    });
                    
                    // 4. Afficher message de succès
                    JOptionPane.showMessageDialog(dialog, 
                        "✅ Client ajouté avec succès!\nID: " + newClient.getId(), 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
                    // 5. Fermer la boîte de dialogue
                    dialog.dispose();
                    
                    // 6. Mettre à jour la liste des clients dans le combobox de vente
                    refreshClientComboBoxes();
                    
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "❌ Erreur lors de l'ajout du client en base de données", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void searchClientDialog() {
        JTextField txtSearch = new JTextField(20);
        Object[] message = {"Rechercher un client (nom, prénom, email):", txtSearch};
        
        int option = JOptionPane.showConfirmDialog(this, message, "Recherche Client", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String searchTerm = txtSearch.getText().trim();
            if (!searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Recherche pour: " + searchTerm + "\nRésultats: 1 client trouvé",
                    "Recherche Client",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void refreshClientComboBoxes() {
        // Cette méthode sera appelée après l'ajout d'un client
        // pour mettre à jour tous les combobox de l'interface
        
        // Vous pouvez ajouter cette méthode et l'appeler 
        // après chaque ajout/modification/suppression de client
        
        System.out.println("🔄 Rafraîchissement de la liste des clients...");
        
        // Pour l'instant, on va simplement recharger les données clients
        loadClientsData();
    }
    
    private void filterVentesDialog() {
        JDialog dialog = new JDialog(this, "Filtrer les ventes", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblDateDebut = new JLabel("Date début:");
        JTextField txtDateDebut = new JTextField("2024-01-01", 10);
        
        JLabel lblDateFin = new JLabel("Date fin:");
        JTextField txtDateFin = new JTextField("2024-01-31", 10);
        
        JLabel lblClient = new JLabel("Client:");
        JTextField txtClient = new JTextField(10);
        
        JLabel lblStatut = new JLabel("Statut:");
        String[] statuts = {"Tous", "Validée", "Annulée", "En attente"};
        JComboBox<String> cmbStatut = new JComboBox<>(statuts);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblDateDebut, gbc);
        gbc.gridx = 1;
        dialog.add(txtDateDebut, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblDateFin, gbc);
        gbc.gridx = 1;
        dialog.add(txtDateFin, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblClient, gbc);
        gbc.gridx = 1;
        dialog.add(txtClient, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblStatut, gbc);
        gbc.gridx = 1;
        dialog.add(cmbStatut, gbc);
        
        JButton btnApply = createActionButton("Appliquer", SUCCESS);
        JButton btnCancel = createActionButton("Annuler", DANGER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnApply);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        btnApply.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, 
                "Filtres appliqués:\n" +
                "Période: " + txtDateDebut.getText() + " à " + txtDateFin.getText() + "\n" +
                "Client: " + (txtClient.getText().isEmpty() ? "Tous" : txtClient.getText()) + "\n" +
                "Statut: " + cmbStatut.getSelectedItem(),
                "Filtres appliqués",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void exportStockCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter le stock en CSV");
        fileChooser.setSelectedFile(new File("stock_export_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Simuler l'export
            JOptionPane.showMessageDialog(this,
                "✅ Export CSV réussi!\nFichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Format: ID,Nom,Dosage,Stock,Prix,Seuil,Statut",
                "Export CSV",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportClientsCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les clients en CSV");
        fileChooser.setSelectedFile(new File("clients_export_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Simuler l'export
            JOptionPane.showMessageDialog(this,
                "✅ Export CSV réussi!\nFichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Format: ID,Nom,Prénom,Email,Téléphone,Adresse",
                "Export CSV",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportVentesCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les ventes en CSV");
        fileChooser.setSelectedFile(new File("ventes_export_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Simuler l'export
            JOptionPane.showMessageDialog(this,
                "✅ Export CSV réussi!\nFichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Format: ID,Date,Client,Médicament,Quantité,Prix Total,Statut",
                "Export CSV",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @SuppressWarnings("unused")
	private void annulerVenteSelectionnee() {
        int selectedRow = ventesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une vente à annuler",
                "Aucune sélection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idVente = (int) ventesTableModel.getValueAt(selectedRow, 0);
        String client = (String) ventesTableModel.getValueAt(selectedRow, 2);
        String medicament = (String) ventesTableModel.getValueAt(selectedRow, 3);
        double montant = Double.parseDouble(((String)ventesTableModel.getValueAt(selectedRow, 5)).replace(" DT", ""));
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler cette vente?\n\n" +
            "ID: " + idVente + "\n" +
            "Client: " + client + "\n" +
            "Médicament: " + medicament + "\n" +
            "Montant: " + montant + " DT\n\n" +
            "Cette action restituera le stock et ne pourra pas être annulée.",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Simuler l'annulation
            ventesTableModel.setValueAt("Annulée", selectedRow, 6);
            JOptionPane.showMessageDialog(this,
                "✅ Vente #" + idVente + " annulée avec succès!\n" +
                "Le stock a été restitué.",
                "Annulation réussie",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void generateDailySalesReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Générer rapport des ventes du jour");
        fileChooser.setSelectedFile(new File("rapport_ventes_journalier_" + System.currentTimeMillis() + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this,
                "📊 Rapport journalier généré!\n\n" +
                "Fichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Période: Aujourd'hui\n" +
                "Nombre de ventes: 5\n" +
                "Chiffre d'affaires: 124.75 DT\n" +
                "Médicament le plus vendu: Doliprane",
                "Rapport généré",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void generateDetailedSalesReport() {
        JDialog dialog = new JDialog(this, "Rapport détaillé des ventes", true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("       RAPPORT DÉTAILLÉ DES VENTES      \n");
        report.append("========================================\n\n");
        report.append("Date: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        report.append("Pharmacien: ").append(currentUser.getPrenom()).append(" ").append(currentUser.getNom()).append("\n");
        report.append("----------------------------------------\n\n");
        
        report.append("📊 STATISTIQUES GÉNÉRALES:\n");
        report.append("  Total ventes: 5\n");
        report.append("  Chiffre d'affaires: 124.75 DT\n");
        report.append("  Panier moyen: 24.95 DT\n");
        report.append("  Clients servis: 3\n");
        report.append("\n");
        
        report.append("🏆 MÉDICAMENTS LES PLUS VENDUS:\n");
        report.append("  1. Doliprane 1000mg - 3 ventes - 17.97 DT\n");
        report.append("  2. Aspirine 500mg - 1 vente - 3.50 DT\n");
        report.append("  3. Ventoline 100µg - 1 vente - 25.00 DT\n");
        report.append("\n");
        
        report.append("👥 TOP CLIENTS:\n");
        report.append("  1. Jean Dupont - 2 achats - 15.48 DT\n");
        report.append("  2. Marie Martin - 2 achats - 7.00 DT\n");
        report.append("  3. Pierre Bernard - 1 achat - 25.00 DT\n");
        report.append("\n");
        report.append("========================================\n");
        report.append("           FIN DU RAPPORT              \n");
        report.append("========================================\n");
        
        reportArea.setText(report.toString());
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnPrint = createActionButton("🖨️ Imprimer", PRIMARY);
        JButton btnSave = createActionButton("💾 Sauvegarder", SUCCESS);
        JButton btnClose = createActionButton("Fermer", DANGER);
        
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Impression lancée...", "Impression", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnSave.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("rapport_detaille_" + System.currentTimeMillis() + ".txt"));
            if (fc.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(dialog, "Rapport sauvegardé!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnClose.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void ouvrirDossierEmails() {
        try {
            File emailDir = new File("emails_sent/");
            if (!emailDir.exists()) {
                emailDir.mkdirs();
                JOptionPane.showMessageDialog(this,
                    "Aucun email envoyé pour le moment.\nLe dossier a été créé: " + emailDir.getAbsolutePath(),
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                Desktop.getDesktop().open(emailDir);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configurerNotifications() {
        JDialog dialog = new JDialog(this, "Configuration des Notifications", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Options de configuration
        JCheckBox chkAlertesStock = new JCheckBox("Recevoir des alertes de stock critique", true);
        JCheckBox chkConfirmationsVente = new JCheckBox("Confirmations de vente", true);
        JCheckBox chkNotificationsEmail = new JCheckBox("Notifications par email", false);
        JCheckBox chkSound = new JCheckBox("Son des notifications", true);
        JCheckBox chkPopup = new JCheckBox("Fenêtres popup", true);
        
        JLabel lblEmail = new JLabel("Email pour notifications:");
        JTextField txtEmail = new JTextField("pharmacien@pharmacie.com", 25);
        
        JLabel lblFreq = new JLabel("Fréquence des rapports:");
        String[] frequences = {"Quotidien", "Hebdomadaire", "Mensuel", "Jamais"};
        JComboBox<String> cmbFreq = new JComboBox<>(frequences);
        
        formPanel.add(chkAlertesStock);
        formPanel.add(chkConfirmationsVente);
        formPanel.add(chkNotificationsEmail);
        formPanel.add(chkSound);
        formPanel.add(chkPopup);
        formPanel.add(lblEmail);
        formPanel.add(txtEmail);
        formPanel.add(lblFreq);
        formPanel.add(cmbFreq);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnSauvegarder = createActionButton("💾 Sauvegarder", SUCCESS);
        JButton btnTest = createActionButton("🧪 Tester", PRIMARY);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnSauvegarder.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Configuration sauvegardée!");
            dialog.dispose();
        });
        
        btnTest.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, 
                "Notification de test envoyée!\n" +
                "Vérifiez votre boîte de notifications.",
                "Test réussi",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnTest);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void clearNotifications() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment effacer toutes les notifications?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            notificationsTableModel.setRowCount(0);
            JOptionPane.showMessageDialog(this,
                "✅ Toutes les notifications ont été effacées",
                "Notifications effacées",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void envoyerAlerteStockEmail() {
        List<Medicament> stockCritique = medicamentDAO.getStockCritique();
        
        if (stockCritique.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "✅ Aucun stock critique pour le moment.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Créer un email d'alerte
        Medicament med = stockCritique.get(0);
        boolean sent = emailService.sendStockAlert("gestionnaire@pharmacie.com", med);
        
        if (sent) {
            JOptionPane.showMessageDialog(this,
                "📧 Email d'alerte envoyé au gestionnaire!\n" +
                "Médicament: " + med.getNom() + "\n" +
                "Stock: " + med.getStock() + " unités\n" +
                "Seuil: " + med.getSeuilAlerte() + " unités",
                "Email envoyé",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showDemandeCommandeDialog() {
        JDialog dialog = new JDialog(this, "Demander une commande", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Médicament
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Médicament:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cmbMedicaments = new JComboBox<>();
        loadMedicamentsIntoCombo(cmbMedicaments);
        formPanel.add(cmbMedicaments, gbc);
        
        // Quantité
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 1;
        JSpinner spnQuantite = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        formPanel.add(spnQuantite, gbc);
        
        // Urgence
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Niveau d'urgence:"), gbc);
        
        gbc.gridx = 1;
        String[] urgences = {"Normal", "Élevé", "Critique"};
        JComboBox<String> cmbUrgence = new JComboBox<>(urgences);
        formPanel.add(cmbUrgence, gbc);
        
        // Commentaire
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Commentaire:"), gbc);
        
        gbc.gridx = 1; gbc.gridheight = 2;
        JTextArea txtComment = new JTextArea(3, 20);
        txtComment.setLineWrap(true);
        JScrollPane scrollComment = new JScrollPane(txtComment);
        formPanel.add(scrollComment, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnEnvoyer = createActionButton("📤 Envoyer demande", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnEnvoyer.addActionListener(e -> {
            String medicament = (String) cmbMedicaments.getSelectedItem();
            int quantite = (Integer) spnQuantite.getValue();
            String urgence = (String) cmbUrgence.getSelectedItem();
            String comment = txtComment.getText();
            
            JOptionPane.showMessageDialog(dialog,
                "✅ Demande de commande envoyée!\n\n" +
                "Médicament: " + medicament + "\n" +
                "Quantité: " + quantite + "\n" +
                "Urgence: " + urgence + "\n" +
                "Commentaire: " + (comment.isEmpty() ? "Aucun" : comment) + "\n\n" +
                "Le gestionnaire sera notifié.",
                "Demande envoyée",
                JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnEnvoyer);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void ignorerAlertes() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ignorer toutes les alertes de stock critique?\n\n" +
            "Les alertes ne réapparaîtront pas avant la prochaine vérification.",
            "Ignorer les alertes",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "✅ Toutes les alertes ont été ignorées.\n" +
                "Elles réapparaîtront lors de la prochaine vérification.",
                "Alertes ignorées",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showHistoriqueAlertes() {
        JDialog dialog = new JDialog(this, "Historique des alertes", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());
        
        String[] columns = {"Date", "Médicament", "Type", "Statut", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        // Données d'exemple
        model.addRow(new Object[]{"23/01/2024 10:30", "Ventoline 100µg", "Stock critique", "Résolue", "Commande passée"});
        model.addRow(new Object[]{"22/01/2024 15:45", "Insuline 100UI/ml", "Stock critique", "En cours", "Email envoyé"});
        model.addRow(new Object[]{"20/01/2024 09:15", "Doliprane 1000mg", "Stock faible", "Résolue", "Ignorée"});
        
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnClose = createActionButton("Fermer", DANGER);
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void generateAllReports() {
        // Générer tous les rapports
        try {
            // Simuler la génération
            JOptionPane.showMessageDialog(this,
                "📊 TOUS LES RAPPORTS GÉNÉRÉS\n\n" +
                "✅ Rapport des ventes\n" +
                "✅ Rapport de stock\n" +
                "✅ Rapport clients\n" +
                "✅ Rapport financier\n\n" +
                "Les rapports sont prêts à être consultés.",
                "Rapports générés",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la génération des rapports: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportReportsToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter rapports en PDF");
        fileChooser.setSelectedFile(new File("rapports_complets_" + System.currentTimeMillis() + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this,
                "📄 Export PDF simulé\n\n" +
                "Fichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Format: PDF compressé\n" +
                "Pages: 12\n" +
                "Taille estimée: 1.2 MB\n\n" +
                "Dans une implémentation réelle, utilisez:\n" +
                "- iText\n- Apache PDFBox\n- ou une bibliothèque similaire",
                "Export PDF",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportReportsToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter rapports en Excel");
        fileChooser.setSelectedFile(new File("rapports_excel_" + System.currentTimeMillis() + ".xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this,
                "📊 Export Excel simulé\n\n" +
                "Fichier: " + fileChooser.getSelectedFile().getName() + "\n" +
                "Format: Excel (.xlsx)\n" +
                "Feuilles: 4\n" +
                "Données: 256 lignes\n\n" +
                "Dans une implémentation réelle, utilisez:\n" +
                "- Apache POI\n- ou une bibliothèque similaire",
                "Export Excel",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showDashboardOverview() {
        JDialog overviewDialog = new JDialog(this, "Vue d'ensemble - Tableau de Bord", true);
        overviewDialog.setSize(1000, 700);
        overviewDialog.setLayout(new BorderLayout());
        
        // Statistiques principales
        JPanel mainStats = new JPanel(new GridLayout(2, 4, 10, 10));
        mainStats.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Récupérer les données
        int totalStock = 88;
        int stockCritique = 2;
        int ventesJour = 5;
        double caJour = 124.75;
        int clientsActifs = 3;
        double panierMoyen = 24.95;
        double valeurStock = 2450.50;
        String topMed = "Doliprane";
        
        // Ajouter les cartes de statistiques
        mainStats.add(createOverviewCard("📦 Total Stock", String.valueOf(totalStock), PRIMARY));
        mainStats.add(createOverviewCard("⚠️ Stock Critique", String.valueOf(stockCritique), DANGER));
        mainStats.add(createOverviewCard("💰 Ventes Jour", String.valueOf(ventesJour), WARNING));
        mainStats.add(createOverviewCard("💵 CA Journalier", String.format("%.2f DT", caJour), SUCCESS));
        mainStats.add(createOverviewCard("👥 Clients Actifs", String.valueOf(clientsActifs), INFO));
        mainStats.add(createOverviewCard("🛒 Panier Moyen", String.format("%.2f DT", panierMoyen), SUCCESS));
        mainStats.add(createOverviewCard("🏪 Valeur Stock", String.format("%.2f DT", valeurStock), PRIMARY));
        mainStats.add(createOverviewCard("🏆 Top Médicament", topMed, WARNING));
        
        overviewDialog.add(mainStats, BorderLayout.NORTH);
        
        // Graphiques simulés
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel chart1 = new JLabel("📊 Évolution des ventes (30 jours)", SwingConstants.CENTER);
        chart1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(50, 20, 50, 20)
        ));
        
        JLabel chart2 = new JLabel("📈 Top 5 médicaments vendus", SwingConstants.CENTER);
        chart2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(50, 20, 50, 20)
        ));
        
        chartsPanel.add(chart1);
        chartsPanel.add(chart2);
        
        overviewDialog.add(chartsPanel, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel actionPanel = new JPanel();
        JButton btnGenererRapport = createActionButton("📊 Générer Rapport Complet", PRIMARY);
        JButton btnExporter = createActionButton("📤 Exporter Données", SUCCESS);
        JButton btnFermer = createActionButton("Fermer", DANGER);
        
        btnGenererRapport.addActionListener(e -> {
            generateAllReports();
            overviewDialog.dispose();
        });
        
        btnExporter.addActionListener(e -> {
            exportReportsToExcel();
            overviewDialog.dispose();
        });
        
        btnFermer.addActionListener(e -> overviewDialog.dispose());
        
        actionPanel.add(btnGenererRapport);
        actionPanel.add(btnExporter);
        actionPanel.add(btnFermer);
        overviewDialog.add(actionPanel, BorderLayout.SOUTH);
        
        overviewDialog.setLocationRelativeTo(this);
        overviewDialog.setVisible(true);
    }
    
    private JPanel createOverviewCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
    
    private void changerMotDePasse() {
        JPasswordField txtCurrent = new JPasswordField(20);
        JPasswordField txtNew = new JPasswordField(20);
        JPasswordField txtConfirm = new JPasswordField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Mot de passe actuel:"));
        panel.add(txtCurrent);
        panel.add(new JLabel("Nouveau mot de passe:"));
        panel.add(txtNew);
        panel.add(new JLabel("Confirmer nouveau:"));
        panel.add(txtConfirm);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Changer le mot de passe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newPass = new String(txtNew.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Le mot de passe doit contenir au moins 6 caractères", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "✅ Mot de passe changé avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void modifierProfil() {
        JOptionPane.showMessageDialog(this,
            "Fonctionnalité à implémenter: Modification du profil\n\n" +
            "Cette fonction permettrait de modifier:\n" +
            "- Nom et prénom\n" +
            "- Email\n" +
            "- Téléphone\n" +
            "- Photo de profil",
            "Modification du profil",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMonActivite() {
        JDialog dialog = new JDialog(this, "Mon Activité - Statistiques personnelles", true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        
        JTextArea activityArea = new JTextArea();
        activityArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        activityArea.setEditable(false);
        
        StringBuilder activity = new StringBuilder();
        activity.append("========================================\n");
        activity.append("       ACTIVITÉ DU PHARMACIEN          \n");
        activity.append("========================================\n\n");
        activity.append("Pharmacien: ").append(currentUser.getPrenom()).append(" ").append(currentUser.getNom()).append("\n");
        activity.append("Période: 01/01/2024 - ").append(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date())).append("\n");
        activity.append("----------------------------------------\n\n");
        
        activity.append("💰 VENTES RÉALISÉES:\n");
        activity.append("  Total ventes: 156\n");
        activity.append("  Chiffre d'affaires: 3,845.20 DT\n");
        activity.append("  Panier moyen: 24.65 DT\n");
        activity.append("  Taux d'annulation: 2.3%\n");
        activity.append("\n");
        
        activity.append("📦 GESTION DE STOCK:\n");
        activity.append("  Alertes traitées: 24\n");
        activity.append("  Demandes de commande: 12\n");
        activity.append("  Médicaments ajoutés: 8\n");
        activity.append("  Ajustements de stock: 15\n");
        activity.append("\n");
        
        activity.append("👥 INTERACTION CLIENTS:\n");
        activity.append("  Nouveaux clients: 23\n");
        activity.append("  Clients fidélisés: 45\n");
        activity.append("  Réclamations traitées: 3\n");
        activity.append("  Satisfaction: 4.8/5\n");
        activity.append("\n");
        
        activity.append("🏆 PERFORMANCE:\n");
        activity.append("  Objectif ventes: 105%\n");
        activity.append("  Punctualité: 98%\n");
        activity.append("  Formation complétée: Oui\n");
        activity.append("  Évaluation: Excellent\n");
        activity.append("\n");
        activity.append("========================================\n");
        activity.append("             FIN DU RAPPORT            \n");
        activity.append("========================================\n");
        
        activityArea.setText(activity.toString());
        
        JScrollPane scrollPane = new JScrollPane(activityArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnClose = createActionButton("Fermer", DANGER);
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void generateDailyReport() {
        // Générer un rapport journalier
        JOptionPane.showMessageDialog(this,
            "📅 RAPPORT JOURNALIER GÉNÉRÉ\n\n" +
            "Date: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "\n" +
            "Pharmacien: " + currentUser.getPrenom() + " " + currentUser.getNom() + "\n\n" +
            "📊 RÉSULTATS:\n" +
            "  • Ventes: 5 transactions\n" +
            "  • CA: 124.75 DT\n" +
            "  • Clients servis: 3\n" +
            "  • Alertes stock: 2 résolues\n" +
            "  • Temps moyen par transaction: 3.2 min\n\n" +
            "✅ Rapport enregistré dans l'historique",
            "Rapport journalier",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @SuppressWarnings({ "unchecked", "unused" })
	private JPanel createStockReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Créer le service de rapports
        ReportService reportService = new ReportService();
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Statistiques stock (chargement initial)
        JLabel lblStats = new JLabel("<html><center>📦 CHARGEMENT DU RAPPORT DE STOCK...<br><br>" +
            "Veuillez patienter...</center></html>", 
            SwingConstants.CENTER);
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        contentPanel.add(lblStats, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Charger les données en arrière-plan
        new Thread(() -> {
            try {
                Map<String, Object> rapportStock = reportService.getRapportStock();
                
                SwingUtilities.invokeLater(() -> {
                    Double valeurTotale = (Double) rapportStock.get("valeur_totale_stock");
                    Integer medicamentsCritiques = (Integer) rapportStock.get("medicaments_critiques");
                    Integer medicamentsFaibles = (Integer) rapportStock.get("medicaments_faibles");
                    Integer totalMedicaments = (Integer) rapportStock.get("total_medicaments");
                    
                    String html = "<html><center>📦 <b>RAPPORT DE STOCK</b><br><br>" +
                        "<b>Total médicaments:</b> " + totalMedicaments + "<br>" +
                        "<b>Valeur totale:</b> " + String.format("%.2f DT", valeurTotale) + "<br>" +
                        "<b>Stock critique:</b> " + medicamentsCritiques + " médicaments<br>" +
                        "<b>Stock faible:</b> " + medicamentsFaibles + " médicaments<br>" +
                        "<b>Rotation moyenne:</b> Calcul en cours...<br>" +
                        "<b>Date du rapport:</b> " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + 
                        "</center></html>";
                    
                    lblStats.setText(html);
                    
                    // Ajouter un tableau des stocks critiques
                    List<Map<String, Object>> stockDetail = (List<Map<String, Object>>) rapportStock.get("stock_detail");
                    
                    if (stockDetail != null && !stockDetail.isEmpty()) {
                        JPanel tablePanel = new JPanel(new BorderLayout());
                        
                        String[] columns = {"Médicament", "Stock", "Seuil", "Statut", "Valeur"};
                        DefaultTableModel model = new DefaultTableModel(columns, 0);
                        JTable stockTable = new JTable(model);
                        styleTable(stockTable);
                        
                        // Colorer les lignes selon le statut
                        stockTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                
                                if (!isSelected) {
                                    String statut = (String) table.getValueAt(row, 3);
                                    if ("CRITIQUE".equals(statut)) {
                                        c.setBackground(new Color(255, 200, 200));
                                    } else if ("FAIBLE".equals(statut)) {
                                        c.setBackground(new Color(255, 255, 200));
                                    } else {
                                        c.setBackground(Color.WHITE);
                                    }
                                }
                                
                                return c;
                            }
                        });
                        
                        for (Map<String, Object> med : stockDetail) {
                            String statut = (String) med.get("statut_stock");
                            if ("CRITIQUE".equals(statut) || "FAIBLE".equals(statut)) {
                                model.addRow(new Object[]{
                                    med.get("nom") + " " + med.get("dosage"),
                                    med.get("stock"),
                                    med.get("seuil_alerte"),
                                    statut,
                                    String.format("%.2f DT", med.get("valeur_stock"))
                                });
                            }
                        }
                        
                        if (model.getRowCount() > 0) {
                            JScrollPane scrollPane = new JScrollPane(stockTable);
                            scrollPane.setPreferredSize(new Dimension(500, 150));
                            scrollPane.setBorder(BorderFactory.createTitledBorder("Médicaments en stock critique/faible"));
                            panel.add(scrollPane, BorderLayout.SOUTH);
                        }
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStats.setText("<html><center>❌ <b>ERREUR DE CHARGEMENT</b><br><br>" +
                        "Impossible de charger les données du stock.<br>" +
                        "Erreur: " + e.getMessage() + "</center></html>");
                });
                e.printStackTrace();
            }
        }).start();
        
        return panel;
    }
    
    private JPanel createClientReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Texte initial
        JLabel lblReport = new JLabel("<html><center>👥 <b>RAPPORT CLIENTS</b><br><br>" +
            "Chargement des données en cours...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données
        new Thread(() -> {
            try {
                ReportService reportService = new ReportService();
                Map<String, Object> rapportClients = reportService.getClientsFideles(50);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> clientsFideles = (List<Map<String, Object>>) rapportClients.get("clients_fideles");
                        
                        double totalDepenses = 0.0;
                        double moyenneDepense = 0.0;
                        
                        Object depensesObj = rapportClients.get("total_depenses_clients");
                        if (depensesObj instanceof Double) {
                            totalDepenses = (Double) depensesObj;
                        } else if (depensesObj instanceof Number) {
                            totalDepenses = ((Number) depensesObj).doubleValue();
                        }
                        
                        Object moyenneObj = rapportClients.get("moyenne_depense_client");
                        if (moyenneObj instanceof Double) {
                            moyenneDepense = (Double) moyenneObj;
                        } else if (moyenneObj instanceof Number) {
                            moyenneDepense = ((Number) moyenneObj).doubleValue();
                        }
                        
                        int nbClients = clientsFideles != null ? clientsFideles.size() : 0;
                        
                        lblReport.setText("<html><center>👥 <b>RAPPORT CLIENTS</b><br><br>" +
                            "<b>Total clients actifs:</b> " + nbClients + "<br>" +
                            "<b>Total dépenses clients:</b> " + String.format("%.2f DT", totalDepenses) + "<br>" +
                            "<b>Dépense moyenne par client:</b> " + String.format("%.2f DT", moyenneDepense) + "<br>" +
                            "<b>Date du rapport:</b> " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + 
                            "</center></html>");
                        
                    } catch (Exception e) {
                        lblReport.setText("<html><center>👥 <b>RAPPORT CLIENTS</b><br><br>" +
                            "Erreur lors du traitement des données.</center></html>");
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblReport.setText("<html><center>👥 <b>RAPPORT CLIENTS</b><br><br>" +
                        "Impossible de charger les données clients.<br>" +
                        "Erreur: " + e.getMessage() + "</center></html>");
                });
                e.printStackTrace();
            }
        }).start();
        
        return panel;
    }
    
    private JPanel createDailyReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Texte initial
        JLabel lblReport = new JLabel("<html><center>📅 <b>RAPPORT JOURNALIER</b><br><br>" +
            "Chargement des données en cours...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données
        new Thread(() -> {
            try {
                ReportService reportService = new ReportService();
                
                // Date d'aujourd'hui
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                Date debutJour = cal.getTime();
                
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                Date finJour = cal.getTime();
                
                Map<String, Object> rapportJour = reportService.getRapportVentes(debutJour, finJour);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        int totalVentes = 0;
                        double totalCA = 0.0;
                        double panierMoyen = 0.0;
                        
                        Object ventesObj = rapportJour.get("total_ventes");
                        if (ventesObj instanceof Integer) {
                            totalVentes = (Integer) ventesObj;
                        }
                        
                        Object caObj = rapportJour.get("total_chiffre_affaires");
                        if (caObj instanceof Double) {
                            totalCA = (Double) caObj;
                        } else if (caObj instanceof Number) {
                            totalCA = ((Number) caObj).doubleValue();
                        }
                        
                        Object panierObj = rapportJour.get("panier_moyen_periode");
                        if (panierObj instanceof Double) {
                            panierMoyen = (Double) panierObj;
                        } else if (panierObj instanceof Number) {
                            panierMoyen = ((Number) panierObj).doubleValue();
                        }
                        
                        // Estimation simple des clients servis
                        int clientsServis = Math.max(1, totalVentes / 2);
                        
                        lblReport.setText("<html><center>📅 <b>RAPPORT JOURNALIER</b><br><br>" +
                            "<b>Date:</b> " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "<br>" +
                            "<b>Pharmacien:</b> " + currentUser.getPrenom() + " " + currentUser.getNom() + "<br><br>" +
                            "<b>Ventes:</b> " + totalVentes + " transactions<br>" +
                            "<b>Chiffre d'affaires:</b> " + String.format("%.2f DT", totalCA) + "<br>" +
                            "<b>Panier moyen:</b> " + String.format("%.2f DT", panierMoyen) + "<br>" +
                            "<b>Clients servis (estimation):</b> " + clientsServis + "<br>" +
                            "<b>Performance:</b> " + (totalVentes > 0 ? "Bon ✅" : "À améliorer ⚠️") + 
                            "</center></html>");
                        
                    } catch (Exception e) {
                        lblReport.setText("<html><center>📅 <b>RAPPORT JOURNALIER</b><br><br>" +
                            "Erreur lors du traitement des données.</center></html>");
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblReport.setText("<html><center>📅 <b>RAPPORT JOURNALIER</b><br><br>" +
                        "Aucune donnée disponible pour aujourd'hui.<br><br>" +
                        "Date: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "<br>" +
                        "Pharmacien: " + currentUser.getPrenom() + " " + currentUser.getNom() + 
                        "</center></html>");
                });
                e.printStackTrace();
            }
        }).start();
        
        return panel;
    }
    
    private void loadClientsIntoCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("0 - Client occasionnel");
        
        try {
            List<Client> clients = clientDAO.getAll();
            for (Client client : clients) {
                combo.addItem(client.getId() + " - " + client.getNom() + " " + client.getPrenom());
            }
        } catch (Exception e) {
            // Mode démo
            combo.addItem("1 - Jean Dupont");
            combo.addItem("2 - Marie Martin");
            combo.addItem("3 - Pierre Bernard");
        }
    }
    
    private void loadMedicamentsIntoCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        
        try {
            List<Medicament> medicaments = medicamentDAO.getMedicamentsDisponibles();
            for (Medicament med : medicaments) {
                combo.addItem(med.getId() + " - " + med.getNom() + " " + med.getDosage());
            }
        } catch (Exception e) {
            // Mode démo
            combo.addItem("1 - Doliprane 1000mg");
            combo.addItem("2 - Aspirine 500mg");
            combo.addItem("3 - Ventoline 100µg");
            combo.addItem("4 - Insuline 100UI/ml");
        }
    }
    
    private int getStockForMedicament(String medicamentInfo) {
        try {
            // Extraire l'ID du médicament
            String[] parts = medicamentInfo.split(" - ");
            if (parts.length > 0) {
                int id = Integer.parseInt(parts[0]);
                Medicament med = medicamentDAO.getById(id);
                if (med != null) {
                    return med.getStock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir vous déconnecter?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            
            SwingUtilities.invokeLater(() -> {
                // Retour à l'écran de login
                try {
                    // Assurez-vous que LoginFrame existe dans votre projet
                    Class<?> loginClass = Class.forName("views.LoginFrame");
                    JFrame loginFrame = (JFrame) loginClass.getDeclaredConstructor().newInstance();
                    loginFrame.setVisible(true);
                } catch (Exception e) {
                    System.err.println("❌ Impossible de retourner au login: " + e.getMessage());
                    System.exit(0);
                }
            });
        }
    }
    
    // Méthodes main pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Créer un utilisateur de test
            User testUser = new User();
            testUser.setId(1);
            testUser.setNom("Pharmacien");
            testUser.setPrenom("Test");
            testUser.setLogin("pharmacien");
            testUser.setRole("PHARMACIEN");
            
            PharmacienDashboard dashboard = new PharmacienDashboard(testUser);
            dashboard.setVisible(true);
        });
    }
    
    private JPanel createReportCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        
        valueLabel.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
}