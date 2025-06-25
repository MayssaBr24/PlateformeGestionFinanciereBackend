package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.repository.EtudiantRepository;
import com.example.gestionfinance.auth.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EtatFinancierService {

    private final PaiementRepository paiementRepository;
    private final EtudiantRepository etudiantRepository;

    @Autowired
    public EtatFinancierService(PaiementRepository paiementRepository,
                                EtudiantRepository etudiantRepository) {
        this.paiementRepository = paiementRepository;
        this.etudiantRepository = etudiantRepository;
    }




    public BigDecimal getMontantAttenduParClasse(String classe) {


        // Utilisez equals() au lieu de la comparaison par défaut pour les Strings
        switch(classe.toUpperCase()) {
            case "INGÉNIERIE - GÉNIE LOGICIEL - 1":
            case "INGÉNIERIE - GÉNIE LOGICIEL - 2":
            case "INGÉNIERIE - GÉNIE LOGICIEL - 3":
            case "INGÉNIERIE - RÉSEAUX ET TÉLÉCOMMUNICATIONS - 2":
            case "INGÉNIERIE - RÉSEAUX ET TÉLÉCOMMUNICATIONS - 3":
            case "INGÉNIERIE - TRONC COMMUN - 1":
                return new BigDecimal("5000.00");
            case "INGÉNIERIE - GENIE ÉLÉCRTIQUE AUTOMATIQUE - 1":
            case "INGÉNIERIE - GENIE ÉLÉCRTIQUE AUTOMATIQUE - 2":
            case "INGÉNIERIE - GENIE ÉLÉCRTIQUE AUTOMATIQUE - 3":
            case "INGÉNIERIE - GÉNIE CIVIL - 1":
            case "INGÉNIERIE - GÉNIE CIVIL - 2":
            case "INGÉNIERIE - GÉNIE CIVIL - 3":

            case "INGÉNIERIE - LISCENCE - BUSNINESS INFORMATION SYSTEM - 1 ":
            case "INGÉNIERIE - LISCENCE - BUSNINESS INFORMATION SYSTEM - 2 ":
            case "INGÉNIERIE - LISCENCE - BUSNINESS INFORMATION SYSTEM - 3 ":
                return new BigDecimal("5200.00");


                // ... autres cas
            default:
                return new BigDecimal("4300.00");
        }
    }
}