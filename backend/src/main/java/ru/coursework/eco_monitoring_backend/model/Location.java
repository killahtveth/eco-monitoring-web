package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    @Builder.Default
    private Boolean active = true;
}