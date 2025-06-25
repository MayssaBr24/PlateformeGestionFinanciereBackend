package com.example.gestionfinance.auth.dto;

import com.example.gestionfinance.auth.model.Paiement;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaiementResponseDto {
    private Long id;
    private BigDecimal montant;
    private LocalDate date;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantClasse;
    private String etudiantNumInscription;
    private BigDecimal totalPaye;






    //  utilisé si besoin
    public PaiementResponseDto(Long id, BigDecimal montant, LocalDate date, Long etudiantId) {

        this.etudiantId = id;
        this.id = id;
        this.montant = montant;
        this.date = date;
    }

}
