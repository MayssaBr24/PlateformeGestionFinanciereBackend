package com.example.gestionfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.gestionfinance") // Incluez tous les sous-packages
public class GestionfinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestionfinanceApplication.class, args);
    }
}