package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.model.EnseignantPermanent;
import com.example.gestionfinance.auth.repository.EnseignantPermanentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnseignantPermanentService {

    @Autowired
    private EnseignantPermanentRepository repository;

    public Optional<EnseignantPermanent> ajouterAvance(Long id, double nouvelleAvance) {
        Optional<EnseignantPermanent> optEns = repository.findById(id);
        if (optEns.isEmpty()) return Optional.empty();

        EnseignantPermanent enseignant = optEns.get();

        double ancienneAvance = enseignant.getTotalAvances();
        double totalAvance = ancienneAvance + nouvelleAvance;
        enseignant.setTotalAvances(totalAvance);


        repository.save(enseignant);
        return Optional.of(enseignant);
    }

    public Optional<EnseignantPermanent> payerSalaire(Long id) {
        Optional<EnseignantPermanent> optEns = repository.findById(id);
        if (optEns.isEmpty()) return Optional.empty();

        EnseignantPermanent enseignant = optEns.get();

        double salaireBrut = enseignant.getSalaireBrut(); // si tu stockes le salaire brut à part
        double avance = enseignant.getTotalAvances(); // plus besoin de vérifier si null
        double salaireNet = salaireBrut - avance;
        enseignant.setSalaireNet(salaireNet); // on met bien le salaire net payé
        enseignant.setTotalAvances(0.0); // on remet l'avance à zéro

        repository.save(enseignant);
        return Optional.of(enseignant);
    }

    public List<EnseignantPermanent> findByEtat(boolean etat) {
        return repository.findByEtat(etat);
    }

    public EnseignantPermanent toggleEtat(Long id) {
        EnseignantPermanent enseignant = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        enseignant.setEtat(!enseignant.isEtat());
        return repository.save(enseignant);
    }




}
