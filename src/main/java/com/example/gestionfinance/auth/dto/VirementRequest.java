package com.example.gestionfinance.auth.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VirementRequest {
    private LocalDate date;
    private List<Long> enseignantIds;
}
