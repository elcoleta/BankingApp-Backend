package com.example.bankingapp.dto;

import java.time.LocalDateTime;

public class TransactionDTO {

    private Long id;
    private String fromIban;
    private String toIban;
    private double amount;
    private LocalDateTime timestamp;
    private String description;

    public TransactionDTO(Long id, String fromIban, String toIban,
                          double amount, LocalDateTime timestamp, String description) {
        this.id = id;
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getFromIban() { return fromIban; }
    public String getToIban() { return toIban; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
}