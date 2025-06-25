package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.dto.VacataireInfoDTO;
import com.example.gestionfinance.auth.dto.VirementRequest;
import com.example.gestionfinance.auth.model.EnseignantPermanent;
import com.example.gestionfinance.auth.model.EnseignantVacataire;
import com.example.gestionfinance.auth.repository.EnseignantVacataireRepository;
import com.example.gestionfinance.auth.service.EnseignantVacataireService;
import com.example.gestionfinance.auth.service.OrdreDeVirementVacataireService;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enseignants-vacataires")
@CrossOrigin(origins = "https://localhost")
public class EnseignantVacataireController {


    @Autowired
    private EnseignantVacataireRepository vacataireRepository;

    @Autowired
    private OrdreDeVirementVacataireService ordreDeVirementService;

    @Autowired
    private EnseignantVacataireService vacataireService;

    @GetMapping
    public List<EnseignantVacataire> getAllVacataires() {
        return vacataireRepository.findAll();
    }

    @PostMapping
    public EnseignantVacataire createVacataire(@RequestBody EnseignantVacataire vacataire) {
        return vacataireRepository.save(vacataire);
    }

    @PostMapping("/{id}/paiement")
    public ResponseEntity<?> effectuerPaiement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        String type = (String) request.get("type");

        if ("avance".equals(type)) {
            double montant = Double.parseDouble(request.get("montant").toString());
            Optional<EnseignantVacataire> optVacataire = vacataireService.ajouterAvance(id, montant);
            return optVacataire.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }
        else if ("salaire".equals(type)) {
            Integer nombreHeures = Integer.parseInt(request.get("nombreHeures").toString());
            Double tauxHoraire = Double.parseDouble(request.get("tauxHoraire").toString());

            Optional<EnseignantVacataire> optVacataire = vacataireService.calculerSalaire(
                    id, nombreHeures, tauxHoraire);

            return optVacataire.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }
        else {
            return ResponseEntity.badRequest().body("Type de paiement non reconnu");
        }
    }

    @PostMapping("/virement-multiple")
    public ResponseEntity<byte[]> genererVirementPourVacataires(@RequestBody VirementRequest request) throws Exception {
        byte[] pdf = ordreDeVirementService.genererOrdreDeVirement(
                request.getDate(),
                request.getEnseignantIds());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("ordre-de-virement-vacataires-" + request.getDate() + ".pdf")
                .build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EnseignantVacataire> updateVacataire(
            @PathVariable Long id,
            @RequestBody EnseignantVacataire vacataireDetails) {

        Optional<EnseignantVacataire> optVacataire = vacataireRepository.findById(id);

        if (optVacataire.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EnseignantVacataire vacataire = optVacataire.get();

        // Mise à jour des champs modifiables
        vacataire.setNom(vacataireDetails.getNom());
        vacataire.setPrenom(vacataireDetails.getPrenom());
        vacataire.setEmail(vacataireDetails.getEmail());
        vacataire.setTauxHoraire(vacataireDetails.getTauxHoraire());
        vacataire.setCompteBancaire(vacataireDetails.getCompteBancaire());
        vacataire.setBanque(vacataireDetails.getBanque());
        vacataire.setType(vacataireDetails.getType());

        EnseignantVacataire updatedVacataire = vacataireRepository.save(vacataire);
        return ResponseEntity.ok(updatedVacataire);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnseignantVacataire> getVacataireById(@PathVariable Long id) {
        Optional<EnseignantVacataire> vacataire = vacataireRepository.findById(id);
        return vacataire.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ajouter")
    public ResponseEntity<?> ajouterVacataire(@RequestBody EnseignantVacataire vacataire) {
        try {
            // Log des données reçues
            System.out.println("Données reçues: " + vacataire.toString());

            // Vérification de l'email
            if (vacataireRepository.existsByEmail(vacataire.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Un enseignant vacataire avec cet email existe déjà");
            }







            EnseignantVacataire saved = vacataireRepository.save(vacataire);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Erreur technique: " + e.getMessage());
        }
    }
    @PostMapping("/{id}/recu")
    public ResponseEntity<byte[]> genererRecu(@PathVariable Long id, @RequestBody VacataireInfoDTO vacataire) {
        try {
            String nom = vacataire.getNom();
            String prenom = vacataire.getPrenom();
            String cin = vacataire.getCin();
            double nombreHeures = vacataire.getNombreHeures();
            double tauxHoraire = vacataire.getTauxHoraire();
            double montantAvance = vacataire.getMontantAvance();
            double retenueSourcePercentage = vacataire.getRetenueSource(); // Récupération du pourcentage depuis le DTO

            double montantBrut = nombreHeures * tauxHoraire;
            double montantRetenue = montantBrut * (retenueSourcePercentage / 100);
            double montantFinal = montantBrut - montantRetenue - montantAvance;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 72, 72);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Style des polices
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLACK);
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
            Font tableContentFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK);
            Font tableTotalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
            Font signatureFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);

            // 1. Titre dans un cadre centré
            Paragraph title = new Paragraph("FRAIS D'HONORAIRE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            Paragraph espaceApresTitre = new Paragraph();
            espaceApresTitre.setSpacingAfter(20f);
            document.add(espaceApresTitre);

            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(60);
            titleTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell titleCell = new PdfPCell(title);
            titleCell.setBorder(Rectangle.BOX);
            titleCell.setPadding(15);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setBackgroundColor(BaseColor.WHITE);
            titleTable.addCell(titleCell);

            document.add(titleTable);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // 2. Informations personnelles
            Paragraph nomParagraph = new Paragraph("Nom : " + nom.toUpperCase(), infoFont);
            nomParagraph.setAlignment(Element.ALIGN_LEFT);
            nomParagraph.setSpacingAfter(10);
            document.add(nomParagraph);

            Paragraph prenomParagraph = new Paragraph("Prénom : " + prenom.toUpperCase(), infoFont);
            prenomParagraph.setAlignment(Element.ALIGN_LEFT);
            prenomParagraph.setSpacingAfter(10);
            document.add(prenomParagraph);

            Paragraph cinParagraph = new Paragraph("CIN : " + cin, infoFont);
            cinParagraph.setAlignment(Element.ALIGN_LEFT);
            cinParagraph.setSpacingAfter(20);
            document.add(cinParagraph);

            // 3. Tableau
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[]{3, 1});
            table.setSpacingBefore(20);
            table.setSpacingAfter(30);

            addBorderedCell(table, "Salaire Brut", String.format("%.3f", montantBrut), tableContentFont);
            addBorderedCell(table, "Montant Avance", String.format("%.3f", montantAvance), tableContentFont);
            addBorderedCell(table,
                    String.format("Retenue à la source (%.1f%%)", retenueSourcePercentage),
                    String.format("%.3f", montantRetenue),
                    tableContentFont);

            PdfPCell finalLabelCell = new PdfPCell(new Phrase("Montant Final à Payer", tableTotalFont));
            PdfPCell finalValueCell = new PdfPCell(new Phrase(String.format("%.3f", montantFinal), tableTotalFont));

            finalLabelCell.setBorder(Rectangle.BOX);
            finalValueCell.setBorder(Rectangle.BOX);
            finalLabelCell.setPadding(10);
            finalValueCell.setPadding(10);
            finalLabelCell.setMinimumHeight(30);
            finalValueCell.setMinimumHeight(30);

            table.addCell(finalLabelCell);
            table.addCell(finalValueCell);

            document.add(table);

            // 4. Signatures
            float signatureWidth = 250f;
            float spacerWidth = 50f;

            PdfPTable signatureTable = new PdfPTable(new float[]{signatureWidth, spacerWidth, signatureWidth});
            signatureTable.setWidthPercentage(90);
            signatureTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            signatureTable.setSpacingBefore(40);
            signatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell leftSignatureCell = new PdfPCell(new Phrase("RESPONSABLE FINANCIER", signatureFont));
            leftSignatureCell.setBorder(Rectangle.BOX);
            leftSignatureCell.setPadding(15);
            leftSignatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            leftSignatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            leftSignatureCell.setFixedHeight(80);

            PdfPCell spacerCell = new PdfPCell();
            spacerCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell rightSignatureCell = new PdfPCell(new Phrase("BÉNÉFICIAIRE", signatureFont));
            rightSignatureCell.setBorder(Rectangle.BOX);
            rightSignatureCell.setPadding(15);
            rightSignatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            rightSignatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            rightSignatureCell.setFixedHeight(80);

            signatureTable.addCell(leftSignatureCell);
            signatureTable.addCell(spacerCell);
            signatureTable.addCell(rightSignatureCell);

            document.add(signatureTable);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "recu_" + nom + "_" + prenom + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private void addBorderedCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));

        labelCell.setBorder(Rectangle.BOX);
        valueCell.setBorder(Rectangle.BOX);
        labelCell.setPadding(10);
        valueCell.setPadding(10);
        labelCell.setMinimumHeight(30);
        valueCell.setMinimumHeight(30);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    @PutMapping("/{id}/reset-avance")
    public ResponseEntity<?> resetAvance(@PathVariable Long id) {
        Optional<EnseignantVacataire> vacataireOptional = vacataireRepository.findById(id);
        if (vacataireOptional.isPresent()) {
            EnseignantVacataire vacataire = vacataireOptional.get();
            vacataire.setTotalAvances(0.0); // ou 0 selon ton type
            vacataireRepository.save(vacataire);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/actifs")
    public List<EnseignantVacataire> getActifs() {
        return vacataireService.findByEtat(true);
    }

    @GetMapping("/inactifs")
    public List<EnseignantVacataire> getInactifs() {
        return vacataireService.findByEtat(false);
    }

    @PutMapping("/{id}/etat")
    public ResponseEntity<EnseignantVacataire> toggleEtat(@PathVariable Long id) {
        EnseignantVacataire updated = vacataireService.toggleEtat(id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }




}



