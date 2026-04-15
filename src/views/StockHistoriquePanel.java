package views;

import dao.StockHistoriqueDAO;
import models.StockHistorique;
import services.ExportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

@SuppressWarnings("serial")
public class StockHistoriquePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private StockHistoriqueDAO historiqueDAO;
    private ExportService exportService;

    public StockHistoriquePanel() {
        historiqueDAO = new StockHistoriqueDAO();
        exportService = new ExportService();
        initUI();
        loadStockHistorique();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        // Table model avec toutes les colonnes
        String[] columns = {"ID", "Médicament", "Quantité Avant", "Quantité Après", "Différence", "Type Opération", "ID Opération", "Date"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Définir les types de données pour chaque colonne
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 6) {
                    return Integer.class;
                } else if (columnIndex == 5) {
                    return String.class;
                } else if (columnIndex == 7) {
                    return java.util.Date.class;
                }
                return Object.class;
            }
        };
        table = new JTable(model);
        
        // Personnalisation du tableau
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(33, 150, 243));
        table.setSelectionForeground(Color.WHITE);
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel title = new JLabel("📈 Historique des Mouvements de Stock", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(33, 150, 243));
        titlePanel.add(title, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = createStyledButton("🔄 Actualiser", new Color(33, 150, 243));
        JButton filterBtn = createStyledButton("🔍 Filtrer", new Color(156, 39, 176));
        JButton exportBtn = createStyledButton("📊 Exporter CSV", new Color(76, 175, 80));
        JButton clearBtn = createStyledButton("🧹 Effacer Filtres", new Color(244, 67, 54));
        
        refreshBtn.addActionListener(e -> loadStockHistorique());
        filterBtn.addActionListener(e -> showFilterDialog());
        exportBtn.addActionListener(e -> exportToCSV());
        clearBtn.addActionListener(e -> loadStockHistorique());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(filterBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(clearBtn);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("✅ Différence positive = Augmentation (commande) | ❌ Différence négative = Diminution (vente)");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(infoPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void loadStockHistorique() {
        model.setRowCount(0); // Clear table
        
        try {
            List<StockHistorique> historiqueList = historiqueDAO.getAll();
            
            for (StockHistorique sh : historiqueList) {
                int difference = sh.getQuantiteApres() - sh.getQuantiteAvant();
                Object[] row = {
                    sh.getId(),
                    sh.getMedicamentNom() != null ? sh.getMedicamentNom() : "ID: " + sh.getIdMedicament(),
                    sh.getQuantiteAvant(),
                    sh.getQuantiteApres(),
                    difference,
                    sh.getTypeOperation(),
                    sh.getIdOperation(),
                    sh.getDateOperation()
                };
                model.addRow(row);
            }
            
            // Colorier les lignes selon la différence
            table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                            c.setBackground(Color.WHITE);
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
    
    private void showFilterDialog() {
        JDialog filterDialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "Filtrer l'historique", true);
        filterDialog.setSize(400, 300);
        filterDialog.setLayout(new GridBagLayout());
        filterDialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel typeLabel = new JLabel("Type d'opération:");
        String[] types = {"Tous", "vente", "commande", "ajustement"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        
        JLabel medicamentLabel = new JLabel("ID Médicament:");
        JTextField medicamentField = new JTextField(15);
        
        JLabel dateLabel = new JLabel("Période (optionnel):");
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JLabel fromLabel = new JLabel("Du:");
        JTextField fromField = new JTextField("YYYY-MM-DD", 10);
        JLabel toLabel = new JLabel("Au:");
        JTextField toField = new JTextField("YYYY-MM-DD", 10);
        
        datePanel.add(fromLabel);
        datePanel.add(fromField);
        datePanel.add(toLabel);
        datePanel.add(toField);
        
        gbc.gridx = 0; gbc.gridy = 0;
        filterDialog.add(typeLabel, gbc);
        gbc.gridx = 1;
        filterDialog.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        filterDialog.add(medicamentLabel, gbc);
        gbc.gridx = 1;
        filterDialog.add(medicamentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        filterDialog.add(dateLabel, gbc);
        gbc.gridx = 1;
        filterDialog.add(datePanel, gbc);
        
        JButton applyBtn = createStyledButton("Appliquer", new Color(33, 150, 243));
        JButton cancelBtn = createStyledButton("Annuler", new Color(244, 67, 54));
        
        applyBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String idMedicament = medicamentField.getText().trim();
            @SuppressWarnings("unused")
			String fromDate = fromField.getText();
            @SuppressWarnings("unused")
			String toDate = toField.getText();
            
            try {
                List<StockHistorique> filteredList = historiqueDAO.getAll();
                
                // Filtrer par type
                if (!"Tous".equals(type)) {
                    filteredList = historiqueDAO.getByTypeOperation(type);
                }
                
                // Filtrer par médicament si spécifié
                if (!idMedicament.isEmpty()) {
                    int id = Integer.parseInt(idMedicament);
                    filteredList = historiqueDAO.getByMedicament(id);
                }
                
                // Mettre à jour le tableau
                model.setRowCount(0);
                for (StockHistorique sh : filteredList) {
                    int difference = sh.getQuantiteApres() - sh.getQuantiteAvant();
                    Object[] row = {
                        sh.getId(),
                        sh.getMedicamentNom() != null ? sh.getMedicamentNom() : "ID: " + sh.getIdMedicament(),
                        sh.getQuantiteAvant(),
                        sh.getQuantiteApres(),
                        difference,
                        sh.getTypeOperation(),
                        sh.getIdOperation(),
                        sh.getDateOperation()
                    };
                    model.addRow(row);
                }
                
                filterDialog.dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(filterDialog, "ID médicament invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(filterDialog, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> filterDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        filterDialog.add(buttonPanel, gbc);
        
        filterDialog.setVisible(true);
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter l'historique");
        fileChooser.setSelectedFile(new File("historique_stock_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            boolean success = exportService.exportHistoriqueCSV(file.getAbsolutePath());
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Historique exporté avec succès!\nFichier: " + file.getName(),
                    "Export Réussi",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}