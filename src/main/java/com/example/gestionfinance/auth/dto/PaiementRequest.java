package com.example.gestionfinance.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaiementRequest {
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double montant;

    @NotBlank(message = "La date est obligatoire")
    private String date; // Format attendu: "yyyy-MM-dd"

    @NotNull(message = "L'ID étudiant est obligatoire")
    private Long etudiantId;

}