package com.example.bankingapp.service;

import java.security.SecureRandom;
import java.util.Base64;

import com.example.bankingapp.dto.AuthRequest;
import com.example.bankingapp.dto.AuthResponse;
import com.example.bankingapp.exception.AuthException;
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
    public AuthResponse register(AuthRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new AuthException("Username is already taken");
        }

        AppUser user = new AppUser(username, passwordEncoder.encode(request.password()));
        user.setToken(generateToken());
        AppUser savedUser = userRepository.save(user);

        return new AuthResponse(savedUser.getToken(), savedUser.getUsername());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid username or password");
        }

        user.setToken(generateToken());
        return new AuthResponse(user.getToken(), user.getUsername());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
