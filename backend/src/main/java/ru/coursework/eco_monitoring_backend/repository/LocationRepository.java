package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.coursework.eco_monitoring_backend.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByActiveTrue();
    Location findByName(String name);

    @Query("SELECT l FROM Location l WHERE EXISTS " +
           "(SELECT m FROM Measurement m WHERE m.location = l)")
    List<Location> findWithMeasurements();
}