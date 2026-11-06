package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.service.collector.Collector;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCollectionService {

    private final DataSourceRepository dataSources;
    private final LocationRepository locations;
    private final ParameterRepository parameters;
    private final MeasurementRepository measurements;
    private final List<Collector> collectors;
    private final DataValidationService validator;
    private final NotificationService notificationService;   // ← НОВОЕ поле

    @Scheduled(cron = "${app.collector.cron:0 */5 * * * *}")
    public void scheduledCollect() {
        collectAll();
    }

    @Transactional
    public int collectAll() {
        int total = 0;
        for (DataSource ds : dataSources.findByActiveTrue()) {
            if (ds.getLastFetchAt() != null && ds.getIntervalMinutes() != null) {
                LocalDateTime nextDue = ds.getLastFetchAt().plusMinutes(ds.getIntervalMinutes());
                if (nextDue.isAfter(LocalDateTime.now())) continue;
            }
            total += collectAndTrack(ds);
        }
        return total;
    }

    @Transactional
    public int collectAndTrack(DataSource ds) {
        try {
            int saved = collectOne(ds);
            ds.setStatus("ACTIVE");
            ds.setLastError(null);
            return saved;
        } catch (Exception e) {
            log.error("Collector '{}' failed: {}", ds.getName(), e.getMessage());
            ds.setStatus("ERROR");
            ds.setLastError(e.getMessage());
            return 0;
        } finally {
            ds.setLastFetchAt(LocalDateTime.now());
            dataSources.save(ds);
        }
    }

    @Transactional
    public int collectOne(DataSource ds) {
        Collector c = collectors.stream()
            .filter(x -> x.supports().equalsIgnoreCase(ds.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No collector for type " + ds.getType()));

        List<Collector.RawMeasurement> raws = c.fetch(ds);
        int saved = 0;
        for (Collector.RawMeasurement r : raws) {
            Location loc = resolveLocation(ds, r.locationName());

            Parameter param = parameters.findByCode(r.parameterCode()).orElse(null);
            if (param == null) { log.warn("Unknown parameter {}", r.parameterCode()); continue; }

            if (!validator.isValid(param, r.value())) {
                log.warn("Dropped invalid {}={}", param.getCode(), r.value());
                continue;
            }

            if (measurements.existsByLocationIdAndParameterIdAndTimestamp(
                    loc.getId(), param.getId(), r.ts())) {
                log.info("Skipped duplicate {} at {}", param.getCode(), r.ts());
                continue;
            }

            Measurement savedEntity = measurements.save(Measurement.builder()
                .location(loc).parameter(param)
                .value(r.value()).threshold(param.getDefaultThreshold())
                .timestamp(r.ts()).source(ds.getName()).valid(true).build());

            notificationService.check(savedEntity);   // ← вот та самая «дополнительная строка»
            saved++;
        }
        return saved;
    }

    private Location resolveLocation(DataSource ds, String locationName) {
        Location loc = locations.findByName(locationName);
        double[] coords = parseCoordinates(ds.getUrl());

        if (loc == null) {
            Location.LocationBuilder builder = Location.builder().name(locationName);
            if (coords != null) {
                builder.latitude(coords[0]).longitude(coords[1]);
            }
            return locations.save(builder.build());
        }

        if (coords != null && (loc.getLatitude() == null || loc.getLongitude() == null)) {
            loc.setLatitude(coords[0]);
            loc.setLongitude(coords[1]);
            return locations.save(loc);
        }

        return loc;
    }

    private static double[] parseCoordinates(String url) {
        if (url == null || url.isBlank()) {
            return new double[] {54.3142, 48.4031};
        }
        String[] parts = url.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new double[] {
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}