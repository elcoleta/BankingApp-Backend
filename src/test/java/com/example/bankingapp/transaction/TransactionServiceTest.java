package com.example.bankingapp.transaction;

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
import com.example.bankingapp.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private AppUserRepository userRepository;
    private TransactionService service;

    private AppUser owner;
    private AppUser otherUser;
    private Account activeAccount;
    private Account inactiveAccount;
    private Account otherAccount;

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String IBAN_A = "NL01BANK0000000001";
    private static final String IBAN_B = "NL01BANK0000000002";
    private static final String IBAN_C = "NL01BANK0000000003";

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        userRepository = mock(AppUserRepository.class);
        service = new TransactionService(transactionRepository, accountRepository, userRepository);

        owner = new AppUser("Alice", "Smith", OWNER_EMAIL, "111111111", "+31600000001", "hash");
        otherUser = new AppUser("Bob", "Jones", OTHER_EMAIL, "222222222", "+31600000002", "hash");

        // balance=500, floor=0, dailyLimit=1000
        activeAccount = new Account(IBAN_A, "CHECKING", 500.0, 0.0, 1000.0, true, owner);
        inactiveAccount = new Account(IBAN_C, "CHECKING", 500.0, 0.0, 1000.0, false, owner);
        otherAccount = new Account(IBAN_B, "CHECKING", 200.0, 0.0, 1000.0, true, otherUser);

        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(userRepository.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(otherUser));
        when(accountRepository.findByIban(IBAN_A)).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByIban(IBAN_B)).thenReturn(Optional.of(otherAccount));
        when(accountRepository.findByIban(IBAN_C)).thenReturn(Optional.of(inactiveAccount));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumTodayOutgoing(any(), any())).thenReturn(0.0);
    }

    // --- deposit ---

    @Test
    void depositAddsBalanceAndReturnsDTO() {
        TransactionDTO result = service.deposit(OWNER_EMAIL, IBAN_A, 100.0);

        assertThat(activeAccount.getBalance()).isEqualTo(600.0);
        assertThat(result.fromIban()).isEqualTo("ATM");
        assertThat(result.toIban()).isEqualTo(IBAN_A);
        assertThat(result.amount()).isEqualTo(100.0);
        verify(accountRepository).save(activeAccount);
    }

    @Test
    void depositThrowsWhenAccountNotFound() {
        when(accountRepository.findByIban("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deposit(OWNER_EMAIL, "UNKNOWN", 50.0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void depositThrowsWhenNotOwner() {
        assertThatThrownBy(() -> service.deposit(OTHER_EMAIL, IBAN_A, 50.0))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not own this account");
    }

    @Test
    void depositThrowsWhenAccountInactive() {
        assertThatThrownBy(() -> service.deposit(OWNER_EMAIL, IBAN_C, 50.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    // --- withdraw ---

    @Test
    void withdrawDeductsBalanceAndReturnsDTO() {
        TransactionDTO result = service.withdraw(OWNER_EMAIL, IBAN_A, 100.0);

        assertThat(activeAccount.getBalance()).isEqualTo(400.0);
        assertThat(result.fromIban()).isEqualTo(IBAN_A);
        assertThat(result.toIban()).isEqualTo("ATM");
        assertThat(result.amount()).isEqualTo(100.0);
    }

    @Test
    void withdrawThrowsWhenBalanceBelowFloor() {
        // floor=100, balance=500 → withdrawing 450 would leave 50 < 100
        Account flooredAccount = new Account(IBAN_A, "CHECKING", 500.0, 100.0, 1000.0, true, owner);
        when(accountRepository.findByIban(IBAN_A)).thenReturn(Optional.of(flooredAccount));

        assertThatThrownBy(() -> service.withdraw(OWNER_EMAIL, IBAN_A, 450.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum floor");
    }

    @Test
    void withdrawThrowsWhenDailyLimitExceeded() {
        when(transactionRepository.sumTodayOutgoing(eq(IBAN_A), any())).thenReturn(900.0);

        assertThatThrownBy(() -> service.withdraw(OWNER_EMAIL, IBAN_A, 200.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Daily limit exceeded");
    }

    @Test
    void withdrawThrowsWhenNotOwner() {
        assertThatThrownBy(() -> service.withdraw(OTHER_EMAIL, IBAN_A, 50.0))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void withdrawThrowsWhenAccountInactive() {
        assertThatThrownBy(() -> service.withdraw(OWNER_EMAIL, IBAN_C, 50.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    // --- transfer ---

    @Test
    void transferMovesBalanceBetweenAccounts() {
        TransferRequest request = new TransferRequest(IBAN_A, IBAN_B, 100.0, "Invoice #1");

        TransactionDTO result = service.transfer(OWNER_EMAIL, request);

        assertThat(activeAccount.getBalance()).isEqualTo(400.0);
        assertThat(otherAccount.getBalance()).isEqualTo(300.0);
        assertThat(result.fromIban()).isEqualTo(IBAN_A);
        assertThat(result.toIban()).isEqualTo(IBAN_B);
        assertThat(result.description()).isEqualTo("Invoice #1");
    }

    @Test
    void transferUsesDefaultDescriptionWhenBlank() {
        TransferRequest request = new TransferRequest(IBAN_A, IBAN_B, 50.0, "  ");

        TransactionDTO result = service.transfer(OWNER_EMAIL, request);

        assertThat(result.description()).isEqualTo("Transfer");
    }

    @Test
    void transferThrowsWhenSameAccount() {
        TransferRequest request = new TransferRequest(IBAN_A, IBAN_A, 100.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different");
    }

    @Test
    void transferThrowsWhenSourceNotOwned() {
        // owner tries to send from otherUser's account
        TransferRequest request = new TransferRequest(IBAN_B, IBAN_A, 50.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void transferThrowsWhenBelowMinimumBalance() {
        Account flooredAccount = new Account(IBAN_A, "CHECKING", 500.0, 100.0, 1000.0, true, owner);
        when(accountRepository.findByIban(IBAN_A)).thenReturn(Optional.of(flooredAccount));

        TransferRequest request = new TransferRequest(IBAN_A, IBAN_B, 450.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum floor");
    }

    @Test
    void transferThrowsWhenDailyLimitExceeded() {
        when(transactionRepository.sumTodayOutgoing(eq(IBAN_A), any())).thenReturn(900.0);

        TransferRequest request = new TransferRequest(IBAN_A, IBAN_B, 200.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Daily limit exceeded");
    }

    @Test
    void transferThrowsWhenSourceAccountInactive() {
        TransferRequest request = new TransferRequest(IBAN_C, IBAN_B, 50.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void transferThrowsWhenDestinationAccountInactive() {
        Account inactiveDestination = new Account(IBAN_B, "CHECKING", 200.0, 0.0, 1000.0, false, otherUser);
        when(accountRepository.findByIban(IBAN_B)).thenReturn(Optional.of(inactiveDestination));

        TransferRequest request = new TransferRequest(IBAN_A, IBAN_B, 50.0, null);

        assertThatThrownBy(() -> service.transfer(OWNER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }
}
