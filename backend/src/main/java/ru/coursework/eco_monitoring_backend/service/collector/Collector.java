package ru.coursework.eco_monitoring_backend.service.collector;

import ru.coursework.eco_monitoring_backend.model.DataSource;

import java.time.LocalDateTime;
import java.util.List;

public interface Collector {
    String supports();

    List<RawMeasurement> fetch(DataSource source);

    record RawMeasurement(String locationName, String parameterCode,
                          double value, String unit, LocalDateTime ts) {}
}