package com.example.gestionfinance.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "enseignant_vacataire")
@Getter
@Setter
public class EnseignantVacataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private Double tauxHoraire;
    private Integer nombreHeures;
    private Double salaireBrut;
    private Double salaireNet;
    private boolean paiementEffectue;
    @Column(nullable = false, unique = true)
    private String cin;



    @Column(nullable = false)
    private Double totalAvances = 0.0;

    private String compteBancaire;
    private String type;
    private String banque;
    private boolean etat = true;

}