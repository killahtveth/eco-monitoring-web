package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.dto.ProfileDto;
import ru.coursework.eco_monitoring_backend.service.UserSettingsService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserSettingsService service;

    @GetMapping
    public ProfileDto get(@AuthenticationPrincipal String email) {
        return service.get(email);
    }

    @PutMapping
    public ProfileDto update(@AuthenticationPrincipal String email, @RequestBody ProfileDto dto) {
        return service.update(email, dto);
    }
}