package ru.coursework.eco_monitoring_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.eco_monitoring_backend.model.Parameter;

import java.util.Optional;

public interface ParameterRepository extends JpaRepository<Parameter, Long> {
    Optional<Parameter> findByCode(String code);
}