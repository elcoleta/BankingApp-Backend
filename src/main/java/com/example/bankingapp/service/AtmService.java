package com.example.bankingapp.service;

import com.example.bankingapp.dto.AccountDTO;
import com.example.bankingapp.dto.AtmRequest;
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

@Service
public class AtmService {

    private static final String ATM = "ATM";

    private final AccountRepository accountRepository;
    private final AppUserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AtmService(AccountRepository accountRepository,
                      AppUserRepository userRepository,
                      TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Withdraw cash from the authenticated user's account via ATM.
     */
    @Transactional
    public AccountDTO withdraw(AtmRequest request, String username) {
        Account account = resolveOwnedAccount(request.iban(), username);
        double amount = request.amount();

        if (amount <= 0) {
            throw new InsufficientFundsException("Withdrawal amount must be positive");
        }

        // Absolute limit check
        if (account.getBalance() - amount < account.getAbsoluteTransferLimit()) {
            throw new InsufficientFundsException(
                    "Insufficient balance: withdrawal would bring balance below the absolute limit of "
                    + account.getAbsoluteTransferLimit());
        }

        // Daily limit check (outgoing ATM transactions count toward daily limit)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double sentToday = transactionRepository.sumAmountSentFromIbanSince(request.iban(), startOfDay);
        if (sentToday + amount > account.getDailyTransferLimit()) {
            throw new InsufficientFundsException(
                    "Daily withdrawal limit of " + account.getDailyTransferLimit()
                    + " would be exceeded. Already withdrawn today: " + sentToday);
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        transactionRepository.save(
                new Transaction(request.iban(), ATM, amount, LocalDateTime.now(), "ATM Withdrawal"));

        return toDTO(account);
    }

    /**
     * Deposit cash into the authenticated user's account via ATM.
     */
    @Transactional
    public AccountDTO deposit(AtmRequest request, String username) {
        Account account = resolveOwnedAccount(request.iban(), username);
        double amount = request.amount();

        if (amount <= 0) {
            throw new InsufficientFundsException("Deposit amount must be positive");
        }

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        transactionRepository.save(
                new Transaction(ATM, request.iban(), amount, LocalDateTime.now(), "ATM Deposit"));

        return toDTO(account);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Account resolveOwnedAccount(String iban, String username) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + iban));

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Account " + iban + " does not belong to you");
        }
        return account;
    }

    private AccountDTO toDTO(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getIban(),
                account.getAccountType(),
                account.getBalance(),
                account.getAbsoluteTransferLimit(),
                account.getDailyTransferLimit(),
                account.isActive()
        );
    }
}
