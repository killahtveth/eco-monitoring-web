package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;

import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
public class ParameterController {
    private final ParameterRepository repository;

    @GetMapping
    public List<Parameter> list() {
        return repository.findAll();
    }
}