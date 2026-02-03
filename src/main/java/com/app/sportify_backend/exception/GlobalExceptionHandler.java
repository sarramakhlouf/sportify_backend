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

    @ExceptionHandler(AccountNotEnabledException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotEnabledException(AccountNotEnabledException ex) {
        System.out.println("AccountNotEnabledException capturée: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "ACCOUNT_NOT_ENABLED");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        System.out.println("BadCredentialsException capturée: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "INVALID_CREDENTIALS");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        System.out.println("RuntimeException capturée par GlobalExceptionHandler: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        System.out.println("IllegalArgumentException capturée: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        System.out.println("AccessDeniedException capturée: " + ex.getMessage());
        System.out.println("Ceci ne devrait pas se produire pour les endpoints publics!");
        System.out.println("Vérifiez la configuration Spring Security");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Accès refusé. Veuillez vérifier vos permissions.");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        System.out.println("Exception générique capturée: " + ex.getClass().getName());
        System.out.println("Message: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "Une erreur est survenue. Veuillez réessayer");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
