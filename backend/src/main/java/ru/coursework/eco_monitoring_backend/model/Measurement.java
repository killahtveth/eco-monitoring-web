package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements",
       indexes = {
           @Index(name = "idx_meas_timestamp", columnList = "timestamp"),
           @Index(name = "idx_meas_loc_ts", columnList = "location_id, timestamp"),
           @Index(name = "idx_meas_param_ts", columnList = "parameter_id, timestamp")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Measurement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id")
    private Parameter parameter;

    @Column(name = "measurement_value")
    private Double value;

    private LocalDateTime timestamp;
    private Double threshold;

    @Builder.Default
    private Boolean valid = true;

    private String source;
}