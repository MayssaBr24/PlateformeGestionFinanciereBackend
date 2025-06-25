package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.dto.HistoriqueEtudiantDto;
import com.example.gestionfinance.auth.dto.PaiementRequest;
import com.example.gestionfinance.auth.dto.PaiementResponseDto;
import com.example.gestionfinance.auth.dto.RemboursementRequest;
import com.example.gestionfinance.auth.model.CoordonneeEtudiant;
import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.model.Paiement;
import com.example.gestionfinance.auth.repository.EtudiantRepository;
import com.example.gestionfinance.auth.repository.PaiementRepository;
import com.example.gestionfinance.auth.repository.RemboursementRepository;
import com.example.gestionfinance.auth.service.PaiementService;
import com.example.gestionfinance.exception.ResourceNotFoundException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import com.itextpdf.layout.element.Cell;



@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "https://localhost")
public class PaiementController {

    private final PaiementRepository paiementRepository;
    private final EtudiantRepository etudiantRepository;
    private final PaiementService paiementService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Autowired
    private WebClient webClient;
    @Autowired
    private EtatFinancierService etatFinancierService;
    @Autowired
    private RemboursementRepository remboursementRepository;

    public List<CoordonneeEtudiant> appelerApiAvecWebClient() {
        return webClient.get()
                //.uri("http://192.168.1.85:8080/ESSAT_ERP_war_exploded/testmayssa")
                .uri("http://localhost/etudiant/Etudiant.json")
                .retrieve()
                .bodyToFlux(CoordonneeEtudiant.class)
                .collectList()
                .block(); // blocage uniquement si tu n'es pas en réactif pur
    }
    @Autowired
    public PaiementController(PaiementRepository paiementRepository,
                              EtudiantRepository etudiantRepository,
                              PaiementService paiementService) {
        this.paiementRepository = paiementRepository;
        this.etudiantRepository = etudiantRepository;
        this.paiementService = paiementService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addPaiement(@Valid @RequestBody PaiementRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId()).orElseGet(() -> {
            List<CoordonneeEtudiant> etudiants = appelerApiAvecWebClient();
            Optional<CoordonneeEtudiant> oCoordonneeEtudiant = etudiants.stream()
                    .filter(e -> e.getEtudiantId() == request.getEtudiantId())
                    .findFirst();

            if (oCoordonneeEtudiant.isPresent()) {
                CoordonneeEtudiant c = oCoordonneeEtudiant.get();
                Etudiant newEtudiant = new Etudiant(
                        (long) c.getEtudiantId(),
                        c.getNumInscription(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getClasse(),
                        0.0,
                        null
                );
                return etudiantRepository.save(newEtudiant);
            }

            // Si on ne trouve pas l'étudiant, on peut soit lancer une exception, soit retourner null
            // Ici on va lancer une exception (mieux pour éviter des erreurs null plus loin)
            throw new RuntimeException("Étudiant introuvable même après appel à l'API");
        });
                //.orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé pour l'ID : " + request.getEtudiantId()));
        // Mettre à jour le solde en utilisant request.getMontant()
        double nouveauSolde = etudiant.getSolde() != null ?
                etudiant.getSolde() + request.getMontant().doubleValue() :
                request.getMontant().doubleValue();

        etudiant.setSolde(nouveauSolde);
        etudiantRepository.save(etudiant);

        Paiement paiement = new Paiement();
        paiement.setMontant(BigDecimal.valueOf(request.getMontant()));
        paiement.setDate(LocalDate.parse(request.getDate()));
        paiement.setEtudiant(etudiant);

        Paiement savedPaiement = paiementRepository.save(paiement);
        updateEtatFinancier(etudiant.getId());

        // Recharger l'étudiant avec les données fraîches
        Etudiant updatedEtudiant = etudiantRepository.findById(etudiant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé après mise à jour"));

        // Retourner les données mises à jour
        Map<String, Object> response = new HashMap<>();
        response.put("paiement", mapToDto(savedPaiement));
        response.put("etatFinancier", updatedEtudiant.getEtatFinancier());
        response.put("totalPaye", paiementRepository.getTotalPaiementByEtudiant(etudiant.getId()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/Etudiant/{etudiantId}/total")
    public ResponseEntity<BigDecimal> getTotalPaiement(@PathVariable Long etudiantId) {
        BigDecimal total = paiementRepository.getTotalPaiementByEtudiant(etudiantId);
        return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
    }

    @GetMapping("/Etudiant/{etudiantId}")
    public ResponseEntity<List<PaiementResponseDto>> getPaiementsByEtudiant(@PathVariable Long etudiantId) {
        List<PaiementResponseDto> paiements = paiementService.getPaiementsByEtudiant(etudiantId);
        return ResponseEntity.ok(paiements);
    }

    private void updateEtatFinancier(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        BigDecimal totalPaye = paiementRepository.getTotalPaiementByEtudiant(etudiantId);
        if(totalPaye == null) {
            totalPaye = BigDecimal.ZERO;
        }

        BigDecimal montantAttendu = etatFinancierService.getMontantAttenduParClasse(etudiant.getClasse());

        System.out.println("Debug - Total payé: " + totalPaye); // Log de débogage
        System.out.println("Debug - Montant attendu: " + montantAttendu); // Log de débogage

        if (montantAttendu.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pourcentage = totalPaye
                    .divide(montantAttendu, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            System.out.println("Debug - Pourcentage calculé: " + pourcentage); // Log de débogage

            etudiant.setEtatFinancier(pourcentage.min(new BigDecimal("100")).doubleValue());
        } else {
            etudiant.setEtatFinancier(0.0);
        }

        etudiantRepository.save(etudiant);
    }
    @GetMapping("/{paiementId}/recus")
    public ResponseEntity<byte[]> generateRecuPdf(@PathVariable Long paiementId) throws IOException {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        Etudiant etudiant = paiement.getEtudiant();
        BigDecimal totalPaye = paiementRepository.getTotalPaiementByEtudiant(etudiant.getId());
        BigDecimal montantAttendu = etatFinancierService.getMontantAttenduParClasse(etudiant.getClasse());

        double pourcentage = (montantAttendu != null && montantAttendu.compareTo(BigDecimal.ZERO) > 0)
                ? totalPaye.divide(montantAttendu, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        // Couleurs et polices
        Color primaryColor = new DeviceRgb(59, 130, 246);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header : Logo + Titre
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 3})).useAllAvailableWidth();

        try {
            Image logo = new Image(ImageDataFactory.create("src/main/resources/static/logo.png"))
                    .setAutoScale(true)
                    .setWidth(70);
            header.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
        } catch (Exception e) {
            header.addCell(new Cell().add(new Paragraph("ÉCOLE").setFont(bold)).setBorder(Border.NO_BORDER));
        }

        header.addCell(new Cell()
                .add(new Paragraph("ÉCOLE SUPÉRIEURE DES SCIENCES APPLIQUÉES")
                        .setFont(bold)
                        .setFontSize(14)
                        .setFontColor(primaryColor))
                .add(new Paragraph("Reçu de Paiement Officiel").setFont(bold).setFontSize(12))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

        document.add(header);
        document.add(new Paragraph("\n"));

        // Section Étudiant
        document.add(buildInfoTable("ÉTUDIANT", new String[][]{
                {"Nom complet:", etudiant.getNom() + " " + etudiant.getPrenom()},
                {"Classe:", etudiant.getClasse()},
                {"N° Inscription:", etudiant.getnumInscription()}
        }, bold, regular));

        // Section Paiement
        document.add(buildInfoTable("DÉTAILS DU PAIEMENT", new String[][]{
                {"Référence:", "PAI-" + paiement.getId()},
                {"Date:", paiement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))},
                {"Montant:", String.format("%,.2f DT", paiement.getMontant())},
                {"Total payé:", String.format("%,.2f DT", totalPaye)},
                {"Montant attendu:", String.format("%,.2f DT", montantAttendu)},
                {"Pourcentage:", String.format("%.2f%%", pourcentage)}
        }, bold, regular));

        // Ligne de séparation
        document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(10).setMarginBottom(10));

        // Signature
        document.add(new Paragraph("Signature et cachet:")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFont(regular)
                .setMarginTop(30));
        document.add(new Paragraph("Date: ___________")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFont(regular));

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }


    private Cell createStyledCell(String content, PdfFont font, boolean isSectionTitle) {
        Color sectionColor = new DeviceRgb(59, 130, 246); // Bleu clair
        Color textColor = new DeviceRgb(0, 0, 0);          // Noir

        Paragraph paragraph = new Paragraph(content)
                .setFont(font)
                .setFontSize(isSectionTitle ? 12 : 10)
                .setFontColor(isSectionTitle ? sectionColor : textColor)
                .setBold(); // Gras pour les titres

        Cell cell = new Cell().add(paragraph)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);

        if (isSectionTitle) {
            cell.setProperty(Property.COLSPAN, 2);
            // Étendre sur deux colonnes
        }

        return cell;
    }




    private PaiementResponseDto mapToDto(Paiement paiement) {
        PaiementResponseDto dto = new PaiementResponseDto();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDate(paiement.getDate());
        dto.setEtudiantId(paiement.getEtudiant().getId());
        return dto;
    }


    private Table buildInfoTable(String title, String[][] data, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        table.addCell(createStyledCell(title.toUpperCase(), bold, true));
        table.addCell(createStyledCell("", bold, false));

        for (String[] row : data) {
            table.addCell(createStyledCell(row[0], regular, false));
            table.addCell(createStyledCell(row[1], regular, false));
        }

        return table;
    }
    @GetMapping("/etudiants/all")
    public ResponseEntity<List<HistoriqueEtudiantDto>> getHistoriquePaiements() {
        try {
            List<Paiement> paiements = paiementRepository.findAllWithEtudiant();
            Map<Long, HistoriqueEtudiantDto> historiqueMap = new HashMap<>();

            for (Paiement p : paiements) {
                if (p.getEtudiant() == null || p.getDate() == null) {
                    continue;
                }

                String mois = p.getDate()
                        .getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                        .toUpperCase();

                HistoriqueEtudiantDto dto = historiqueMap.computeIfAbsent(
                        p.getEtudiant().getId(),
                        id -> {
                            HistoriqueEtudiantDto newDto = new HistoriqueEtudiantDto();
                            newDto.setEtudiantId(id);
                            newDto.setEtudiantNom(p.getEtudiant().getNom());
                            newDto.setEtudiantPrenom(p.getEtudiant().getPrenom());
                            newDto.setEtudiantClasse(p.getEtudiant().getClasse());
                            newDto.setNumInscription(p.getEtudiant().getnumInscription());
                            return newDto;
                        }
                );

                BigDecimal montant = p.getMontant() != null ? p.getMontant() : BigDecimal.ZERO;
                dto.getPaiementsParMois().merge(mois, montant, BigDecimal::add);
            }

            historiqueMap.values().forEach(dto ->
                    dto.setTotalPaye(
                            dto.getPaiementsParMois().values().stream()
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    )
            );

            return ResponseEntity.ok(new ArrayList<>(historiqueMap.values()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/remboursements/{etudiantId}/recu")
    public ResponseEntity<byte[]> generateRecuRemboursement(
            @PathVariable Long etudiantId,
            @RequestParam Double montant) throws IOException {

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Génération de la date
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        // Couleurs et polices
        Color primaryColor = new DeviceRgb(59, 130, 246);
        Color redColor = new DeviceRgb(220, 53, 69);
        Color grayColor = new DeviceRgb(128, 128, 128);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Création des deux copies
        String[] mentions = {
                "EXEMPLAIRE ÉTUDIANT - À CONSERVER PAR L'ÉTUDIANT",
                "EXEMPLAIRE ADMINISTRATION - À CONSERVER PAR LE DIRECTEUR"
        };

        for (int i = 0; i < 2; i++) {
            // En-tête
            Table header = new Table(UnitValue.createPercentArray(new float[]{1, 3}))
                    .useAllAvailableWidth();

            try {
                Image logo = new Image(ImageDataFactory.create("src/main/resources/static/logo.png"))
                        .setAutoScale(true)
                        .setWidth(70);
                header.addCell(new Cell().add(logo)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            } catch (Exception e) {
                header.addCell(new Cell().add(new Paragraph("ÉCOLE").setFont(bold))
                        .setBorder(Border.NO_BORDER));
            }

            header.addCell(new Cell()
                    .add(new Paragraph("ÉCOLE SUPÉRIEURE DES SCIENCES APPLIQUÉES")
                            .setFont(bold)
                            .setFontSize(14)
                            .setFontColor(primaryColor))
                    .add(new Paragraph("ATTESTATION DE REMBOURSEMENT")
                            .setFont(bold)
                            .setFontSize(12))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            document.add(header);
            document.add(new Paragraph("\n"));

            // Section Étudiant
            document.add(createInfoTable("INFORMATIONS ÉTUDIANT", new String[][]{
                    {"Nom complet:", etudiant.getNom() + " " + etudiant.getPrenom()},
                    {"Classe:", etudiant.getClasse()},
                    {"N° Inscription:", etudiant.getnumInscription()}
            }, bold, regular));

            // Section Remboursement
            Table remboursementTable = new Table(UnitValue.createPercentArray(new float[]{1, 3}))
                    .useAllAvailableWidth()
                    .setMarginTop(10);

            remboursementTable.addCell(createTitleCell("DÉTAILS DU REMBOURSEMENT", bold));
            remboursementTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            remboursementTable.addCell(createRegularCell("Date du remboursement:", regular));
            remboursementTable.addCell(createRegularCell(formattedDate, regular));

            remboursementTable.addCell(createRegularCell("Montant remboursé:", regular));
            remboursementTable.addCell(new Cell()
                    .add(new Paragraph(String.format("%,.2f DT", montant))
                            .setFont(bold)
                            .setFontColor(redColor)
                            .setFontSize(12))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5));

            document.add(remboursementTable);

            // Signature et cachet avec date
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Fait à Gabès, le " + formattedDate)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFont(regular));

            document.add(new Paragraph("Signature et cachet:")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFont(regular)
                    .setMarginTop(20));

            // Mention en bas de page (sur la même page)
            Paragraph mention = new Paragraph(mentions[i])
                    .setFontSize(8)
                    .setFontColor(grayColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(
                            document.getLeftMargin(),
                            document.getBottomMargin() - 20, // Positionnement en bas
                            document.getPageEffectiveArea(PageSize.A4).getWidth())
                    .setBorderTop(new SolidBorder(grayColor, 0.5f))
                    .setPaddingTop(5);

            document.add(mention);

            if (i < 1) {
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            }
        }

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=remboursement_" + formattedDate.replace("/", "-") + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }


    private Cell createTitleCell(String content, PdfFont font) {
        Paragraph paragraph = new Paragraph(content.toUpperCase())
                .setFont(font)
                .setFontSize(12)
                .setFontColor(new DeviceRgb(59, 130, 246));

        return new Cell()
                .add(paragraph)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell createRegularCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Table createInfoTable(String title, String[][] data, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        table.addCell(createTitleCell(title, bold));
        table.addCell(new Cell().setBorder(Border.NO_BORDER));

        for (String[] row : data) {
            table.addCell(createRegularCell(row[0], regular));
            table.addCell(createRegularCell(row[1], regular));
        }

        return table;
    }

    @PostMapping("/remboursement")
    public ResponseEntity<?> enregistrerRemboursement(@RequestBody RemboursementRequest request) {
        try {
            // Vérification et création du remboursement
            if (request.getMontant() > 0) {
                request.setMontant(-request.getMontant());
            }

            Paiement remboursement = new Paiement();
            remboursement.setMontant(BigDecimal.valueOf(request.getMontant()));
            remboursement.setDate(LocalDate.parse(request.getDate()));

            Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

            remboursement.setEtudiant(etudiant);
            paiementRepository.save(remboursement);

            // Réinitialisation simple à 0 sans calcul
            etudiant.setEtatFinancier(0.0);
            etudiantRepository.save(etudiant);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du remboursement: " + e.getMessage());
        }
    }





}

















