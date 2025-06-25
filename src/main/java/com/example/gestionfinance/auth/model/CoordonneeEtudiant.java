package com.example.gestionfinance.auth.model;

public class CoordonneeEtudiant {
    String nom;
    String prenom;
    String classe;
    String cycle;
    String parcour;
    String specialite;
    String niveau;
    String numInscription;
    int idEnregistrement;
    int etudiantId;

    public int getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(int etudiantId) {
        this.etudiantId = etudiantId;
    }

    public CoordonneeEtudiant(String nom, String prenom, String classe, String cycle, String parcour, String specialite, String niveau, String numInscription, int idEnregistrement, int etudiantId) {
        this.nom = nom;
        this.prenom = prenom;
        this.classe = classe;
        this.cycle = cycle;
        this.parcour = parcour;
        this.specialite = specialite;
        this.niveau = niveau;
        this.numInscription = numInscription;
        this.idEnregistrement = idEnregistrement;
        this.etudiantId = etudiantId;
    }

    public CoordonneeEtudiant() {
    }

    @Override
    public String toString() {
        return "CoordonneeEtudiant{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", classe='" + classe + '\'' +
                ", cycle='" + cycle + '\'' +
                ", parcour='" + parcour + '\'' +
                ", specialite='" + specialite + '\'' +
                ", niveau='" + niveau + '\'' +
                ", numInscription='" + numInscription + '\'' +
                ", idEnregistrement=" + idEnregistrement +
                '}';
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getParcour() {
        return parcour;
    }

    public void setParcour(String parcour) {
        this.parcour = parcour;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getNumInscription() {
        return numInscription;
    }

    public void setNumInscription(String numInscription) {
        this.numInscription = numInscription;
    }

    public int getIdEnregistrement() {
        return idEnregistrement;
    }

    public void setIdEnregistrement(int idEnregistrement) {
        this.idEnregistrement = idEnregistrement;
    }
}
