package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.UserSettings;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserId(Long userId);
}