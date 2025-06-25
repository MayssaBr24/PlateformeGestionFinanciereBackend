package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.EnseignantVacataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnseignantVacataireRepository extends JpaRepository<EnseignantVacataire, Long> {
    boolean existsByEmail(String email);
    List<EnseignantVacataire> findByEtat(boolean etat);

}