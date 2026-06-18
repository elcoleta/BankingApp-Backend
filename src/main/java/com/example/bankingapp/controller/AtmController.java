package com.example.bankingapp.controller;

import com.example.bankingapp.dto.AtmRequest;
import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/atm")
public class AtmController {

    private final TransactionService transactionService;

    public AtmController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO deposit(@RequestBody @Valid AtmRequest request, Principal principal) {
        return transactionService.deposit(principal.getName(), request.iban(), request.amount());
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO withdraw(@RequestBody @Valid AtmRequest request, Principal principal) {
        return transactionService.withdraw(principal.getName(), request.iban(), request.amount());
    }
}
