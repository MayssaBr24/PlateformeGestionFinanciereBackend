package com.example.gestionfinance.auth.repository;

import com.example.gestionfinance.auth.model.ForgetPwdToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgetPwdTokenRepository extends JpaRepository<ForgetPwdToken, Long> {
    ForgetPwdToken findByToken(String token);
}
