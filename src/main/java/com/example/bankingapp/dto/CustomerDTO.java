package com.example.bankingapp.dto;

public record CustomerDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber,
        String status
) {
}
