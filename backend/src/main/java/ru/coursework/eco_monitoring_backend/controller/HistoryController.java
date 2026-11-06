package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final MeasurementRepository repository;

    @GetMapping
    public List<Map<String, Object>> series(
        @RequestParam Long locationId,
        @RequestParam Long parameterId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        return repository.findByLocationIdAndTimestampBetween(locationId, toLocal(from), toLocal(to)).stream()
            .filter(m -> m.getParameter().getId().equals(parameterId))
            .map(m -> Map.<String, Object>of("ts", m.getTimestamp(), "value", m.getValue()))
            .toList();
    }

    private static LocalDateTime toLocal(OffsetDateTime value) {
        return value.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
