package ru.coursework.eco_monitoring_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;
import ru.coursework.eco_monitoring_backend.service.DataCollectionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sources")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSourceController {

    private final DataSourceRepository repo;
    private final DataCollectionService collector;

    @GetMapping
    public List<DataSource> list() {
        return repo.findAll();
    }

    @PostMapping
    public DataSource create(@RequestBody DataSource ds) {
        ds.setId(null);
        ds.setStatus("ACTIVE");
        if (ds.getActive() == null) ds.setActive(true);
        return repo.save(ds);
    }

    @PutMapping("/{id}")
    public DataSource update(@PathVariable Long id, @RequestBody DataSource ds) {
        ds.setId(id);
        return repo.save(ds);
    }

    @PostMapping("/{id}/toggle")
    public DataSource toggle(@PathVariable Long id) {
        DataSource ds = repo.findById(id).orElseThrow();
        ds.setActive(!Boolean.TRUE.equals(ds.getActive()));
        ds.setStatus(Boolean.TRUE.equals(ds.getActive()) ? "ACTIVE" : "DISABLED");
        return repo.save(ds);
    }

    @PostMapping("/{id}/run")
    public Map<String, Object> run(@PathVariable Long id) {
        return Map.of("saved", collector.collectOne(repo.findById(id).orElseThrow()));
    }
}