package com.example.bankingapp.dto;

import java.util.List;

public record CustomerSearchDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        List<String> ibans
) {}
