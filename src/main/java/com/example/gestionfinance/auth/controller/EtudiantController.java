package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.model.CoordonneeEtudiant;
import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.model.Paiement;
import com.example.gestionfinance.auth.repository.EtudiantRepository;
import com.example.gestionfinance.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/Etudiant")
@CrossOrigin(origins = "https://localhost")

public class EtudiantController {

    private final EtudiantRepository etudiantRepository;

    @Autowired
    public EtudiantController(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }


    @Autowired
    private WebClient webClient;

    public List<CoordonneeEtudiant> appelerApiAvecWebClient() {
        return webClient.get()
                //.uri("http://192.168.1.85:8080/ESSAT_ERP_war_exploded/testmayssa")
                .uri("http://localhost:8585/etudiant/Etudiant.json")
                .retrieve()
                .bodyToFlux(CoordonneeEtudiant.class)
                .collectList()
                .block(); // blocage uniquement si tu n'es pas en réactif pur
    }

    @GetMapping
    public List<Etudiant> getAllEtudiants() {


        List<CoordonneeEtudiant> etudiants = appelerApiAvecWebClient();

        List<Etudiant> listeEtudiants = new ArrayList<>();//etudiantRepository.findAll();

        for (CoordonneeEtudiant etudiant : etudiants) {
            Optional<Etudiant> etf = etudiantRepository.findById((long) etudiant.getEtudiantId());
            listeEtudiants.add(new Etudiant((long) etudiant.getEtudiantId(), etudiant.getNumInscription(), etudiant.getNom(), etudiant.getPrenom(), etudiant.getClasse(), etf.isPresent()? etf.get().getEtatFinancier():0.0, new ArrayList<Paiement>()));
        }

        return listeEtudiants;
    }

    @GetMapping("/search")
    public List<Etudiant> searchEtudiants(@RequestParam String query) {
        return etudiantRepository.findByNomContainingOrPrenomContaining(query, query);
    }

    @GetMapping("/Etudiant/{id}")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable("id") Long id) {
        List<CoordonneeEtudiant> etudiants = appelerApiAvecWebClient();

        List<Etudiant> listeEtudiants = new ArrayList<>();//etudiantRepository.findAll();

        for (CoordonneeEtudiant etudiant : etudiants) {
            Optional<Etudiant> etf = etudiantRepository.findById((long) etudiant.getEtudiantId());
            listeEtudiants.add(new Etudiant((long) etudiant.getEtudiantId(), etudiant.getNumInscription(), etudiant.getNom(), etudiant.getPrenom(), etudiant.getClasse(), etf.isPresent()? etf.get().getEtatFinancier():0.0, new ArrayList<Paiement>()));
        }

        return listeEtudiants.stream()
                .filter(etudiant -> Objects.equals(etudiant.getId(), id))
                .findFirst()
                .map(etudiant -> new ResponseEntity<>(etudiant, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}