package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.model.EnseignantVacataire;
import com.example.gestionfinance.auth.repository.EnseignantVacataireRepository;
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
public class OrdreDeVirementVacataireService {

    @Autowired
    private EnseignantVacataireRepository vacataireRepo;

    @Autowired
    private EnseignantVacataireService vacataireService;

    public byte[] genererOrdreDeVirement(LocalDate date, List<Long> vacataireIds) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            List<EnseignantVacataire> vacataires = vacataireRepo.findAllById(vacataireIds).stream()
                    .filter(v -> v.getCompteBancaire() != null && !v.getCompteBancaire().trim().isEmpty()
                            && "virement bancaire".equalsIgnoreCase(v.getType()))
                    .collect(Collectors.toList());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
            String moisEtAnnee = date.format(formatter);
            String dateDuJour = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Titre
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titre = new Paragraph("Ordre de Virement des Enseignants Vacataires - " + moisEtAnnee, fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20f);
            document.add(titre);

            // Informations ESSAT
            Paragraph essatInfo = new Paragraph();
            essatInfo.add(new Chunk("ESSAT Privée de Gabès", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            essatInfo.add(new Chunk("\nCompte N° : 41 10 51144 8\n\n"));
            document.add(essatInfo);
            essatInfo.setSpacingAfter(15f);

            // Introduction
            Paragraph intro = new Paragraph("Nous venons par la présente vous demander de bien vouloir exécuter les ordres de virements suivants :\n\n");
            intro.setSpacingAfter(15f);
            document.add(intro);

            // Table avec colonnes alignées sur OrdreDeVirementService
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

            for (EnseignantVacataire v : vacataires) {
                vacataireService.payerSalaire(v.getId());
                v = vacataireRepo.findById(v.getId()).orElse(v);
                double montant = v.getSalaireNet() != null ? v.getSalaireNet() : 0.0;

                total += montant;
                table.addCell(convertToLetters((int) montant) + " dinars");
                table.addCell(String.format(Locale.FRANCE, "%.3f", montant));
                table.addCell((v.getPrenom() != null ? v.getPrenom() : "") + " " + (v.getNom() != null ? v.getNom() : ""));
                table.addCell(v.getBanque() != null ? v.getBanque() : "—");
                table.addCell("Salaire " + moisEtAnnee);
                table.addCell(v.getCompteBancaire());
            }

            document.add(table);
            document.add(new Paragraph("\n\n"));

            // Montant total aligné à gauche
            Paragraph totalParagraph = new Paragraph("Montant total : " + String.format(Locale.FRANCE, "%.3f", total) + " DT");
            totalParagraph.setAlignment(Element.ALIGN_LEFT);
            totalParagraph.setSpacingAfter(5f);
            document.add(totalParagraph);

            // Note sur les exemplaires alignée à gauche
            Paragraph note = new Paragraph("*Cet ordre est établi en deux exemplaires (un pour la banque et un pour l'ESSAT)");
            note.setAlignment(Element.ALIGN_LEFT);
            note.setSpacingAfter(30f);
            document.add(note);

            // Signature et date alignés à droite
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


