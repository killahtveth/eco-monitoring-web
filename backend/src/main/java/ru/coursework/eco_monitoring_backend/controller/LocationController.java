package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.service.ThresholdService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locations;
    private final MeasurementRepository measurements;
    private final UserRepository users;
    private final ThresholdService thresholdService;

    @GetMapping
    public List<Location> all() {
        return locations.findWithMeasurements();
    }

    @GetMapping("/latest")
    public List<Map<String, Object>> withLatest(@AuthenticationPrincipal Object principal) {
        List<Measurement> latest = measurements.findLatestPerParameterAndLocation();
        Map<Long, Double> overrides = overrides(principal);
        Set<Long> locationIds = latest.stream()
            .map(m -> m.getLocation().getId())
            .collect(Collectors.toSet());

        return locations.findAll().stream()
            .filter(l -> locationIds.contains(l.getId()))
            .map(l -> Map.of(
                "id", l.getId(),
                "name", l.getName(),
                "region", l.getRegion() == null ? "" : l.getRegion(),
                "latitude", l.getLatitude() == null ? 0 : l.getLatitude(),
                "longitude", l.getLongitude() == null ? 0 : l.getLongitude(),
                "measurements", latest.stream()
                    .filter(m -> m.getLocation().getId().equals(l.getId()))
                    .map(m -> thresholdService.withEffectiveThreshold(m, overrides))
                    .toList()
            ))
            .toList();
    }

    private Map<Long, Double> overrides(Object principal) {
        String email = extractEmail(principal);
        if (email == null || email.equals("anonymousUser")) {
            return Map.of();
        }
        User u = users.findByEmail(email).orElse(null);
        return thresholdService.overrides(u);
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
