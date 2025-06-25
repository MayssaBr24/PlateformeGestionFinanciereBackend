package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.Remboursement;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {
    List<Remboursement> findByEtudiantId(Long etudiantId);


    @Query("SELECT r FROM Remboursement r WHERE r.etudiant.id = :etudiantId AND r.annule = false")
    List<Remboursement> findRemboursementsNonAnnulesByEtudiant(@Param("etudiantId") Long etudiantId);

    // Pas besoin de SUM car les montants sont déjà négatifs
    @Query("SELECT COALESCE(SUM(r.montant), 0) FROM Remboursement r WHERE r.etudiant.id = :etudiantId AND r.annule = false")
    Double getTotalRemboursementNonAnnuleByEtudiant(@Param("etudiantId") Long etudiantId);
}