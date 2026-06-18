package com.example.bankingapp.service;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.dto.TransferRequest;
import com.example.bankingapp.exception.AuthException;
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

    public List<TransactionDTO> getMyTransactions(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        List<String> myIbans = accountRepository.findByUser(user).stream()
                .map(Account::getIban)
                .toList();

        return transactionRepository
                .findByFromIbanInOrToIbanInOrderByTimestampDesc(myIbans, myIbans)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public TransactionDTO transfer(String email, TransferRequest request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account fromAccount = accountRepository.findByIban(request.fromIban())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.fromIban()));

        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new AuthException("You do not own the source account");
        }

        Account toAccount = accountRepository.findByIban(request.toIban())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + request.toIban()));

        double amount = request.amount();

        // Minimum balance floor: balance after transfer must not drop below absoluteTransferLimit
        if (fromAccount.getBalance() - amount < fromAccount.getAbsoluteTransferLimit()) {
            throw new IllegalArgumentException(String.format(
                    "Transfer would bring balance below the minimum floor of €%.2f",
                    fromAccount.getAbsoluteTransferLimit()));
        }

        // Daily limit: sum of today's outgoing transfers + new amount must not exceed dailyTransferLimit
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double todayOutgoing = transactionRepository.sumTodayOutgoing(request.fromIban(), startOfDay);
        if (todayOutgoing + amount > fromAccount.getDailyTransferLimit()) {
            throw new IllegalArgumentException(String.format(
                    "Daily transfer limit exceeded. Already transferred €%.2f today, limit is €%.2f",
                    todayOutgoing, fromAccount.getDailyTransferLimit()));
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String description = (request.description() != null && !request.description().isBlank())
                ? request.description() : "Transfer";

        Transaction tx = transactionRepository.save(
                new Transaction(request.fromIban(), request.toIban(), amount, LocalDateTime.now(), description));

        return toDTO(tx);
    }

    @Transactional
    public TransactionDTO deposit(String email, String iban, double amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + iban));

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new AuthException("You do not own this account");
        }

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction tx = transactionRepository.save(
                new Transaction("ATM", iban, amount, LocalDateTime.now(), "ATM Deposit"));
        return toDTO(tx);
    }

    @Transactional
    public TransactionDTO withdraw(String email, String iban, double amount) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + iban));

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new AuthException("You do not own this account");
        }

        if (account.getBalance() - amount < 0) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient funds. Available balance: €%.2f", account.getBalance()));
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        Transaction tx = transactionRepository.save(
                new Transaction(iban, "ATM", amount, LocalDateTime.now(), "ATM Withdrawal"));
        return toDTO(tx);
    }

    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByTimestampDesc(pageable).map(this::toDTO);
    }

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(t.getId(), t.getFromIban(), t.getToIban(),
                t.getAmount(), t.getTimestamp(), t.getDescription());
    }
}
