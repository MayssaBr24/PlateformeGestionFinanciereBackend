package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.EnseignantPermanent;
import com.example.gestionfinance.auth.model.EnseignantVacataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Ajout de l'annotation Repository
public interface EnseignantPermanentRepository
        extends JpaRepository<EnseignantPermanent, Long> {
    boolean existsByEmail(String email);
    List<EnseignantPermanent> findByEtat(boolean etat);

}