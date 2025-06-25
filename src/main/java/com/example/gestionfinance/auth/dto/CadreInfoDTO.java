package com.example.gestionfinance.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadreInfoDTO {
    private String nom;
    private String prenom;
    private double salaireBrut;
    private double montantAvance;
    private double salaireNet;
}

