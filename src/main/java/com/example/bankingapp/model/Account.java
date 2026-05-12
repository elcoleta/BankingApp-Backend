package com.example.bankingapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(nullable = false)
    private String accountType; // CHECKING or SAVINGS

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private double absoluteTransferLimit;

    @Column(nullable = false)
    private double dailyTransferLimit;

    @Column(nullable = false)
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    protected Account() {}

    public Account(String iban, String accountType, double balance,
                   double absoluteTransferLimit, double dailyTransferLimit,
                   boolean isActive, AppUser user) {
        this.iban = iban;
        this.accountType = accountType;
        this.balance = balance;
        this.absoluteTransferLimit = absoluteTransferLimit;
        this.dailyTransferLimit = dailyTransferLimit;
        this.isActive = isActive;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getIban() { return iban; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public double getAbsoluteTransferLimit() { return absoluteTransferLimit; }
    public double getDailyTransferLimit() { return dailyTransferLimit; }
    public boolean isActive() { return isActive; }
    public AppUser getUser() { return user; }

    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { isActive = active; }
    public void setAbsoluteTransferLimit(double limit) { this.absoluteTransferLimit = limit; }
    public void setDailyTransferLimit(double limit) { this.dailyTransferLimit = limit; }
}
