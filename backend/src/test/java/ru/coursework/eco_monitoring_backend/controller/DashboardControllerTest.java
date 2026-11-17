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
import ru.coursework.eco_monitoring_backend.model.UserSettings;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserSettingsRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.ThresholdService;
import ru.coursework.eco_monitoring_backend.support.SecurityMvcTestConfig;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityMvcTestConfig.class, ThresholdService.class})
class DashboardControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private MeasurementRepository repository;
    @MockitoBean private UserRepository users;
    @MockitoBean private UserSettingsRepository settingsRepo;
    @MockitoBean private UserThresholdRepository thresholdRepo;
    @MockitoBean private JwtService jwtService;

    @Test
    void shouldReturnAllMeasurementsWhenUserNotAuthenticated() throws Exception {
        Parameter p = Parameter.builder().id(1L).code("PM2.5").unit("μg/m³").build();
        Location l = Location.builder().name("Test").build();
        Measurement m = Measurement.builder()
                .id(1L).parameter(p).location(l)
                .value(12.5).threshold(15.0)
                .timestamp(LocalDateTime.now())
                .build();
        when(repository.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/dashboard/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(12.5))
                .andExpect(jsonPath("$[0].parameter.code").value("PM2.5"))
                .andExpect(jsonPath("$[0].timestamp").value(org.hamcrest.Matchers.endsWith(
                        ZoneId.systemDefault().getRules().getOffset(Instant.now()).getId())));
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void shouldFilterByUserProfileWhenAuthenticated() throws Exception {
        Parameter pm25 = Parameter.builder().id(1L).code("PM2.5").unit("μg/m³").build();
        Parameter pm10 = Parameter.builder().id(2L).code("PM10").unit("μg/m³").build();
        Location l = Location.builder().name("Test").build();
        
        Measurement m1 = Measurement.builder()
                .id(1L).parameter(pm25).location(l)
                .value(12.5).threshold(15.0)
                .timestamp(LocalDateTime.now())
                .build();
        Measurement m2 = Measurement.builder()
                .id(2L).parameter(pm10).location(l)
                .value(30.0).threshold(45.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        when(repository.findLatestPerParameterAndLocation()).thenReturn(List.of(m1, m2));
        
        User u = User.builder().id(1L).email("test@eco.local").build();
        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        
        UserSettings settings = UserSettings.builder()
                .user(u)
                .displayedParameters(Set.of(pm25)) // Пользователь выбрал только PM2.5
                .build();
        when(settingsRepo.findByUserId(1L)).thenReturn(Optional.of(settings));

        mockMvc.perform(get("/api/dashboard/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].parameter.code").value("PM2.5"));
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void shouldUseUserThresholdInsteadOfParameterDefault() throws Exception {
        Parameter p = Parameter.builder().id(1L).code("PM2.5")
                .unit("ug/m3").defaultThreshold(15.0).build();
        Location l = Location.builder().name("Test").build();
        Measurement m = Measurement.builder()
                .id(1L).parameter(p).location(l)
                .value(12.5).threshold(15.0)
                .timestamp(LocalDateTime.now())
                .build();
        when(repository.findLatestPerParameterAndLocation()).thenReturn(List.of(m));

        User u = User.builder().id(1L).email("test@eco.local").build();
        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        when(settingsRepo.findByUserId(1L)).thenReturn(Optional.empty());
        when(thresholdRepo.findByUser_Id(1L)).thenReturn(List.of(
                UserThreshold.builder().user(u).parameter(p).threshold(8.0).build()));

        mockMvc.perform(get("/api/dashboard/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].threshold").value(8.0));
    }
}