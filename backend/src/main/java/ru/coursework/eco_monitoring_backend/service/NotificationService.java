package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Notification;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.NotificationRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserThresholdRepository thresholds;
    private final NotificationRepository notifications;
    private final UserSettingsRepository settings;
    private final MeasurementRepository measurements;

    public void check(Measurement m) {
        if (m == null || m.getParameter() == null) return;
        for (var t : thresholds.findByParameterId(m.getParameter().getId())) {
            createIfExceeded(t.getUser(), m.getParameter(), t.getThreshold(), m);
        }
    }

    public void evaluateLatest(User user, Parameter parameter, Double threshold) {
        if (user == null || parameter == null || threshold == null) return;

        measurements.findFirstByParameterIdOrderByTimestampDesc(parameter.getId())
            .ifPresent(m -> createIfExceeded(user, parameter, threshold, m));
    }

    private void createIfExceeded(User user, Parameter parameter, Double threshold, Measurement m) {
        if (user == null || threshold == null || m == null || m.getValue() == null) return;
        if (m.getValue() < threshold) return;
        if (!notificationsEnabled(user)) return;
        if (m.getId() != null && notifications.existsByUser_IdAndParameter_IdAndMeasurement_Id(
                user.getId(), parameter.getId(), m.getId())) {
            return;
        }

        notifications.save(Notification.builder()
            .user(user)
            .measurement(m)
            .parameter(parameter)
            .value(m.getValue())
            .threshold(threshold)
            .createdAt(LocalDateTime.now())
            .read(false)
            .build());
        log.info("Notification for user {}: {}={} >= {}",
            user.getEmail(), parameter.getCode(), m.getValue(), threshold);
    }

    private boolean notificationsEnabled(User user) {
        return settings.findByUserId(user.getId())
            .map(s -> !Boolean.FALSE.equals(s.getNotificationsEnabled()))
            .orElse(true);
    }
}
