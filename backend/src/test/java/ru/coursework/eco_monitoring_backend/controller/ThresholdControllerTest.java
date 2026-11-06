package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.NotificationService;
import ru.coursework.eco_monitoring_backend.support.SecurityMvcTestConfig;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThresholdController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityMvcTestConfig.class)
class ThresholdControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserThresholdRepository thresholds;
    @MockitoBean private UserRepository users;
    @MockitoBean private ParameterRepository parameters;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private JwtService jwtService;

    private User user() {
        return User.builder().id(1L).email("test@eco.local").build();
    }

    private Parameter parameter() {
        return Parameter.builder().id(10L).code("PM10").unit("ug/m3").defaultThreshold(45.0).build();
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void savesThresholdAndChecksLatestMeasurement() throws Exception {
        User u = user();
        Parameter p = parameter();
        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        when(parameters.findById(10L)).thenReturn(Optional.of(p));
        when(thresholds.findByUser_IdAndParameter_Id(1L, 10L)).thenReturn(Optional.empty());
        when(thresholds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/thresholds")
                .contentType("application/json")
                .content("{\"parameterId\":10,\"threshold\":8}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.parameterId").value(10))
            .andExpect(jsonPath("$.threshold").value(8.0));

        verify(thresholds).save(any(UserThreshold.class));
        verify(notificationService).evaluateLatest(u, p, 8.0);
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void nullThresholdResetsToDefault() throws Exception {
        User u = user();
        Parameter p = parameter();
        UserThreshold existing = UserThreshold.builder().id(5L).user(u).parameter(p).threshold(8.0).build();

        when(users.findByEmail("test@eco.local")).thenReturn(Optional.of(u));
        when(parameters.findById(10L)).thenReturn(Optional.of(p));
        when(thresholds.findByUser_IdAndParameter_Id(1L, 10L)).thenReturn(Optional.of(existing));

        mockMvc.perform(put("/api/thresholds")
                .contentType("application/json")
                .content("{\"parameterId\":10,\"threshold\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threshold").value(org.hamcrest.Matchers.nullValue()));

        verify(thresholds).delete(existing);
        verify(thresholds, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @WithMockUser(username = "test@eco.local", roles = "USER")
    void rejectsNonPositiveThreshold() throws Exception {
        mockMvc.perform(put("/api/thresholds")
                .contentType("application/json")
                .content("{\"parameterId\":10,\"threshold\":0}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService);
    }
}
