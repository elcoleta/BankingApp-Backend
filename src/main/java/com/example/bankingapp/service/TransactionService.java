package com.example.bankingapp.service;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.dto.TransferRequest;
import com.example.bankingapp.exception.ForbiddenException;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<TransactionDTO> getMyTransactions(String email, Pageable pageable) {
        AppUser user = findUser(email);
        List<String> myIbans = accountRepository.findByUser(user).stream()
                .map(Account::getIban)
                .toList();
        return transactionRepository
                .findByFromIbanInOrToIbanInOrderByTimestampDesc(myIbans, myIbans, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public TransactionDTO transfer(String email, TransferRequest request) {
        if (request.fromIban().equals(request.toIban())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        AppUser user = findUser(email);

        Account fromAccount = findAccount(request.fromIban());
        validateOwner(fromAccount, user);
        validateActive(fromAccount);

        Account toAccount = findAccount(request.toIban());
        validateActive(toAccount);

        double amount = request.amount();
        validateMinimumBalance(fromAccount, amount);
        validateDailyLimit(fromAccount, amount);

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String description = (request.description() != null && !request.description().isBlank())
                ? request.description() : "Transfer";

        return toDTO(transactionRepository.save(
                new Transaction(request.fromIban(), request.toIban(), amount, LocalDateTime.now(), description)));
    }

    @Transactional
    public TransactionDTO deposit(String email, String iban, double amount) {
        Account account = findAccount(iban);
        validateOwner(account, findUser(email));
        validateActive(account);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        return toDTO(transactionRepository.save(
                new Transaction("ATM", iban, amount, LocalDateTime.now(), "ATM Deposit")));
    }

    @Transactional
    public TransactionDTO withdraw(String email, String iban, double amount) {
        Account account = findAccount(iban);
        validateOwner(account, findUser(email));
        validateActive(account);
        validateMinimumBalance(account, amount);
        validateDailyLimit(account, amount);

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        return toDTO(transactionRepository.save(
                new Transaction(iban, "ATM", amount, LocalDateTime.now(), "ATM Withdrawal")));
    }

    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByTimestampDesc(pageable).map(this::toDTO);
    }

    // --- private helpers ---

    private AppUser findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Account findAccount(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + iban));
    }

    private void validateOwner(Account account, AppUser user) {
        if (!account.getUser().getEmail().equals(user.getEmail())) {
            throw new ForbiddenException("You do not own this account");
        }
    }

    private void validateActive(Account account) {
        if (!account.isActive()) {
            throw new IllegalArgumentException("Account " + account.getIban() + " is not active");
        }
    }

    private void validateMinimumBalance(Account account, double amount) {
        if (account.getBalance() - amount < account.getAbsoluteTransferLimit()) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient funds. Balance after operation (€%.2f) would fall below the minimum floor of €%.2f",
                    account.getBalance() - amount, account.getAbsoluteTransferLimit()));
        }
    }

    private void validateDailyLimit(Account account, double amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double todayOutgoing = transactionRepository.sumTodayOutgoing(account.getIban(), startOfDay);
        if (todayOutgoing + amount > account.getDailyTransferLimit()) {
            throw new IllegalArgumentException(String.format(
                    "Daily limit exceeded. Already sent €%.2f today, limit is €%.2f",
                    todayOutgoing, account.getDailyTransferLimit()));
        }
    }

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(t.getId(), t.getFromIban(), t.getToIban(),
                t.getAmount(), t.getTimestamp(), t.getDescription());
    }
}
