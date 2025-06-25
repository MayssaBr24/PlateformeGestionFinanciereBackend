package com.example.gestionfinance.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VacataireInfoDTO {
    private String nom;
    private String prenom;
    private String cin;
    private double nombreHeures;
    private double tauxHoraire;
    private double montantAvance; // AJOUTÉ

    private double retenueSource ;
}
