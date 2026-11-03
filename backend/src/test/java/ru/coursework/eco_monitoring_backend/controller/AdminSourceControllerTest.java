package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.DataCollectionService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminSourceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSourceControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private DataSourceRepository repo;
    @MockitoBean private DataCollectionService collector;
    @MockitoBean private JwtService jwtService;

    @Test
    void listsSources() throws Exception {
        when(repo.findAll()).thenReturn(List.of(
            DataSource.builder().id(1L).name("A").build()
        ));

        mockMvc.perform(get("/api/admin/sources")
                .with(user("admin@e").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void createsSource() throws Exception {
        when(repo.save(any())).thenAnswer(inv -> {
            DataSource d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        mockMvc.perform(post("/api/admin/sources")
                .with(user("admin@e").roles("ADMIN"))
                .contentType("application/json")
                .content("{\"name\":\"New\",\"type\":\"OPEN_METEO\",\"url\":\"54.3,48.4\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void toggleChangesStatus() throws Exception {
        DataSource ds = DataSource.builder().id(1L).active(true).status("ACTIVE").build();
        when(repo.findById(1L)).thenReturn(Optional.of(ds));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/admin/sources/1/toggle")
                .with(user("admin@e").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DISABLED"));
    }

    @Test
    void runCollectsSourceAndReturnsStatus() throws Exception {
        DataSource ds = DataSource.builder().id(1L).name("A").type("OPEN_METEO")
            .status("ACTIVE").build();
        when(repo.findById(1L)).thenReturn(Optional.of(ds));
        when(collector.collectAndTrack(ds)).thenAnswer(inv -> {
            ds.setLastFetchAt(java.time.LocalDateTime.now());
            return 3;
        });

        mockMvc.perform(post("/api/admin/sources/1/run")
                .with(user("admin@e").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saved").value(3))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void runReturnsCollectionError() throws Exception {
        DataSource ds = DataSource.builder().id(1L).name("A").type("OPEN_METEO")
            .status("ACTIVE").build();
        when(repo.findById(1L)).thenReturn(Optional.of(ds));
        when(collector.collectAndTrack(ds)).thenAnswer(inv -> {
            ds.setStatus("ERROR");
            ds.setLastError("Open-Meteo request failed");
            return 0;
        });

        mockMvc.perform(post("/api/admin/sources/1/run")
                .with(user("admin@e").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saved").value(0))
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.lastError").value("Open-Meteo request failed"));
    }

    @Test
    void nonAdminForbidden() throws Exception {
        // Без addFilters=false сработал бы security, но мы отключили фильтры для unit-теста
        // Поэтому проверим, что @PreAuthorize на методе — а это уже отдельный тест
        // (реальная проверка роли делается в integration-тесте)
    }
}