package com.example.bankingapp.controller;

import com.example.bankingapp.dto.ApiError;
import com.example.bankingapp.dto.AtmRequest;
import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "ATM", description = "Cash deposits and withdrawals")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/atm")
public class AtmController {

    private final TransactionService transactionService;

    public AtmController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Deposit cash", description = "Credit cash to the specified account. The account must belong to the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Deposit transaction created")
    @ApiResponse(responseCode = "400", description = "Validation error or account not active",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Account does not belong to the authenticated user",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO deposit(@RequestBody @Valid AtmRequest request, Principal principal) {
        return transactionService.deposit(principal.getName(), request.iban(), request.amount());
    }

    @Operation(summary = "Withdraw cash", description = "Debit cash from the specified account. Respects minimum balance floor and daily limit.")
    @ApiResponse(responseCode = "201", description = "Withdrawal transaction created")
    @ApiResponse(responseCode = "400", description = "Insufficient funds, daily limit exceeded, or account not active",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Account does not belong to the authenticated user",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO withdraw(@RequestBody @Valid AtmRequest request, Principal principal) {
        return transactionService.withdraw(principal.getName(), request.iban(), request.amount());
    }
}
