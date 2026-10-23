package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.*;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MeasurementRepository repository;
    @MockitoBean private JwtService jwtService;

    @Test
    void aggregatesMinMaxAvgAndExceeded() throws Exception {
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("μg/m³").build();
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(
            Measurement.builder().parameter(p).value(10.0).threshold(15.0).timestamp(t).build(),
            Measurement.builder().parameter(p).value(20.0).threshold(15.0).timestamp(t).build(),
            Measurement.builder().parameter(p).value(30.0).threshold(15.0).timestamp(t).build()
        ));

        mockMvc.perform(get("/api/reports")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].parameter").value("PM2.5"))
            .andExpect(jsonPath("$[0].min").value(10.0))
            .andExpect(jsonPath("$[0].max").value(30.0))
            .andExpect(jsonPath("$[0].avg").value(20.0))
            .andExpect(jsonPath("$[0].exceeded").value(2))
            .andExpect(jsonPath("$[0].count").value(3));
    }

    @Test
    void returnsEmptyListWhenNoData() throws Exception {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}