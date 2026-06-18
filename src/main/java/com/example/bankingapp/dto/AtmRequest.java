package com.example.bankingapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtmRequest(
        @NotBlank String iban,
        @NotNull @DecimalMin("0.01") Double amount
) {}
