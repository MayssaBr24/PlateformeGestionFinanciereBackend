package com.example.gestionfinance.auth.service;


import com.example.gestionfinance.auth.dto.EtudiantApiExamen;
import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.repository.EtudiantApiExamenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class ExamenService {

    private final EtudiantApiExamenRepository etudiantRepository;

    @Autowired
    public ExamenService(EtudiantApiExamenRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    public List<EtudiantApiExamen> getEtudiantsAptesPourExamen(String classe, String semestre) {
        List<Etudiant> etudiants = etudiantRepository.findByClasseComplete(classe);

        return etudiants.stream()
                .map(etudiant -> {
                    boolean apte = checkAptitudeExamen(etudiant.getEtatFinancier(), semestre);
                    return new EtudiantApiExamen(
                            etudiant.getId(),
                            etudiant.getnumInscription(),  // correction nom de méthode
                            etudiant.getNom(),
                            etudiant.getPrenom(),
                            etudiant.getClasse(),
                            etudiant.getEtatFinancier(),
                            semestre,
                            apte
                    );
                })
                .collect(Collectors.toList());
    }

    private boolean checkAptitudeExamen(Double etatFinancier, String semestre) {
        if (etatFinancier == null) return false;

        switch (semestre.toLowerCase()) {
            case "ds1": return etatFinancier >= 35;
            case "dc1": return etatFinancier >= 50;
            case "ds2": return etatFinancier >= 65;
            case "dc2": return etatFinancier >= 85;
            default: return false;
        }
    }
}
