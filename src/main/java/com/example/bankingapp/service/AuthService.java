package com.example.bankingapp.service;

import java.security.SecureRandom;
import java.util.Base64;

import com.example.bankingapp.dto.AuthRequest;
import com.example.bankingapp.dto.AuthResponse;
import com.example.bankingapp.dto.RegisterRequest;
import com.example.bankingapp.exception.AuthException;
import com.example.bankingapp.exception.DuplicateEmailException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        if (userRepository.existsByBsn(request.bsn().trim())) {
            throw new DuplicateEmailException("An account with this BSN already exists");
        }

        AppUser user = new AppUser(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                request.bsn().trim(),
                request.phoneNumber().trim(),
                passwordEncoder.encode(request.password())
        );
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        user.setToken(generateToken());
        return new AuthResponse(user.getToken(), user.getEmail(), user.getStatus().name(), user.getRole().name());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
