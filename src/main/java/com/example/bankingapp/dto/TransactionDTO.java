package com.example.bankingapp.dto;

import java.time.LocalDateTime;

public record TransactionDTO(
        Long id,
        String fromIban,
        String toIban,
        double amount,
        LocalDateTime timestamp,
        String description
) {}
