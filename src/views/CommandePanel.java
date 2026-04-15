package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import models.Commande;
import services.CommandeService;

@SuppressWarnings("serial")
public class CommandePanel extends JPanel {

    private CommandeService service;
    private JTable table;
    private DefaultTableModel model;

    public CommandePanel() {
        service = new CommandeService();
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        model = new DefaultTableModel(
            new String[]{"ID", "Gestionnaire", "Médicament", "Quantité", "Statut", "Date"}, 0
        );
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        List<Commande> commandes = service.getAllCommandes();
        for (Commande c : commandes) {
            model.addRow(new Object[]{
                c.getId(),
                c.getIdGestionnaire(),
                c.getIdMedicament(),
                c.getQuantite(),
                c.getStatut(),
                c.getDateCommande()
            });
        }
    }
}
