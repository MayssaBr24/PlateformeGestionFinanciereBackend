package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.dto.CadreInfoDTO;
import com.example.gestionfinance.auth.dto.RecuRequest;
import com.example.gestionfinance.auth.dto.VirementRequest;
import com.example.gestionfinance.auth.model.CadreAdmnistratif;
import com.example.gestionfinance.auth.repository.CadreAdmnistrativRepository;
import com.example.gestionfinance.auth.service.CadreAdmnistratifService;
import com.example.gestionfinance.auth.service.OrdreDeVirementCadreService;
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
@RequestMapping("/api/cadres-administratifs")
@CrossOrigin(origins = "https://localhost")
public class CadreAdmnistratifController {

    @Autowired
    private CadreAdmnistrativRepository cadreRepository;

    @Autowired
    private OrdreDeVirementCadreService ordreDeVirementService;

    @Autowired
    private CadreAdmnistratifService cadreService;

    @GetMapping
    public List<CadreAdmnistratif> getAllCadres() {
        return cadreRepository.findAll();
    }

    @PostMapping
    public CadreAdmnistratif createCadre(@RequestBody CadreAdmnistratif cadre) {
        return cadreRepository.save(cadre);
    }

    @PostMapping("/{id}/paiement")
    public ResponseEntity<?> effectuerPaiement(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        double montant = Double.parseDouble(request.get("montant").toString());

        Optional<CadreAdmnistratif> optCadre;

        if ("avance".equals(type)) {
            optCadre = cadreService.ajouterAvance(id, montant);
        } else if ("salaire".equals(type)) {
            // Récupérer le montant à rembourser s'il existe
            double avanceARembourser = request.containsKey("avanceARembourser") ?
                    Double.parseDouble(request.get("avanceARembourser").toString()) : 0.0;

            optCadre = cadreService.payerSalaire(id, avanceARembourser);
        } else {
            return ResponseEntity.badRequest().body("Type de paiement non reconnu");
        }

        return optCadre.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/virement-multiple")
    public ResponseEntity<byte[]> genererVirementPourCadres(@RequestBody VirementRequest request) throws Exception {
        byte[] pdf = ordreDeVirementService.genererOrdreDeVirement(
                request.getDate(),
                request.getEnseignantIds());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("ordre-de-virement-cadres-" + request.getDate() + ".pdf")
                .build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<CadreAdmnistratif> updateCadre(@PathVariable Long id, @RequestBody CadreAdmnistratif cadreDetails) {
        Optional<CadreAdmnistratif> optCadre = cadreRepository.findById(id);

        if (optCadre.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CadreAdmnistratif cadre = optCadre.get();

        // Mise à jour des champs modifiables
        cadre.setNom(cadreDetails.getNom());
        cadre.setPrenom(cadreDetails.getPrenom());
        cadre.setEmail(cadreDetails.getEmail());

        cadre.setSalaireBrut(cadreDetails.getSalaireBrut());
        cadre.setCompteBancaire(cadreDetails.getCompteBancaire());
        cadre.setBanque(cadreDetails.getBanque());
        cadre.setType(cadreDetails.getType());


        CadreAdmnistratif updatedCadre = cadreRepository.save(cadre);
        return ResponseEntity.ok(updatedCadre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CadreAdmnistratif> getCadreById(@PathVariable Long id) {
        Optional<CadreAdmnistratif> cadre = cadreRepository.findById(id);
        return cadre.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ajouter")
    public ResponseEntity<?> ajouterCadre(@RequestBody CadreAdmnistratif cadre) {
        try {
            // Log des données reçues
            System.out.println("Données reçues: " + cadre.toString());

            // Vérification de l'email
            if (cadreRepository.existsByEmail(cadre.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Un cadre avec cet email existe déjà");
            }



            // Log avant sauvegarde
            System.out.println("Tentative d'insertion dans cadre_ad avec: " + cadre);

            CadreAdmnistratif saved = cadreRepository.save(cadre);

            // Log après sauvegarde
            System.out.println("Insertion réussie. ID: " + saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Erreur technique: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/recu")
    public ResponseEntity<byte[]> genererRecu(@PathVariable Long id, @RequestBody RecuRequest recuRequest) {
        try {
            String nom = recuRequest.getNom();
            String prenom = recuRequest.getPrenom();
            double salaireBrut = recuRequest.getSalaireBrut();
            double avanceRemboursee = recuRequest.getAvanceRemboursee();
            double salaireNet = recuRequest.getSalaireNet();
            double resteAvance = recuRequest.getResteAvance();

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
            Paragraph title = new Paragraph("BULLETIN DE PAIEMENT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);

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

            // Espace
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
            prenomParagraph.setSpacingAfter(20);
            document.add(prenomParagraph);

            // 3. Tableau
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[]{3, 1});
            table.setSpacingBefore(20);
            table.setSpacingAfter(30);

            // Ligne Salaire Brut
            addBorderedCell(table, "Salaire Brut", String.format("%.3f", salaireBrut), tableContentFont);

            // Ligne Avance Remboursée
            addBorderedCell(table, "Avance Remboursée", String.format("%.3f", avanceRemboursee), tableContentFont);

            // Ligne Reste d'Avance
            addBorderedCell(table, "Reste d'Avance", String.format("%.3f", resteAvance), tableContentFont);

            // Ligne Salaire Net (en gras)
            PdfPCell finalLabelCell = new PdfPCell(new Phrase("Salaire Net à Payer", tableTotalFont));
            PdfPCell finalValueCell = new PdfPCell(new Phrase(String.format("%.3f", salaireNet), tableTotalFont));

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
        PdfPCell cell1 = new PdfPCell(new Phrase(label, font));
        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));

        cell1.setBorder(Rectangle.BOX);
        cell2.setBorder(Rectangle.BOX);
        cell1.setPadding(10);
        cell2.setPadding(10);
        cell1.setMinimumHeight(30);
        cell2.setMinimumHeight(30);

        table.addCell(cell1);
        table.addCell(cell2);
    }
    @PutMapping("/{id}/reset-avance")
    public ResponseEntity<?> resetAvance(@PathVariable Long id) {
        Optional<CadreAdmnistratif> cadreOptional = cadreRepository.findById(id);
        if (cadreOptional.isPresent()) {
            CadreAdmnistratif cadre = cadreOptional.get();
            cadre.setTotalAvances(0.0);
            cadreRepository.save(cadre);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}


