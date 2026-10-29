package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameter; // Например, "PM2.5" или "CO2"
    private Double value;
    private String unit;      // "мкг/м³" или "ppm"
    private LocalDateTime timestamp;
    private Double threshold; // Порог для предупреждения

    // Пустой конструктор нужен для JPA
    public Measurement() {}

    public Measurement(String parameter, Double value, String unit, LocalDateTime timestamp, Double threshold) {
        this.parameter = parameter;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.threshold = threshold;
    }

    // Геттеры и сеттеры (можешь сгенерировать через Alt+Insert в IntelliJ или добавить Lombok)
    public Long getId() { return id; }
    public String getParameter() { return parameter; }
    public Double getValue() { return value; }
    public String getUnit() { return unit; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Double getThreshold() { return threshold; }
}