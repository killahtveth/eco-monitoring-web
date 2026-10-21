package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Notification;
import ru.coursework.eco_monitoring_backend.repository.NotificationRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserThresholdRepository thresholds;
    private final NotificationRepository notifications;

    public void check(Measurement m) {
        for (var t : thresholds.findByParameterId(m.getParameter().getId())) {
            if (m.getValue() == null || t.getThreshold() == null) continue;
            if (m.getValue() < t.getThreshold()) continue;

            boolean dup = notifications.existsByUser_IdAndParameter_IdAndCreatedAtAfter(
                t.getUser().getId(), m.getParameter().getId(),
                m.getTimestamp().minusMinutes(30));
            if (dup) continue;

            notifications.save(Notification.builder()
                .user(t.getUser())
                .measurement(m)
                .parameter(m.getParameter())
                .value(m.getValue())
                .threshold(t.getThreshold())
                .createdAt(LocalDateTime.now())
                .read(false)
                .build());
            log.info("Notification for user {}: {}={} >= {}", 
                t.getUser().getEmail(), m.getParameter().getCode(), m.getValue(), t.getThreshold());
        }
    }
}