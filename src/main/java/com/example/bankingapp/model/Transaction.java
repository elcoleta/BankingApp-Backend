package com.example.bankingapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromIban;

    @Column(nullable = false)
    private String toIban;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String description;

    protected Transaction() {}

    public Transaction(String fromIban, String toIban, double amount,
                       LocalDateTime timestamp, String description) {
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
