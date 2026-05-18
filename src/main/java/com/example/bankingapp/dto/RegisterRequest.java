package com.example.bankingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 1, max = 50) String firstName,
        @NotBlank @Size(min = 1, max = 50) String lastName,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{9}", message = "BSN must be exactly 9 digits") String bsn,
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
