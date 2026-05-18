package com.example.bankingapp.dto;

public record CustomerAccountDTO(
        Long accountId,
        String iban,
        String accountType,
        double balance,
        double absoluteTransferLimit,
        double dailyTransferLimit,
        boolean active,
        String customerName,
        String customerEmail,
        String customerStatus
) {
}
