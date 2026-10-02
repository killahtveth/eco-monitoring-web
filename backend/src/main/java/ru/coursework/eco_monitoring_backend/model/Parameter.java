package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "parameters", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parameter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;      // PM2.5, PM10, CO2, NO2
    private String name;      // Человекочитаемое имя
    private String unit;      // мкг/м³, ppm
    private Double minValid;  // нижняя граница валидности
    private Double maxValid;  // верхняя граница валидности
    private Double defaultThreshold;
}