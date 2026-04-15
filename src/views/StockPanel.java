package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import models.Medicament;
import dao.MedicamentDAO;

@SuppressWarnings("serial")
public class StockPanel extends JPanel {

    private MedicamentDAO medicamentDAO;
    private JTable tableStock;
    private DefaultTableModel tableModel;
    
    private JLabel lblTotalMedicaments;
    private JLabel lblStockCritique;
    private JLabel lblValeurStock;

    public StockPanel() {
        medicamentDAO = new MedicamentDAO();
        setLayout(new BorderLayout(10, 10));
        initUI();
        loadData();
        updateStats();
    }

    private void initUI() {
        // Panel des statistiques
        JPanel panelStats = new JPanel(new GridLayout(1, 3, 10, 10));
        panelStats.setBorder(BorderFactory.createTitledBorder("Statistiques du Stock"));
        
        lblTotalMedicaments = createStatCard("Total Médicaments", "0", new Color(33, 150, 243));
        lblStockCritique = createStatCard("Stock Critique", "0", new Color(244, 67, 54));
        lblValeurStock = createStatCard("Valeur Totale", "0.00 DT", new Color(76, 175, 80));
        
        panelStats.add(lblTotalMedicaments);
        panelStats.add(lblStockCritique);
        panelStats.add(lblValeurStock);
        
        add(panelStats, BorderLayout.NORTH);
        
        // Tableau du stock
        String[] columns = {"ID", "Nom", "Dosage", "Stock", "Seuil", "Prix Unitaire", "Valeur", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4) return Integer.class;
                if (columnIndex == 5 || columnIndex == 6) return Double.class;
                return String.class;
            }
        };
        
        tableStock = new JTable(tableModel);
        tableStock.setRowHeight(25);
        tableStock.setAutoCreateRowSorter(true);
        
        // Personnaliser l'affichage des lignes selon le stock
        tableStock.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    try {
                        Object stockObj = table.getValueAt(row, 3);
                        Object seuilObj = table.getValueAt(row, 4);
                        
                        if (stockObj != null && seuilObj != null) {
                            int stock = Integer.parseInt(stockObj.toString());
                            int seuil = Integer.parseInt(seuilObj.toString());
                            
                            if (stock <= 0) {
                                c.setBackground(new Color(255, 200, 200)); // Rouge clair (rupture)
                                c.setForeground(Color.red);
                            } else if (stock <= seuil) {
                                c.setBackground(new Color(255, 235, 156)); // Orange clair (critique)
                                c.setForeground(Color.red);
                            } else if (stock <= seuil * 2) {
                                c.setBackground(new Color(255, 255, 200)); // Jaune clair (attention)
                                c.setForeground(Color.DARK_GRAY);
                            } else {
                                c.setBackground(Color.WHITE);
                                c.setForeground(Color.BLACK);
                            }
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs de parsing
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableStock);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des Médicaments en Stock"));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnActualiser = new JButton("Actualiser");
        btnActualiser.addActionListener(e -> {
            loadData();
            updateStats();
        });
        
        JButton btnStockCritique = new JButton("Afficher Stock Critique");
        btnStockCritique.addActionListener(e -> afficherStockCritique());
        
        JButton btnExporter = new JButton("Exporter CSV");
        btnExporter.addActionListener(e -> exporterStock());
        
        panelBoutons.add(btnActualiser);
        panelBoutons.add(btnStockCritique);
        panelBoutons.add(btnExporter);
        
        add(panelBoutons, BorderLayout.SOUTH);
    }
    
    private JLabel createStatCard(String titre, String valeur, Color couleur) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(couleur, 2));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(150, 80));
        
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitre.setForeground(Color.DARK_GRAY);
        
        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 18));
        lblValeur.setForeground(couleur);
        
        panel.add(lblTitre, BorderLayout.NORTH);
        panel.add(lblValeur, BorderLayout.CENTER);
        
        return new JLabel() {
            {
                setLayout(new BorderLayout());
                add(panel, BorderLayout.CENTER);
            }
        };
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Medicament> medicaments = medicamentDAO.getAll();
        
        for (Medicament med : medicaments) {
            double valeur = med.getStock() * med.getPrixUnitaire();
            String statut;
            
            if (med.getStock() <= 0) {
                statut = "RUPTURE";
            } else if (med.getStock() <= med.getSeuilAlerte()) {
                statut = "CRITIQUE";
            } else if (med.getStock() <= med.getSeuilAlerte() * 2) {
                statut = "ATTENTION";
            } else {
                statut = "NORMAL";
            }
            
            Object[] row = {
                med.getId(),
                med.getNom(),
                med.getDosage(),
                med.getStock(),
                med.getSeuilAlerte(),
                String.format("%.2f", med.getPrixUnitaire()),
                String.format("%.2f", valeur),
                statut
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStats() {
        List<Medicament> medicaments = medicamentDAO.getAll();
        
        // Total médicaments
        int total = medicaments.size();
        updateStatCard(lblTotalMedicaments, String.valueOf(total));
        
        // Stock critique
        List<Medicament> critique = medicamentDAO.getStockCritique();
        int nbCritique = critique.size();
        updateStatCard(lblStockCritique, String.valueOf(nbCritique));
        
        // Valeur totale du stock
        double valeurTotale = 0;
        for (Medicament med : medicaments) {
            valeurTotale += med.getStock() * med.getPrixUnitaire();
        }
        updateStatCard(lblValeurStock, String.format("%.2f DT", valeurTotale));
    }
    
    private void updateStatCard(JLabel statCard, String nouvelleValeur) {
        Component panel = statCard.getComponent(0);
        if (panel instanceof JPanel) {
            Component valeurComp = ((JPanel) panel).getComponent(1);
            if (valeurComp instanceof JLabel) {
                ((JLabel) valeurComp).setText(nouvelleValeur);
            }
        }
    }
    
    private void afficherStockCritique() {
        List<Medicament> critique = medicamentDAO.getStockCritique();
        
        if (critique.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "✅ Aucun médicament en stock critique",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder message = new StringBuilder("⚠️ MÉDICAMENTS EN STOCK CRITIQUE ⚠️\n\n");
        for (Medicament med : critique) {
            message.append(String.format("• %s (%s)\n", med.getNom(), med.getDosage()));
            message.append(String.format("  Stock: %d / Seuil: %d (Déficit: %d)\n\n",
                med.getStock(), med.getSeuilAlerte(), med.getSeuilAlerte() - med.getStock()));
        }
        
        JOptionPane.showMessageDialog(this,
            message.toString(),
            "⚠️ Stock Critique",
            JOptionPane.WARNING_MESSAGE);
    }
    
    @SuppressWarnings("unused")
	private void exporterStock() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter le stock en CSV");
        fileChooser.setSelectedFile(new java.io.File("stock_pharmacie.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            // TODO: Implémenter l'export CSV
            JOptionPane.showMessageDialog(this,
                "Fonctionnalité d'export CSV à implémenter",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}