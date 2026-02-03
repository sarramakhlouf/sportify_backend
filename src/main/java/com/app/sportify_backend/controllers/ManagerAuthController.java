package com.app.sportify_backend.controllers;

import com.app.sportify_backend.dto.LoginRequest;
import com.app.sportify_backend.dto.ManagerRegisterRequest;
import com.app.sportify_backend.exception.AccountNotEnabledException;
import com.app.sportify_backend.models.User;
import com.app.sportify_backend.repositories.UserRepository;
import com.app.sportify_backend.services.ManagerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/manager")
@RequiredArgsConstructor
public class ManagerAuthController {

    private final ManagerAuthService managerAuthService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerManager(@Validated @RequestBody ManagerRegisterRequest request) {
        try {
            User manager = managerAuthService.registerManager(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription réussie ! Votre compte est en attente de validation par un administrateur.");
            response.put("email", manager.getEmail());
            response.put("status", "PENDING_VALIDATION");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}