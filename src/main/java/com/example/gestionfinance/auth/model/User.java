package com.example.gestionfinance.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@Setter
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Méthodes obligatoires de l'interface UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourne une liste vide car nous n'utilisons pas de rôles pour l'instant
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.email; // Utilise l'email comme nom d'utilisateur
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Le compte n'est jamais expiré
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Le compte n'est jamais verrouillé
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Les informations d'identification ne sont jamais expirées
    }

    @Override
    public boolean isEnabled() {
        return true; // Le compte est toujours activé
    }
}