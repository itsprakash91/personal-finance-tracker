package com.prakash.personal_finance_backend.auth;

import com.prakash.personal_finance_backend.auth.dto.AuthResponse;
import com.prakash.personal_finance_backend.auth.dto.LoginRequest;
import com.prakash.personal_finance_backend.auth.dto.RegisterRequest;
import com.prakash.personal_finance_backend.auth.dto.UserResponse;
import com.prakash.personal_finance_backend.entity.User;
import com.prakash.personal_finance_backend.repository.UserRepository;
import com.prakash.personal_finance_backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String DEFAULT_CURRENCY = "INR";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String currency = (request.defaultCurrency() == null || request.defaultCurrency().isBlank())
                ? DEFAULT_CURRENCY
                : request.defaultCurrency().trim().toUpperCase();

        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim(), currency);
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                // Same message as a wrong password below — don't reveal whether the
                // email exists at all.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
        }

        JwtService.IssuedToken issued = jwtService.issueToken(user.getId(), user.getEmail());
        return new AuthResponse("Bearer", issued.token(), issued.expiresAt(), UserResponse.fromEntity(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}