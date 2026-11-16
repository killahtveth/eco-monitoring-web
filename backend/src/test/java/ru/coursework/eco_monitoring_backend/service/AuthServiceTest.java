package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.coursework.eco_monitoring_backend.model.Role;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        jwt = mock(JwtService.class);
        when(jwt.generate(any(), any())).thenReturn("fake-token");
        service = new AuthService(users, encoder, jwt);
    }

    @Test
    void register_createsUserAndReturnsToken() {
        when(users.existsByEmail("a@b")).thenReturn(false);
        when(users.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setRole(Role.USER);
            return u;
        });

        var res = service.register("a@b", "secret12");

        assertThat(res.token()).isEqualTo("fake-token");
        assertThat(res.email()).isEqualTo("a@b");
        assertThat(res.role()).isEqualTo("USER");
    }

    @Test
    void register_duplicate_throws() {
        when(users.existsByEmail("a@b")).thenReturn(true);
        assertThatThrownBy(() -> service.register("a@b", "secret12"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_wrongPassword_throws() {
        User u = User.builder().email("a@b").password(encoder.encode("correct")).role(Role.USER).build();
        when(users.findByEmail("a@b")).thenReturn(Optional.of(u));
        assertThatThrownBy(() -> service.login("a@b", "wrong"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_success() {
        User u = User.builder().email("a@b").password(encoder.encode("correct")).role(Role.USER).build();
        when(users.findByEmail("a@b")).thenReturn(Optional.of(u));
        var res = service.login("a@b", "correct");
        assertThat(res.token()).isEqualTo("fake-token");
    }
}