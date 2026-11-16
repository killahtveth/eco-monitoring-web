package ru.coursework.eco_monitoring_backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MeasurementRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private MeasurementRepository repository;

    @Test
    void shouldSaveAndFindAllMeasurements() {
        Location l = em.persist(Location.builder().name("Test").build());
        Parameter p1 = em.persist(Parameter.builder().code("PM2.5").build());
        Parameter p2 = em.persist(Parameter.builder().code("CO2").build());
        LocalDateTime now = LocalDateTime.now();
        em.persist(Measurement.builder().location(l).parameter(p1).value(10.0).timestamp(now).build());
        em.persist(Measurement.builder().location(l).parameter(p2).value(450.0).timestamp(now).build());
        em.flush();

        List<Measurement> all = repository.findAll();
        assertThat(all).hasSize(2)
            .extracting(m -> m.getParameter().getCode())
            .containsExactlyInAnyOrder("PM2.5", "CO2");
    }

    @Test
    void findLatestPerParameterAndLocation_shouldReturnOnlyLatest() {
        Location l = em.persist(Location.builder().name("L").build());
        Parameter p = em.persist(Parameter.builder().code("PM2.5").build());
        LocalDateTime t1 = LocalDateTime.now().minusHours(2);
        LocalDateTime t2 = LocalDateTime.now();
        em.persist(Measurement.builder().location(l).parameter(p).value(1.0).timestamp(t1).build());
        em.persist(Measurement.builder().location(l).parameter(p).value(2.0).timestamp(t2).build());
        em.flush();

        List<Measurement> latest = repository.findLatestPerParameterAndLocation();
        assertThat(latest).hasSize(1);
        assertThat(latest.get(0).getValue()).isEqualTo(2.0);
    }
}