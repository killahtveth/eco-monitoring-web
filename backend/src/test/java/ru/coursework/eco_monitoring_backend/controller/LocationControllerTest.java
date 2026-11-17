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
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LocationRepository locations;
    @MockitoBean private MeasurementRepository measurements;
    @MockitoBean private JwtService jwtService;

    @Test
    void listsAllLocations() throws Exception {
        when(locations.findAll()).thenReturn(List.of(
            Location.builder().id(1L).name("A").build(),
            Location.builder().id(2L).name("B").build()
        ));

        mockMvc.perform(get("/api/locations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void latestJoinsLocationWithMeasurements() throws Exception {
        Location l = Location.builder().id(1L).name("A").latitude(54.3).longitude(48.4).build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("μg/m³").build();
        Measurement m = Measurement.builder()
            .id(1L).location(l).parameter(p)
            .value(12.5).threshold(15.0)
            .timestamp(LocalDateTime.now()).build();

        when(locations.findAll()).thenReturn(List.of(l));
        when(measurements.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/locations/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].latitude").value(54.3))
            .andExpect(jsonPath("$[0].measurements[0].value").value(12.5));
    }
}