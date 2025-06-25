package com.example.gestionfinance.auth.repository;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter

@Setter
public class PaiementRequest {

    private Long etudiantId;
    private BigDecimal montant;
    private String date;




}