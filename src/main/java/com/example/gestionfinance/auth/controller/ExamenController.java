package com.example.gestionfinance.auth.controller;


import com.example.gestionfinance.auth.dto.EtudiantApiExamen;
import com.example.gestionfinance.auth.service.ExamenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/examen")
public class ExamenController {

    private final ExamenService examenService;

    @Autowired
    public ExamenController(ExamenService examenService) {
        this.examenService = examenService;
    }

    @GetMapping("/aptitude/{classe}")
    public ResponseEntity<List<EtudiantApiExamen>> getEtudiantsAptes(
            @PathVariable String classe,
            @RequestParam String semestre) {

        List<EtudiantApiExamen> etudiantsAptes = examenService.getEtudiantsAptesPourExamen(classe, semestre);

        // ❗ On retourne uniquement les étudiants aptes
        List<EtudiantApiExamen> etudiantsEligibles = etudiantsAptes.stream()
                .filter(EtudiantApiExamen::isApte)
                .collect(Collectors.toList());

        return ResponseEntity.ok(etudiantsEligibles);
    }
}
