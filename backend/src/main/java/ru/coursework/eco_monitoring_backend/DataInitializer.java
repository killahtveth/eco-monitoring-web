package ru.coursework.eco_monitoring_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(MeasurementRepository repository) {
        return args -> {
            if (repository.count() == 0) { // Заполняем только если база пустая
                repository.saveAll(List.of(
                    new Measurement("PM2.5", 12.5, "мкг/м³", LocalDateTime.now(), 35.0),
                    new Measurement("PM10", 45.0, "мкг/м³", LocalDateTime.now(), 50.0),
                    new Measurement("CO2", 420.0, "ppm", LocalDateTime.now(), 400.0),
                    new Measurement("NO2", 0.04, "мг/м³", LocalDateTime.now(), 0.06)
                ));
                System.out.println("Тестовые данные успешно загружены в БД!");
            }
        };
    }
}