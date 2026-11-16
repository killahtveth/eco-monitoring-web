package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_sources")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataSource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "source_type")
    private String type;  // OPEN_METEO

    private String url;   // "54.3142,48.4031" (lat,lon) для OPEN_METEO

    @Builder.Default
    private Integer intervalMinutes = 60;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private String status = "ACTIVE";

    private String lastError;
    private LocalDateTime lastFetchAt;
}