package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import models.Vente;
import models.Medicament;
import models.Client;
import dao.MedicamentDAO;
import dao.ClientDAO;
import services.VenteService;

@SuppressWarnings("serial")
public class VentePharmacienPanel extends JPanel {

    private VenteService venteService;
    private MedicamentDAO medicamentDAO;
    private ClientDAO clientDAO;
    private int idPharmacienConnecte;
    
    // Composants
    private JComboBox<Medicament> comboMedicaments;
    private JComboBox<Client> comboClients;
    private JTextField txtQuantite;
    private JLabel lblPrixUnitaire;
    private JLabel lblStockDisponible;
    private JLabel lblPrixTotal;
    private JButton btnEnregistrerVente;
    private JButton btnActualiser;
    private JTable tableVentes;
    private DefaultTableModel tableModel;

    public VentePharmacienPanel(int idPharmacien) {
        this.idPharmacienConnecte = idPharmacien;
        
        // Initialisation des services
        venteService = new VenteService();
        medicamentDAO = new MedicamentDAO();
        clientDAO = new ClientDAO();
        
        // Configuration du layout
        setLayout(new BorderLayout(10, 10));
        
        // Initialisation de l'interface
        initUI();
        
        // Chargement des données
        chargerDonnees();
        
        // Configuration des écouteurs
        configurerEcouteurs();
    }

    private void initUI() {
        // ==================== PANEL HAUT : Formulaire de vente ====================
        JPanel panelFormulaire = new JPanel(new GridBagLayout());
        panelFormulaire.setBorder(BorderFactory.createTitledBorder("Nouvelle Vente"));
        panelFormulaire.setBackground(new Color(240, 240, 240));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 1. Médicament
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulaire.add(new JLabel("Médicament:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        comboMedicaments = new JComboBox<>();
        comboMedicaments.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Medicament) {
                    Medicament med = (Medicament) value;
                    if (med.getId() == -1) {
                        setText(med.getNom());
                    } else {
                        setText(med.getId() + " - " + med.getNom() + " (" + med.getDosage() + ")");
                    }
                }
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });
        comboMedicaments.setPreferredSize(new Dimension(300, 25));
        panelFormulaire.add(comboMedicaments, gbc);
        
        // 2. Client
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulaire.add(new JLabel("Client:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        comboClients = new JComboBox<>();
        comboClients.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Client) {
                    Client client = (Client) value;
                    if (client.getId() == 0) {
                        setText("Client occasionnel");
                    } else {
                        setText(client.getId() + " - " + client.getNom() + " " + client.getPrenom());
                    }
                }
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });
        comboClients.setPreferredSize(new Dimension(300, 25));
        panelFormulaire.add(comboClients, gbc);
        
        // 3. Quantité
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulaire.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        txtQuantite = new JTextField();
        txtQuantite.setPreferredSize(new Dimension(100, 25));
        panelFormulaire.add(txtQuantite, gbc);
        
        // 4. Prix unitaire
        gbc.gridx = 0; gbc.gridy = 3;
        panelFormulaire.add(new JLabel("Prix unitaire:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        lblPrixUnitaire = new JLabel("0.00 DT");
        panelFormulaire.add(lblPrixUnitaire, gbc);
        
        // 5. Stock disponible
        gbc.gridx = 0; gbc.gridy = 4;
        panelFormulaire.add(new JLabel("Stock disponible:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        lblStockDisponible = new JLabel("0");
        panelFormulaire.add(lblStockDisponible, gbc);
        
        // 6. Prix total
        gbc.gridx = 0; gbc.gridy = 5;
        panelFormulaire.add(new JLabel("Prix total:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        lblPrixTotal = new JLabel("0.00 DT");
        lblPrixTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrixTotal.setForeground(Color.BLUE);
        panelFormulaire.add(lblPrixTotal, gbc);
        
        // 7. Boutons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnEnregistrerVente = new JButton("Enregistrer Vente");
        btnEnregistrerVente.setBackground(new Color(76, 175, 80));
        btnEnregistrerVente.setForeground(Color.WHITE);
        btnEnregistrerVente.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnActualiser = new JButton("Actualiser");
        
        panelBoutons.add(btnEnregistrerVente);
        panelBoutons.add(btnActualiser);
        
        panelFormulaire.add(panelBoutons, gbc);
        
        add(panelFormulaire, BorderLayout.NORTH);
        
        // ==================== PANEL CENTRE : Table des ventes ====================
        String[] colonnes = {"ID", "Date", "Client", "Médicament", "Quantité", "Prix Total", "Statut"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableVentes = new JTable(tableModel);
        tableVentes.setRowHeight(25);
        tableVentes.getColumnModel().getColumn(5).setPreferredWidth(100); // Prix Total
        
        JScrollPane scrollPane = new JScrollPane(tableVentes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mes Ventes Récentes"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void chargerDonnees() {
        // Charger les médicaments disponibles
        chargerMedicaments();
        
        // Charger les clients
        chargerClients();
        
        // Charger les ventes de ce pharmacien
        chargerVentesPharmacien();
        
        // Mettre à jour les informations du médicament sélectionné
        mettreAJourInfosMedicament();
    }
    
    private void chargerMedicaments() {
        comboMedicaments.removeAllItems();
        List<Medicament> medicaments = medicamentDAO.getMedicamentsDisponibles();
        
        if (medicaments.isEmpty()) {
            // Ajouter un élément vide
            Medicament vide = new Medicament();
            vide.setId(-1);
            vide.setNom("Aucun médicament disponible");
            comboMedicaments.addItem(vide);
        } else {
            for (Medicament med : medicaments) {
                comboMedicaments.addItem(med);
            }
        }
    }
    
    private void chargerClients() {
        comboClients.removeAllItems();
        
        // Ajouter un client "Occasionnel"
        Client clientOccasionnel = new Client();
        clientOccasionnel.setId(0);
        clientOccasionnel.setNom("Client");
        clientOccasionnel.setPrenom("Occasionnel");
        comboClients.addItem(clientOccasionnel);
        
        // Ajouter les clients de la base
        List<Client> clients = clientDAO.getAll();
        for (Client client : clients) {
            comboClients.addItem(client);
        }
    }
    
    private void chargerVentesPharmacien() {
        tableModel.setRowCount(0); // Vider la table
        
        // Récupérer les ventes de ce pharmacien
        List<Vente> ventes = venteService.getVentesByPharmacien(idPharmacienConnecte);
        
        if (ventes.isEmpty()) {
            System.out.println("⚠ Aucune vente trouvée pour ce pharmacien");
        } else {
            System.out.println("✅ " + ventes.size() + " ventes chargées pour pharmacien ID: " + idPharmacienConnecte);
        }
        
        for (Vente vente : ventes) {
            String statut = "valide".equalsIgnoreCase(vente.getStatut()) ? "✓ Valide" : "✗ Annulée";
            String clientNom = vente.getClientNom();
            if (clientNom == null || clientNom.isEmpty()) {
                clientNom = "Client occasionnel";
            }
            
            Object[] ligne = {
                vente.getId(),
                vente.getDateVente() != null ? vente.getDateVente().toString() : "",
                clientNom,
                vente.getMedicamentNom(),
                vente.getQuantite(),
                String.format("%.2f DT", vente.getPrixTotal()),
                statut
            };
            tableModel.addRow(ligne);
        }
    }
    
    private void configurerEcouteurs() {
        // Sélection d'un médicament
        comboMedicaments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mettreAJourInfosMedicament();
                calculerPrixTotal();
            }
        });
        
        // Changement de quantité
        txtQuantite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculerPrixTotal();
            }
        });
        
        // Bouton Enregistrer Vente
        btnEnregistrerVente.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enregistrerVente();
            }
        });
        
        // Bouton Actualiser
        btnActualiser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chargerDonnees();
                JOptionPane.showMessageDialog(VentePharmacienPanel.this, 
                    "Données actualisées", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
    
    private void mettreAJourInfosMedicament() {
        Medicament medicament = (Medicament) comboMedicaments.getSelectedItem();
        if (medicament != null && medicament.getId() > 0) {
            lblPrixUnitaire.setText(String.format("%.2f DT", medicament.getPrixUnitaire()));
            lblStockDisponible.setText(String.valueOf(medicament.getStock()));
        } else {
            lblPrixUnitaire.setText("0.00 DT");
            lblStockDisponible.setText("0");
        }
    }
    
    private void calculerPrixTotal() {
        try {
            Medicament medicament = (Medicament) comboMedicaments.getSelectedItem();
            if (medicament != null && medicament.getId() > 0) {
                String quantiteText = txtQuantite.getText().trim();
                if (!quantiteText.isEmpty()) {
                    int quantite = Integer.parseInt(quantiteText);
                    double prixTotal = medicament.getPrixUnitaire() * quantite;
                    
                    // Vérifier le stock
                    if (quantite <= 0) {
                        lblPrixTotal.setForeground(Color.RED);
                        lblPrixTotal.setText("Quantité invalide");
                    } else if (quantite > medicament.getStock()) {
                        lblPrixTotal.setForeground(Color.RED);
                        lblPrixTotal.setText("Stock insuffisant! (" + medicament.getStock() + " disponible)");
                    } else {
                        lblPrixTotal.setForeground(Color.BLUE);
                        lblPrixTotal.setText(String.format("%.2f DT", prixTotal));
                    }
                } else {
                    lblPrixTotal.setText("0.00 DT");
                    lblPrixTotal.setForeground(Color.BLUE);
                }
            }
        } catch (NumberFormatException e) {
            lblPrixTotal.setText("Quantité invalide");
            lblPrixTotal.setForeground(Color.RED);
        }
    }
    
    private void enregistrerVente() {
        try {
            // 1. Récupérer le médicament
            Medicament medicament = (Medicament) comboMedicaments.getSelectedItem();
            if (medicament == null || medicament.getId() <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un médicament valide", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Récupérer le client
            Client client = (Client) comboClients.getSelectedItem();
            int idClient = (client != null) ? client.getId() : 0;
            
            // 3. Récupérer la quantité
            String quantiteText = txtQuantite.getText().trim();
            if (quantiteText.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez saisir une quantité", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int quantite = Integer.parseInt(quantiteText);
            
            // 4. Vérifier si la vente est possible
            String verification = venteService.verifierVentePossible(medicament.getId(), quantite);
            if (!"OK".equals(verification)) {
                JOptionPane.showMessageDialog(this, verification, 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 5. Calculer le prix total
            double prixTotal = medicament.getPrixUnitaire() * quantite;
            
            // 6. Confirmation
            String messageConfirmation = String.format(
                "Confirmer l'enregistrement de la vente?\n\n" +
                "Médicament: %s\n" +
                "Client: %s\n" +
                "Quantité: %d\n" +
                "Prix total: %.2f DT",
                medicament.getNom(),
                (idClient == 0 ? "Client occasionnel" : client.getNom() + " " + client.getPrenom()),
                quantite,
                prixTotal
            );
            
            int confirmation = JOptionPane.showConfirmDialog(this, 
                messageConfirmation, 
                "Confirmation de Vente", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                System.out.println("=== ENREGISTREMENT DE VENTE PAR PHARMACIEN ===");
                System.out.println("Pharmacien ID: " + idPharmacienConnecte);
                System.out.println("Client ID: " + idClient);
                System.out.println("Médicament ID: " + medicament.getId());
                System.out.println("Quantité: " + quantite);
                System.out.println("Prix total: " + prixTotal);
                
                // 7. Enregistrer la vente
                boolean succes = venteService.enregistrerVente(idPharmacienConnecte, idClient, medicament.getId(), quantite);
                
                if (succes) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Vente enregistrée avec succès!\n" +
                        "Stock mis à jour: " + (medicament.getStock() - quantite), 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
                    // 8. Réinitialiser et recharger
                    txtQuantite.setText("");
                    chargerDonnees();
                    
                    // 9. Journal
                    System.out.println("✅ Vente enregistrée dans la base de données par pharmacien ID: " + idPharmacienConnecte);
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Erreur lors de l'enregistrement de la vente", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.err.println("❌ Échec de l'enregistrement de la vente");
                }
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "La quantité doit être un nombre valide", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur inattendue: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}