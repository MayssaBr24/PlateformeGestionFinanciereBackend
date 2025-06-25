package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.Paiement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Méthode 1: Calcul du total des paiements pour un étudiant donné
    @Query("SELECT COALESCE(SUM(p.montant), 0.00) FROM Paiement p WHERE p.etudiant.id = :etudiantId")
    BigDecimal getTotalPaiementByEtudiant(@Param("etudiantId") Long etudiantId);

    // Méthode 2: Recherche des paiements par ID étudiant (implémentation automatique)
    @EntityGraph(attributePaths = {"etudiant"})
    List<Paiement> findByEtudiantId(Long etudiantId);


    @Query("SELECT p FROM Paiement p JOIN FETCH p.etudiant ORDER BY p.date DESC")
    List<Paiement> findAllWithEtudiant();

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.etudiant.id = :etudiantId AND p.annule = false")
    BigDecimal getTotalPaiementNonAnnuleByEtudiant(@Param("etudiantId") Long etudiantId);

    List<Paiement> findByEtudiantIdOrderByDateDesc(Long etudiantId);


}