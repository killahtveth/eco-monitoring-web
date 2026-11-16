package ru.coursework.eco_monitoring_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.model.Parameter;

@Service
@Slf4j
public class DataValidationService {

    public boolean isValid(Parameter parameter, Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            log.warn("Reject {}: null/NaN", parameter.getCode());
            return false;
        }
        if (parameter.getMinValid() != null && value < parameter.getMinValid()) {
            log.warn("Reject {}={} < min {}", parameter.getCode(), value, parameter.getMinValid());
            return false;
        }
        if (parameter.getMaxValid() != null && value > parameter.getMaxValid()) {
            log.warn("Reject {}={} > max {}", parameter.getCode(), value, parameter.getMaxValid());
            return false;
        }
        return true;
    }
}