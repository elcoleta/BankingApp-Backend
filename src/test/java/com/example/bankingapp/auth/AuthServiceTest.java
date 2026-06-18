package com.example.bankingapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.bankingapp.dto.AuthRequest;
import com.example.bankingapp.dto.AuthResponse;
import com.example.bankingapp.dto.RegisterRequest;
import com.example.bankingapp.exception.AuthException;
import com.example.bankingapp.exception.DuplicateEmailException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private AppUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerSavesUserWithEncodedPassword() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(new RegisterRequest("Jan", "Jansen", " demo@example.com ", "123456789", "+31600000000", "password123"));

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Jan", "Jansen", "demo@example.com", "123456789", "+31600000000", "password123")))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("An account with this email already exists");
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        AppUser user = new AppUser("Jan", "Jansen", "demo@example.com", "123456789", "+31600000000", "encoded-password");
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(new AuthRequest("demo@example.com", "password123"));

        assertThat(response.email()).isEqualTo("demo@example.com");
        assertThat(response.token()).isNotBlank();
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void loginRejectsInvalidPassword() {
        AppUser user = new AppUser("Jan", "Jansen", "demo@example.com", "123456789", "+31600000000", "encoded-password");
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest("demo@example.com", "wrong")))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid email or password");
    }
}
