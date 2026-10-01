package ru.coursework.eco_monitoring_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeasurementRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Measurement m = new Measurement("PM10", 45.5, "мкг/м³", LocalDateTime.now(), 50.0);
        repository.save(m);
    }

    @Test
    void shouldReturnMeasurementsFromDatabaseViaApi() throws Exception {
        mockMvc.perform(get("/api/dashboard/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parameter").value("PM10"))
                .andExpect(jsonPath("$[0].value").value(45.5));
    }
}