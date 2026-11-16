package ru.coursework.eco_monitoring_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser
class EcoMonitoringBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
