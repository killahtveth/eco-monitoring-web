package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.coursework.eco_monitoring_backend.model.Measurement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    Optional<Measurement> findFirstByParameterIdOrderByTimestampDesc(Long parameterId);

    List<Measurement> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    List<Measurement> findByLocationIdAndTimestampBetween(Long locationId, LocalDateTime from, LocalDateTime to);

    List<Measurement> findByParameterIdAndTimestampBetween(Long parameterId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT m FROM Measurement m " +
           "WHERE m.location.id = :locId AND m.timestamp >= :from AND m.timestamp < :to " +
           "ORDER BY m.timestamp ASC")
    List<Measurement> findSeries(@Param("locId") Long locId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Query("SELECT m FROM Measurement m WHERE m.timestamp = (SELECT MAX(x.timestamp) FROM Measurement x WHERE x.parameter.id = m.parameter.id AND x.location.id = m.location.id)")
    List<Measurement> findLatestPerParameterAndLocation();
}