package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserSettings;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserSettingsServiceTest {

    private UserRepository users;
    private UserSettingsRepository settings;
    private ParameterRepository params;
    private UserSettingsService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        settings = mock(UserSettingsRepository.class);
        params = mock(ParameterRepository.class);
        service = new UserSettingsService(users, settings, params);
        User u = User.builder().id(1L).email("u@e").build();
        when(users.findByEmail("u@e")).thenReturn(Optional.of(u));
        when(settings.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(settings.findByUserId(1L)).thenReturn(Optional.empty());
        when(params.findAllById(any())).thenReturn(List.of(
            Parameter.builder().id(10L).code("PM2.5").build()));
    }

    @Test
    void get_createsDefaultSettings() {
        var dto = service.get("u@e");
        assertThat(dto.refreshIntervalSeconds()).isEqualTo(60);
        assertThat(dto.notificationsEnabled()).isTrue();
    }

    @Test
    void update_savesSettings() {
        var dto = service.update("u@e",
            new ru.coursework.eco_monitoring_backend.dto.ProfileDto(
                "Москва", List.of(10L), 30, false));
        assertThat(dto.region()).isEqualTo("Москва");
        assertThat(dto.parameterIds()).containsExactly(10L);
        assertThat(dto.refreshIntervalSeconds()).isEqualTo(30);
    }
}