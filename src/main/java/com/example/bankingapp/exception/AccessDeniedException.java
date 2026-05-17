package com.example.bankingapp.exception;

/**
 * Thrown when a user tries to access an account that doesn't belong to them.
 * Maps to HTTP 403 Forbidden.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
