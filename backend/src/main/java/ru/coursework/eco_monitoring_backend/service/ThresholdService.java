package ru.coursework.eco_monitoring_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final UserThresholdRepository thresholds;

    public Map<Long, Double> overrides(User user) {
        if (user == null) return Map.of();
        List<UserThreshold> rows = thresholds.findByUser_Id(user.getId());
        if (rows == null || rows.isEmpty()) return Map.of();

        Map<Long, Double> result = new HashMap<>();
        for (UserThreshold t : rows) {
            if (t.getParameter() != null && t.getThreshold() != null) {
                result.put(t.getParameter().getId(), t.getThreshold());
            }
        }
        return result;
    }

    public Double effective(Measurement m, Map<Long, Double> overrides) {
        if (m.getParameter() != null) {
            Double override = overrides.get(m.getParameter().getId());
            if (override != null) return override;
        }
        return m.getThreshold();
    }

    public Measurement withEffectiveThreshold(Measurement m, Map<Long, Double> overrides) {
        Double effective = effective(m, overrides);
        if (effective == null || effective.equals(m.getThreshold())) return m;

        return Measurement.builder()
            .id(m.getId())
            .location(m.getLocation())
            .parameter(m.getParameter())
            .value(m.getValue())
            .threshold(effective)
            .timestamp(m.getTimestamp())
            .valid(m.getValid())
            .source(m.getSource())
            .build();
    }
}
