package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.model.EnseignantVacataire;
import com.example.gestionfinance.auth.repository.EnseignantVacataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnseignantVacataireService {

    @Autowired
    private EnseignantVacataireRepository repository;

    public Optional<EnseignantVacataire> ajouterAvance(Long id, double nouvelleAvance) {
        Optional<EnseignantVacataire> optVacataire = repository.findById(id);
        if (optVacataire.isEmpty()) return Optional.empty();

        EnseignantVacataire vacataire = optVacataire.get();
        vacataire.setTotalAvances(vacataire.getTotalAvances() + nouvelleAvance);
        repository.save(vacataire);
        return Optional.of(vacataire);
    }

    public Optional<EnseignantVacataire> calculerSalaire(Long id, Integer nombreHeures, Double tauxHoraire) {
        Optional<EnseignantVacataire> optVacataire = repository.findById(id);
        if (optVacataire.isEmpty()) return Optional.empty();

        EnseignantVacataire vacataire = optVacataire.get();
        vacataire.setNombreHeures(nombreHeures);
        vacataire.setTauxHoraire(tauxHoraire);

        double salaireBrut = nombreHeures * tauxHoraire;
        vacataire.setSalaireBrut(salaireBrut);

        double salaireNet = salaireBrut - vacataire.getTotalAvances();
        vacataire.setSalaireNet(salaireNet);
        vacataire.setPaiementEffectue(true);

        repository.save(vacataire);
        return Optional.of(vacataire);
    }
    public Double payerSalaire(Long id) {
        Optional<EnseignantVacataire> optVacataire = repository.findById(id);
        if (optVacataire.isEmpty()) return null;

        EnseignantVacataire vacataire = optVacataire.get();

        // Vérification des champs requis
        if (vacataire.getNombreHeures() == null || vacataire.getTauxHoraire() == null) {
            return null;
        }

        // Calcul du salaire
        double salaireBrut = vacataire.getNombreHeures() * vacataire.getTauxHoraire();
        double salaireNet = salaireBrut - (vacataire.getTotalAvances() != null ? vacataire.getTotalAvances() : 0);

        return salaireNet;
    }



    public List<EnseignantVacataire> findByEtat(boolean etat) {
        return repository.findByEtat(etat);
    }

    public EnseignantVacataire toggleEtat(Long id) {
        EnseignantVacataire vacataire = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        vacataire.setEtat(!vacataire.isEtat());
        return repository.save(vacataire);
    }
}