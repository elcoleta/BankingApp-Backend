package com.example.bankingapp.controller;

import com.example.bankingapp.dto.AccountDTO;
import com.example.bankingapp.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // GET /accounts/my — returns all accounts for the logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<AccountDTO>> getMyAccounts(Principal principal) {
        List<AccountDTO> accounts = accountService.getMyAccounts(principal.getName());
        return ResponseEntity.ok(accounts);
    }
}
