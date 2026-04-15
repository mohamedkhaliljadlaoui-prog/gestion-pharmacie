package test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import dao.MedicamentDAO;
import models.Medicament;

public class TestAlertes {
    
    public static void main(String[] args) {
        // Créer une fenêtre de test
        JFrame frame = new JFrame("Test Alertes Stock Critique");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Panneau d'en-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("⚠️ Alertes Stock Critique - TEST DIRECT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tableau des alertes
        String[] columns = {"Médicament", "Dosage", "Stock Actuel", "Seuil", "Différence", "État"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Couleurs selon l'état
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    try {
                        String etat = (String) table.getValueAt(row, 5);
                        if (etat != null) {
                            switch (etat) {
                                case "CRITIQUE":
                                    c.setBackground(new Color(255, 200, 200));
                                    c.setForeground(Color.BLACK);
                                    break;
                                case "FAIBLE":
                                    c.setBackground(new Color(255, 255, 200));
                                    c.setForeground(Color.BLACK);
                                    break;
                                case "RUPTURE":
                                    c.setBackground(new Color(220, 100, 100));
                                    c.setForeground(Color.WHITE);
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
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Boutons
        JPanel buttonPanel = new JPanel();
        JButton btnCharger = new JButton("🔄 Charger depuis BD");
        JButton btnDemo = new JButton("📊 Données Démo");
        
        btnCharger.setBackground(new Color(33, 150, 243));
        btnCharger.setForeground(Color.WHITE);
        btnCharger.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        btnDemo.setBackground(new Color(76, 175, 80));
        btnDemo.setForeground(Color.WHITE);
        btnDemo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        buttonPanel.add(btnCharger);
        buttonPanel.add(btnDemo);
        
        // Actions des boutons
        btnCharger.addActionListener(e -> {
            chargerDonneesReelles(model);
        });
        
        btnDemo.addActionListener(e -> {
            chargerDonneesDemo(model);
        });
        
        // Assembler la fenêtre
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Charger automatiquement au démarrage
        SwingUtilities.invokeLater(() -> {
            chargerDonneesReelles(model);
        });
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void chargerDonneesReelles(DefaultTableModel model) {
        model.setRowCount(0);
        
        try {
            System.out.println("🔄 Connexion à la base de données...");
            MedicamentDAO dao = new MedicamentDAO();
            
            // Méthode 1: Récupérer tous et filtrer
            List<Medicament> allMedicaments = dao.getAll();
            System.out.println("✅ " + allMedicaments.size() + " médicaments trouvés au total");
            
            int compteurCritiques = 0;
            for (Medicament med : allMedicaments) {
                if (med.getStock() <= med.getSeuilAlerte()) {
                    compteurCritiques++;
                    int difference = med.getSeuilAlerte() - med.getStock();
                    String etat = med.getStock() == 0 ? "RUPTURE" : 
                                 med.getStock() <= med.getSeuilAlerte() * 0.3 ? "CRITIQUE" : "FAIBLE";
                    
                    model.addRow(new Object[]{
                        med.getNom(),
                        med.getDosage(),
                        med.getStock(),
                        med.getSeuilAlerte(),
                        difference,
                        etat
                    });
                    
                    System.out.println("📌 " + med.getNom() + " " + med.getDosage() + 
                                     " - Stock: " + med.getStock() + 
                                     " / Seuil: " + med.getSeuilAlerte() + 
                                     " -> " + etat);
                }
            }
            
            if (compteurCritiques == 0) {
                model.addRow(new Object[]{"✅ Aucun stock critique", "-", "-", "-", "-", "NORMAL"});
            }
            
            System.out.println("✅ " + compteurCritiques + " médicaments en stock critique");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"❌ Erreur BD", e.getMessage(), "-", "-", "-", "ERREUR"});
        }
    }
    
    private static void chargerDonneesDemo(DefaultTableModel model) {
        model.setRowCount(0);
        
        // Données de démo basées sur votre base
        model.addRow(new Object[]{"Ventoline", "100µg", 3, 10, 7, "CRITIQUE"});
        model.addRow(new Object[]{"Insuline", "100UI/ml", 5, 8, 3, "CRITIQUE"});
        model.addRow(new Object[]{"Doliprane", "1000mg", 9, 10, 1, "FAIBLE"});
        model.addRow(new Object[]{"Aspirine", "500mg", 15, 20, 5, "FAIBLE"});
        
        System.out.println("📊 Données démo chargées");
    }
}