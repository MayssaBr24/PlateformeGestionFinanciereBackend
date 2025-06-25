package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.CadreAdmnistratif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CadreAdmnistrativRepository extends JpaRepository<CadreAdmnistratif, Long> {
    boolean existsByEmail(String email);
    Optional<CadreAdmnistratif> findByEmail(String email);
}