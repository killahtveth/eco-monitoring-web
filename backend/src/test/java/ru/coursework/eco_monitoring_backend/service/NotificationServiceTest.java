package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.coursework.eco_monitoring_backend.model.*;
import ru.coursework.eco_monitoring_backend.repository.NotificationRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private UserThresholdRepository thresholds;
    private NotificationRepository notifications;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        thresholds = mock(UserThresholdRepository.class);
        notifications = mock(NotificationRepository.class);
        service = new NotificationService(thresholds, notifications);
    }

    private Measurement measurement(double value, Parameter p) {
        return Measurement.builder()
            .id(1L).value(value).parameter(p)
            .timestamp(LocalDateTime.now()).build();
    }

    @Test
    void createsNotificationWhenAboveThreshold() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("μg/m³").build();
        UserThreshold t = UserThreshold.builder().user(u).parameter(p).threshold(15.0).build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(t));
        when(notifications.existsByUser_IdAndParameter_IdAndCreatedAtAfter(any(), any(), any()))
            .thenReturn(false);

        service.check(measurement(20.0, p));

        verify(notifications).save(any(Notification.class));
    }

    @Test
    void noNotificationWhenBelowThreshold() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("μg/m³").build();
        UserThreshold t = UserThreshold.builder().user(u).parameter(p).threshold(15.0).build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(t));

        service.check(measurement(10.0, p));

        verify(notifications, never()).save(any());
    }

    @Test
    void skipsDuplicateWithin30Minutes() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("μg/m³").build();
        UserThreshold t = UserThreshold.builder().user(u).parameter(p).threshold(15.0).build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(t));
        when(notifications.existsByUser_IdAndParameter_IdAndCreatedAtAfter(any(), any(), any()))
            .thenReturn(true);

        service.check(measurement(20.0, p));

        verify(notifications, never()).save(any());
    }
}