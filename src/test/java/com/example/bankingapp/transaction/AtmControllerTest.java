package com.example.bankingapp.transaction;

import com.example.bankingapp.config.BearerTokenAuthenticationFilter;
import com.example.bankingapp.config.SecurityConfig;
import com.example.bankingapp.controller.AtmController;
import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.exception.ForbiddenException;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtmController.class)
@Import({SecurityConfig.class, BearerTokenAuthenticationFilter.class})
class AtmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AppUserRepository userRepository;

    private static final String TOKEN = "test-token-atm";
    private static final String EMAIL = "atm@example.com";
    private static final String IBAN = "NL01BANK0000000001";

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser("ATM", "User", EMAIL, "111111111", "+31600000001", "hash");
        user.setToken(TOKEN);
        when(userRepository.findByToken(TOKEN)).thenReturn(Optional.of(user));
    }

    @Test
    void depositReturns201WithTransactionBody() throws Exception {
        TransactionDTO dto = new TransactionDTO(1L, "ATM", IBAN, 200.0, LocalDateTime.now(), "ATM Deposit");
        when(transactionService.deposit(eq(EMAIL), eq(IBAN), eq(200.0))).thenReturn(dto);

        mockMvc.perform(post("/atm/deposit")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 200.0}
                                """.formatted(IBAN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value("ATM"))
                .andExpect(jsonPath("$.toIban").value(IBAN))
                .andExpect(jsonPath("$.amount").value(200.0));
    }

    @Test
    void depositReturns401WhenNoToken() throws Exception {
        mockMvc.perform(post("/atm/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 100.0}
                                """.formatted(IBAN)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void depositReturns400WhenAmountMissing() throws Exception {
        mockMvc.perform(post("/atm/deposit")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s"}
                                """.formatted(IBAN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void depositReturns400WhenAmountTooLow() throws Exception {
        mockMvc.perform(post("/atm/deposit")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 0.0}
                                """.formatted(IBAN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void depositReturns403WhenNotOwner() throws Exception {
        when(transactionService.deposit(eq(EMAIL), eq(IBAN), eq(100.0)))
                .thenThrow(new ForbiddenException("You do not own this account"));

        mockMvc.perform(post("/atm/deposit")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 100.0}
                                """.formatted(IBAN)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not own this account"));
    }

    @Test
    void depositReturns404WhenAccountNotFound() throws Exception {
        when(transactionService.deposit(eq(EMAIL), eq(IBAN), eq(100.0)))
                .thenThrow(new ResourceNotFoundException("Account not found: " + IBAN));

        mockMvc.perform(post("/atm/deposit")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 100.0}
                                """.formatted(IBAN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: " + IBAN));
    }

    @Test
    void withdrawReturns201WithTransactionBody() throws Exception {
        TransactionDTO dto = new TransactionDTO(2L, IBAN, "ATM", 50.0, LocalDateTime.now(), "ATM Withdrawal");
        when(transactionService.withdraw(eq(EMAIL), eq(IBAN), eq(50.0))).thenReturn(dto);

        mockMvc.perform(post("/atm/withdraw")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 50.0}
                                """.formatted(IBAN)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value(IBAN))
                .andExpect(jsonPath("$.toIban").value("ATM"))
                .andExpect(jsonPath("$.amount").value(50.0));
    }

    @Test
    void withdrawReturns400WhenInsufficientFunds() throws Exception {
        when(transactionService.withdraw(eq(EMAIL), eq(IBAN), eq(999999.0)))
                .thenThrow(new IllegalArgumentException("Insufficient funds. Balance after operation would fall below the minimum floor"));

        mockMvc.perform(post("/atm/withdraw")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iban": "%s", "amount": 999999.0}
                                """.formatted(IBAN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
