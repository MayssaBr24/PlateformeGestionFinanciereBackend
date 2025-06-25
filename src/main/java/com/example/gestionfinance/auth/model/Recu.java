package com.example.gestionfinance.auth.model;



import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Data
public class Recu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String responsable;
    private String montant;
    private String sujet;
    private String date;
    private String modePaiement;
    private String remarques;
    private String signature;
    private String numeroRecu;
}

