package ru.coursework.eco_monitoring_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSourceSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void userCannotAccessAdminSources() throws Exception {
        mockMvc.perform(get("/api/admin/sources")
                .with(user("u@e").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminSources() throws Exception {
        mockMvc.perform(get("/api/admin/sources")
                .with(user("a@e").roles("ADMIN")))
            .andExpect(status().isOk());
    }
}