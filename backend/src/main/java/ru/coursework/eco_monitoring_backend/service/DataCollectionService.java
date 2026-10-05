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
                if (nextDue.isAfter(LocalDateTime.now())) {
                    continue;
                }
            }
            try {
                total += collectOne(ds);
                ds.setStatus("ACTIVE");
                ds.setLastError(null);
            } catch (Exception e) {
                log.error("Collector '{}' failed: {}", ds.getName(), e.getMessage());
                ds.setStatus("ERROR");
                ds.setLastError(e.getMessage());
            } finally {
                ds.setLastFetchAt(LocalDateTime.now());
                dataSources.save(ds);
            }
        }
        return total;
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
            Location loc = locations.findByName(r.locationName());
            if (loc == null) {
                loc = locations.save(Location.builder().name(r.locationName()).build());
            }
            Parameter param = parameters.findByCode(r.parameterCode()).orElse(null);
            if (param == null) {
                log.warn("Unknown parameter code {}, skipping", r.parameterCode());
                continue;
            }
            measurements.save(Measurement.builder()
                .location(loc)
                .parameter(param)
                .value(r.value())
                .threshold(param.getDefaultThreshold())
                .timestamp(r.ts())
                .source(ds.getName())
                .valid(true)
                .build());
            saved++;
        }
        return saved;
    }
}