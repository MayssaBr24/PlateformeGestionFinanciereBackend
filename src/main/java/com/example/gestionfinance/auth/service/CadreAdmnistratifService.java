package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.dto.RecuRequest;
import com.example.gestionfinance.auth.model.CadreAdmnistratif;
import com.example.gestionfinance.auth.repository.CadreAdmnistrativRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CadreAdmnistratifService {

    @Autowired
    private CadreAdmnistrativRepository repository;
    @Autowired
    private CadreAdmnistrativRepository cadreAdmnistrativRepository;


    public Optional<CadreAdmnistratif> payerSalaire(Long id, double avanceARembourser) {
        Optional<CadreAdmnistratif> cadreOpt = cadreAdmnistrativRepository.findById(id);
        if (cadreOpt.isPresent()) {
            CadreAdmnistratif cadre = cadreOpt.get();

            // Vérifier que l'avance à rembourser ne dépasse pas le total
            if (avanceARembourser > cadre.getTotalAvances()) {
                throw new IllegalArgumentException("Le montant à rembourser dépasse le total des avances");
            }

            // Calculer le salaire net AVANT de mettre à jour l'avance
            double salaireNet = cadre.getSalaireBrut() - avanceARembourser;

            // Mettre à jour l'avance totale
            cadre.setTotalAvances(cadre.getTotalAvances() - avanceARembourser);

            cadre.setSalaireNet(salaireNet);
            cadre.setPaiementEffectue(true);

            cadreAdmnistrativRepository.save(cadre);
            return Optional.of(cadre);
        }
        return Optional.empty();
    }

    public Optional<CadreAdmnistratif> ajouterAvance(Long id, double montant) {
        Optional<CadreAdmnistratif> cadreOpt = cadreAdmnistrativRepository.findById(id);
        if (cadreOpt.isPresent()) {
            CadreAdmnistratif cadre = cadreOpt.get();
            cadre.setTotalAvances(cadre.getTotalAvances() + montant);
            cadreAdmnistrativRepository.save(cadre);
            return Optional.of(cadre);
        }
        return Optional.empty();
    }
}