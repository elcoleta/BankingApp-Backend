package com.example.bankingapp.controller;

import com.example.bankingapp.dto.AccountDTO;
import com.example.bankingapp.dto.AtmRequest;
import com.example.bankingapp.service.AtmService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/atm")
public class AtmController {

    private final AtmService atmService;

    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    /**
     * POST /atm/withdraw
     * Withdraw cash from the authenticated user's account.
     */
    @PostMapping("/withdraw")
    public ResponseEntity<AccountDTO> withdraw(
            @Valid @RequestBody AtmRequest request,
            Principal principal
    ) {
        AccountDTO result = atmService.withdraw(request, principal.getName());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /atm/deposit
     * Deposit cash into the authenticated user's account.
     */
    @PostMapping("/deposit")
    public ResponseEntity<AccountDTO> deposit(
            @Valid @RequestBody AtmRequest request,
            Principal principal
    ) {
        AccountDTO result = atmService.deposit(request, principal.getName());
        return ResponseEntity.ok(result);
    }
}
