package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_thresholds",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "parameter_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserThreshold {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "parameter_id")
    private Parameter parameter;

    private Double threshold;
}