package com.example.gestionfinance.auth.service;

import com.example.gestionfinance.auth.model.ForgetPwdToken;
import com.example.gestionfinance.auth.repository.ForgetPwdTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Autowired
    private ForgetPwdTokenRepository forgetPwdTokenRepository;


    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000; // 24 heures
    private static final long RESET_TOKEN_EXPIRATION = 3600000; // 1 heure

    // Génère un token JWT standard pour l'authentification
    public String generateToken(String email) {
        return buildToken(new HashMap<>(), email, ACCESS_TOKEN_EXPIRATION);
    }

    // Génère un token pour la réinitialisation de mot de passe
    public String generatePasswordResetToken(String email) {
        String token = buildToken(new HashMap<>(), email, RESET_TOKEN_EXPIRATION);
        ForgetPwdToken forgetPwdToken = new ForgetPwdToken();
        forgetPwdToken.setToken(token);
        forgetPwdTokenRepository.save(forgetPwdToken);
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valide un token avec UserDetails
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Valide n'importe quel token
    public boolean validateToken(String token) {

        // verifier si le tocken est ds la base de donnee !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // si non false

        ForgetPwdToken forgetPwdToken = forgetPwdTokenRepository.findByToken(token);
        if (forgetPwdToken == null) {
            return false;
        }


        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            forgetPwdTokenRepository.delete(forgetPwdToken);
            return false;
        }
    }

    // Extrait l'email d'un token (alias de extractUsername pour cohérence)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrait l'email d'un token de réinitialisation
    public String extractEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Méthode générique pour extraire des claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Vérifie si un token est expiré
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extrait la date d'expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrait toutes les claims d'un token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Alias pour extractEmail (maintenu pour compatibilité)
    public String extractUsername(String token) {
        return extractEmail(token);
    }

    public void invalidateToken(String token) {
        // supprimer le tocken de la bd
        ForgetPwdToken forgetPwdToken = forgetPwdTokenRepository.findByToken(token);
        if (forgetPwdToken != null) {
            forgetPwdTokenRepository.delete(forgetPwdToken);
        }
    }
}