package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.dto.VirementRequest;
import com.example.gestionfinance.auth.model.CadreAdmnistratif;
import com.example.gestionfinance.auth.model.EnseignantPermanent;
import com.example.gestionfinance.auth.repository.EnseignantPermanentRepository;
import com.example.gestionfinance.auth.service.OrdreDeVirementService;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.gestionfinance.auth.service.EnseignantPermanentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enseignants-permanents")
@CrossOrigin(origins = "https://localhost")
public class EnseignantPermanentController {

    @Autowired
    private EnseignantPermanentRepository enseignantRepository;
    private OrdreDeVirementService ordreDeVirementService;
    @Autowired
    private EnseignantPermanentService enseignantPermanentService;

    public EnseignantPermanentController(OrdreDeVirementService ordreDeVirementService) {
        this.ordreDeVirementService = ordreDeVirementService;
    }

    @GetMapping
    public List<EnseignantPermanent> getAllEnseignants() {
        return enseignantRepository.findAll();
    }

    @PostMapping
    public EnseignantPermanent createEnseignant(@RequestBody EnseignantPermanent enseignant) {
        return enseignantRepository.save(enseignant);
    }

    @PostMapping("/{id}/paiement")
    public ResponseEntity<?> effectuerPaiement(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        double montant = Double.parseDouble(request.get("montant").toString());

        Optional<EnseignantPermanent> optEns;

        if ("avance".equals(type)) {
            optEns = enseignantPermanentService.ajouterAvance(id, (int) montant);
        } else if ("salaire".equals(type)) {
            optEns = enseignantPermanentService.payerSalaire(id);
        } else {
            return ResponseEntity.badRequest().body("Type de paiement non reconnu");
        }

        return optEns.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/virement-multiple")
    public ResponseEntity<byte[]> genererVirementPourEnseignants(@RequestBody VirementRequest request) throws Exception {
        byte[] pdf = ordreDeVirementService.genererOrdreDeVirement(request.getDate(), request.getEnseignantIds());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("ordre-de-virement-" + request.getDate() + ".pdf")
                .build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }




    @PostMapping("/{id}/avance")
    public ResponseEntity<?> ajouterAvance(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        Double nouvelleAvance = request.get("avance");

        // Appel de la méthode non statique via l'instance injectée
        return enseignantPermanentService.ajouterAvance(id, nouvelleAvance)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<EnseignantPermanent> updateEnseignant(
            @PathVariable Long id,
            @RequestBody EnseignantPermanent enseignantDetails) {

        Optional<EnseignantPermanent> optEnseignant = enseignantRepository.findById(id);

        if (optEnseignant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EnseignantPermanent enseignant = optEnseignant.get();

        // Mise à jour des champs modifiables
        enseignant.setNom(enseignantDetails.getNom());
        enseignant.setPrenom(enseignantDetails.getPrenom());
        enseignant.setEmail(enseignantDetails.getEmail());
        enseignant.setSalaireBrut(enseignantDetails.getSalaireBrut());
        enseignant.setCompteBancaire(enseignantDetails.getCompteBancaire());
        enseignant.setBanque(enseignantDetails.getBanque());
        enseignant.setType(enseignantDetails.getType());

        EnseignantPermanent updatedEnseignant = enseignantRepository.save(enseignant);
        return ResponseEntity.ok(updatedEnseignant);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnseignantPermanent> getEnseignantById(@PathVariable Long id) {
        Optional<EnseignantPermanent> enseignant = enseignantRepository.findById(id);
        return enseignant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @PostMapping("/ajouter")
    public ResponseEntity<?> ajouterEnseignant(@RequestBody EnseignantPermanent enseignant) {
        try {
            // Log des données reçues
            System.out.println("Données reçues: " + enseignant.toString());

            // Vérification des champs obligatoires
            if (enseignant.getSalaireBrut() == null) {
                return ResponseEntity.badRequest()
                        .body("Le salaire brut est obligatoire");
            }

            // Vérification de l'email
            if (enseignantRepository.existsByEmail(enseignant.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Un enseignant permanent avec cet email existe déjà");
            }


            EnseignantPermanent saved = enseignantRepository.save(enseignant);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Erreur technique: " + e.getMessage());
        }
    }
    @GetMapping("/actifs")
    public ResponseEntity<List<EnseignantPermanent>> getEnseignantsActifs() {
        List<EnseignantPermanent> enseignants = enseignantPermanentService.findByEtat(true);
        return ResponseEntity.ok(enseignants);
    }

    @GetMapping("/inactifs")
    public ResponseEntity<List<EnseignantPermanent>> getEnseignantsInactifs() {
        List<EnseignantPermanent> enseignants = enseignantPermanentService.findByEtat(false);
        return ResponseEntity.ok(enseignants);
    }

    @PutMapping("/{id}/etat")
    public ResponseEntity<EnseignantPermanent> toggleEtat(@PathVariable Long id) {
        EnseignantPermanent enseignant = enseignantPermanentService.toggleEtat(id);
        return ResponseEntity.ok(enseignant);
    }



}
