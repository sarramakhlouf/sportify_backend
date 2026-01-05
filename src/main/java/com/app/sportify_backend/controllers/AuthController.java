package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.LoginRequest;
import com.app.sportify_backend.dto.RegisterRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permet les requêtes depuis Flutter
public class AuthController {

    private final UserService userService;

    // ------------------ REGISTER ------------------
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        System.out.println("Register reçu: " + request.getEmail());
        User user = userService.registerUser(request, image);

        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie",
                "user", user
        ));
    }

    // ------------------ LOGIN ------------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        System.out.println("Login reçu pour: " + request.getEmail());
        String token = userService.loginUser(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(Map.of("token", token));
    }

    // ------------------ AUTO-LOGIN ------------------
    @GetMapping("/auto-login")
    public ResponseEntity<User> autoLogin(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Auto-login header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token manquant ou mal formaté");
        }
        String token = authHeader.substring(7);
        User user = userService.autoLogin(token);

        System.out.println("Utilisateur récupéré: " + user.getEmail());
        return ResponseEntity.ok(user);
    }

    // ------------------ VERIFY MANAGER ------------------
    @PostMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyManager(@PathVariable String userId) {
        User user = userService.verifyManager(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Manager vérifié",
                "user", user
        ));
    }

    // ------------------ FORGOT PASSWORD ------------------
    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@RequestParam String email) {
        System.out.println("OTP demandé pour: " + email);
        User user = userService.getUserByEmail(email);
        userService.generateOtp(user);

        return ResponseEntity.ok(Map.of("message", "OTP envoyé à l'email " + email));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean valid = userService.verifyOtp(email, otp);
        if (!valid) {
            throw new RuntimeException("OTP invalide ou expiré");
        }
        return ResponseEntity.ok(Map.of("message", "OTP validé"));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        userService.resetPassword(email, newPassword);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}
