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

        // ── Employee ────────────────────────────────────────────────────────────
        AppUser employee = new AppUser("Bank", "Employee", "employee@bank.com",
                "000000001", "+31600000001", passwordEncoder.encode("employee123"));
        employee.setStatus(AppUser.Status.APPROVED);
        employee.setRole(AppUser.Role.EMPLOYEE);
        userRepository.save(employee);

        // ── Customer 1: Achraf ──────────────────────────────────────────────────
        AppUser achraf = new AppUser("Achraf", "El Moussaoui", "achraf@example.com",
                "123456789", "+31612345678", passwordEncoder.encode("password123"));
        achraf.setStatus(AppUser.Status.APPROVED);
        userRepository.save(achraf);

        Account achrafChecking = accountRepository.save(new Account(
                "NL91INHO0417164300", "CHECKING", 1500.00, 100.00, 500.00, true, achraf));
        Account achrafSavings = accountRepository.save(new Account(
                "NL91INHO0417164301", "SAVINGS", 3200.00, 100.00, 500.00, true, achraf));

        // ── Customer 2: Jan ─────────────────────────────────────────────────────
        AppUser jan = new AppUser("Jan", "de Vries", "jan@example.com",
                "987654321", "+31687654321", passwordEncoder.encode("password123"));
        jan.setStatus(AppUser.Status.APPROVED);
        userRepository.save(jan);

        Account janChecking = accountRepository.save(new Account(
                "NL29INHO0417164302", "CHECKING", 800.00, 100.00, 500.00, true, jan));
        accountRepository.save(new Account(
                "NL29INHO0417164303", "SAVINGS", 1200.00, 100.00, 500.00, true, jan));

        // ── Transactions spread across dates for date-filter demo ────────────────
        LocalDateTime now = LocalDateTime.now();

        // Achraf outgoing
        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), "NL20INGB0001234567",
                250.00, now.minusDays(1), "Rent payment"));

        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), janChecking.getIban(),
                100.00, now.minusDays(3), "Loan repayment"));

        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), achrafSavings.getIban(),
                200.00, now.minusDays(5), "Transfer to savings"));

        transactionRepository.save(new Transaction(
                achrafSavings.getIban(), "NL20INGB0001234567",
                150.00, now.minusDays(7), "Subscription"));

        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), janChecking.getIban(),
                75.00, now.minusDays(10), "Dinner split"));

        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), "NL20INGB0001234567",
                320.00, now.minusDays(14), "Insurance"));

        transactionRepository.save(new Transaction(
                achrafChecking.getIban(), janChecking.getIban(),
                50.00, now.minusDays(20), "Gift"));

        // Achraf incoming
        transactionRepository.save(new Transaction(
                "NL02ABNA0123456789", achrafChecking.getIban(),
                1000.00, now.minusDays(3), "Salary"));

        transactionRepository.save(new Transaction(
                janChecking.getIban(), achrafChecking.getIban(),
                200.00, now.minusDays(8), "Refund"));

        // Jan transactions
        transactionRepository.save(new Transaction(
                janChecking.getIban(), "NL20INGB0001234567",
                90.00, now.minusDays(2), "Utilities"));

        transactionRepository.save(new Transaction(
                "NL02ABNA0123456789", janChecking.getIban(),
                1200.00, now.minusDays(4), "Salary"));

        System.out.println("Seed data loaded:");
        System.out.println("  employee@bank.com / employee123 (EMPLOYEE)");
        System.out.println("  achraf@example.com / password123 (CUSTOMER, APPROVED)");
        System.out.println("    Checking: " + achrafChecking.getIban() + " | Savings: " + achrafSavings.getIban());
        System.out.println("  jan@example.com / password123 (CUSTOMER, APPROVED)");
        System.out.println("    Checking: " + janChecking.getIban());
    }
}
