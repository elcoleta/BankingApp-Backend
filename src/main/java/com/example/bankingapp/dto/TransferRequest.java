package com.example.bankingapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotBlank String fromIban,
        @NotBlank String toIban,
        @NotNull @DecimalMin("0.01") Double amount,
        String description
) {}
