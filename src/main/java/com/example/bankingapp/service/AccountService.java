package com.example.bankingapp.service;

import com.example.bankingapp.dto.AccountDTO;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AppUserRepository userRepository;

    public AccountService(AccountRepository accountRepository, AppUserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // Get all accounts for the currently logged-in user
    public List<AccountDTO> getMyAccounts(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return accountRepository.findByUser(user).stream()
                .map(account -> new AccountDTO(
                        account.getId(),
                        account.getIban(),
                        account.getAccountType(),
                        account.getBalance(),
                        account.getAbsoluteTransferLimit(),
                        account.getDailyTransferLimit(),
                        account.isActive()
                ))
                .toList();
    }
}
