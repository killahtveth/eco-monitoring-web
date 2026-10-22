package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class HistoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MeasurementRepository repository;
    @MockitoBean private JwtService jwtService;

    @Test
    void returnsSeriesForLocationAndParameter() throws Exception {
        Parameter p = Parameter.builder().id(10L).code("PM2.5").build();
        Location l = Location.builder().id(1L).name("Test").build();
        Measurement m = Measurement.builder()
            .id(1L).parameter(p).location(l)
            .value(12.5).timestamp(LocalDateTime.of(2026, 1, 1, 12, 0)).build();

        when(repository.findByLocationIdAndTimestampBetween(any(), any(), any()))
            .thenReturn(List.of(m));

        mockMvc.perform(get("/api/history")
                .param("locationId", "1")
                .param("parameterId", "10")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].value").value(12.5));
    }

    @Test
    void returnsEmptyWhenNoData() throws Exception {
        when(repository.findByLocationIdAndTimestampBetween(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/history")
                .param("locationId", "1")
                .param("parameterId", "10")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}