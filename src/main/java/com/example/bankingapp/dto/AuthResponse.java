package com.example.bankingapp.dto;

public record AuthResponse(String token, String email, String status, String role) {
}
