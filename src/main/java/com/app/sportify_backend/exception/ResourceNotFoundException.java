package com.app.sportify_backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé avec %s : '%s'", resource, fieldName, fieldValue));
    }
}