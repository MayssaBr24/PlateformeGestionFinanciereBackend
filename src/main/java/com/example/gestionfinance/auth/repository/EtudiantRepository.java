package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.Etudiant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByNomContainingOrPrenomContaining(String nom, String prenom);


}


