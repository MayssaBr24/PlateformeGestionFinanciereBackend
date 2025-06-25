package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.model.CadreAdmnistratif;
import com.example.gestionfinance.auth.repository.CadreAdmnistrativRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OrdreDeVirementCadreService {

    @Autowired
    private CadreAdmnistrativRepository cadreRepo;

    @Autowired
    private CadreAdmnistratifService cadreService;

    public byte[] genererOrdreDeVirement(LocalDate date, List<Long> cadreIds) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            List<CadreAdmnistratif> cadres = cadreRepo.findAllById(cadreIds).stream()
                    .filter(c -> c.getCompteBancaire() != null && !c.getCompteBancaire().trim().isEmpty()
                            && "virement bancaire".equalsIgnoreCase(c.getType()))
                    .collect(Collectors.toList());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
            String moisEtAnnee = date.format(formatter);
            String dateDuJour = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Titre
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titre = new Paragraph("Ordre de Virement de cadre administratifcd   - " + moisEtAnnee, fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20f);
            document.add(titre);

            // Informations ESSAT
            Paragraph essatInfo = new Paragraph();
            essatInfo.add(new Chunk("ESSAT Privée de Gabès", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            essatInfo.add(new Chunk("\nCompte N° : 41 10 51144 8\n\n"));
            document.add(essatInfo);
            essatInfo.setSpacingAfter(15f);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.5f, 2.5f, 3.5f, 3.5f, 4.5f, 4.5f});
            table.addCell("Montant en lettres");
            table.addCell("Montant (DT)");
            table.addCell("Bénéficiaire");
            table.addCell("Auprès de");
            table.addCell("Libellé");
            table.addCell("RIB");

            double total = 0.0;

            for (CadreAdmnistratif c : cadres) {
                // Appeler payerSalaire avec l'avance à rembourser (0.0 si pas d'avance)
                cadreService.payerSalaire(c.getId(), 0.0); // Ici on passe 0.0 comme avance à rembourser

                c = cadreRepo.findById(c.getId()).orElse(c);
                Double salaire = c.getSalaireNet();
                double montant = salaire != null ? salaire : 0.0;

                total += montant;
                table.addCell(convertToLetters((int) montant) + " dinars");
                table.addCell(String.format(Locale.FRANCE, "%.3f", montant));
                table.addCell((c.getPrenom() != null ? c.getPrenom() : "") + " " + (c.getNom() != null ? c.getNom() : ""));
                table.addCell(c.getBanque() != null ? c.getBanque() : "—");
                table.addCell("Salaire " + moisEtAnnee);
                table.addCell(c.getCompteBancaire());
            }

            document.add(table);
            document.add(new Paragraph("\n\n"));

            Paragraph totalParagraph = new Paragraph("Montant total : " + String.format(Locale.FRANCE, "%.3f", total) + " DT");
            totalParagraph.setAlignment(Element.ALIGN_LEFT);
            totalParagraph.setSpacingAfter(5f);
            document.add(totalParagraph);

            Paragraph note = new Paragraph("*Cet ordre est établi en deux exemplaires (un pour la banque et un pour l'ESSAT)");
            note.setAlignment(Element.ALIGN_LEFT);
            note.setSpacingAfter(30f);
            document.add(note);

            Paragraph signature = new Paragraph();
            signature.add(new Chunk("Fait à Gabès, le " + dateDuJour + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            signature.add(new Chunk("Signature et cachet", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            signature.add(new Chunk("\n✓"));
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new Exception("Erreur lors de la génération de l'ordre de virement", e);
        }
    }
    private String convertToLetters(int number) {
        if (number == 0) return "zéro";

        StringBuilder words = new StringBuilder();

        if (number >= 1000000) {
            return String.valueOf(number); // Hors portée simple
        }

        int thousands = number / 1000;
        int rest = number % 1000;

        if (thousands > 0) {
            if (thousands == 1) {
                words.append("mille ");
            } else {
                words.append(convertBelowThousand(thousands)).append(" mille ");
            }
        }

        if (rest > 0) {
            words.append(convertBelowThousand(rest));
        }

        return words.toString().trim();
    }

    private String convertBelowThousand(int number) {
        String[] units = {
                "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
                "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
                "dix-sept", "dix-huit", "dix-neuf"
        };

        String[] tens = {
                "", "", "vingt", "trente", "quarante", "cinquante", "soixante",
                "soixante", "quatre-vingt", "quatre-vingt"
        };

        StringBuilder part = new StringBuilder();

        int hundreds = number / 100;
        int remainder = number % 100;

        if (hundreds > 0) {
            if (hundreds == 1) {
                part.append("cent");
            } else {
                part.append(units[hundreds]).append(" cent");
            }

            if (remainder == 0 && hundreds > 1) {
                part.append("s");
            }

            if (remainder != 0) {
                part.append(" ");
            }
        }

        if (remainder < 20) {
            part.append(units[remainder]);
        } else {
            int ten = remainder / 10;
            int unit = remainder % 10;

            if (ten == 7 || ten == 9) {
                part.append(tens[ten]).append("-").append(units[10 + unit]);
            } else {
                part.append(tens[ten]);
                if (unit == 1 && (ten != 8)) {
                    part.append("-et-un");
                } else if (unit > 0) {
                    part.append("-").append(units[unit]);
                } else if (ten == 8) {
                    part.append("s"); // ex : quatre-vingts
                }
            }
        }

        return part.toString();
    }
}


