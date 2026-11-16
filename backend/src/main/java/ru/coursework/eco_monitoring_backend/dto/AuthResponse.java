package ru.coursework.eco_monitoring_backend.dto;

public record AuthResponse(String token, String email, String role) {}