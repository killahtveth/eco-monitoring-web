package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
       indexes = @Index(name = "idx_notif_user_created", columnList = "user_id, created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "measurement_id")
    private Measurement measurement;

    @ManyToOne @JoinColumn(name = "parameter_id")
    private Parameter parameter;
    @Column(name = "notification_value")  
    private Double value;
    private Double threshold;
    private LocalDateTime createdAt;
    @Builder.Default
    @Column(name = "is_read")  
    private Boolean read = false;
}