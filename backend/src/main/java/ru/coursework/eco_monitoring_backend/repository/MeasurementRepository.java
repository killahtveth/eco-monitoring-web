package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.Measurement;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
}