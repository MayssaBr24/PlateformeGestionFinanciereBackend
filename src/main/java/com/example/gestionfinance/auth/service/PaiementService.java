package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.dto.PaiementResponseDto;
import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.model.Paiement;
import com.example.gestionfinance.auth.repository.EtudiantRepository;
import com.example.gestionfinance.auth.repository.PaiementRepository;
import com.example.gestionfinance.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    @Transactional
    public List<PaiementResponseDto> getPaiementsByEtudiant(Long etudiantId) {
        List<Paiement> paiements = paiementRepository.findByEtudiantId(etudiantId);

        return paiements.stream()
                .map(this::convertToDto) // Utilisation effective de la méthode
                .collect(Collectors.toList());
    }

    private PaiementResponseDto convertToDto(Paiement paiement) {
        return new PaiementResponseDto(
                paiement.getId(),
                paiement.getMontant(),
                paiement.getDate(),
                paiement.getEtudiant() != null ? paiement.getEtudiant().getId() : null
        );
    }








}