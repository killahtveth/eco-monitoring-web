package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.*;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.ThresholdService;
import ru.coursework.eco_monitoring_backend.support.SecurityMvcTestConfig;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityMvcTestConfig.class, ThresholdService.class})
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MeasurementRepository repository;
    @MockitoBean private UserRepository users;
    @MockitoBean private UserThresholdRepository thresholdRepo;
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
                .param("from", "2026-01-01T00:00:00Z")
                .param("to", "2026-01-02T00:00:00Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].parameter").value("PM2.5"))
            .andExpect(jsonPath("$[0].min").value(10.0))
            .andExpect(jsonPath("$[0].max").value(30.0))
            .andExpect(jsonPath("$[0].avg").value(20.0))
            .andExpect(jsonPath("$[0].exceeded").value(2))
            .andExpect(jsonPath("$[0].count").value(3));
    }

    @Test
    void convertsOffsetToServerLocalTime() throws Exception {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports")
                .param("from", "2026-01-01T00:00:00Z")
                .param("to", "2026-01-02T00:00:00Z"))
            .andExpect(status().isOk());

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findByTimestampBetween(fromCaptor.capture(), toCaptor.capture());

        ZoneId zone = ZoneId.systemDefault();
        assertThat(fromCaptor.getValue())
            .isEqualTo(LocalDateTime.ofInstant(Instant.parse("2026-01-01T00:00:00Z"), zone));
        assertThat(toCaptor.getValue())
            .isEqualTo(LocalDateTime.ofInstant(Instant.parse("2026-01-02T00:00:00Z"), zone));
    }

    @Test
    void returnsEmptyListWhenNoData() throws Exception {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports")
                .param("from", "2026-01-01T00:00:00Z")
                .param("to", "2026-01-02T00:00:00Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void countsExceededAgainstUserThreshold() throws Exception {
        Parameter p = Parameter.builder().id(10L).code("PM10").unit("ug/m3").defaultThreshold(45.0).build();
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of(
            Measurement.builder().parameter(p).value(9.0).threshold(45.0).timestamp(t).build(),
            Measurement.builder().parameter(p).value(6.0).threshold(45.0).timestamp(t).build()
        ));

        User u = User.builder().id(1L).email("test@eco.local").build();
        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        when(thresholdRepo.findByUser_Id(1L)).thenReturn(List.of(
            UserThreshold.builder().user(u).parameter(p).threshold(8.0).build()));

        mockMvc.perform(get("/api/reports")
                .param("from", "2026-01-01T00:00:00Z")
                .param("to", "2026-01-02T00:00:00Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].exceeded").value(1));
    }
}
