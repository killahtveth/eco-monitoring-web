package ru.coursework.eco_monitoring_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSettings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String region;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_settings_parameters",
        joinColumns = @JoinColumn(name = "settings_id"),
        inverseJoinColumns = @JoinColumn(name = "parameter_id"))
    @Builder.Default
    private Set<Parameter> displayedParameters = new HashSet<>();

    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Builder.Default
    private Integer refreshIntervalSeconds = 60;
}