package ru.coursework.eco_monitoring_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.dto.AuthResponse;
import ru.coursework.eco_monitoring_backend.dto.LoginRequest;
import ru.coursework.eco_monitoring_backend.dto.RegisterRequest;
import ru.coursework.eco_monitoring_backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest req) {
        return authService.register(req.email(), req.password());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req.email(), req.password());
    }
}