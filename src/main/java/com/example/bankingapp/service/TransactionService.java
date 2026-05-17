package com.example.bankingapp.service;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.dto.TransferRequest;
import com.example.bankingapp.exception.AccessDeniedException;
import com.example.bankingapp.exception.InsufficientFundsException;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AppUserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              AppUserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<TransactionDTO> getMyTransactions(String username) {
        // 1. Find the logged-in user
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 2. Get all IBANs belonging to this user
        List<String> myIbans = accountRepository.findByUser(user)
                .stream()
                .map(Account::getIban)
                .toList();

        // 3. Find all transactions where user is sender OR receiver
        // 4. Convert each Transaction → TransactionDTO
        return transactionRepository
                .findByFromIbanInOrToIbanInOrderByTimestampDesc(myIbans, myIbans)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Transfer money between two accounts that both belong to the authenticated user.
     */
    @Transactional
    public TransactionDTO transferBetweenOwnAccounts(TransferRequest request, String username) {
        // Resolve accounts
        Account fromAccount = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.fromIban()));
        Account toAccount = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.toIban()));

        // Ownership check — both accounts must belong to the authenticated user
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Account " + request.fromIban() + " does not belong to you");
        }
        if (!toAccount.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Account " + request.toIban() + " does not belong to you");
        }

        double amount = request.amount();

        // Amount must be positive (also enforced by @Positive on the DTO)
        if (amount <= 0) {
            throw new InsufficientFundsException("Transfer amount must be positive");
        }

        // Absolute transfer limit: balance after withdrawal cannot go below the limit
        if (fromAccount.getBalance() - amount < fromAccount.getAbsoluteTransferLimit()) {
            throw new InsufficientFundsException(
                    "Insufficient balance: transfer would bring balance below the absolute limit of "
                    + fromAccount.getAbsoluteTransferLimit());
        }

        // Daily transfer limit check
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double sentToday = transactionRepository.sumAmountSentFromIbanSince(request.fromIban(), startOfDay);
        if (sentToday + amount > fromAccount.getDailyTransferLimit()) {
            throw new InsufficientFundsException(
                    "Daily transfer limit of " + fromAccount.getDailyTransferLimit()
                    + " would be exceeded. Already sent today: " + sentToday);
        }

        // Execute the transfer
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String description = request.description() != null ? request.description() : "Own account transfer";
        Transaction tx = transactionRepository.save(
                new Transaction(request.fromIban(), request.toIban(), amount, LocalDateTime.now(), description));

        return toDTO(tx);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(
                t.getId(),
                t.getFromIban(),
                t.getToIban(),
                t.getAmount(),
                t.getTimestamp(),
                t.getDescription()
        );
    }
}