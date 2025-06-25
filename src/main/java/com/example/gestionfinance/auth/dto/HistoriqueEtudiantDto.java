package com.example.gestionfinance.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class HistoriqueEtudiantDto {
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantClasse;
    private String numInscription;
    private Map<String, BigDecimal> paiementsParMois = new HashMap<>();
    private BigDecimal totalPaye = BigDecimal.ZERO;

    // Constructeur par défaut nécessaire pour la désérialisation JSON
    public HistoriqueEtudiantDto() {

    }
}