package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;
import ru.coursework.eco_monitoring_backend.service.DataCollectionService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/collector")
@RequiredArgsConstructor
public class CollectorAdminController {

    private final DataCollectionService service;
    private final DataSourceRepository dataSources;

    @PostMapping("/run")
    public Map<String, Object> runAll() {
        return Map.of("saved", service.collectAll());
    }

    @PostMapping("/{id}/run")
    public Map<String, Object> runOne(@PathVariable Long id) {
        DataSource ds = dataSources.findById(id).orElseThrow();
        return Map.of("saved", service.collectOne(ds));
    }
}