package ru.coursework.eco_monitoring_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.coursework.eco_monitoring_backend.model.*;
import ru.coursework.eco_monitoring_backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
public class DataInitializer {
    @Bean
    CommandLineRunner initDatabase(LocationRepository locations,
                                   ParameterRepository parameters,
                                   MeasurementRepository measurements, 
                                   DataSourceRepository dataSources) {
        return args -> {
            if (parameters.count() > 0) return; // Уже инициализировано

            Location loc1 = locations.save(Location.builder()
                    .name("Центр Ульяновска")
                    .region("Ульяновская обл.")
                    .latitude(54.3142)
                    .longitude(48.4031)
                    .build());

            Parameter pm25 = parameters.save(Parameter.builder()
                    .code("PM2.5").name("Мелкодисперсные частицы")
                    .unit("μg/m³").minValid(0.0).maxValid(1000.0).defaultThreshold(15.0).build());
            Parameter pm10 = parameters.save(Parameter.builder()
                    .code("PM10").name("Взвешенные частицы")
                    .unit("μg/m³").minValid(0.0).maxValid(2000.0).defaultThreshold(45.0).build());

            LocalDateTime now = LocalDateTime.now();
            measurements.saveAll(List.of(
                    Measurement.builder().location(loc1).parameter(pm25).value(12.5)
                            .threshold(pm25.getDefaultThreshold()).timestamp(now).source("seed").build(),
                    Measurement.builder().location(loc1).parameter(pm10).value(30.0)
                            .threshold(pm10.getDefaultThreshold()).timestamp(now).source("seed").build()
            ));

            // ВАЖНО: Имя источника ДОЛЖНО совпадать с именем локации, 
            // чтобы DataCollectionService нашел её через findByName и не создал дубликат!
            dataSources.save(DataSource.builder()
                    .name("Центр Ульяновска") 
                    .type("OPEN_METEO")
                    .url("54.3142,48.4031")
                    .intervalMinutes(60)
                    .active(true)
                    .status("ACTIVE")
                    .build());
            
            System.out.println("✅ База инициализирована корректно (без дубликатов)");
        };
    }
}