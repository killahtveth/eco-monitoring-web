package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locations;
    private final MeasurementRepository measurements;

    @GetMapping
    public List<Location> all() {
        return locations.findAll();
    }

    @GetMapping("/latest")
    public List<Map<String, Object>> withLatest() {
        List<Measurement> latest = measurements.findLatestPerParameterAndLocation();
        return locations.findAll().stream()
            .map(l -> Map.of(
                "id", l.getId(),
                "name", l.getName(),
                "region", l.getRegion() == null ? "" : l.getRegion(),
                "latitude", l.getLatitude() == null ? 0 : l.getLatitude(),
                "longitude", l.getLongitude() == null ? 0 : l.getLongitude(),
                "measurements", latest.stream()
                    .filter(m -> m.getLocation().getId().equals(l.getId()))
                    .toList()
            ))
            .toList();
    }
}