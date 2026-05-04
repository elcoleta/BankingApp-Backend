package com.example.bankingapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.bankingapp.dto.AuthRequest;
import com.example.bankingapp.dto.AuthResponse;
import com.example.bankingapp.exception.AuthException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

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
    void registerCreatesUserWithEncodedPasswordAndToken() {
        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(new AuthRequest(" demo ", "password123"));

        assertThat(response.username()).isEqualTo("demo");
        assertThat(response.token()).isNotBlank();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("demo")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new AuthRequest("demo", "password123")))
                .isInstanceOf(AuthException.class)
                .hasMessage("Username is already taken");
    }

    @Test
    void loginReturnsNewTokenForValidPassword() {
        AppUser user = new AppUser("demo", "encoded-password");
        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        AuthResponse response = authService.login(new AuthRequest("demo", "password123"));

        assertThat(response.username()).isEqualTo("demo");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginRejectsInvalidPassword() {
        AppUser user = new AppUser("demo", "encoded-password");
        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest("demo", "wrong-password")))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid username or password");
    }
}
