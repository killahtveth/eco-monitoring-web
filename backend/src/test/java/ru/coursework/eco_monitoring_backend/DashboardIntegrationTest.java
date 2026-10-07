package ru.coursework.eco_monitoring_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MeasurementRepository measurements;
    @Autowired private LocationRepository locations;
    @Autowired private ParameterRepository parameters;

    @BeforeEach
    void setUp() {
        measurements.deleteAll();
        locations.deleteAll();
        parameters.deleteAll();

        Location loc = locations.save(Location.builder().name("Ulyanovsk").region("UL").build());
        Parameter pm10 = parameters.save(Parameter.builder()
            .code("PM10").unit("μg/m³")
            .minValid(0.0).maxValid(1000.0).defaultThreshold(45.0)
            .build());

        measurements.save(Measurement.builder()
            .location(loc).parameter(pm10)
            .value(45.5).threshold(45.0)
            .timestamp(LocalDateTime.now()).source("test")
            .build());
    }

    @Test
    void shouldReturnMeasurementsFromDatabaseViaApi() throws Exception {
        mockMvc.perform(get("/api/dashboard/current")
                .with(user("test@eco.local").roles("USER")))   // ← явно, локально, гарантированно
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].parameter.code").value("PM10"))
            .andExpect(jsonPath("$[0].value").value(45.5));
    }
}