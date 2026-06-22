package com.example.bankingapp.transaction;

import com.example.bankingapp.config.BearerTokenAuthenticationFilter;
import com.example.bankingapp.config.SecurityConfig;
import com.example.bankingapp.controller.TransactionController;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, BearerTokenAuthenticationFilter.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AppUserRepository userRepository;

    private static final String TOKEN = "test-token-txn";
    private static final String EMAIL = "txn@example.com";
    private static final String IBAN_FROM = "NL01BANK0000000001";
    private static final String IBAN_TO = "NL01BANK0000000002";

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser("Txn", "User", EMAIL, "333333333", "+31600000003", "hash");
        user.setToken(TOKEN);
        when(userRepository.findByToken(TOKEN)).thenReturn(Optional.of(user));
    }

    @Test
    void getMyTransactionsReturns200WithPage() throws Exception {
        TransactionDTO dto = new TransactionDTO(1L, IBAN_FROM, IBAN_TO, 75.0, LocalDateTime.now(), "Test");
        when(transactionService.getMyTransactions(eq(EMAIL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/transactions/my")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromIban").value(IBAN_FROM))
                .andExpect(jsonPath("$.content[0].amount").value(75.0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMyTransactionsReturns401WhenNoToken() throws Exception {
        mockMvc.perform(get("/transactions/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyTransactionsRespectsPageParameters() throws Exception {
        when(transactionService.getMyTransactions(eq(EMAIL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/transactions/my?page=0&size=5")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void transferReturns201WithTransactionBody() throws Exception {
        TransactionDTO dto = new TransactionDTO(2L, IBAN_FROM, IBAN_TO, 100.0, LocalDateTime.now(), "Rent");
        when(transactionService.transfer(eq(EMAIL), any())).thenReturn(dto);

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "%s",
                                  "toIban": "%s",
                                  "amount": 100.0,
                                  "description": "Rent"
                                }
                                """.formatted(IBAN_FROM, IBAN_TO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromIban").value(IBAN_FROM))
                .andExpect(jsonPath("$.toIban").value(IBAN_TO))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Rent"));
    }

    @Test
    void transferReturns400WhenAmountMissing() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "%s",
                                  "toIban": "%s"
                                }
                                """.formatted(IBAN_FROM, IBAN_TO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void transferReturns400WhenFromIbanBlank() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "",
                                  "toIban": "%s",
                                  "amount": 100.0
                                }
                                """.formatted(IBAN_TO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void transferReturns403WhenNotOwner() throws Exception {
        when(transactionService.transfer(eq(EMAIL), any()))
                .thenThrow(new ForbiddenException("You do not own this account"));

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "%s",
                                  "toIban": "%s",
                                  "amount": 100.0
                                }
                                """.formatted(IBAN_FROM, IBAN_TO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not own this account"));
    }

    @Test
    void transferReturns404WhenAccountNotFound() throws Exception {
        when(transactionService.transfer(eq(EMAIL), any()))
                .thenThrow(new ResourceNotFoundException("Account not found: " + IBAN_FROM));

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "%s",
                                  "toIban": "%s",
                                  "amount": 100.0
                                }
                                """.formatted(IBAN_FROM, IBAN_TO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: " + IBAN_FROM));
    }

    @Test
    void transferReturns400WhenBusinessRuleViolated() throws Exception {
        when(transactionService.transfer(eq(EMAIL), any()))
                .thenThrow(new IllegalArgumentException("Daily limit exceeded. Already sent €900.00 today, limit is €1000.00"));

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromIban": "%s",
                                  "toIban": "%s",
                                  "amount": 200.0
                                }
                                """.formatted(IBAN_FROM, IBAN_TO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
