package ru.coursework.eco_monitoring_backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id, String parameter, String unit,
    Double value, Double threshold,
    LocalDateTime createdAt, Boolean read) {}