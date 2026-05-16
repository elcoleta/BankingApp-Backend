package com.example.bankingapp.controller;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET /transactions/my — returns all transactions for the logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions(Principal principal) {
        List<TransactionDTO> transactions = transactionService.getMyTransactions(principal.getName());
        return ResponseEntity.ok(transactions);
    }
}