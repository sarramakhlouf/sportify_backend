package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.LoginRequest;
import com.app.sportify_backend.dto.RegisterRequest;
import com.app.sportify_backend.dto.UpdateProfileRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.security.JwtService;
import com.app.sportify_backend.services.PlayerAuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PlayerAuthController {

    private final PlayerAuthService playerAuthService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping( value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<?> register(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        RegisterRequest request = objectMapper.readValue(data, RegisterRequest.class);

        System.out.println("Register reçu: " + request.getEmail());
        User user = playerAuthService.registerUser(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Inscription réussie",
                        "user", user
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        System.out.println("Login reçu pour: " + request.getEmail());

        String accessToken = playerAuthService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", user
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token manquant"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setRefreshToken(null);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Déconnexion réussie")
        );
    }

    @GetMapping("/auto-login")
    public ResponseEntity<User> autoLogin(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Auto-login header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token manquant ou mal formaté");
        }
        String token = authHeader.substring(7);
        User user = playerAuthService.autoLogin(token);

        System.out.println("Utilisateur récupéré: " + user.getEmail());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyManager(@PathVariable String userId) {
        User user = playerAuthService.verifyManager(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Manager vérifié",
                "user", user
        ));
    }

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@RequestParam String email) {
        System.out.println("OTP demandé pour: " + email);
        User user = playerAuthService.getUserByEmail(email);
        playerAuthService.generateOtp(user);

        return ResponseEntity.ok(Map.of("message", "OTP envoyé à l'email " + email));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean valid = playerAuthService.verifyOtp(email, otp);
        if (!valid) {
            throw new RuntimeException("OTP invalide ou expiré");
        }
        return ResponseEntity.ok(Map.of("message", "OTP validé"));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        playerAuthService.resetPassword(email, newPassword);
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

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    @PostMapping(
            value = "/users/{id}/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<User> updateProfile(
            @PathVariable String id,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {

        UpdateProfileRequest request =
                objectMapper.readValue(data, UpdateProfileRequest.class);

        // Vérification côté backend pour le mot de passe
        if (request.getPassword() != null && !request.getPassword().isEmpty() &&
                (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty())) {
            throw new RuntimeException("Le mot de passe actuel est requis pour changer le mot de passe");
        }

        User updatedUser = playerAuthService.updateProfile(
                id,
                request,
                request.getCurrentPassword(),
                image
        );
        updatedUser.setPassword(null);

        return ResponseEntity.ok(updatedUser);
    }
}
