package com.example.bankingapp.dto;

public class AccountDTO {

    private Long id;
    private String iban;
    private String accountType;
    private double balance;
    private double absoluteTransferLimit;
    private double dailyTransferLimit;
    private boolean isActive;

    public AccountDTO(Long id, String iban, String accountType, double balance,
                      double absoluteTransferLimit, double dailyTransferLimit, boolean isActive) {
        this.id = id;
        this.iban = iban;
        this.accountType = accountType;
        this.balance = balance;
        this.absoluteTransferLimit = absoluteTransferLimit;
        this.dailyTransferLimit = dailyTransferLimit;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public String getIban() { return iban; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public double getAbsoluteTransferLimit() { return absoluteTransferLimit; }
    public double getDailyTransferLimit() { return dailyTransferLimit; }
    public boolean isActive() { return isActive; }
}
