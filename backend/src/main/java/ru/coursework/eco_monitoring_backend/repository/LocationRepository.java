package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByActiveTrue();
    Location findByName(String name);
}