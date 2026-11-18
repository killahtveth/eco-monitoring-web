package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
    boolean existsByUser_IdAndParameter_IdAndMeasurement_Id(
        Long userId, Long parameterId, Long measurementId);
}