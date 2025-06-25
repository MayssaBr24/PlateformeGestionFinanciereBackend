package com.example.gestionfinance.auth.dto;

import com.example.gestionfinance.auth.model.Paiement;

public class PaiementMapper {
    public static PaiementResponseDto toDto(Paiement paiement) {
        return new PaiementResponseDto(
                paiement.getId(),
                paiement.getMontant(),
                paiement.getDate(),
                paiement.getEtudiant().getId() // On ne prend que l'ID
        );
    }
}