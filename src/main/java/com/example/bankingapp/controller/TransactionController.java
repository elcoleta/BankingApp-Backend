package com.example.bankingapp.controller;

import com.example.bankingapp.dto.ApiError;
import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.dto.TransferRequest;
import com.example.bankingapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Transactions", description = "Transfer funds and view transaction history")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "List my transactions (paginated)",
     description = "Returns all transactions involving the authenticated user's accounts, newest first.")
    @ApiResponse(responseCode = "200", description = "Page of transactions")
    @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/my")
    public ResponseEntity<Page<TransactionDTO>> getMyTransactions(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        return ResponseEntity.ok(transactionService.getMyTransactions(principal.getName(), pageable));
    }

    @Operation(summary = "Transfer funds", description = "Move money from one of the authenticated user's accounts to any other account.")
    @ApiResponse(responseCode = "201", description = "Transaction created")
    @ApiResponse(responseCode = "400", description = "Validation error or business rule violation",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Source account is not owned by the authenticated user",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO transfer(@RequestBody @Valid TransferRequest request, Principal principal) {
        return transactionService.transfer(principal.getName(), request);
    }
}
