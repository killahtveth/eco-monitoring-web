package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.coursework.eco_monitoring_backend.dto.AuthResponse;
import ru.coursework.eco_monitoring_backend.security.JwtAuthFilter;
import ru.coursework.eco_monitoring_backend.security.JwtService;
import ru.coursework.eco_monitoring_backend.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;
    @MockitoBean private JwtService jwtService;

    @Test
    void register_returnsToken() throws Exception {
        when(authService.register("a@b", "secret12"))
            .thenReturn(new AuthResponse("tok", "a@b", "USER"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b\",\"password\":\"secret12\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("tok"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"secret12\"}"))
            .andExpect(status().isBadRequest());
    }
}