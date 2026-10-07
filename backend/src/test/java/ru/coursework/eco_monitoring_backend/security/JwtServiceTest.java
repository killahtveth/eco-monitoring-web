package ru.coursework.eco_monitoring_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired private JwtService jwt;

    @Test
    void roundTrip() {
        String token = jwt.generate("user@test", "USER");
        var claims = jwt.parse(token);
        assertThat(claims.getSubject()).isEqualTo("user@test");
        assertThat(claims.get("role")).isEqualTo("USER");
    }
}