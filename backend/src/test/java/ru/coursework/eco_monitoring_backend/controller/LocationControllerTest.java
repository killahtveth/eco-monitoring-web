package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.ThresholdService;
import ru.coursework.eco_monitoring_backend.support.SecurityMvcTestConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityMvcTestConfig.class, ThresholdService.class})
class LocationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LocationRepository locations;
    @MockitoBean private MeasurementRepository measurements;
    @MockitoBean private UserRepository users;
    @MockitoBean private UserThresholdRepository thresholdRepo;
    @MockitoBean private JwtService jwtService;

    @Test
    void listsOnlyLocationsWithMeasurements() throws Exception {
        when(locations.findWithMeasurements()).thenReturn(List.of(
            Location.builder().id(1L).name("A").build()
        ));

        mockMvc.perform(get("/api/locations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void latestJoinsLocationWithMeasurements() throws Exception {
        Location l = Location.builder().id(1L).name("A").latitude(54.3).longitude(48.4).build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();
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

    @Test
    void latestHidesLocationsWithoutMeasurements() throws Exception {
        Location withData = Location.builder().id(1L).name("A").build();
        Location empty = Location.builder().id(2L).name("B").build();
        Parameter p = Parameter.builder().id(10L).code("PM2.5").unit("ug/m3").build();
        Measurement m = Measurement.builder()
            .id(1L).location(withData).parameter(p)
            .value(12.5).threshold(15.0)
            .timestamp(LocalDateTime.now()).build();

        when(locations.findAll()).thenReturn(List.of(withData, empty));
        when(measurements.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/locations/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void latestAppliesUserThresholdOverride() throws Exception {
        Location l = Location.builder().id(1L).name("A").build();
        Parameter p = Parameter.builder().id(10L).code("PM10").unit("ug/m3").defaultThreshold(45.0).build();
        Measurement m = Measurement.builder()
            .id(1L).location(l).parameter(p)
            .value(9.0).threshold(45.0)
            .timestamp(LocalDateTime.now()).build();

        when(locations.findAll()).thenReturn(List.of(l));
        when(measurements.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        User u = User.builder().id(1L).email("test@eco.local").build();
        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        when(thresholdRepo.findByUser_Id(1L)).thenReturn(List.of(
            UserThreshold.builder().user(u).parameter(p).threshold(8.0).build()));

        mockMvc.perform(get("/api/locations/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].measurements[0].threshold").value(8.0));
    }
}
