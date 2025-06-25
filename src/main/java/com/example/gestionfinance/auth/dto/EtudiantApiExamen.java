package com.example.gestionfinance.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantApiExamen {

    private Long id;
    private String numInscription;
    private String nom;
    private String prenom;
    private String classe;
    private Double etatFinancier;
    private String semestre;
    private boolean apte;
}
