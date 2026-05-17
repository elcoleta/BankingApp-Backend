package com.example.bankingapp.exception;

/**
 * Thrown when a transfer or withdrawal violates balance/limit rules.
 * Maps to HTTP 400 Bad Request.
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
