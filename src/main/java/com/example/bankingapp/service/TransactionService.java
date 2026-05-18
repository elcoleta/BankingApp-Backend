package com.example.bankingapp.service;

import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;

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

        // 2. Get all IBANs belonging to this user
        List<String> myIbans = accountRepository.findByUser(user)
                .stream()
                .map(account -> account.getIban())
                .toList();

        // 3. Find all transactions where user is sender OR receiver
        // 4. Convert each Transaction → TransactionDTO
        return transactionRepository
                .findByFromIbanInOrToIbanInOrderByTimestampDesc(myIbans, myIbans)
                .stream()
                .map(t -> new TransactionDTO(
                        t.getId(),
                        t.getFromIban(),
                        t.getToIban(),
                        t.getAmount(),
                        t.getTimestamp(),
                        t.getDescription()
                ))
                .toList();
    }
}