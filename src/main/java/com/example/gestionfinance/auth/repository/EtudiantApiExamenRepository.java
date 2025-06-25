package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.Etudiant;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EtudiantApiExamenRepository extends JpaRepository<Etudiant, Long> {

    @Query("SELECT e FROM Etudiant e WHERE e.classe = :classe")
    List<Etudiant> findByClasseComplete(@Param("classe") String classe);
}
