package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.dto.NotificationDto;
import ru.coursework.eco_monitoring_backend.model.Notification;
import ru.coursework.eco_monitoring_backend.model.User;
import ru.coursework.eco_monitoring_backend.repository.NotificationRepository;
import ru.coursework.eco_monitoring_backend.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notifications;
    private final UserRepository users;

    @GetMapping
public List<NotificationDto> mine(@AuthenticationPrincipal Object principal) {
    String email = extractEmail(principal);
    User u = users.findByEmail(email).orElseThrow();
    return notifications.findByUser_IdOrderByCreatedAtDesc(u.getId()).stream()
        .map(this::toDto).toList();
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

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        notifications.findById(id).ifPresent(n -> {
            n.setRead(true);
            notifications.save(n);
        });
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(),
            n.getParameter().getCode(), n.getParameter().getUnit(),
            n.getValue(), n.getThreshold(), n.getCreatedAt(), n.getRead());
    }
}