package com.example.bankingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank String fromIban,
        @NotBlank String toIban,
        @Positive double amount,
        String description
) {
}
