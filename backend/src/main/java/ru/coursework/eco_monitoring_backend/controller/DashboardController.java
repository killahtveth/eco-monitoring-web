package ru.coursework.eco_monitoring_backend.controller;

import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173") // Разрешаем React'у делать запросы
public class DashboardController {

    private final MeasurementRepository repository;

    public DashboardController(MeasurementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/current")
    public List<Measurement> getCurrentMeasurements() {
        // В реальном проекте тут была бы сложная логика выборки последних значений.
        // Для прототипа просто возвращаем всё из базы.
        return repository.findAll();
    }
}