package com.example.gestionfinance.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cadre_ad")
@Getter
@Setter
public class CadreAdmnistratif {
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
    private Double salaireBrut;

    private Double salaireNet;
    private boolean paiementEffectue;

    @Column(nullable = false)
    private Double totalAvances = 0.0;

    private String compteBancaire;
    private String type;
    private String banque;
}