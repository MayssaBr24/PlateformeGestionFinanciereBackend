package com.example.gestionfinance.auth.controller;
import com.example.gestionfinance.auth.model.Recu;
import com.example.gestionfinance.auth.repository.RecuRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import java.io.OutputStream;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/recus")
@CrossOrigin(origins = "https://localhost")
public class RecuController {
    private final RecuRepository recuRepository;

    @Autowired
    public RecuController(RecuRepository recuRepository) {
        this.recuRepository = recuRepository;
    }

    @GetMapping
    public List<Recu> getAllRecus() {
        return recuRepository.findAll();
    }

    @PostMapping
    public void createRecu(@RequestBody Recu recu, HttpServletResponse response) {
        try {
            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            recu.setDate(formattedDate);

            Recu savedRecu = recuRepository.save(recu);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"justificatif_" + savedRecu.getNumeroRecu() + ".pdf\"");
            OutputStream out = response.getOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Configuration des polices
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(0, 51, 102));
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            Font signatureFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLDITALIC, BaseColor.DARK_GRAY);

            // Chargement du logo
            Image logo = Image.getInstance("src/main/resources/static/logo.png");
            logo.scaleToFit(150, 70);

            // Création de la méthode pour générer un reçu
            Consumer<Document> generateReceipt = doc -> {
                try {
                    // Ajout du logo
                    Image logoCopy = Image.getInstance(logo);
                    logoCopy.setAbsolutePosition(36, PageSize.A4.getHeight() - 100);
                    doc.add(logoCopy);

                    // Titre principal
                    Paragraph title = new Paragraph("JUSTIFICATIF DE TRANSACTION", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    doc.add(title);

                    // Ligne de séparation
                    LineSeparator line = new LineSeparator();
                    doc.add(new Chunk(line));

                    // Tableau des informations
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(90);
                    table.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.setSpacingBefore(20);
                    table.setSpacingAfter(30);

                    addTableRow(table, "Type:", savedRecu.getType().equalsIgnoreCase("entrant") ? "ENTRÉE" : "SORTIE", headerFont, contentFont);
                    addTableRow(table, "Responsable:", savedRecu.getResponsable(), headerFont, contentFont);
                    addTableRow(table, "Montant:", savedRecu.getMontant() + " DT", headerFont, contentFont);
                    addTableRow(table, "Sujet:", savedRecu.getSujet(), headerFont, contentFont);
                    addTableRow(table, "Mode de Paiement:", savedRecu.getModePaiement(), headerFont, contentFont);
                    addTableRow(table, "Remarques:", savedRecu.getRemarques(), headerFont, contentFont);

                    doc.add(table);

                    // Section signature et date
                    Paragraph signatureSection = new Paragraph();
                    signatureSection.setSpacingBefore(40);


                        signatureSection.add(new Chunk("Signature: _________________________", signatureFont));
                        signatureSection.add(Chunk.NEWLINE);


                    signatureSection.add(new Chunk("Fait à Gabès, le " + formattedDate, signatureFont));
                    signatureSection.setAlignment(Element.ALIGN_RIGHT);
                    doc.add(signatureSection);

                    // Nouvelle page pour la deuxième copie
                    doc.newPage();
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de la génération du reçu", e);
                }
            };

            // Générer deux copies
            generateReceipt.accept(document);
            generateReceipt.accept(document);

            document.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}