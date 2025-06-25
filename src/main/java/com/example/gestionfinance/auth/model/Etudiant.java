package com.example.gestionfinance.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "etudiant")
public class Etudiant {
    @Id
    private Long id;

    private String numInscription;
    private String nom;
    private String prenom;
    private String classe;
    private Double solde;

    public Etudiant() {
    }

    public Etudiant(Long id, String numInscription, String nom, String prenom, String classe, Double etatFinancier, List<Paiement> paiements) {
        this.id = id;
        this.numInscription = numInscription;
        this.nom = nom;
        this.prenom = prenom;
        this.classe = classe;
        this.etatFinancier = etatFinancier;
        this.paiements = paiements;
    }



    @Column(name = "etatFinancier") // Assure la correspondance avec la colonne existante
    private Double etatFinancier; // Changement de type ici

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Paiement> paiements = new ArrayList<>();


    public String getnumInscription() {
        return numInscription;
    }
}