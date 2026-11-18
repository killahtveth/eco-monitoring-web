package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Notification;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserSettings;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.NotificationRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private UserThresholdRepository thresholds;
    private NotificationRepository notifications;
    private UserSettingsRepository settings;
    private MeasurementRepository measurements;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        thresholds = mock(UserThresholdRepository.class);
        notifications = mock(NotificationRepository.class);
        settings = mock(UserSettingsRepository.class);
        measurements = mock(MeasurementRepository.class);
        service = new NotificationService(thresholds, notifications, settings, measurements);
    }

    private Measurement measurement(double value, Parameter p) {
        return Measurement.builder()
            .id(1L).value(value).parameter(p)
            .timestamp(LocalDateTime.now()).build();
    }

    private UserThreshold threshold(User u, Parameter p, double value) {
        return UserThreshold.builder().user(u).parameter(p).threshold(value).build();
    }

    @Test
    void createsNotificationWhenAboveThreshold() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(threshold(u, p, 15.0)));
        when(settings.findByUserId(1L)).thenReturn(Optional.empty());
        when(notifications.existsByUser_IdAndParameter_IdAndMeasurement_Id(1L, 10L, 1L))
            .thenReturn(false);

        service.check(measurement(20.0, p));

        verify(notifications).save(any(Notification.class));
    }

    @Test
    void noNotificationWhenBelowThreshold() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(threshold(u, p, 15.0)));

        service.check(measurement(10.0, p));

        verify(notifications, never()).save(any());
    }

    @Test
    void skipsDuplicateNotificationForSameMeasurement() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(threshold(u, p, 15.0)));
        when(settings.findByUserId(1L)).thenReturn(Optional.empty());
        when(notifications.existsByUser_IdAndParameter_IdAndMeasurement_Id(1L, 10L, 1L))
            .thenReturn(true);

        service.check(measurement(20.0, p));

        verify(notifications, never()).save(any());
    }

    @Test
    void skipsWhenNotificationsDisabled() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();

        when(thresholds.findByParameterId(10L)).thenReturn(List.of(threshold(u, p, 15.0)));
        when(settings.findByUserId(1L)).thenReturn(Optional.of(
            UserSettings.builder().user(u).notificationsEnabled(false).build()));

        service.check(measurement(20.0, p));

        verify(notifications, never()).save(any());
    }

    @Test
    void evaluateLatestCreatesNotificationForLastMeasurement() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM10").unit("ug/m3").build();

        when(measurements.findFirstByParameterIdOrderByTimestampDesc(10L))
            .thenReturn(Optional.of(measurement(9.0, p)));
        when(settings.findByUserId(1L)).thenReturn(Optional.empty());
        when(notifications.existsByUser_IdAndParameter_IdAndMeasurement_Id(1L, 10L, 1L))
            .thenReturn(false);

        service.evaluateLatest(u, p, 8.0);

        verify(notifications).save(any(Notification.class));
    }

    @Test
    void evaluateLatestDoesNothingWhenValueBelowThreshold() {
        User u = User.builder().id(1L).email("u@e").build();
        Parameter p = Parameter.builder().id(10L).code("PM10").unit("ug/m3").build();

        when(measurements.findFirstByParameterIdOrderByTimestampDesc(10L))
            .thenReturn(Optional.of(measurement(6.0, p)));

        service.evaluateLatest(u, p, 8.0);

        verify(notifications, never()).save(any());
    }
}
