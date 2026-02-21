package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.ManagerRegisterRequest;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.services.ManagerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/manager")
@RequiredArgsConstructor
public class ManagerAuthController {

    private final ManagerAuthService managerAuthService;
    private final UserRepository userRepository;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerManager(
            @RequestPart("data") @Validated ManagerRegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            User manager = managerAuthService.registerManager(request, image);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription réussie ! Votre compte est en attente de validation par un administrateur.");
            response.put("email", manager.getEmail());
            response.put("status", "PENDING_VALIDATION");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload de l'image"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}