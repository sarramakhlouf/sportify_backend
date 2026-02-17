package com.app.sportify_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEPTIONS D'AUTHENTIFICATION ====================

    @ExceptionHandler(AccountNotEnabledException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotEnabledException(AccountNotEnabledException ex) {
        System.out.println("AccountNotEnabledException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "ACCOUNT_NOT_ENABLED");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        System.out.println("BadCredentialsException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "INVALID_CREDENTIALS");
        error.put("message", "Email ou mot de passe incorrect");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        System.out.println("AccessDeniedException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "ACCESS_DENIED");
        error.put("message", "Accès refusé. Vous n'avez pas les permissions nécessaires.");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // ==================== EXCEPTIONS MÉTIER ====================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        System.out.println("ResourceNotFoundException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "RESOURCE_NOT_FOUND");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
        System.out.println("UnauthorizedException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "UNAUTHORIZED");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    // ==================== EXCEPTIONS DE VALIDATION ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        System.out.println("IllegalArgumentException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "INVALID_ARGUMENT");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        System.out.println("IllegalStateException: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "INVALID_STATE");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // ==================== EXCEPTIONS RUNTIME ====================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        System.out.println("RuntimeException: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "RUNTIME_ERROR");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==================== EXCEPTION GÉNÉRIQUE ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        System.out.println("Exception générique: " + ex.getClass().getName());
        System.out.println("Message: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "INTERNAL_SERVER_ERROR");
        error.put("message", "Une erreur interne est survenue. Veuillez réessayer plus tard.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}