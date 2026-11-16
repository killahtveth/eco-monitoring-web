package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementRepository repository;

    @Test
    void shouldReturnListOfMeasurements() throws Exception {
        Parameter p = Parameter.builder().code("PM2.5").unit("μg/m³").build();
        Location l = Location.builder().name("Test").build();
        Measurement m = Measurement.builder()
            .id(1L).parameter(p).location(l).value(12.5).threshold(15.0)
            .timestamp(LocalDateTime.now()).build();

        when(repository.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/dashboard/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].value").value(12.5))
            .andExpect(jsonPath("$[0].parameter.code").value("PM2.5"));
    }
}