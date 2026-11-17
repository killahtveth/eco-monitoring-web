package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.coursework.eco_monitoring_backend.dto.ThresholdDto;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;
import ru.coursework.eco_monitoring_backend.service.NotificationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class ThresholdController {

    private final UserThresholdRepository thresholds;
    private final UserRepository users;
    private final ParameterRepository parameters;
    private final NotificationService notificationService;

    @GetMapping
    public List<ThresholdDto> mine(@AuthenticationPrincipal Object principal) {
        String email = extractEmail(principal);
        User u = users.findByEmail(email).orElseThrow();
        return thresholds.findByUser_Id(u.getId()).stream()
            .map(t -> new ThresholdDto(t.getParameter().getId(), t.getThreshold()))
            .toList();
    }

    @PutMapping
    public ThresholdDto upsert(@AuthenticationPrincipal Object principal,
                               @RequestBody ThresholdDto dto) {
        if (dto.threshold() != null && dto.threshold() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Порог должен быть положительным числом");
        }

        String email = extractEmail(principal);
        User u = users.findByEmail(email).orElseThrow();
        Parameter p = parameters.findById(dto.parameterId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Параметр не найден"));

        Optional<UserThreshold> existing = thresholds.findByUser_IdAndParameter_Id(u.getId(), p.getId());

        if (dto.threshold() == null) {
            existing.ifPresent(thresholds::delete);
            return new ThresholdDto(p.getId(), null);
        }

        UserThreshold t = existing
            .orElseGet(() -> UserThreshold.builder().user(u).parameter(p).build());
        t.setThreshold(dto.threshold());
        thresholds.save(t);

        notificationService.evaluateLatest(u, p, t.getThreshold());
        return new ThresholdDto(p.getId(), t.getThreshold());
    }

    private String extractEmail(Object principal) {
        if (principal instanceof String s) {
            return s;
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
