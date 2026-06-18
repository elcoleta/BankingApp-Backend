package com.example.bankingapp.controller;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.dto.TransferRequest;
import com.example.bankingapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions(Principal principal) {
        return ResponseEntity.ok(transactionService.getMyTransactions(principal.getName()));
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO transfer(@RequestBody @Valid TransferRequest request, Principal principal) {
        return transactionService.transfer(principal.getName(), request);
    }
}
