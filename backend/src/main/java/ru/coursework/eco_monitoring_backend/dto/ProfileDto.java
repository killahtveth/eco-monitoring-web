package ru.coursework.eco_monitoring_backend.dto;

import java.util.List;

public record ProfileDto(
    String region,
    List<Long> parameterIds,
    Integer refreshIntervalSeconds,
    Boolean notificationsEnabled
) {}