package ru.coursework.eco_monitoring_backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import ru.coursework.eco_monitoring_backend.model.Measurement;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MeasurementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MeasurementRepository repository;

    @Test
    void shouldSaveAndFindAllMeasurements() {
        Measurement m1 = new Measurement("PM2.5", 10.0, "мкг/м³", LocalDateTime.now(), 35.0);
        Measurement m2 = new Measurement("CO2", 450.0, "ppm", LocalDateTime.now(), 400.0);
        entityManager.persist(m1);
        entityManager.persist(m2);
        entityManager.flush();

        List<Measurement> all = repository.findAll();
        assertThat(all).hasSize(2).extracting(Measurement::getParameter)
                .containsExactlyInAnyOrder("PM2.5", "CO2");
    }
}