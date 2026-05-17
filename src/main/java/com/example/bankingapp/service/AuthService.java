package com.example.bankingapp.service;

import com.example.bankingapp.dto.AuthRequest;
import com.example.bankingapp.dto.AuthResponse;
import com.example.bankingapp.exception.AuthException;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.model.Role;
import com.example.bankingapp.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new AuthException("Username is already taken");
        }

        // New registrations are CUSTOMER by default
        AppUser user = new AppUser(username, passwordEncoder.encode(request.password()), Role.CUSTOMER);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
