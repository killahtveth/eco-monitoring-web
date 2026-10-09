package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.eco_monitoring_backend.dto.ProfileDto;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserSettings;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository users;
    private final UserSettingsRepository settings;
    private final ParameterRepository parameters;

    public ProfileDto get(String email) {
        User u = users.findByEmail(email).orElseThrow();
        UserSettings s = settings.findByUserId(u.getId())
            .orElseGet(() -> settings.save(UserSettings.builder().user(u).build()));
        return toDto(s);
    }

    @Transactional
    public ProfileDto update(String email, ProfileDto dto) {
        User u = users.findByEmail(email).orElseThrow();
        UserSettings s = settings.findByUserId(u.getId())
            .orElseGet(() -> settings.save(UserSettings.builder().user(u).build()));
        s.setRegion(dto.region());
        s.setRefreshIntervalSeconds(dto.refreshIntervalSeconds());
        s.setNotificationsEnabled(dto.notificationsEnabled());
        if (dto.parameterIds() != null) {
            Set<Parameter> params = new HashSet<>(parameters.findAllById(dto.parameterIds()));
            s.setDisplayedParameters(params);
        }
        return toDto(settings.save(s));
    }

    private ProfileDto toDto(UserSettings s) {
        return new ProfileDto(
            s.getRegion(),
            s.getDisplayedParameters().stream().map(Parameter::getId).toList(),
            s.getRefreshIntervalSeconds(),
            s.getNotificationsEnabled());
    }
}