package com.example.bankingapp.config;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository,
                           AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        AppUser employee = new AppUser(
                "Bank",
                "Employee",
                "employee@bank.com",
                "000000001",
                "+31600000001",
                passwordEncoder.encode("employee123")
        );
        employee.setStatus(AppUser.Status.APPROVED);
        employee.setRole(AppUser.Role.EMPLOYEE);
        userRepository.save(employee);

        AppUser customer = new AppUser(
                "Achraf",
                "El Moussaoui",
                "achraf@example.com",
                "123456789",
                "+31612345678",
                passwordEncoder.encode("password123")
        );
        customer.setStatus(AppUser.Status.APPROVED);
        userRepository.save(customer);

        accountRepository.save(new Account(
                "NL91INHO0417164300",
                "CHECKING",
                1500.00,
                100.00,
                500.00,
                true,
                customer
        ));

        accountRepository.save(new Account(
                "NL91INHO0417164301",
                "SAVINGS",
                3200.00,
                100.00,
                500.00,
                true,
                customer
        ));

        transactionRepository.save(new Transaction(
                "NL91INHO0417164300", "NL20INGB0001234567",
                250.00, LocalDateTime.now().minusDays(1), "Rent payment"));

        transactionRepository.save(new Transaction(
                "NL02ABNA0123456789", "NL91INHO0417164300",
                1000.00, LocalDateTime.now().minusDays(3), "Salary"));

        transactionRepository.save(new Transaction(
                "NL91INHO0417164300", "NL91INHO0417164301",
                200.00, LocalDateTime.now().minusDays(5), "Transfer to savings"));

        transactionRepository.save(new Transaction(
                "NL91INHO0417164301", "NL20INGB0001234567",
                150.00, LocalDateTime.now().minusDays(7), "Subscription"));

        System.out.println("Test data loaded:");
        System.out.println("  employee@bank.com / employee123 (EMPLOYEE)");
        System.out.println("  achraf@example.com / password123 (CUSTOMER, APPROVED)");
    }
}
