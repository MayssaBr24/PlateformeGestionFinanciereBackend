package com.example.gestionfinance.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemboursementRequest {
    private double montant;
    private String date;
    private Long etudiantId;

}
