package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.Test;
import ru.coursework.eco_monitoring_backend.model.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class DataValidationServiceTest {

    private final DataValidationService validator = new DataValidationService();
    private final Parameter p = Parameter.builder()
        .code("X").minValid(0.0).maxValid(100.0).build();

    @Test void acceptsInRange() {
        assertThat(validator.isValid(p, 50.0)).isTrue();
    }
    @Test void rejectsBelowMin() {
        assertThat(validator.isValid(p, -1.0)).isFalse();
    }
    @Test void rejectsAboveMax() {
        assertThat(validator.isValid(p, 101.0)).isFalse();
    }
    @Test void rejectsNull() {
        assertThat(validator.isValid(p, null)).isFalse();
    }
    @Test void rejectsNaN() {
        assertThat(validator.isValid(p, Double.NaN)).isFalse();
    }
    @Test void rejectsInfinite() {
        assertThat(validator.isValid(p, Double.POSITIVE_INFINITY)).isFalse();
    }
}