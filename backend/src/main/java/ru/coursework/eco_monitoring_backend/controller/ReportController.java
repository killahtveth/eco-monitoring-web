package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.service.ThresholdService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final MeasurementRepository repository;
    private final UserRepository users;
    private final ThresholdService thresholdService;

    @GetMapping
    public List<ReportRow> report(
        @AuthenticationPrincipal Object principal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        Map<Long, Double> overrides = overrides(principal);
        var all = repository.findByTimestampBetween(toLocal(from), toLocal(to));
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
                    .filter(m -> {
                        Double threshold = thresholdService.effective(m, overrides);
                        return threshold != null && m.getValue() != null && m.getValue() >= threshold;
                    })
                    .count();
                return new ReportRow(p.getCode(), p.getUnit(), min, max, avg, exceeded, list.size());
            })
            .sorted(Comparator.comparing(ReportRow::parameter))
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

    private static LocalDateTime toLocal(OffsetDateTime value) {
        return value.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public record ReportRow(String parameter, String unit, double min, double max,
                            double avg, long exceeded, int count) {}
}
