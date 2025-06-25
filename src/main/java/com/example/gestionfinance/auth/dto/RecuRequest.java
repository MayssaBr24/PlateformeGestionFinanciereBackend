package com.example.gestionfinance.auth.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecuRequest {
    private String nom;
    private String prenom;
    private double salaireBrut;
    private double avanceRemboursee;
    private double salaireNet;
    private double resteAvance;
}