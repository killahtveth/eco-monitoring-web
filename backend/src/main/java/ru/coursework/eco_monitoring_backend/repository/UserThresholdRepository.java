package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.UserThreshold;

import java.util.List;
import java.util.Optional;

public interface UserThresholdRepository extends JpaRepository<UserThreshold, Long> {
    List<UserThreshold> findByUser_Id(Long userId);
    List<UserThreshold> findByParameterId(Long parameterId);
    Optional<UserThreshold> findByUser_IdAndParameter_Id(Long userId, Long parameterId);
}