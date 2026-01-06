package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.LoginRequest;
import com.app.sportify_backend.dto.RegisterRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.security.JwtService;
import com.app.sportify_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // ------------------ REGISTER ------------------
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> register(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            System.out.println("Register reçu: " + request.getEmail());
            User user = userService.registerUser(request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Inscription réussie",
                            "user", user
                    )
            );
        } catch (RuntimeException e) {
            if ("EMAIL_ALREADY_EXISTS".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("error", "Cet email est déjà utilisé")
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // ------------------ LOGIN ------------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        System.out.println("Login reçu pour: " + request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Générer access token
        String accessToken = userService.loginUser(request.getEmail(), request.getPassword());

        // Générer refresh token
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", user
        ));
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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token manquant");
        }

        try {
            String email = jwtService.extractEmail(refreshToken);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier que le refresh token est valide
            if (!jwtService.isTokenValid(refreshToken)) {
                return ResponseEntity.status(403).body("Refresh token invalide");
            }

            // Générer un nouveau access token
            String newAccessToken = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", refreshToken
            ));

        } catch (Exception e) {
            return ResponseEntity.status(403).body("Refresh token expiré ou invalide");
        }
    }
}
