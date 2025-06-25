package com.example.gestionfinance.auth.controller;

import com.example.gestionfinance.auth.dto.AuthResponse;
import com.example.gestionfinance.auth.dto.LoginRequest;
import com.example.gestionfinance.auth.dto.PaiementRequest;
import com.example.gestionfinance.auth.dto.PaiementResponseDto;
import com.example.gestionfinance.auth.model.Etudiant;
import com.example.gestionfinance.auth.model.Paiement;
import com.example.gestionfinance.auth.model.User;
import com.example.gestionfinance.auth.repository.EtudiantRepository;
import com.example.gestionfinance.auth.repository.PaiementRepository;
import com.example.gestionfinance.auth.service.EmailService;
import com.example.gestionfinance.auth.service.UserService;
import com.example.gestionfinance.auth.service.JwtService;
import com.example.gestionfinance.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "https://localhost")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthController(UserService userService, JwtService jwtService, EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public Principal getUser(Principal principal) {
        return principal;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> handlePasswordReset(
            @RequestParam String token,
            @RequestParam String password) {

        try {
            // 1. Valider le token
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.badRequest().body("Token invalide ou expiré");
            }

            // 2. Extraire l'email
            String email = jwtService.extractEmailFromToken(token);

            // 3. Réinitialiser le mot de passe
            userService.resetPassword(email, password);

            // invalidate tocken
             jwtService.invalidateToken(token);


            return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            User user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            String token = jwtService.generateToken(user.getEmail());

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId().longValue(),
                    user.getEmail(),
                    "Login successful"
            );

            return ResponseEntity.ok().body(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            // Correction ici : Remplacement de "templates.email" par "email"
            String email = request.get("email");

            if (!userService.userExists(email)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Email not found"));
            }

            String resetToken = jwtService.generatePasswordResetToken(email);

            // enregistrer le tocken ds la base !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!




            emailService.sendPasswordResetEmail(email, resetToken);

            return ResponseEntity.ok()
                    .body(Map.of("message", "Password reset link has been sent to your email"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in forgot password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error processing your request"));
        }
    }

    @GetMapping(value = "/reset-password", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showResetPasswordPage(@RequestParam String token) {
        try {
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang='fr'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Réinitialisation du mot de passe</title>" +
                    "    <style>" +
                    "        body {" +
                    "            font-family: 'Segoe UI', system-ui, sans-serif;" +
                    "            background: linear-gradient(135deg, #f0f7ff 0%, #3b82f6 100%);" +
                    "            display: flex;" +
                    "            justify-content: center;" +
                    "            align-items: center;" +
                    "            min-height: 100vh;" +
                    "            margin: 0;" +
                    "            color: #1e293b;" +
                    "        }" +
                    "        .card {" +
                    "            background: white;" +
                    "            padding: 2.5rem;" +
                    "            border-radius: 12px;" +
                    "            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);" +
                    "            width: 100%;" +
                    "            max-width: 420px;" +
                    "        }" +
                    "        h1 {" +
                    "            color: #60a5fa;" + // Bleu très clair pour le titre
                    "            text-align: center;" +
                    "            margin-bottom: 2rem;" +
                    "            font-size: 1.8rem;" +
                    "            font-weight: 600;" +
                    "            text-shadow: 0 1px 2px rgba(0,0,0,0.1);" +
                    "        }" +
                    "        .form-group {" +
                    "            margin-bottom: 1.8rem;" +
                    "            padding: 0 0.5rem;" +  // Alignement corrigé
                    "        }" +
                    "        label {" +
                    "            display: block;" +
                    "            margin-bottom: 0.8rem;" +
                    "            font-weight: 500;" +
                    "            color: #334155;" +
                    "            font-size: 0.95rem;" +
                    "        }" +
                    "        input[type='password'] {" +
                    "            width: 100%;" +
                    "            padding: 1rem;" +  // Padding uniforme
                    "            border: 1px solid #e2e8f0;" +
                    "            border-radius: 8px;" +
                    "            font-size: 1rem;" +
                    "            transition: all 0.2s ease;" +
                    "            background-color: #f8fafc;" +
                    "        }" +
                    "        input[type='password']:focus {" +
                    "            border-color: #3b82f6;" +
                    "            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);" +
                    "            outline: none;" +
                    "            background-color: white;" +
                    "        }" +
                    "        button {" +
                    "            background: #60a5fa;" +  // Bleu plus clair pour le bouton
                    "            color: white;" +
                    "            border: none;" +
                    "            padding: 1.1rem;" +
                    "            width: 100%;" +
                    "            border-radius: 8px;" +
                    "            font-size: 1.05rem;" +
                    "            font-weight: 600;" +
                    "            cursor: pointer;" +
                    "            transition: all 0.2s ease;" +
                    "            box-shadow: 0 4px 6px rgba(59, 130, 246, 0.2);" +
                    "        }" +
                    "        button:hover {" +
                    "            background: #3b82f6;" +
                    "            transform: translateY(-2px);" +
                    "            box-shadow: 0 6px 12px rgba(59, 130, 246, 0.25);" +
                    "        }" +
                    "        .password-info {" +
                    "            font-size: 0.85rem;" +
                    "            color: #64748b;" +
                    "            margin-top: 0.5rem;" +
                    "            text-align: left;" +
                    "            padding-left: 0.2rem;" +
                    "        }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='card'>" +
                    "        <h1>Réinitialisation du mot de passe</h1>" +
                    "        <form action='/api/auth/reset-password' method='post'>" +
                    "            <input type='hidden' name='token' value='" + token + "'>" +
                    "            <div class='form-group'>" +
                    "                <label for='password'>Nouveau mot de passe</label>" +
                    "                <input type='password' id='password' name='password' required " +
                    "                       minlength='8' placeholder='Entrez votre mot de passe'>" +
                    "                <div class='password-info'>Minimum 8 caractères</div>" +
                    "            </div>" +
                    "            <button type='submit'>Confirmer</button>" +
                    "        </form>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            return ResponseEntity.ok(htmlContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error");
        }
    }

    @GetMapping("/check-template")
    public ResponseEntity<String> checkTemplate() {
        boolean exists = new ClassPathResource("templates/reset-password.html").exists();
        return ResponseEntity.ok("Template exists: " + exists);
    }

    @GetMapping("/test-template")
    public String testTemplate(Model model) {
        model.addAttribute("token", "test-token");
        return "reset-password";
    }
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false); // Récupère la session si elle existe
        if (session != null) {
            session.invalidate(); // Détruire la session
        }
        Map<String, String> result = new HashMap<>();
        result.put("message", "Déconnecté avec succès");
        return result; // Renvoie un JSON
    }






}