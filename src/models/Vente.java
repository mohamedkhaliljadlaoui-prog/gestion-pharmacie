package models;

import java.sql.Timestamp;

public class Vente {

    private int id;
    private int idPharmacien;
    private int idClient;
    private int idMedicament;
    private int quantite;
    private double prixTotal;
    private Timestamp dateVente;
    private String statut;

    // Champs pour affichage
    private String pharmacienNom;
    private String clientNom;
    private String medicamentNom;

    // ======================
    // CONSTRUCTEURS
    // ======================

    public Vente() {
        this.statut = "valide";
    }

    public Vente(int idPharmacien, int idClient, int idMedicament, int quantite, double prixTotal) {
        this.idPharmacien = idPharmacien;
        this.idClient = idClient;
        this.idMedicament = idMedicament;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.statut = "valide";
    }

    // ======================
    // GETTERS / SETTERS
    // ======================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPharmacien() {
        return idPharmacien;
    }

    public void setIdPharmacien(int idPharmacien) {
        this.idPharmacien = idPharmacien;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public Timestamp getDateVente() {
        return dateVente;
    }

    public void setDateVente(Timestamp dateVente) {
        this.dateVente = dateVente;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPharmacienNom() {
        return pharmacienNom;
    }

    public void setPharmacienNom(String pharmacienNom) {
        this.pharmacienNom = pharmacienNom;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getMedicamentNom() {
        return medicamentNom;
    }

    public void setMedicamentNom(String medicamentNom) {
        this.medicamentNom = medicamentNom;
    }

    // ======================
    // MÉTHODES UTILES
    // ======================

    public boolean isValide() {
        return "valide".equalsIgnoreCase(statut);
    }

    public void annuler() {
        this.statut = "annulee";
    }

    @Override
    public String toString() {
        return "Vente #" + id + " | " + medicamentNom +
                " x" + quantite + " | " + prixTotal + " DT";
    }
}
