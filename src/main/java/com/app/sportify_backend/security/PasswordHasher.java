package com.app.sportify_backend.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123"; // le mot de passe que tu veux
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Mot de passe hashé : " + encodedPassword);
    }
}