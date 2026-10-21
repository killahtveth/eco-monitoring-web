package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.dto.ThresholdDto;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;
import ru.coursework.eco_monitoring_backend.repository.UserThresholdRepository;

import java.util.List;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class ThresholdController {

    private final UserThresholdRepository thresholds;
    private final UserRepository users;
    private final ParameterRepository parameters;

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
    String email = extractEmail(principal);
    User u = users.findByEmail(email).orElseThrow();
    Parameter p = parameters.findById(dto.parameterId()).orElseThrow();
    UserThreshold t = thresholds.findByUser_IdAndParameter_Id(u.getId(), p.getId())
        .orElseGet(() -> UserThreshold.builder().user(u).parameter(p).build());
    t.setThreshold(dto.threshold());
    thresholds.save(t);
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