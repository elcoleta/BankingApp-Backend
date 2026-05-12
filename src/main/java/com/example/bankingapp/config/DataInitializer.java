package com.example.bankingapp.config;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Loads test data on startup for development purposes.
 * This runs once when the application starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository,
                           AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only seed if no users exist
        if (userRepository.count() > 0) return;

        // Create test customer
        AppUser customer = new AppUser("achraf", passwordEncoder.encode("password123"));
        userRepository.save(customer);

        // Create checking account for customer
        accountRepository.save(new Account(
                "NL91INHO0417164300",
                "CHECKING",
                1500.00,
                100.00,   // absolute transfer limit
                500.00,   // daily transfer limit
                true,
                customer
        ));

        // Create savings account for customer
        accountRepository.save(new Account(
                "NL91INHO0417164301",
                "SAVINGS",
                3200.00,
                100.00,
                500.00,
                true,
                customer
        ));

        System.out.println("✓ Test data loaded: user 'achraf' with 2 accounts");
    }
}
