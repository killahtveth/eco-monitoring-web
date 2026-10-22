package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final MeasurementRepository repository;

    @GetMapping
    public List<ReportRow> report(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        var all = repository.findByTimestampBetween(from, to);
        return all.stream()
            .collect(Collectors.groupingBy(
                m -> m.getParameter().getId() + "|" + m.getParameter().getCode() + "|" + m.getParameter().getUnit()))
            .entrySet().stream()
            .map(e -> {
                var list = e.getValue();
                var p = list.get(0).getParameter();
                double min = list.stream().mapToDouble(Measurement::getValue).min().orElse(0);
                double max = list.stream().mapToDouble(Measurement::getValue).max().orElse(0);
                double avg = list.stream().mapToDouble(Measurement::getValue).average().orElse(0);
                long exceeded = list.stream()
                    .filter(m -> m.getValue() >= m.getThreshold()).count();
                return new ReportRow(p.getCode(), p.getUnit(), min, max, avg, exceeded, list.size());
            })
            .sorted(Comparator.comparing(ReportRow::parameter))
            .toList();
    }

    public record ReportRow(String parameter, String unit, double min, double max,
                            double avg, long exceeded, int count) {}
}