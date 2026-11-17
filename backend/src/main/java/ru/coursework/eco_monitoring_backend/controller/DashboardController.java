package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserSettings;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final MeasurementRepository repository;
    private final UserRepository users;
    private final UserSettingsRepository settingsRepo;

    @GetMapping("/current")
    public List<Measurement> getCurrentMeasurements(@AuthenticationPrincipal Object principal) {
        List<Measurement> allLatest = repository.findLatestPerParameterAndLocation();
        
        String email = extractEmail(principal);
        if (email == null || email.equals("anonymousUser")) {
            return allLatest;
        }

        User u = users.findByEmail(email).orElse(null);
        if (u != null) {
            UserSettings settings = settingsRepo.findByUserId(u.getId()).orElse(null);
            if (settings != null && settings.getDisplayedParameters() != null && !settings.getDisplayedParameters().isEmpty()) {
                Set<Long> allowedParamIds = settings.getDisplayedParameters().stream()
                        .map(Parameter::getId)
                        .collect(Collectors.toSet());
                
                return allLatest.stream()
                        .filter(m -> allowedParamIds.contains(m.getParameter().getId()))
                        .toList();
            }
        }
        
        return allLatest;
    }

    private String extractEmail(Object principal) {
        if (principal instanceof String s) {
            return s;
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return null;
    }
}