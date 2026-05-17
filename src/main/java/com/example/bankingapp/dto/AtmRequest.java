package com.example.bankingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AtmRequest(
        @NotBlank String iban,
        @Positive double amount
) {
}
