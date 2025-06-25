package com.example.gestionfinance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class GestionfinanceApplicationTests {
    @Test
    public void contextLoads() {
        // Le test passe si le contexte Spring se charge correctement
    }
}