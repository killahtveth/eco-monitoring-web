package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.controller.ReportController.ReportRow;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.ExportService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ExportController.class, ReportController.class})
@AutoConfigureMockMvc(addFilters = false)
class ExportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MeasurementRepository repository;
    @MockitoBean private ExportService exportService;
    @MockitoBean private JwtService jwtService;

    @Test
    void returnsXlsxWithContentDisposition() throws Exception {
        when(repository.findByTimestampBetween(any(), any())).thenReturn(List.of());
        when(exportService.toXlsx(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/reports/export.xlsx")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
            .andExpect(content().contentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }
}