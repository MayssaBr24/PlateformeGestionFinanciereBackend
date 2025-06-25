package com.example.gestionfinance.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ensP")
@Getter
@Setter
public class EnseignantPermanent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private Double salaireBrut; // Le salaire de base (référence)


    private Double salaireNet;
    // Calculé dynamiquement : salaireBrut - totalAvances (donc pas stocké)

    private boolean paiementEffectue;

    @Column(nullable = false)
    private Double totalAvances = 0.0;
    private String compteBancaire;
    private String type;
    private String banque;
    private boolean etat = true;




}