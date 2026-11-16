package ru.coursework.eco_monitoring_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.coursework.eco_monitoring_backend.model.Role;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.existsByEmail("admin@eco.local")) return;
            users.save(User.builder()
                .email("admin@eco.local")
                .password(encoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());
            System.out.println("Seeded admin: admin@eco.local / admin123");
        };
    }
}