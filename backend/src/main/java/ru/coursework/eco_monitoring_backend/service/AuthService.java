package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.dto.AuthResponse;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthResponse register(String email, String password) {
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User u = users.save(User.builder()
            .email(email).password(encoder.encode(password)).role(ru.coursework.eco_monitoring_backend.model.Role.USER).build());
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getEmail(), u.getRole().name());
    }

    public AuthResponse login(String email, String password) {
        User u = users.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getEmail(), u.getRole().name());
    }
}