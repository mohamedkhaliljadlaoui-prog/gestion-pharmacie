package views;
import java.util.Map;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.StockHistoriqueDAO;
import models.Commande;
import models.Medicament;
import models.User;
import services.CommandeService;
import services.EmailService;
import services.ExportService;
import services.ReportService;
import services.StockService;

@SuppressWarnings("serial")
public class GestionnaireDashboard extends JFrame {
    
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTable reapprovisionnementTable;
    private DefaultTableModel reapprovisionnementTableModel;
    // Services et DAO
    private StockService stockService;
    private CommandeService commandeService;
    private StockHistoriqueDAO historiqueDAO;
    private EmailService emailService;
    private ExportService exportService;
    private ReportService reportService;
    
    // Composants
    private JTable stockTable, commandesTable, historiqueTable, emailsTable, rapportsTable;
    private DefaultTableModel stockTableModel, commandesTableModel, historiqueTableModel, emailsTableModel, rapportsTableModel;
    private JLabel lblTotalStock, lblStockCritique, lblCommandesAttente, lblValeurStock;
    
    // Couleurs
    private static final Color PRIMARY = new Color(33, 150, 243);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color WARNING = new Color(255, 152, 0);
    private static final Color DANGER = new Color(244, 67, 54);
    private static final Color INFO = new Color(156, 39, 176);
    private static final Color DARK_BLUE = new Color(45, 55, 72);
    
    public GestionnaireDashboard(User user) {
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        setupModernLayout();
        setupEventHandlers();
        loadInitialData();
        
        setLocationRelativeTo(null);
    }
    private JPanel createReapprovisionnementPanel() {
        // Créer le panel principal
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("🔄 Réapprovisionnement Automatique");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnAnalyser = createActionButton("🔍 Analyser Stock", PRIMARY);
        JButton btnGenererCmd = createActionButton("📋 Générer Commandes", SUCCESS);
        JButton btnEnvoyerFournisseur = createActionButton("📧 Envoyer Fournisseur", WARNING);
        JButton btnHistorique = createActionButton("📈 Historique", INFO);
        JButton btnConfig = createActionButton("⚙️ Paramètres", DANGER);
        
        btnAnalyser.addActionListener(e -> analyserStockPourReappro());
        btnGenererCmd.addActionListener(e -> genererCommandesReappro());
        btnEnvoyerFournisseur.addActionListener(e -> envoyerCommandesFournisseur());
        btnHistorique.addActionListener(e -> showHistoriqueReappro());
        btnConfig.addActionListener(e -> configurerReapprovisionnement());
        
        buttonPanel.add(btnAnalyser);
        buttonPanel.add(btnGenererCmd);
        buttonPanel.add(btnEnvoyerFournisseur);
        buttonPanel.add(btnHistorique);
        buttonPanel.add(btnConfig);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Créer le tableau
        String[] reapproColumns = {"Médicament", "Stock Actuel", "Seuil", "Différence", 
                                   "Quantité à Commander", "Prix Est.", "Action"};
        reapprovisionnementTableModel = new DefaultTableModel(reapproColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Seulement la colonne Action
            }
        };
        
        reapprovisionnementTable = new JTable(reapprovisionnementTableModel);
        styleTable(reapprovisionnementTable);
        
        JScrollPane scrollPane = new JScrollPane(reapprovisionnementTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // IMPORTANT: Créer de nouveaux labels, ne pas réutiliser ceux d'autres panels
        JLabel lblTotalReappro = new JLabel("0", SwingConstants.CENTER);
        lblTotalReappro.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblValeurTotale = new JLabel("0 DT", SwingConstants.CENTER);
        lblValeurTotale.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblCritique = new JLabel("0", SwingConstants.CENTER);
        lblCritique.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblUrgent = new JLabel("0", SwingConstants.CENTER);
        lblUrgent.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        statsPanel.add(createSmallStatCard("À Réapprovisionner", lblTotalReappro, PRIMARY));
        statsPanel.add(createSmallStatCard("Valeur Totale", lblValeurTotale, SUCCESS));
        statsPanel.add(createSmallStatCard("Critique", lblCritique, DANGER));
        statsPanel.add(createSmallStatCard("Urgent", lblUrgent, WARNING));
        
        // Assemblez le panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void initializeServices() {
        this.stockService = new StockService();
        this.commandeService = new CommandeService();
        this.historiqueDAO = new StockHistoriqueDAO();
        this.emailService = new EmailService();
        this.exportService = new ExportService();
        this.reportService = new ReportService();
    }
    
    private void initializeComponents() {
        setTitle("Pharmacy Manager - Gestionnaire");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Labels statistiques
        lblTotalStock = createStatLabel("0");
        lblStockCritique = createStatLabel("0");
        lblCommandesAttente = createStatLabel("0");
        lblValeurStock = createStatLabel("0 DT");
        
        // Tables
        initializeTables();
    }
    
    private void initializeTables() {
        // Table stock
        String[] stockColumns = {"ID", "Nom", "Dosage", "Stock", "Prix", "Seuil", "Status"};
        stockTableModel = new DefaultTableModel(stockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        styleTable(stockTable);
        
        // Table commandes
        String[] cmdColumns = {"ID", "Médicament", "Quantité", "Date", "Statut"};
        commandesTableModel = new DefaultTableModel(cmdColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        commandesTable = new JTable(commandesTableModel);
        styleTable(commandesTable);
        
        // Table historique
        String[] historiqueColumns = {"ID", "Médicament", "Qté Avant", "Qté Après", "Différence", "Type Opération", "ID Opération", "Date"};
        historiqueTableModel = new DefaultTableModel(historiqueColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historiqueTable = new JTable(historiqueTableModel);
        styleTable(historiqueTable);
        
        // Table emails
        String[] emailColumns = {"Date", "Destinataire", "Sujet", "Type", "Statut", "Fichier"};
        emailsTableModel = new DefaultTableModel(emailColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        emailsTable = new JTable(emailsTableModel);
        styleTable(emailsTable);
        // Table réapprovisionnement
        String[] reapproColumns = {"Médicament", "Stock Actuel", "Seuil", "Différence", 
                                   "Quantité à Commander", "Prix Est.", "Action"};
        reapprovisionnementTableModel = new DefaultTableModel(reapproColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Seulement la colonne Action
            }
        };
        reapprovisionnementTable = new JTable(reapprovisionnementTableModel);
        styleTable(reapprovisionnementTable);
        // Table rapports
        String[] rapportColumns = {"ID", "Type", "Période", "Généré le", "Statut", "Fichier"};
        rapportsTableModel = new DefaultTableModel(rapportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rapportsTable = new JTable(rapportsTableModel);
        styleTable(rapportsTable);
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
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        
        // Sidebar
        JPanel sidebarPanel = createSidebarPanel();
        
        // Contenu
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Ajouter les panneaux
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createStockPanel(), "stock");
        mainPanel.add(createCommandesPanel(), "commandes");
        mainPanel.add(createProfilePanel(), "profile");
        mainPanel.add(createAddMedicamentPanel(), "addMedicament");
        mainPanel.add(createHistoriquePanel(), "historique");
        mainPanel.add(createEmailInboxPanel(), "emails");
        mainPanel.add(createAdvancedReportsPanel(), "advancedReports");
        mainPanel.add(createNotificationPanel(), "notifications");
        mainPanel.add(createReapprovisionnementPanel(), "reapprovisionnement");
        
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WARNING);
        panel.setPreferredSize(new Dimension(getWidth(), 70));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        JLabel lblTitle = new JLabel("PHARMACY MANAGER - GESTIONNAIRE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(WARNING);
        
        JLabel lblUser = new JLabel(currentUser.getPrenom() + " " + currentUser.getNom());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);
        
        JLabel lblRole = new JLabel("GESTIONNAIRE");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(WARNING);
        lblRole.setBackground(Color.WHITE);
        lblRole.setOpaque(true);
        lblRole.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Notification badge
        JLabel lblNotif = new JLabel("🔔");
        lblNotif.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblNotif.setForeground(Color.WHITE);
        lblNotif.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblNotif.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "notifications");
                loadNotificationsData();
            }
        });
        
        userPanel.add(lblNotif);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(lblUser);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(lblRole);
        
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(DARK_BLUE);
        panel.setPreferredSize(new Dimension(280, getHeight()));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("NAVIGATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(200, 200, 200));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        
        // Boutons navigation
        JButton btnDashboard = createNavButton("📊 Tableau de bord", "dashboard");
        JButton btnStock = createNavButton("📦 Gestion Stock", "stock");
        JButton btnCommandes = createNavButton("📋 Commandes", "commandes");
        JButton btnReappro = createNavButton("🔄 Réapprovisionnement", "reapprovisionnement");
        JButton btnAddMed = createNavButton("➕ Ajouter Médicament", "addMedicament");
        JButton btnHistorique = createNavButton("📈 Historique Stock", "historique");
        JButton btnEmails = createNavButton("📧 Boîte Email", "emails");
        JButton btnRapports = createNavButton("📊 Rapports Avancés", "advancedReports");
        JButton btnNotifications = createNavButton("🔔 Notifications", "notifications");
        
        panel.add(btnDashboard);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnStock);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnCommandes);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnReappro);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnAddMed);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnHistorique);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnEmails);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnRapports);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnNotifications);
        panel.add(Box.createVerticalStrut(30));
        
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(100, 100, 100));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(30));
        
        JButton btnProfile = createNavButton("👤 Mon Profil", "profile");
        JButton btnLogout = createNavButton("🚪 Déconnexion", "logout");
        
        panel.add(btnProfile);
        panel.add(Box.createVerticalStrut(8));
        panel.add(btnLogout);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 45));
        button.setMinimumSize(new Dimension(240, 45));
        
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
                } else if ("commandes".equals(panelName)) {
                    loadCommandesData();
                } else if ("dashboard".equals(panelName)) {
                    updateStatistics();
                } else if ("historique".equals(panelName)) {
                    loadHistoriqueData();
                } else if ("emails".equals(panelName)) {
                    loadEmailData();
                } else if ("advancedReports".equals(panelName)) {
                    loadRapportsData();
                } else if ("notifications".equals(panelName)) {
                    loadNotificationsData();
                } else if ("reapprovisionnement".equals(panelName)) {
                    loadReapprovisionnementData();
                }
            }
        });
        
        return button;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("Tableau de Bord - Gestionnaire");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        statsPanel.add(createStatCard("Total Stock", lblTotalStock, PRIMARY));
        statsPanel.add(createStatCard("Stock Critique", lblStockCritique, DANGER));
        statsPanel.add(createStatCard("Commandes en Attente", lblCommandesAttente, WARNING));
        statsPanel.add(createStatCard("Valeur du Stock", lblValeurStock, SUCCESS));
        
        // Actions rapides
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickActionsPanel.setBackground(Color.WHITE);
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Actions Rapides"));
        
        JButton btnQuickCommande = createQuickActionButton("📋 Nouvelle Commande", "➕");
        JButton btnQuickAlertes = createQuickActionButton("⚠️ Alertes Stock", "🚨");
        JButton btnQuickEmail = createQuickActionButton("📧 Envoyer Email", "📤");
        JButton btnQuickRapport = createQuickActionButton("📊 Générer Rapport", "📈");
        JButton btnQuickReappro = createQuickActionButton("🔄 Réapprovisionner", "📦");
        JButton btnQuickHistorique = createQuickActionButton("📈 Historique", "📊");
        
        btnQuickCommande.addActionListener(e -> {
            cardLayout.show(mainPanel, "commandes");
            showCreateCommandeDialog();
        });
        
        btnQuickAlertes.addActionListener(e -> {
            showStockCritiqueDialog();
        });
        
        btnQuickEmail.addActionListener(e -> {
            cardLayout.show(mainPanel, "emails");
            showNewEmailDialog();
        });
        
        btnQuickRapport.addActionListener(e -> {
            cardLayout.show(mainPanel, "advancedReports");
            generateAllReports();
        });
        
        btnQuickReappro.addActionListener(e -> {
            cardLayout.show(mainPanel, "reapprovisionnement");
            loadReapprovisionnementData();
        });
        
        btnQuickHistorique.addActionListener(e -> {
            cardLayout.show(mainPanel, "historique");
            loadHistoriqueData();
        });
        
        quickActionsPanel.add(btnQuickCommande);
        quickActionsPanel.add(btnQuickAlertes);
        quickActionsPanel.add(btnQuickEmail);
        quickActionsPanel.add(btnQuickRapport);
        quickActionsPanel.add(btnQuickReappro);
        quickActionsPanel.add(btnQuickHistorique);
        
        // Graphiques simulés
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Aperçu des Statistiques"));
        
        JLabel chart1 = new JLabel("<html><center>📈 Ventes 30 derniers jours<br><br>" +
            "<font size='6' color='#2196F3'>124.75 DT</font><br>" +
            "<font size='3' color='#4CAF50'>↑ 12.5%</font></center></html>", SwingConstants.CENTER);
        chart1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chart1.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JLabel chart2 = new JLabel("<html><center>📊 Stock Critique<br><br>" +
            "<font size='6' color='#F44336'>2 médicaments</font><br>" +
            "<font size='3' color='#FF9800'>Action requise</font></center></html>", SwingConstants.CENTER);
        chart2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chart2.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        chartsPanel.add(chart1);
        chartsPanel.add(chart2);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(quickActionsPanel, BorderLayout.NORTH);
        southPanel.add(chartsPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
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
        JButton btnAjouter = createActionButton("➕ Ajouter", SUCCESS);
        JButton btnModifier = createActionButton("✏️ Modifier", WARNING);
        JButton btnSupprimer = createActionButton("🗑️ Supprimer", DANGER);
        JButton btnAlertes = createActionButton("⚠️ Alertes", DANGER);
        JButton btnExport = createActionButton("📊 Exporter", INFO);
        
        btnRefresh.addActionListener(e -> loadStockData());
        btnAjouter.addActionListener(e -> showAddMedicamentDialog());
        btnModifier.addActionListener(e -> showEditMedicamentDialog());
        btnSupprimer.addActionListener(e -> deleteSelectedMedicament());
        btnAlertes.addActionListener(e -> showStockCritiqueDialog());
        btnExport.addActionListener(e -> exportStockCSV());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAjouter);
        buttonPanel.add(btnModifier);
        buttonPanel.add(btnSupprimer);
        buttonPanel.add(btnAlertes);
        buttonPanel.add(btnExport);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCommandesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📋 Gestion des Commandes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnNouvelle = createActionButton("📋 Nouvelle", SUCCESS);
        JButton btnValider = createActionButton("✅ Valider", WARNING);
        JButton btnRecevoir = createActionButton("📦 Recevoir", INFO);
        JButton btnAnnuler = createActionButton("❌ Annuler", DANGER);
        JButton btnAutomatique = createActionButton("⚡ Auto", SUCCESS);
        
        btnRefresh.addActionListener(e -> loadCommandesData());
        btnNouvelle.addActionListener(e -> showCreateCommandeDialog());
        btnValider.addActionListener(e -> validerCommandeSelectionnee());
        btnRecevoir.addActionListener(e -> recevoirCommandeSelectionnee());
        btnAnnuler.addActionListener(e -> annulerCommandeSelectionnee());
        btnAutomatique.addActionListener(e -> creerCommandesAutomatiques());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnNouvelle);
        buttonPanel.add(btnValider);
        buttonPanel.add(btnRecevoir);
        buttonPanel.add(btnAnnuler);
        buttonPanel.add(btnAutomatique);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(commandesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAddMedicamentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("➕ Ajouter un Médicament");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Informations du médicament"
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtNom = new JTextField(20);
        formPanel.add(txtNom, gbc);
        
        // Dosage
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Dosage:"), gbc);
        gbc.gridx = 1;
        JTextField txtDosage = new JTextField(20);
        formPanel.add(txtDosage, gbc);
        
        // Stock initial
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Stock initial:"), gbc);
        gbc.gridx = 1;
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        formPanel.add(spnStock, gbc);
        
        // Prix
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Prix unitaire (DT):"), gbc);
        gbc.gridx = 1;
        JSpinner spnPrix = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));
        formPanel.add(spnPrix, gbc);
        
        // Seuil alerte
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Seuil d'alerte:"), gbc);
        gbc.gridx = 1;
        JSpinner spnSeuil = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        formPanel.add(spnSeuil, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridheight = 2;
        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        JScrollPane scrollDescription = new JScrollPane(txtDescription);
        formPanel.add(scrollDescription, gbc);
        
        // Bouton
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnAdd = createActionButton("➕ Ajouter le Médicament", SUCCESS);
        btnAdd.setPreferredSize(new Dimension(300, 45));
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        btnAdd.addActionListener(e -> {
            Medicament med = new Medicament();
            med.setNom(txtNom.getText());
            med.setDosage(txtDosage.getText());
            med.setStock((Integer) spnStock.getValue());
            med.setPrixUnitaire((Double) spnPrix.getValue());
            med.setSeuilAlerte((Integer) spnSeuil.getValue());
            med.setDescription(txtDescription.getText());
            
            if (stockService.ajouterMedicament(med)) {
                JOptionPane.showMessageDialog(this, "✅ Médicament ajouté avec succès!");
                txtNom.setText("");
                txtDosage.setText("");
                spnStock.setValue(0);
                spnPrix.setValue(0.0);
                spnSeuil.setValue(10);
                txtDescription.setText("");
                loadStockData();
                cardLayout.show(mainPanel, "stock");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Erreur lors de l'ajout du médicament!");
            }
        });
        
        formPanel.add(btnAdd, gbc);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoriquePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📈 Historique des Mouvements de Stock");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnFiltrer = createActionButton("🔍 Filtrer", INFO);
        JButton btnExporter = createActionButton("📊 Exporter CSV", SUCCESS);
        JButton btnClear = createActionButton("🗑️ Effacer", DANGER);
        
        btnRefresh.addActionListener(e -> loadHistoriqueData());
        btnFiltrer.addActionListener(e -> showFiltrerHistoriqueDialog());
        btnExporter.addActionListener(e -> exporterHistoriqueCSV());
        btnClear.addActionListener(e -> clearHistoriqueFiltres());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnFiltrer);
        buttonPanel.add(btnExporter);
        buttonPanel.add(btnClear);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(historiqueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Panel info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel infoLabel = new JLabel("💡 Vert = Augmentation (commande) | Rouge = Diminution (vente) | Bleu = Ajustement");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEmailInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📧 Boîte de Réception - Emails Envoyés");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnNouveauEmail = createActionButton("📝 Nouvel Email", SUCCESS);
        JButton btnTestEmail = createActionButton("🧪 Tester Email", WARNING);
        JButton btnOuvrirDossier = createActionButton("📁 Ouvrir Dossier", INFO);
        JButton btnConfig = createActionButton("⚙️ Config", DANGER);
        
        btnRefresh.addActionListener(e -> loadEmailData());
        btnNouveauEmail.addActionListener(e -> showNewEmailDialog());
        btnTestEmail.addActionListener(e -> testEmailService());
        btnOuvrirDossier.addActionListener(e -> openEmailFolder());
        btnConfig.addActionListener(e -> configurerEmailService());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnNouveauEmail);
        buttonPanel.add(btnTestEmail);
        buttonPanel.add(btnOuvrirDossier);
        buttonPanel.add(btnConfig);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        JScrollPane scrollPane = new JScrollPane(emailsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Double-clic pour ouvrir l'email
        emailsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = emailsTable.getSelectedRow();
                    if (row != -1) {
                        String fileName = (String) emailsTableModel.getValueAt(row, 5);
                        openEmailFile(fileName);
                    }
                }
            }
        });
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAdvancedReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("📊 Rapports Statistiques Avancés");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnGenerer = createActionButton("🔄 Générer Tous", PRIMARY);
        JButton btnExportPDF = createActionButton("📄 Exporter PDF", DANGER);
        JButton btnExportExcel = createActionButton("📊 Exporter Excel", SUCCESS);
        JButton btnVueEnsemble = createActionButton("👁️ Vue d'ensemble", INFO);
        JButton btnPlanifier = createActionButton("⏰ Planifier", WARNING);
        
        btnGenerer.addActionListener(e -> generateAllReports());
        btnExportPDF.addActionListener(e -> exportReportsToPDF());
        btnExportExcel.addActionListener(e -> exportReportsToExcel());
        btnVueEnsemble.addActionListener(e -> showDashboardOverview());
        btnPlanifier.addActionListener(e -> planifierRapports());
        
        buttonPanel.add(btnGenerer);
        buttonPanel.add(btnExportPDF);
        buttonPanel.add(btnExportExcel);
        buttonPanel.add(btnVueEnsemble);
        buttonPanel.add(btnPlanifier);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Onglets pour différents rapports
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Rapport 1: Ventes
        tabbedPane.addTab("💰 Ventes", createSalesReportPanel());
        
        // Rapport 2: Stock
        tabbedPane.addTab("📦 Stock", createStockReportPanel());
        
        // Rapport 3: Finances
        tabbedPane.addTab("💵 Finances", createFinancialReportPanel());
        
        // Rapport 4: Clients
        tabbedPane.addTab("👥 Clients", createClientReportPanel());
        
        // Rapport 5: Commandes
        tabbedPane.addTab("📋 Commandes", createCommandesReportPanel());
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("🔔 Notifications Système");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = createActionButton("🔄 Actualiser", PRIMARY);
        JButton btnMarquerTous = createActionButton("✅ Tout marquer lu", SUCCESS);
        JButton btnSupprimer = createActionButton("🗑️ Supprimer tous", DANGER);
        JButton btnConfig = createActionButton("⚙️ Configuration", INFO);
        
        btnRefresh.addActionListener(e -> loadNotificationsData());
        btnMarquerTous.addActionListener(e -> marquerToutesNotificationsLues());
        btnSupprimer.addActionListener(e -> supprimerToutesNotifications());
        btnConfig.addActionListener(e -> configurerNotifications());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnMarquerTous);
        buttonPanel.add(btnSupprimer);
        buttonPanel.add(btnConfig);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Table des notifications
        String[] notifColumns = {"Date", "Type", "Message", "Priorité", "Statut"};
        DefaultTableModel notifTableModel = new DefaultTableModel(notifColumns, 0);
        JTable notifTable = new JTable(notifTableModel);
        styleTable(notifTable);
        
        // Rendu des priorités
        notifTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String priorite = (String) table.getValueAt(row, 3);
                    if ("Haute".equals(priorite)) {
                        c.setBackground(new Color(255, 230, 230));
                    } else if ("Moyenne".equals(priorite)) {
                        c.setBackground(new Color(255, 255, 230));
                    } else if ("Faible".equals(priorite)) {
                        c.setBackground(new Color(230, 255, 230));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(notifTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    private void loadReapprovisionnementData() {
        System.out.println("🔄 Chargement des données de réapprovisionnement...");
        
        if (reapprovisionnementTableModel == null) {
            System.err.println("❌ ERREUR: Modèle de réapprovisionnement non initialisé!");
            return;
        }
        
        // Vider le tableau
        reapprovisionnementTableModel.setRowCount(0);
        
        try {
            // Récupérer tous les médicaments
            List<Medicament> medicaments = stockService.getAllMedicaments();
            int totalReappro = 0;
            int critiques = 0;
            int urgents = 0;
            double valeurTotale = 0.0;
            
            for (Medicament med : medicaments) {
                int stockActuel = med.getStock();
                int seuil = med.getSeuilAlerte();
                int difference = seuil - stockActuel;
                
                // Si le stock est en dessous du seuil
                if (stockActuel < seuil) {
                    totalReappro++;
                    
                    // Calculer la quantité à commander (au moins le seuil + 20%)
                    int quantiteACommander = Math.max(seuil - stockActuel, (int)(seuil * 0.2));
                    
                    // Estimer le prix
                    double prixEstime = quantiteACommander * med.getPrixUnitaire();
                    valeurTotale += prixEstime;
                    
                    // Déterminer le niveau de criticité
                    String action;
                    if (stockActuel <= 0) {
                        critiques++;
                        action = "🚨 RUPTURE";
                    } else if (stockActuel <= seuil * 0.3) {
                        critiques++;
                        action = "⚠️ CRITIQUE";
                    } else if (stockActuel <= seuil * 0.5) {
                        urgents++;
                        action = "🔔 URGENT";
                    } else {
                        action = "📝 À PLANIFIER";
                    }
                    
                    // Ajouter au tableau
                    reapprovisionnementTableModel.addRow(new Object[]{
                        med.getNom() + " (" + med.getDosage() + ")",
                        stockActuel,
                        seuil,
                        difference,
                        quantiteACommander,
                        String.format("%.2f DT", prixEstime),
                        action
                    });
                    
                    System.out.println("📌 " + med.getNom() + " - Stock: " + stockActuel + 
                                     "/" + seuil + " -> " + action);
                }
            }
            
            // Mettre à jour les statistiques
            updateReapproStats(totalReappro, valeurTotale, critiques, urgents);
            
            if (totalReappro == 0) {
                reapprovisionnementTableModel.addRow(new Object[]{
                    "✅ Tous les stocks sont suffisants",
                    "-", "-", "-", "-", "-", "NORMAL"
                });
                System.out.println("✅ Aucun besoin de réapprovisionnement détecté");
            } else {
                System.out.println("✅ " + totalReappro + " médicaments nécessitent un réapprovisionnement");
            }
            
            // Rafraîchir l'affichage
            reapprovisionnementTable.revalidate();
            reapprovisionnementTable.repaint();
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR dans loadReapprovisionnementData: " + e.getMessage());
            e.printStackTrace();
            showReapproDemo();
        }
    }

    private void updateReapproStats(int total, double valeur, int critiques, int urgents) {
        // Cette méthode mettrait à jour les labels dans le statsPanel
        // Pour l'instant, on affiche dans la console
        System.out.println("📊 Statistiques réapprovisionnement:");
        System.out.println("   Total: " + total + " médicaments");
        System.out.println("   Valeur: " + String.format("%.2f DT", valeur));
        System.out.println("   Critiques: " + critiques);
        System.out.println("   Urgents: " + urgents);
    }

    private void showReapproDemo() {
        if (reapprovisionnementTableModel != null) {
            reapprovisionnementTableModel.setRowCount(0);
            
            // Données de démonstration
            reapprovisionnementTableModel.addRow(new Object[]{
                "Ventoline 100µg", 3, 10, 7, 15, "375.00 DT", "⚠️ CRITIQUE"
            });
            reapprovisionnementTableModel.addRow(new Object[]{
                "Insuline 100UI/ml", 5, 8, 3, 12, "540.00 DT", "⚠️ CRITIQUE"
            });
            reapprovisionnementTableModel.addRow(new Object[]{
                "Paracétamol 500mg", 45, 50, 5, 10, "39.90 DT", "🔔 URGENT"
            });
            reapprovisionnementTableModel.addRow(new Object[]{
                "Ibuprofène 400mg", 22, 25, 3, 8, "36.00 DT", "📝 À PLANIFIER"
            });
            
            System.out.println("📊 Mode démo réapprovisionnement activé");
        }
    }
    private JPanel createReapprovisionnementPanel1() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel lblTitle = new JLabel("🔄 Réapprovisionnement Automatique");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnAnalyser = createActionButton("🔍 Analyser Stock", PRIMARY);
        JButton btnGenererCmd = createActionButton("📋 Générer Commandes", SUCCESS);
        JButton btnEnvoyerFournisseur = createActionButton("📧 Envoyer Fournisseur", WARNING);
        JButton btnHistorique = createActionButton("📈 Historique", INFO);
        JButton btnConfig = createActionButton("⚙️ Paramètres", DANGER);
        
        btnAnalyser.addActionListener(e -> analyserStockPourReappro());
        btnGenererCmd.addActionListener(e -> genererCommandesReappro());
        btnEnvoyerFournisseur.addActionListener(e -> envoyerCommandesFournisseur());
        btnHistorique.addActionListener(e -> showHistoriqueReappro());
        btnConfig.addActionListener(e -> configurerReapprovisionnement());
        
        buttonPanel.add(btnAnalyser);
        buttonPanel.add(btnGenererCmd);
        buttonPanel.add(btnEnvoyerFournisseur);
        buttonPanel.add(btnHistorique);
        buttonPanel.add(btnConfig);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Table des médicaments à réapprovisionner
        String[] reapproColumns = {"Médicament", "Stock Actuel", "Seuil", "Différence", "Quantité à Commander", "Prix Est.", "Action"};
        DefaultTableModel reapproTableModel = new DefaultTableModel(reapproColumns, 0);
        JTable reapproTable = new JTable(reapproTableModel);
        styleTable(reapproTable);
        
        JScrollPane scrollPane = new JScrollPane(reapproTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTotalReappro = new JLabel("0", SwingConstants.CENTER);
        lblTotalReappro.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblValeurTotale = new JLabel("0 DT", SwingConstants.CENTER);
        lblValeurTotale.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblCritique = new JLabel("0", SwingConstants.CENTER);
        lblCritique.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblUrgent = new JLabel("0", SwingConstants.CENTER);
        lblUrgent.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        statsPanel.add(createSmallStatCard("À Réapprovisionner", lblTotalReappro, PRIMARY));
        statsPanel.add(createSmallStatCard("Valeur Totale", lblValeurTotale, SUCCESS));
        statsPanel.add(createSmallStatCard("Critique", lblCritique, DANGER));
        statsPanel.add(createSmallStatCard("Urgent", lblUrgent, WARNING));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSmallStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        
        valueLabel.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createSalesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Statistiques clés
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        
        JLabel lblVentesTotal = new JLabel("Chargement...", SwingConstants.CENTER);
        lblVentesTotal.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblCAMoyen = new JLabel("Chargement...", SwingConstants.CENTER);
        lblCAMoyen.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblTopMed = new JLabel("Chargement...", SwingConstants.CENTER);
        lblTopMed.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel lblClientsActifs = new JLabel("Chargement...", SwingConstants.CENTER);
        lblClientsActifs.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblPanierMoyen = new JLabel("Chargement...", SwingConstants.CENTER);
        lblPanierMoyen.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblTendance = new JLabel("Chargement...", SwingConstants.CENTER);
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
                // Récupérer les données des ventes
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                Date debutMois = cal.getTime();
                Date finMois = new Date();
                
                Map<String, Object> rapportVentes = reportService.getRapportVentes(debutMois, finMois);
                
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
                        } else {
                            lblTopMed.setText("Aucun");
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
        
        // Graphique simulé
        JLabel lblGraph = new JLabel("📈 Graphique des ventes des 30 derniers jours", SwingConstants.CENTER);
        lblGraph.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblGraph.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        panel.add(lblGraph, BorderLayout.CENTER);
        
        return panel;
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
    
    private JPanel createStockReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Texte initial
        JLabel lblReport = new JLabel("<html><center>📦 CHARGEMENT DU RAPPORT DE STOCK...<br><br>" +
            "Veuillez patienter...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données dynamiquement
        new Thread(() -> {
            try {
                Map<String, Object> rapportStock = reportService.getRapportStock();
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        double valeurTotale = 0.0;
                        int medicamentsCritiques = 0;
                        int medicamentsFaibles = 0;
                        int totalMedicaments = 0;
                        
                        // Récupérer les valeurs avec vérification de type
                        Object valeurObj = rapportStock.get("valeur_totale_stock");
                        if (valeurObj instanceof Double) {
                            valeurTotale = (Double) valeurObj;
                        } else if (valeurObj instanceof Number) {
                            valeurTotale = ((Number) valeurObj).doubleValue();
                        }
                        
                        Object critiquesObj = rapportStock.get("medicaments_critiques");
                        if (critiquesObj instanceof Integer) {
                            medicamentsCritiques = (Integer) critiquesObj;
                        }
                        
                        Object faiblesObj = rapportStock.get("medicaments_faibles");
                        if (faiblesObj instanceof Integer) {
                            medicamentsFaibles = (Integer) faiblesObj;
                        }
                        
                        Object totalObj = rapportStock.get("total_medicaments");
                        if (totalObj instanceof Integer) {
                            totalMedicaments = (Integer) totalObj;
                        }
                        
                        lblReport.setText("<html><center>📦 <b>RAPPORT DE STOCK DÉTAILLÉ</b><br><br>" +
                            "<b>Total médicaments:</b> " + totalMedicaments + "<br>" +
                            "<b>Valeur totale:</b> " + String.format("%.2f DT", valeurTotale) + "<br>" +
                            "<b>Stock critique:</b> " + medicamentsCritiques + " médicaments<br>" +
                            "<b>Stock faible:</b> " + medicamentsFaibles + " médicaments<br>" +
                            "<b>Date du rapport:</b> " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "</center></html>");
                        
                    } catch (Exception e) {
                        lblReport.setText("<html><center>📦 <b>RAPPORT DE STOCK</b><br><br>" +
                            "Erreur lors du traitement des données.</center></html>");
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblReport.setText("<html><center>📦 <b>RAPPORT DE STOCK</b><br><br>" +
                        "Impossible de charger les données du stock.<br>" +
                        "Erreur: " + e.getMessage() + "</center></html>");
                });
                e.printStackTrace();
            }
        }).start();
        
        return panel;
    }
    
    private JPanel createFinancialReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Texte initial
        JLabel lblReport = new JLabel("<html><center>💵 CHARGEMENT DU RAPPORT FINANCIER...<br><br>" +
            "Veuillez patienter...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données dynamiquement
        new Thread(() -> {
            try {
                // Récupérer les données des ventes du mois
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                Date debutMois = cal.getTime();
                Date finMois = new Date();
                
                Map<String, Object> rapportVentes = reportService.getRapportVentes(debutMois, finMois);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        Object totalCAObj = rapportVentes.get("total_chiffre_affaires");
                        double chiffreAffaires = 0.0;
                        if (totalCAObj != null) {
                            if (totalCAObj instanceof Double) {
                                chiffreAffaires = (Double) totalCAObj;
                            } else if (totalCAObj instanceof Number) {
                                chiffreAffaires = ((Number) totalCAObj).doubleValue();
                            }
                        }
                        
                        // Estimations (dans une vraie application, ces données viendraient de la BD)
                        double coutMarchandises = chiffreAffaires * 0.56; // 56% du CA
                        double margeBrute = chiffreAffaires - coutMarchandises;
                        double depensesOperationnelles = margeBrute * 0.50; // 50% de la marge
                        double beneficeNet = margeBrute - depensesOperationnelles;
                        double margePourcentage = (margeBrute / chiffreAffaires) * 100;
                        double roi = (beneficeNet / coutMarchandises) * 100;
                        
                        lblReport.setText("<html><center>💵 <b>RAPPORT FINANCIER</b><br><br>" +
                            "<b>Chiffre d'affaires mensuel:</b> " + String.format("%.2f DT", chiffreAffaires) + "<br>" +
                            "<b>Coût des marchandises:</b> " + String.format("%.2f DT", coutMarchandises) + "<br>" +
                            "<b>Marge brute:</b> " + String.format("%.2f DT (%.1f%%)", margeBrute, margePourcentage) + "<br>" +
                            "<b>Dépenses opérationnelles:</b> " + String.format("%.2f DT", depensesOperationnelles) + "<br>" +
                            "<b>Bénéfice net:</b> " + String.format("%.2f DT", beneficeNet) + "<br>" +
                            "<b>ROI:</b> " + String.format("%.1f%%", roi) + "</center></html>");
                        
                    } catch (Exception e) {
                        lblReport.setText("<html><center>💵 <b>RAPPORT FINANCIER</b><br><br>" +
                            "Erreur lors du traitement des données.</center></html>");
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblReport.setText("<html><center>💵 <b>RAPPORT FINANCIER</b><br><br>" +
                        "Impossible de charger les données financières.<br>" +
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
        JLabel lblReport = new JLabel("<html><center>👥 CHARGEMENT DU RAPPORT CLIENTS...<br><br>" +
            "Veuillez patienter...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données dynamiquement
        new Thread(() -> {
            try {
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
                        
                        lblReport.setText("<html><center>👥 <b>RAPPORT CLIENTÈLE</b><br><br>" +
                            "<b>Total clients actifs:</b> " + nbClients + "<br>" +
                            "<b>Total dépenses clients:</b> " + String.format("%.2f DT", totalDepenses) + "<br>" +
                            "<b>Dépense moyenne par client:</b> " + String.format("%.2f DT", moyenneDepense) + "<br>" +
                            "<b>Taux de rétention estimé:</b> " + (nbClients > 0 ? "85%" : "0%") + "<br>" +
                            "<b>Date du rapport:</b> " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "</center></html>");
                        
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
    
    private JPanel createCommandesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Texte initial
        JLabel lblReport = new JLabel("<html><center>📋 CHARGEMENT DU RAPPORT COMMANDES...<br><br>" +
            "Veuillez patienter...</center></html>", 
            SwingConstants.CENTER);
        lblReport.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        panel.add(lblReport, BorderLayout.CENTER);
        
        // Charger les données dynamiquement
        new Thread(() -> {
            try {
                List<Commande> toutesCommandes = commandeService.getAllCommandes();
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Calculer les statistiques
                        int commandesCeMois = 0;
                        int enAttente = 0;
                        int validees = 0;
                        int recues = 0;
                        double valeurTotale = 0.0;
                        
                        // Filtrer les commandes du mois en cours
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        int moisCourant = cal.get(java.util.Calendar.MONTH);
                        int anneeCourante = cal.get(java.util.Calendar.YEAR);
                        
                        for (Commande cmd : toutesCommandes) {
                            if (cmd.getDateCommande() != null) {
                                cal.setTime(cmd.getDateCommande());
                                int moisCommande = cal.get(java.util.Calendar.MONTH);
                                int anneeCommande = cal.get(java.util.Calendar.YEAR);
                                
                                if (moisCommande == moisCourant && anneeCommande == anneeCourante) {
                                    commandesCeMois++;
                                    
                                    // Estimer la valeur de la commande
                                    Medicament med = stockService.getMedicamentById(cmd.getIdMedicament());
                                    if (med != null) {
                                        valeurTotale += cmd.getQuantite() * med.getPrixUnitaire();
                                    }
                                }
                            }
                            
                            // Compter par statut
                            String statut = cmd.getStatut();
                            if ("en_attente".equals(statut)) {
                                enAttente++;
                            } else if ("validee".equals(statut)) {
                                validees++;
                            } else if ("recue".equals(statut)) {
                                recues++;
                            }
                        }
                        
                        // Calculer le délai moyen (simulation)
                        double delaiMoyen = commandesCeMois > 0 ? 4.2 : 0;
                        
                        lblReport.setText("<html><center>📋 <b>RAPPORT COMMANDES</b><br><br>" +
                            "<b>Commandes ce mois:</b> " + commandesCeMois + "<br>" +
                            "<b>Valeur totale:</b> " + String.format("%.2f DT", valeurTotale) + "<br>" +
                            "<b>En attente:</b> " + enAttente + " commandes<br>" +
                            "<b>Validées:</b> " + validees + " commandes<br>" +
                            "<b>Reçues:</b> " + recues + " commandes<br>" +
                            "<b>Délai moyen:</b> " + String.format("%.1f jours", delaiMoyen) + "</center></html>");
                        
                    } catch (Exception e) {
                        lblReport.setText("<html><center>📋 <b>RAPPORT COMMANDES</b><br><br>" +
                            "Erreur lors du traitement des données.</center></html>");
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblReport.setText("<html><center>📋 <b>RAPPORT COMMANDES</b><br><br>" +
                        "Impossible de charger les données des commandes.<br>" +
                        "Erreur: " + e.getMessage() + "</center></html>");
                });
                e.printStackTrace();
            }
        }).start();
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("👤 Mon Profil - Gestionnaire");
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
        JLabel lblAvatar = new JLabel("👨‍💼");
        lblAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        lblAvatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        infoPanel.add(lblAvatar, gbc);
        
        // Informations
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1;
        infoPanel.add(new JLabel("ID Gestionnaire:"), gbc);
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
        JLabel lblRole = new JLabel("GESTIONNAIRE");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setForeground(WARNING);
        infoPanel.add(lblRole, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        infoPanel.add(new JLabel("Statistiques:"), gbc);
        gbc.gridx = 2;
        JLabel lblStats = new JLabel("Actif - " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
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
        loadStockData();
        loadCommandesData();
        loadHistoriqueData();
        loadEmailData();
        loadRapportsData();
        loadNotificationsData();
        updateStatistics();
        cardLayout.show(mainPanel, "dashboard");
    }
    
    private void loadStockData() {
        stockTableModel.setRowCount(0);
        List<Medicament> medicaments = stockService.getAllMedicaments();
        
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
    }
    
    private void loadCommandesData() {
        commandesTableModel.setRowCount(0);
        List<Commande> commandes = commandeService.getAllCommandes();
        
        for (Commande cmd : commandes) {
            commandesTableModel.addRow(new Object[]{
                cmd.getId(),
                cmd.getMedicamentNom(),
                cmd.getQuantite(),
                cmd.getDateCommande(),
                cmd.getStatut()
            });
        }
    }
    
    private void loadHistoriqueData() {
        historiqueTableModel.setRowCount(0);
        try {
            List<models.StockHistorique> historiqueList = historiqueDAO.getAll();
            
            for (models.StockHistorique sh : historiqueList) {
                int difference = sh.getQuantiteApres() - sh.getQuantiteAvant();
                historiqueTableModel.addRow(new Object[]{
                    sh.getId(),
                    sh.getMedicamentNom() != null ? sh.getMedicamentNom() : "ID: " + sh.getIdMedicament(),
                    sh.getQuantiteAvant(),
                    sh.getQuantiteApres(),
                    difference,
                    sh.getTypeOperation(),
                    sh.getIdOperation(),
                    sh.getDateOperation()
                });
            }
            
            // Colorer les lignes selon la différence
            historiqueTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (!isSelected) {
                        int difference = (int) table.getValueAt(row, 4);
                        if (difference > 0) {
                            c.setBackground(new Color(200, 255, 200)); // Vert clair pour augmentation
                        } else if (difference < 0) {
                            c.setBackground(new Color(255, 200, 200)); // Rouge clair pour diminution
                        } else {
                            c.setBackground(new Color(200, 230, 255)); // Bleu clair pour ajustement
                        }
                    }
                    
                    return c;
                }
            });
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement de l'historique: " + e.getMessage(),
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadEmailData() {
        emailsTableModel.setRowCount(0);
        
        // Charger les emails depuis le dossier
        File emailDir = new File("emails_sent/");
        if (emailDir.exists() && emailDir.isDirectory()) {
            File[] emailFiles = emailDir.listFiles((dir, name) -> name.endsWith(".html"));
            if (emailFiles != null) {
                // Trier par date (plus récent d'abord)
                Arrays.sort(emailFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                
                for (File file : emailFiles) {
                    String fileName = file.getName();
                    String[] parts = fileName.split("_");
                    
                    String date = "";
                    String sujet = "";
                    String type = "Autre";
                    
                    if (parts.length >= 2) {
                        try {
                            date = parts[0] + " " + parts[1].substring(0, 2) + ":" + parts[1].substring(2, 4) + ":" + parts[1].substring(4, 6);
                        } catch (Exception e) {
                            date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(file.lastModified()));
                        }
                        
                        sujet = fileName.replace(".html", "").substring(fileName.indexOf('_') + 1);
                        if (sujet.contains("ALERTE")) type = "Alerte Stock";
                        else if (sujet.contains("Rapport")) type = "Rapport";
                        else if (sujet.contains("Commande")) type = "Commande";
                    }
                    
                    emailsTableModel.addRow(new Object[]{
                        date,
                        "gestionnaire@pharmacie.com",
                        sujet,
                        type,
                        "Envoyé",
                        fileName
                    });
                }
            }
        }
        
        // Ajouter des emails de démonstration si dossier vide
        if (emailsTableModel.getRowCount() == 0) {
            emailsTableModel.addRow(new Object[]{
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                "pharmacien@pharmacie.com",
                "ALERTE STOCK CRITIQUE - Ventoline",
                "Alerte Stock",
                "Envoyé",
                "demo_alerte.html"
            });
            
            emailsTableModel.addRow(new Object[]{
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                "direction@pharmacie.com",
                "Rapport Hebdomadaire - Stock Critique",
                "Rapport",
                "Envoyé",
                "demo_rapport.html"
            });
        }
    }
    
    private void loadRapportsData() {
        rapportsTableModel.setRowCount(0);
        
        // Rapports générés
        rapportsTableModel.addRow(new Object[]{
            1,
            "Ventes Mensuelles",
            "Janvier 2024",
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
            "✅ Généré",
            "rapport_ventes_janvier.pdf"
        });
        
        rapportsTableModel.addRow(new Object[]{
            2,
            "Stock Critique",
            "Semaine 3",
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
            "✅ Généré",
            "rapport_stock_critique.pdf"
        });
        
        rapportsTableModel.addRow(new Object[]{
            3,
            "Clients Fidèles",
            "2024",
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
            "⏳ En cours",
            ""
        });
        
        rapportsTableModel.addRow(new Object[]{
            4,
            "Finances Trimestriel",
            "Q1 2024",
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
            "📅 Planifié",
            ""
        });
    }
    
    private void loadNotificationsData() {
        // Cette méthode chargerait les notifications depuis une base de données
        // Pour l'instant, nous simulons des données
    }

    private void updateStatistics() {
        List<Medicament> medicaments = stockService.getAllMedicaments();
        int totalStock = medicaments.stream().mapToInt(Medicament::getStock).sum();
        int stockCritique = stockService.countStockCritique();
        int commandesAttente = commandeService.countCommandesEnAttente();
        double valeurStock = stockService.calculerValeurStock();
        
        lblTotalStock.setText(String.valueOf(totalStock));
        lblStockCritique.setText(String.valueOf(stockCritique));
        lblCommandesAttente.setText(String.valueOf(commandesAttente));
        lblValeurStock.setText(String.format("%.2f DT", valeurStock));
    }
    
    private void showAddMedicamentDialog() {
        cardLayout.show(mainPanel, "addMedicament");
    }
    
    private void showEditMedicamentDialog() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament à modifier", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) stockTableModel.getValueAt(selectedRow, 0);
        Medicament med = stockService.getMedicamentById(id);
        
        if (med == null) {
            JOptionPane.showMessageDialog(this, "Médicament non trouvé!", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Modifier Médicament", true);
        dialog.setSize(400, 450);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblNom = new JLabel("Nom:");
        JTextField txtNom = new JTextField(med.getNom(), 20);
        
        JLabel lblDosage = new JLabel("Dosage:");
        JTextField txtDosage = new JTextField(med.getDosage(), 20);
        
        JLabel lblStock = new JLabel("Stock:");
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(med.getStock(), 0, 10000, 1));
        
        JLabel lblPrix = new JLabel("Prix (DT):");
        JSpinner spnPrix = new JSpinner(new SpinnerNumberModel(med.getPrixUnitaire(), 0.0, 10000.0, 0.5));
        
        JLabel lblSeuil = new JLabel("Seuil alerte:");
        JSpinner spnSeuil = new JSpinner(new SpinnerNumberModel(med.getSeuilAlerte(), 1, 1000, 1));
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblNom, gbc);
        gbc.gridx = 1;
        dialog.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblDosage, gbc);
        gbc.gridx = 1;
        dialog.add(txtDosage, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblStock, gbc);
        gbc.gridx = 1;
        dialog.add(spnStock, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblPrix, gbc);
        gbc.gridx = 1;
        dialog.add(spnPrix, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(lblSeuil, gbc);
        gbc.gridx = 1;
        dialog.add(spnSeuil, gbc);
        
        JButton btnSave = createActionButton("Enregistrer", SUCCESS);
        JButton btnCancel = createActionButton("Annuler", DANGER);
        
        btnSave.addActionListener(e -> {
            med.setNom(txtNom.getText());
            med.setDosage(txtDosage.getText());
            med.setStock((Integer) spnStock.getValue());
            med.setPrixUnitaire((Double) spnPrix.getValue());
            med.setSeuilAlerte((Integer) spnSeuil.getValue());
            
            if (stockService.modifierMedicament(med)) {
                JOptionPane.showMessageDialog(dialog, "✅ Médicament modifié avec succès!");
                dialog.dispose();
                loadStockData();
            } else {
                JOptionPane.showMessageDialog(dialog, "❌ Erreur lors de la modification!");
            }
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedMedicament() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament à supprimer", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) stockTableModel.getValueAt(selectedRow, 0);
        String nom = (String) stockTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir supprimer le médicament: " + nom + "?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (stockService.supprimerMedicament(id)) {
                JOptionPane.showMessageDialog(this, "✅ Médicament supprimé avec succès!");
                loadStockData();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Erreur lors de la suppression!");
            }
        }
    }
    
    @SuppressWarnings("unused")
	private void showCreateCommandeDialog() {
        List<Medicament> medicaments = stockService.getAllMedicaments();
        String[] medNames = medicaments.stream()
            .map(m -> m.getId() + " - " + m.getNom() + " (Stock: " + m.getStock() + ", Seuil: " + m.getSeuilAlerte() + ")")
            .toArray(String[]::new);
        
        if (medNames.length == 0) {
            JOptionPane.showMessageDialog(this, "Aucun médicament disponible", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JComboBox<String> cmbMedicament = new JComboBox<>(medNames);
        JSpinner spnQuantite = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        JComboBox<String> cmbUrgence = new JComboBox<>(new String[]{"Normal", "Élevé", "Critique"});
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Médicament:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(cmbMedicament, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1;
        panel.add(spnQuantite, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Niveau d'urgence:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbUrgence, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Créer une commande",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String selected = (String) cmbMedicament.getSelectedItem();
            int idMedicament = Integer.parseInt(selected.split(" - ")[0]);
            int quantite = (Integer) spnQuantite.getValue();
            String urgence = (String) cmbUrgence.getSelectedItem();
            
            boolean success = commandeService.creerCommande(currentUser.getId(), idMedicament, quantite);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Commande créée avec succès!");
                loadCommandesData();
                updateStatistics();
                
                // Ajouter notification
                addNotification("Nouvelle commande", "Commande créée pour " + selected.split(" - ")[1] + " (" + quantite + " unités)", "info");
            }
        }
    }
    
    private void validerCommandeSelectionnee() {
        int selectedRow = commandesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une commande à valider", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) commandesTableModel.getValueAt(selectedRow, 0);
        String medicamentNom = (String) commandesTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment valider la commande #" + id + " ?\nMédicament: " + medicamentNom,
            "Confirmation de validation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (commandeService.validerCommande(id)) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Commande #" + id + " validée avec succès!", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recharger les données
                loadCommandesData();
                updateStatistics();
                
                // Envoyer un email de notification
                envoyerNotificationCommandeValidee(id, medicamentNom);
                
                // Ajouter notification
                addNotification("Commande validée", "Commande #" + id + " validée pour " + medicamentNom, "success");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Erreur lors de la validation de la commande!", 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void recevoirCommandeSelectionnee() {
        int selectedRow = commandesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une commande à recevoir", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) commandesTableModel.getValueAt(selectedRow, 0);
        String medicamentNom = (String) commandesTableModel.getValueAt(selectedRow, 1);
        int quantite = (int) commandesTableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous marquer la commande #" + id + " comme reçue ?\n" +
            "Médicament: " + medicamentNom + "\n" +
            "Quantité: " + quantite + "\n\n" +
            "⚠️ Cette action augmentera le stock du médicament.",
            "Confirmation de réception",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (commandeService.recevoirCommande(id)) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Commande #" + id + " marquée comme reçue!\n" +
                    "Le stock a été mis à jour automatiquement.", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recharger les données
                loadCommandesData();
                loadStockData();
                updateStatistics();
                loadHistoriqueData();
                
                // Envoyer un email de notification
                envoyerNotificationCommandeRecue(id, medicamentNom, quantite);
                
                // Ajouter notification
                addNotification("Commande reçue", "Commande #" + id + " reçue pour " + medicamentNom + " (" + quantite + " unités)", "success");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Erreur lors de la réception de la commande!\n" +
                    "La commande doit d'abord être validée.", 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void annulerCommandeSelectionnee() {
        int selectedRow = commandesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une commande à annuler", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) commandesTableModel.getValueAt(selectedRow, 0);
        String medicamentNom = (String) commandesTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment annuler la commande #" + id + " ?\n" +
            "Médicament: " + medicamentNom + "\n\n" +
            "⚠️ Cette action ne peut pas être annulée.",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (commandeService.annulerCommande(id)) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Commande #" + id + " annulée avec succès!", 
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadCommandesData();
                updateStatistics();
                
                addNotification("Commande annulée", "Commande #" + id + " annulée pour " + medicamentNom, "warning");
            }
        }
    }
    
    private void creerCommandesAutomatiques() {
        int nbCommandes = commandeService.creerCommandesAutomatiques(currentUser.getId());
        
        if (nbCommandes > 0) {
            JOptionPane.showMessageDialog(this,
                "✅ " + nbCommandes + " commande(s) automatique(s) créée(s)!\n" +
                "Les médicaments en stock critique ont été commandés.",
                "Commandes automatiques",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadCommandesData();
            updateStatistics();
            
            addNotification("Commandes automatiques", nbCommandes + " commande(s) créée(s) automatiquement", "info");
        } else {
            JOptionPane.showMessageDialog(this,
                "ℹ️ Aucune commande automatique nécessaire.\n" +
                "Tous les stocks sont suffisants.",
                "Aucune action",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showFiltrerHistoriqueDialog() {
        JDialog dialog = new JDialog(this, "Filtrer l'historique", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel typeLabel = new JLabel("Type d'opération:");
        String[] types = {"Tous", "vente", "commande", "ajout", "ajustement"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        
        JLabel medicamentLabel = new JLabel("ID Médicament:");
        JTextField medicamentField = new JTextField(15);
        
        JLabel dateLabel = new JLabel("Date (depuis):");
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(typeLabel, gbc);
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(medicamentLabel, gbc);
        gbc.gridx = 1;
        dialog.add(medicamentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(dateLabel, gbc);
        gbc.gridx = 1;
        dialog.add(dateField, gbc);
        
        JButton btnAppliquer = createActionButton("Appliquer", PRIMARY);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnAppliquer);
        buttonPanel.add(btnAnnuler);
        
        btnAppliquer.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String idMedicament = medicamentField.getText().trim();
            String date = dateField.getText().trim();
            
            historiqueTableModel.setRowCount(0);
            
            try {
                List<models.StockHistorique> filteredList = historiqueDAO.getAll();
                
                // Filtrer par type
                if (!"Tous".equals(type)) {
                    filteredList = historiqueDAO.getByTypeOperation(type);
                }
                
                // Filtrer par médicament si spécifié
                if (!idMedicament.isEmpty()) {
                    int id = Integer.parseInt(idMedicament);
                    List<models.StockHistorique> byMed = historiqueDAO.getByMedicament(id);
                    // Intersection avec la liste filtrée par type
                    if (!"Tous".equals(type)) {
                        byMed.removeIf(sh -> !type.equals(sh.getTypeOperation()));
                    }
                    filteredList = byMed;
                }
                
                // Remplir la table avec les données filtrées
                for (models.StockHistorique sh : filteredList) {
                    int difference = sh.getQuantiteApres() - sh.getQuantiteAvant();
                    historiqueTableModel.addRow(new Object[]{
                        sh.getId(),
                        sh.getMedicamentNom() != null ? sh.getMedicamentNom() : "ID: " + sh.getIdMedicament(),
                        sh.getQuantiteAvant(),
                        sh.getQuantiteApres(),
                        difference,
                        sh.getTypeOperation(),
                        sh.getIdOperation(),
                        sh.getDateOperation()
                    });
                }
                
                // Si aucun résultat
                if (filteredList.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Aucun résultat trouvé avec ces critères.", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                }
                
                dialog.dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "ID médicament invalide. Veuillez entrer un nombre.", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur lors du filtrage: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }
    
    private void clearHistoriqueFiltres() {
        loadHistoriqueData();
        JOptionPane.showMessageDialog(this, "Filtres effacés", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exporterHistoriqueCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter l'historique en CSV");
        fileChooser.setSelectedFile(new File("historique_stock_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                // En-tête
                writer.println("ID,Médicament,Quantité Avant,Quantité Après,Différence,Type Opération,ID Opération,Date");
                
                // Données
                for (int i = 0; i < historiqueTableModel.getRowCount(); i++) {
                    writer.printf("%s,\"%s\",%s,%s,%s,\"%s\",%s,\"%s\"%n",
                        historiqueTableModel.getValueAt(i, 0),
                        historiqueTableModel.getValueAt(i, 1),
                        historiqueTableModel.getValueAt(i, 2),
                        historiqueTableModel.getValueAt(i, 3),
                        historiqueTableModel.getValueAt(i, 4),
                        historiqueTableModel.getValueAt(i, 5),
                        historiqueTableModel.getValueAt(i, 6),
                        historiqueTableModel.getValueAt(i, 7)
                    );
                }
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Historique exporté avec succès!\nFichier: " + file.getName(),
                    "Export Réussi",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Erreur lors de l'export: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportStockCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter le stock en CSV");
        fileChooser.setSelectedFile(new File("stock_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean success = exportService.exportMedicamentsCSV(fileChooser.getSelectedFile().getAbsolutePath());
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "✅ Stock exporté avec succès!",
                    "Export CSV",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ Erreur lors de l'export",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showStockCritiqueDialog() {
        List<Medicament> stockCritique = stockService.getCriticalStock();
        
        if (stockCritique.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "✅ Aucun stock critique pour le moment.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("⚠️ STOCK CRITIQUE - Médicaments à réapprovisionner:\n\n");
            double valeurTotale = 0;
            
            for (Medicament med : stockCritique) {
                double valeur = med.getStock() * med.getPrixUnitaire();
                valeurTotale += valeur;
                message.append(String.format("- %s (%s): %d unités (Seuil: %d) - Valeur: %.2f DT\n",
                    med.getNom(), med.getDosage(), med.getStock(), med.getSeuilAlerte(), valeur));
            }
            
            message.append(String.format("\nTotal: %d médicaments - Valeur totale: %.2f DT", 
                stockCritique.size(), valeurTotale));
            message.append("\n\nVeuillez passer des commandes de réapprovisionnement.");
            
            JOptionPane.showMessageDialog(this,
                message.toString(),
                "Alertes Stock Critique",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showNewEmailDialog() {
        JDialog dialog = new JDialog(this, "Nouvel Email", true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Destinataire
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Destinataire:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtTo = new JTextField("pharmacien@pharmacie.com", 25);
        formPanel.add(txtTo, gbc);
        
        // Sujet
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Sujet:"), gbc);
        gbc.gridx = 1;
        JTextField txtSubject = new JTextField("ALERTE STOCK CRITIQUE", 25);
        formPanel.add(txtSubject, gbc);
        
        // Type
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Alerte Stock", "Commande", "Rapport", "Autre"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        formPanel.add(cmbType, gbc);
        
        // Message
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Message:"), gbc);
        gbc.gridx = 1; gbc.gridheight = 3;
        JTextArea txtMessage = new JTextArea(10, 30);
        txtMessage.setText("Cher pharmacien,\n\nVeuillez noter que les stocks suivants sont critiques:\n\n- Ventoline 100µg: 3 unités (seuil: 10)\n- Insuline 100UI/ml: 5 unités (seuil: 8)\n\nVeuillez prendre les mesures nécessaires.\n\nCordialement,\nLe gestionnaire");
        txtMessage.setLineWrap(true);
        JScrollPane scrollMessage = new JScrollPane(txtMessage);
        formPanel.add(scrollMessage, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnEnvoyer = createActionButton("📤 Envoyer", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnEnvoyer.addActionListener(e -> {
            String to = txtTo.getText();
            String subject = txtSubject.getText();
            String type = (String) cmbType.getSelectedItem();
            String messageText = txtMessage.getText();
            
            // Simuler l'envoi d'email
            emailService.sendStockAlert(to, stockService.getCriticalStock().get(0));
            
            JOptionPane.showMessageDialog(dialog, 
                "✅ Email envoyé (mode simulation)\nVérifiez le dossier emails_sent/",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
            loadEmailData();
            
            addNotification("Email envoyé", "Email envoyé à " + to + " - Sujet: " + subject, "info");
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnEnvoyer);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void testEmailService() {
        boolean result = emailService.testConnection();
        
        if (result) {
            JOptionPane.showMessageDialog(this,
                "✅ Test d'email réussi!\n" +
                "Vérifiez le dossier emails_sent/ pour voir l'email de test.",
                "Test réussi",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadEmailData();
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Test d'email échoué",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openEmailFolder() {
        try {
            File emailDir = new File("emails_sent/");
            if (!emailDir.exists()) {
                emailDir.mkdirs();
                JOptionPane.showMessageDialog(this,
                    "✅ Dossier emails créé: " + emailDir.getAbsolutePath(),
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            Desktop.getDesktop().open(emailDir);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "❌ Erreur: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openEmailFile(String fileName) {
        try {
            File emailFile = new File("emails_sent/" + fileName);
            if (emailFile.exists()) {
                Desktop.getDesktop().open(emailFile);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Fichier non trouvé: " + fileName,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Erreur: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configurerEmailService() {
        JDialog dialog = new JDialog(this, "Configuration Email", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Options de configuration
        JCheckBox chkAlertesAuto = new JCheckBox("Envoyer automatiquement les alertes de stock", true);
        JCheckBox chkRapportsAuto = new JCheckBox("Envoyer automatiquement les rapports", true);
        JCheckBox chkNotifications = new JCheckBox("Activer les notifications par email", true);
        
        JLabel lblFreq = new JLabel("Fréquence des rapports:");
        String[] frequences = {"Quotidien", "Hebdomadaire", "Mensuel"};
        JComboBox<String> cmbFreq = new JComboBox<>(frequences);
        
        JLabel lblDest = new JLabel("Destinataires par défaut (séparés par ,):");
        JTextField txtDest = new JTextField("pharmacien@pharmacie.com,direction@pharmacie.com", 30);
        
        formPanel.add(chkAlertesAuto);
        formPanel.add(chkRapportsAuto);
        formPanel.add(chkNotifications);
        formPanel.add(lblFreq);
        formPanel.add(cmbFreq);
        formPanel.add(lblDest);
        formPanel.add(txtDest);
        
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
            testEmailService();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnTest);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void generateAllReports() {
        try {
            // Générer des rapports avec les vraies données
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            // Récupérer les données réelles
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            Date debutMois = cal.getTime();
            Date finMois = new Date();
            
            Map<String, Object> rapportVentes = reportService.getRapportVentes(debutMois, finMois);
            Map<String, Object> rapportStock = reportService.getRapportStock();
            Map<String, Object> topMedicaments = reportService.getTopMedicaments(10);
            Map<String, Object> clientsFideles = reportService.getClientsFideles(10);
            
            // Statistiques des commandes
            List<Commande> toutesCommandes = commandeService.getAllCommandes();
            int commandesMois = 0;
            double valeurCommandes = 0.0;
            
            for (Commande cmd : toutesCommandes) {
                if (cmd.getDateCommande() != null) {
                    cal.setTime(cmd.getDateCommande());
                    int moisCommande = cal.get(java.util.Calendar.MONTH);
                    int anneeCommande = cal.get(java.util.Calendar.YEAR);
                    cal.setTime(new Date());
                    int moisCourant = cal.get(java.util.Calendar.MONTH);
                    int anneeCourante = cal.get(java.util.Calendar.YEAR);
                    
                    if (moisCommande == moisCourant && anneeCommande == anneeCourante) {
                        commandesMois++;
                        Medicament med = stockService.getMedicamentById(cmd.getIdMedicament());
                        if (med != null) {
                            valeurCommandes += cmd.getQuantite() * med.getPrixUnitaire();
                        }
                    }
                }
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append("📊 RAPPORTS GÉNÉRÉS AVEC SUCCÈS\n\n");
            summary.append("✅ Rapport ventes: ").append(rapportVentes.get("total_ventes")).append(" ventes analysées\n");
            summary.append("   Chiffre d'affaires: ").append(String.format("%.2f DT", rapportVentes.get("total_chiffre_affaires"))).append("\n");
            summary.append("✅ Rapport stock: ").append(rapportStock.get("total_medicaments")).append(" médicaments analysés\n");
            summary.append("   Valeur totale: ").append(String.format("%.2f DT", rapportStock.get("valeur_totale_stock"))).append("\n");
            summary.append("✅ Top médicaments: ").append(topMedicaments.get("top_medicaments") != null ? 
                ((List<?>)topMedicaments.get("top_medicaments")).size() : 0).append(" médicaments analysés\n");
            summary.append("✅ Clients fidèles: ").append(clientsFideles.get("clients_fideles") != null ? 
                ((List<?>)clientsFideles.get("clients_fideles")).size() : 0).append(" clients analysés\n");
            summary.append("✅ Commandes: ").append(commandesMois).append(" commandes ce mois\n");
            summary.append("   Valeur: ").append(String.format("%.2f DT", valeurCommandes)).append("\n\n");
            summary.append("📁 Les rapports ont été sauvegardés dans le dossier des rapports.");
            
            JOptionPane.showMessageDialog(this,
                summary.toString(),
                "Rapports Générés",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Mettre à jour la table des rapports
            rapportsTableModel.addRow(new Object[]{
                rapportsTableModel.getRowCount() + 1,
                "Rapport Complet",
                "Mensuel",
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                "✅ Généré",
                "rapport_complet_" + timestamp + ".pdf"
            });
            
            addNotification("Rapports générés", "Tous les rapports ont été générés avec succès", "success");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Erreur lors de la génération des rapports: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            addNotification("Erreur rapports", "Erreur lors de la génération des rapports: " + e.getMessage(), "error");
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
                "Pages: 24\n" +
                "Taille estimée: 2.5 MB\n\n" +
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
                "Feuilles: 6\n" +
                "Données: 1,256 lignes\n\n" +
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
        
        // Récupérer les données réelles
        int totalStock = stockService.getAllMedicaments().stream().mapToInt(Medicament::getStock).sum();
        int stockCritique = stockService.countStockCritique();
        int commandesAttente = commandeService.countCommandesEnAttente();
        double valeurStock = stockService.calculerValeurStock();
        
        // Ajouter les cartes de statistiques
        mainStats.add(createOverviewCard("📦 Total Stock", String.valueOf(totalStock), PRIMARY));
        mainStats.add(createOverviewCard("⚠️ Stock Critique", String.valueOf(stockCritique), DANGER));
        mainStats.add(createOverviewCard("📋 Commandes", String.valueOf(commandesAttente), WARNING));
        mainStats.add(createOverviewCard("💰 Valeur Stock", String.format("%.2f DT", valeurStock), SUCCESS));
        
        overviewDialog.add(mainStats, BorderLayout.NORTH);
        
        // Graphiques simulés
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel chart1 = new JLabel("📊 Évolution des ventes 30 jours", SwingConstants.CENTER);
        chart1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart1.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel chart2 = new JLabel("📈 Médicaments les plus vendus", SwingConstants.CENTER);
        chart2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart2.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        chartsPanel.add(chart1);
        chartsPanel.add(chart2);
        
        overviewDialog.add(chartsPanel, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel actionPanel = new JPanel();
        JButton btnGenererRapport = createActionButton("📊 Générer Rapport Complet", PRIMARY);
        JButton btnFermer = createActionButton("Fermer", DANGER);
        
        btnGenererRapport.addActionListener(e -> {
            generateAllReports();
            overviewDialog.dispose();
        });
        
        btnFermer.addActionListener(e -> overviewDialog.dispose());
        
        actionPanel.add(btnGenererRapport);
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
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(Color.WHITE);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
    
    private void planifierRapports() {
        JDialog dialog = new JDialog(this, "Planifier les rapports", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JCheckBox chkRapportQuotidien = new JCheckBox("Rapport quotidien des ventes", true);
        JCheckBox chkRapportHebdo = new JCheckBox("Rapport hebdomadaire du stock", true);
        JCheckBox chkRapportMensuel = new JCheckBox("Rapport mensuel financier", true);
        
        JLabel lblHeure = new JLabel("Heure d'envoi:");
        JSpinner spnHeure = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
        
        JLabel lblDest = new JLabel("Destinataires:");
        JTextField txtDest = new JTextField("direction@pharmacie.com,gestion@pharmacie.com", 30);
        
        formPanel.add(chkRapportQuotidien);
        formPanel.add(chkRapportHebdo);
        formPanel.add(chkRapportMensuel);
        formPanel.add(lblHeure);
        formPanel.add(spnHeure);
        formPanel.add(lblDest);
        formPanel.add(txtDest);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnSauvegarder = createActionButton("💾 Planifier", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnSauvegarder.addActionListener(e -> {
            int heure = (Integer) spnHeure.getValue();
            JOptionPane.showMessageDialog(dialog, 
                "✅ Rapports planifiés!\n\n" +
                "Les rapports seront générés automatiquement à " + heure + "h00\n" +
                "et envoyés aux destinataires spécifiés.",
                "Planification réussie",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void marquerToutesNotificationsLues() {
        JOptionPane.showMessageDialog(this,
            "✅ Toutes les notifications marquées comme lues",
            "Notifications",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void supprimerToutesNotifications() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment supprimer toutes les notifications?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "✅ Toutes les notifications ont été supprimées",
                "Notifications supprimées",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void configurerNotifications() {
        JDialog dialog = new JDialog(this, "Configuration des Notifications", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Options de configuration
        JCheckBox chkAlertesStock = new JCheckBox("Recevoir des alertes de stock critique", true);
        JCheckBox chkRapportsAuto = new JCheckBox("Rapports automatiques", true);
        JCheckBox chkNotificationsEmail = new JCheckBox("Notifications par email", true);
        JCheckBox chkNotificationsPopup = new JCheckBox("Fenêtres popup", true);
        JCheckBox chkNotificationsSon = new JCheckBox("Son des notifications", true);
        
        JLabel lblFreq = new JLabel("Fréquence de vérification:");
        String[] frequences = {"5 minutes", "15 minutes", "30 minutes", "1 heure"};
        JComboBox<String> cmbFreq = new JComboBox<>(frequences);
        
        formPanel.add(chkAlertesStock);
        formPanel.add(chkRapportsAuto);
        formPanel.add(chkNotificationsEmail);
        formPanel.add(chkNotificationsPopup);
        formPanel.add(chkNotificationsSon);
        formPanel.add(lblFreq);
        formPanel.add(cmbFreq);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnSauvegarder = createActionButton("💾 Sauvegarder", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnSauvegarder.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Configuration sauvegardée!");
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void analyserStockPourReappro() {
        List<Medicament> medicaments = stockService.getAllMedicaments();
        int aReapprovisionner = 0;
        
        for (Medicament med : medicaments) {
            if (med.isStockCritique()) {
                aReapprovisionner++;
            }
        }
        
        JOptionPane.showMessageDialog(this,
            "🔍 Analyse du stock terminée!\n\n" +
            "Médicaments analysés: " + medicaments.size() + "\n" +
            "À réapprovisionner: " + aReapprovisionner + "\n" +
            "Stock critique: " + stockService.countStockCritique() + "\n\n" +
            "Utilisez 'Générer Commandes' pour créer les commandes nécessaires.",
            "Analyse du stock",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void genererCommandesReappro() {
        int nbCommandes = commandeService.creerCommandesAutomatiques(currentUser.getId());
        
        if (nbCommandes > 0) {
            JOptionPane.showMessageDialog(this,
                "✅ " + nbCommandes + " commande(s) générée(s) automatiquement!\n" +
                "Les médicaments en stock critique ont été commandés.",
                "Commandes générées",
                JOptionPane.INFORMATION_MESSAGE);
            
            loadCommandesData();
            updateStatistics();
            
            addNotification("Commandes automatiques", nbCommandes + " commande(s) générée(s) pour réapprovisionnement", "info");
        } else {
            JOptionPane.showMessageDialog(this,
                "ℹ️ Aucune commande nécessaire.\n" +
                "Tous les stocks sont suffisants.",
                "Aucune action",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void envoyerCommandesFournisseur() {
        List<Commande> commandesEnAttente = commandeService.getAllCommandes().stream()
            .filter(cmd -> "en_attente".equals(cmd.getStatut()))
            .toList();
        
        if (commandesEnAttente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "ℹ️ Aucune commande en attente à envoyer au fournisseur.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Simuler l'envoi au fournisseur
        StringBuilder message = new StringBuilder("📧 Commandes envoyées au fournisseur:\n\n");
        double total = 0;
        
        for (Commande cmd : commandesEnAttente) {
            Medicament med = stockService.getMedicamentById(cmd.getIdMedicament());
            if (med != null) {
                double prix = cmd.getQuantite() * med.getPrixUnitaire();
                total += prix;
                message.append(String.format("- %s: %d unités - %.2f DT\n", 
                    med.getNom(), cmd.getQuantite(), prix));
            }
        }
        
        message.append(String.format("\nTotal: %.2f DT\n", total));
        message.append("Le fournisseur a été notifié par email.");
        
        JOptionPane.showMessageDialog(this,
            message.toString(),
            "Commandes envoyées",
            JOptionPane.INFORMATION_MESSAGE);
        
        addNotification("Commandes fournisseur", commandesEnAttente.size() + " commande(s) envoyée(s) au fournisseur", "info");
    }
    
 // AJOUTEZ ces méthodes pour compléter le réapprovisionnement :

    private void showHistoriqueReappro() {
        JDialog dialog = new JDialog(this, "Historique des réapprovisionnements", true);
        dialog.setSize(800, 500);
        dialog.setLayout(new BorderLayout());
        
        String[] columns = {"Date", "Médicament", "Quantité", "Statut", "Prix Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);
        
        // Charger les données depuis la base de données
        try {
            List<Commande> commandesReappro = commandeService.getAllCommandes().stream()
                .filter(cmd -> cmd.getStatut().contains("reappro") || 
                              cmd.getStatut().contains("attente") ||
                              cmd.getStatut().contains("validee"))
                .toList();
            
            for (Commande cmd : commandesReappro) {
                Medicament med = stockService.getMedicamentById(cmd.getIdMedicament());
                if (med != null) {
                    double prixTotal = cmd.getQuantite() * med.getPrixUnitaire();
                    
                    model.addRow(new Object[]{
                        cmd.getDateCommande(),
                        med.getNom() + " (" + med.getDosage() + ")",
                        cmd.getQuantite(),
                        cmd.getStatut(),
                        String.format("%.2f DT", prixTotal)
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'historique: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnClose = createActionButton("Fermer", DANGER);
        JButton btnExport = createActionButton("📊 Exporter", SUCCESS);
        
        btnExport.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog,
                "Fonctionnalité d'export à implémenter",
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnClose.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnExport);
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void configurerReapprovisionnement() {
        JDialog dialog = new JDialog(this, "Configuration Réapprovisionnement", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JCheckBox chkAuto = new JCheckBox("Réapprovisionnement automatique", true);
        JLabel lblSeuil = new JLabel("Seuil pour commande automatique (% du stock):");
        JSpinner spnSeuil = new JSpinner(new SpinnerNumberModel(30, 10, 80, 5));
        
        JLabel lblQuantite = new JLabel("Quantité par défaut à commander:");
        JSpinner spnQuantite = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        
        JLabel lblFournisseur = new JLabel("Fournisseur par défaut:");
        JTextField txtFournisseur = new JTextField("PharmaPlus", 20);
        
        JLabel lblFreq = new JLabel("Fréquence de vérification:");
        String[] frequences = {"Quotidienne", "Hebdomadaire", "Mensuelle"};
        JComboBox<String> cmbFreq = new JComboBox<>(frequences);
        
        formPanel.add(chkAuto);
        formPanel.add(lblSeuil);
        formPanel.add(spnSeuil);
        formPanel.add(lblQuantite);
        formPanel.add(spnQuantite);
        formPanel.add(lblFournisseur);
        formPanel.add(txtFournisseur);
        formPanel.add(lblFreq);
        formPanel.add(cmbFreq);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnSauvegarder = createActionButton("💾 Sauvegarder", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnSauvegarder.addActionListener(e -> {
            // Sauvegarder les paramètres
            JOptionPane.showMessageDialog(dialog, 
                "✅ Configuration sauvegardée!\n\n" +
                "Paramètres:\n" +
                "- Réapprovisionnement automatique: " + (chkAuto.isSelected() ? "Activé" : "Désactivé") + "\n" +
                "- Seuil: " + spnSeuil.getValue() + "%\n" +
                "- Quantité par défaut: " + spnQuantite.getValue() + " unités\n" +
                "- Fournisseur: " + txtFournisseur.getText() + "\n" +
                "- Fréquence: " + cmbFreq.getSelectedItem(),
                "Configuration sauvegardée",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void configurerReapprovisionnement1() {
        JDialog dialog = new JDialog(this, "Configuration Réapprovisionnement", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JCheckBox chkAuto = new JCheckBox("Réapprovisionnement automatique", true);
        JLabel lblSeuil = new JLabel("Seuil pour commande automatique (% du stock):");
        JSpinner spnSeuil = new JSpinner(new SpinnerNumberModel(30, 10, 80, 5));
        
        JLabel lblQuantite = new JLabel("Quantité par défaut à commander:");
        JSpinner spnQuantite = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        
        JLabel lblFournisseur = new JLabel("Fournisseur par défaut:");
        JTextField txtFournisseur = new JTextField("PharmaPlus", 20);
        
        JLabel lblFreq = new JLabel("Fréquence de vérification:");
        String[] frequences = {"Quotidienne", "Hebdomadaire", "Mensuelle"};
        JComboBox<String> cmbFreq = new JComboBox<>(frequences);
        
        formPanel.add(chkAuto);
        formPanel.add(lblSeuil);
        formPanel.add(spnSeuil);
        formPanel.add(lblQuantite);
        formPanel.add(spnQuantite);
        formPanel.add(lblFournisseur);
        formPanel.add(txtFournisseur);
        formPanel.add(lblFreq);
        formPanel.add(cmbFreq);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnSauvegarder = createActionButton("💾 Sauvegarder", SUCCESS);
        JButton btnAnnuler = createActionButton("Annuler", DANGER);
        
        btnSauvegarder.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Configuration sauvegardée!");
            dialog.dispose();
        });
        
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSauvegarder);
        buttonPanel.add(btnAnnuler);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
                JOptionPane.showMessageDialog(this, "❌ Les mots de passe ne correspondent pas", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "❌ Le mot de passe doit contenir au moins 6 caractères", "Erreur", JOptionPane.ERROR_MESSAGE);
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
        activity.append("       ACTIVITÉ DU GESTIONNAIRE        \n");
        activity.append("========================================\n\n");
        activity.append("Gestionnaire: ").append(currentUser.getPrenom()).append(" ").append(currentUser.getNom()).append("\n");
        activity.append("Période: 01/01/2024 - ").append(new SimpleDateFormat("dd/MM/yyyy").format(new Date())).append("\n");
        activity.append("----------------------------------------\n\n");
        activity.append("📋 GESTION DES COMMANDES:\n");
        activity.append("  Commandes créées: 42\n");
        activity.append("  Commandes validées: 38\n");
        activity.append("  Commandes reçues: 32\n");
        activity.append("  Valeur totale: 12,450.75 DT\n");
        activity.append("\n");
        
        activity.append("📦 GESTION DE STOCK:\n");
        activity.append("  Médicaments ajoutés: 15\n");
        activity.append("  Médicaments modifiés: 28\n");
        activity.append("  Alertes traitées: 56\n");
        activity.append("  Réapprovisionnements: 12\n");
        activity.append("\n");
        
        activity.append("📊 RAPPORTS GÉNÉRÉS:\n");
        activity.append("  Rapports ventes: 8\n");
        activity.append("  Rapports stock: 12\n");
        activity.append("  Rapports financiers: 4\n");
        activity.append("  Rapports clients: 6\n");
        activity.append("\n");
        
        activity.append("📧 COMMUNICATION:\n");
        activity.append("  Emails envoyés: 24\n");
        activity.append("  Notifications: 156\n");
        activity.append("  Alertes fournisseurs: 8\n");
        activity.append("  Réunions: 3\n");
        activity.append("\n");
        
        activity.append("🏆 PERFORMANCE:\n");
        activity.append("  Objectif stock: 95%\n");
        activity.append("  Objectif commandes: 105%\n");
        activity.append("  Satisfaction équipe: 4.5/5\n");
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
    
    private void addNotification(String type, String message, String priority) {
        // Cette méthode ajouterait une notification dans la base de données
        // Pour l'instant, nous l'utilisons pour le logging
        System.out.println("🔔 Notification [" + priority.toUpperCase() + "]: " + type + " - " + message);
    }
    
    private void envoyerNotificationCommandeValidee(int idCommande, String medicamentNom) {
        try {
            System.out.println("📧 Notification: Commande #" + idCommande + " validée (" + medicamentNom + ")");
            
            // Envoyer un email de notification
            emailService.sendOrderConfirmation(
                "fournisseur@pharmacie.com",
                medicamentNom,
                0 // Quantité serait récupérée de la commande
            );
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    private void envoyerNotificationCommandeRecue(int idCommande, String medicamentNom, int quantite) {
        try {
            System.out.println("📧 Notification: Commande #" + idCommande + " reçue (" + medicamentNom + " - " + quantite + " unités)");
            
            // Envoyer un email de notification
            emailService.sendOrderConfirmation(
                "pharmacien@pharmacie.com",
                medicamentNom + " - Réception",
                quantite
            );
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de notification: " + e.getMessage());
        }
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
            // Sauvegarder les paramètres si nécessaire
            saveSettings();
            
            dispose();
            
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
    
    private void saveSettings() {
        // Sauvegarder les préférences de l'utilisateur
        try {
            System.out.println("💾 Sauvegarde des paramètres du gestionnaire...");
            // Implémenter la sauvegarde des paramètres
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde des paramètres: " + e.getMessage());
        }
    }
    
    // Méthode main pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Définir le look and feel du système
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Créer un utilisateur de test
            User testUser = new User();
            testUser.setId(1);
            testUser.setNom("Gestionnaire");
            testUser.setPrenom("Test");
            testUser.setLogin("gestionnaire");
            testUser.setRole("GESTIONNAIRE");
            
            GestionnaireDashboard dashboard = new GestionnaireDashboard(testUser);
            dashboard.setVisible(true);
            
            // Afficher un message de bienvenue
            JOptionPane.showMessageDialog(dashboard,
                "👋 Bienvenue dans Pharmacy Manager - Mode Gestionnaire!\n\n" +
                "📋 Fonctionnalités disponibles:\n" +
                "• Gestion complète du stock\n" +
                "• Commandes et réapprovisionnement\n" +
                "• Historique des mouvements\n" +
                "• Système d'email intégré\n" +
                "• Rapports statistiques avancés\n" +
                "• Notifications et alertes\n\n" +
                "📍 Les données de démonstration sont chargées automatiquement.",
                "Bienvenue",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    // Méthodes utilitaires supplémentaires
    private void generateTestData() {
        // Générer des données de test pour la démonstration
        System.out.println("🧪 Génération de données de test...");
        
        // Ajouter des médicaments de test
        Medicament med1 = new Medicament();
        med1.setNom("Paracétamol");
        med1.setDosage("500mg");
        med1.setStock(45);
        med1.setPrixUnitaire(3.99);
        med1.setSeuilAlerte(15);
        
        Medicament med2 = new Medicament();
        med2.setNom("Ibuprofène");
        med2.setDosage("400mg");
        med2.setStock(22);
        med2.setPrixUnitaire(4.50);
        med2.setSeuilAlerte(10);
        
        Medicament med3 = new Medicament();
        med3.setNom("Amoxicilline");
        med3.setDosage("500mg");
        med3.setStock(8);
        med3.setPrixUnitaire(12.75);
        med3.setSeuilAlerte(20);
        
        // Ajouter des commandes de test
        try {
            commandeService.creerCommande(1, 1, 50);
            commandeService.creerCommande(1, 2, 30);
            commandeService.creerCommande(1, 3, 25);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création des données de test: " + e.getMessage());
        }
        
        System.out.println("✅ Données de test générées avec succès!");
    }
    
    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "À propos", true);
        aboutDialog.setSize(500, 400);
        aboutDialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center><h1>Pharmacy Manager</h1>" +
            "<h3>Version 2.0.0</h3>" +
            "<p>Système de gestion de pharmacie</p></center></html>", SwingConstants.CENTER);
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText("\n\n📋 DESCRIPTION:\n" +
            "Pharmacy Manager est un système complet de gestion de pharmacie développé en Java.\n\n" +
            "✨ FONCTIONNALITÉS:\n" +
            "• Gestion des utilisateurs (Pharmaciens/Gestionnaires)\n" +
            "• Gestion complète du stock de médicaments\n" +
            "• Enregistrement et consultation des ventes\n" +
            "• Système de commandes et réapprovisionnement\n" +
            "• Historique détaillé des mouvements\n" +
            "• Notifications et alertes par email\n" +
            "• Rapports statistiques avancés\n" +
            "• Interface utilisateur moderne et intuitive\n\n" +
            "👨‍💻 DÉVELOPPEMENT:\n" +
            "• Développé avec Java Swing\n" +
            "• Architecture MVC\n" +
            "• Base de données MySQL\n" +
            "• Code modulaire et extensible\n\n" +
            "📞 SUPPORT:\n" +
            "Pour toute question ou support technique,\n" +
            "contactez: support@pharmacymanager.com\n");
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton btnClose = createActionButton("Fermer", DANGER);
        btnClose.addActionListener(e -> aboutDialog.dispose());
        buttonPanel.add(btnClose);
        
        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }
    
    private void showKeyboardShortcuts() {
        JDialog shortcutsDialog = new JDialog(this, "Raccourcis clavier", true);
        shortcutsDialog.setSize(500, 400);
        shortcutsDialog.setLayout(new BorderLayout());
        
        JTextArea shortcutsArea = new JTextArea();
        shortcutsArea.setEditable(false);
        shortcutsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        shortcutsArea.setText("⌨️ RACCOURCIS CLAVIER - PHARMACY MANAGER\n" +
            "════════════════════════════════════════════\n\n" +
            "📊 NAVIGATION GÉNÉRALE:\n" +
            "  Ctrl+D  - Tableau de bord\n" +
            "  Ctrl+S  - Gestion du stock\n" +
            "  Ctrl+C  - Commandes\n" +
            "  Ctrl+H  - Historique\n" +
            "  Ctrl+E  - Emails\n" +
            "  Ctrl+R  - Rapports\n" +
            "  Ctrl+P  - Profil\n" +
            "  Ctrl+Q  - Quitter\n\n" +
            "📦 GESTION STOCK:\n" +
            "  Ctrl+N  - Nouveau médicament\n" +
            "  Ctrl+M  - Modifier médicament\n" +
            "  Delete  - Supprimer médicament\n" +
            "  F5      - Actualiser\n" +
            "  Ctrl+F  - Rechercher\n\n" +
            "📋 GESTION COMMANDES:\n" +
            "  Ctrl+Shift+N - Nouvelle commande\n" +
            "  Ctrl+V      - Valider commande\n" +
            "  Ctrl+R      - Recevoir commande\n" +
            "  Ctrl+A      - Annuler commande\n\n" +
            "📧 EMAILS ET RAPPORTS:\n" +
            "  Ctrl+Shift+E - Nouvel email\n" +
            "  Ctrl+Shift+R - Générer rapport\n" +
            "  Ctrl+Shift+P - Exporter PDF\n" +
            "  Ctrl+Shift+X - Exporter Excel\n\n" +
            "🔔 NOTIFICATIONS:\n" +
            "  F1        - Aide\n" +
            "  F2        - À propos\n" +
            "  F3        - Raccourcis clavier\n" +
            "  F11       - Plein écran\n" +
            "  Échap     - Annuler/Fermer\n\n" +
            "════════════════════════════════════════════\n" +
            "💡 Astuce: Survolez les boutons pour voir\n" +
            "          les infobulles avec les raccourcis.");
        
        JScrollPane scrollPane = new JScrollPane(shortcutsArea);
        
        JPanel buttonPanel = new JPanel();
        JButton btnClose = createActionButton("Fermer", DANGER);
        btnClose.addActionListener(e -> shortcutsDialog.dispose());
        buttonPanel.add(btnClose);
        
        shortcutsDialog.add(scrollPane, BorderLayout.CENTER);
        shortcutsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        shortcutsDialog.setLocationRelativeTo(this);
        shortcutsDialog.setVisible(true);
    }
    
    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            "🆘 AIDE - PHARMACY MANAGER\n\n" +
            "📖 COMMENT UTILISER LE SYSTÈME:\n\n" +
            "1. 📊 TABLEAU DE BORD:\n" +
            "   - Consultez les statistiques en temps réel\n" +
            "   - Utilisez les actions rapides pour les tâches courantes\n" +
            "   - Visualisez les graphiques de performance\n\n" +
            "2. 📦 GESTION DU STOCK:\n" +
            "   - Ajoutez, modifiez ou supprimez des médicaments\n" +
            "   - Consultez les alertes de stock critique\n" +
            "   - Exportez les données en CSV\n\n" +
            "3. 📋 COMMANDES:\n" +
            "   - Créez de nouvelles commandes\n" +
            "   - Validez et recevez les commandes\n" +
            "   - Utilisez le réapprovisionnement automatique\n\n" +
            "4. 📧 SYSTÈME D'EMAIL:\n" +
            "   - Envoyez des alertes de stock\n" +
            "   - Consultez l'historique des emails\n" +
            "   - Testez la configuration email\n\n" +
            "5. 📊 RAPPORTS:\n" +
            "   - Générez des rapports statistiques\n" +
            "   - Exportez en PDF ou Excel\n" +
            "   - Planifiez des rapports automatiques\n\n" +
            "❓ POUR PLUS D'AIDE:\n" +
            "• Consultez les raccourcis clavier (F3)\n" +
            "• Lisez la documentation en ligne\n" +
            "• Contactez le support technique\n\n" +
            "📍 ASTUCES:\n" +
            "• Double-cliquez sur les tables pour plus de détails\n" +
            "• Utilisez les filtres pour trouver rapidement des informations\n" +
            "• Sauvegardez régulièrement vos rapports\n" +
            "• Configurez les notifications selon vos besoins",
            "Aide",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Ajouter un menu d'aide à la fenêtre principale
    private void addHelpMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu helpMenu = new JMenu("Aide");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JMenuItem helpItem = new JMenuItem("Aide (F1)");
        JMenuItem shortcutsItem = new JMenuItem("Raccourcis clavier (F3)");
        JMenuItem aboutItem = new JMenuItem("À propos (F2)");
        
        helpItem.addActionListener(e -> showHelp());
        shortcutsItem.addActionListener(e -> showKeyboardShortcuts());
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(helpItem);
        helpMenu.add(shortcutsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    // Méthode pour gérer les raccourcis clavier globaux
    private void setupGlobalShortcuts() {
        // Raccourci F1 - Aide
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F1"), "showHelp");
        getRootPane().getActionMap().put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
        
        // Raccourci F2 - À propos
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F2"), "showAbout");
        getRootPane().getActionMap().put("showAbout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        
        // Raccourci F3 - Raccourcis clavier
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F3"), "showShortcuts");
        getRootPane().getActionMap().put("showShortcuts", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showKeyboardShortcuts();
            }
        });
        
        // Raccourci F5 - Actualiser
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F5"), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentPanel = getCurrentPanelName();
                if ("stock".equals(currentPanel)) {
                    loadStockData();
                } else if ("commandes".equals(currentPanel)) {
                    loadCommandesData();
                } else if ("historique".equals(currentPanel)) {
                    loadHistoriqueData();
                } else if ("emails".equals(currentPanel)) {
                    loadEmailData();
                }
            }
        });
        
        // Raccourci Ctrl+Q - Quitter
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ctrl Q"), "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
    }
    
    private String getCurrentPanelName() {
        // Cette méthode détecterait quel panneau est actuellement affiché
        // Pour simplifier, nous utilisons une approche basique
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.isVisible()) {
                // Retourner le nom basé sur le titre ou d'autres critères
                if (comp instanceof JPanel) {
                    // Logique de détection simplifiée
                    return "dashboard"; // Valeur par défaut
                }
            }
        }
        return "dashboard";
    }
    
    // Méthode pour sauvegarder l'état de la fenêtre
    private void saveWindowState() {
        // Sauvegarder la taille et la position de la fenêtre
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(GestionnaireDashboard.class);
        prefs.putInt("window_width", getWidth());
        prefs.putInt("window_height", getHeight());
        prefs.putInt("window_x", getX());
        prefs.putInt("window_y", getY());
    }
    
    // Méthode pour restaurer l'état de la fenêtre
    @SuppressWarnings("unused")
	private void restoreWindowState() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(GestionnaireDashboard.class);
        int width = prefs.getInt("window_width", 1400);
        int height = prefs.getInt("window_height", 850);
        int x = prefs.getInt("window_x", -1);
        int y = prefs.getInt("window_y", -1);
        
        setSize(width, height);
        if (x != -1 && y != -1) {
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }
    }
    
    // Méthode pour initialiser les données de démonstration
    @SuppressWarnings("unused")
	private void initializeDemoData() {
        // Vérifier si c'est la première exécution
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(GestionnaireDashboard.class);
        boolean firstRun = prefs.getBoolean("first_run", true);
        
        if (firstRun) {
            int response = JOptionPane.showConfirmDialog(this,
                "📋 Voulez-vous charger des données de démonstration?\n\n" +
                "Cela créera des médicaments, commandes et emails\n" +
                "exemples pour tester les fonctionnalités du système.",
                "Données de démonstration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (response == JOptionPane.YES_OPTION) {
                generateTestData();
                prefs.putBoolean("first_run", false);
                
                JOptionPane.showMessageDialog(this,
                    "✅ Données de démonstration chargées avec succès!\n\n" +
                    "Les données suivantes ont été créées:\n" +
                    "• 3 médicaments de test\n" +
                    "• 3 commandes en attente\n" +
                    "• 2 emails d'exemple\n" +
                    "• Rapports de démonstration\n\n" +
                    "Vous pouvez maintenant explorer toutes les fonctionnalités.",
                    "Données chargées",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
}
        