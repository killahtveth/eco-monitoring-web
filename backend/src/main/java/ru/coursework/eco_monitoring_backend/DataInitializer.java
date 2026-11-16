package ru.coursework.eco_monitoring_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(LocationRepository locations,
                                   ParameterRepository parameters,
                                   MeasurementRepository measurements, DataSourceRepository dataSources) {
        return args -> {
            if (parameters.count() > 0) return;

            Location loc1 = locations.save(Location.builder()
                .name("Центр Ульяновска")
                .region("Ульяновская обл.")
                .latitude(54.3142)
                .longitude(48.4031)
                .build());
            Location loc2 = locations.save(Location.builder()
                .name("Заволжский район")
                .region("Ульяновская обл.")
                .latitude(54.3500)
                .longitude(48.5000)
                .build());

            Parameter pm25 = parameters.save(Parameter.builder()
                .code("PM2.5").name("Мелкодисперсные частицы")
                .unit("μg/m³").minValid(0.0).maxValid(1000.0).defaultThreshold(15.0).build());
            Parameter pm10 = parameters.save(Parameter.builder()
                .code("PM10").name("Взвешенные частицы")
                .unit("μg/m³").minValid(0.0).maxValid(2000.0).defaultThreshold(45.0).build());
            Parameter co = parameters.save(Parameter.builder()
                .code("CO").name("Оксид углерода")
                .unit("μg/m³").minValid(0.0).maxValid(50000.0).defaultThreshold(4000.0).build());
            Parameter no2 = parameters.save(Parameter.builder()
                .code("NO2").name("Диоксид азота")
                .unit("μg/m³").minValid(0.0).maxValid(2000.0).defaultThreshold(25.0).build());
            Parameter so2 = parameters.save(Parameter.builder()
                .code("SO2").name("Диоксид серы")
                .unit("μg/m³").minValid(0.0).maxValid(2000.0).defaultThreshold(40.0).build());
            Parameter o3 = parameters.save(Parameter.builder()
                .code("O3").name("Озон")
                .unit("μg/m³").minValid(0.0).maxValid(2000.0).defaultThreshold(100.0).build());

            LocalDateTime now = LocalDateTime.now();
            measurements.saveAll(List.of(
                Measurement.builder().location(loc1).parameter(pm25).value(12.5)
                    .threshold(pm25.getDefaultThreshold()).timestamp(now).source("seed").build(),
                Measurement.builder().location(loc1).parameter(pm10).value(30.0)
                    .threshold(pm10.getDefaultThreshold()).timestamp(now).source("seed").build(),
                Measurement.builder().location(loc1).parameter(no2).value(15.0)
                    .threshold(no2.getDefaultThreshold()).timestamp(now).source("seed").build(),
                Measurement.builder().location(loc2).parameter(co).value(800.0)
                    .threshold(co.getDefaultThreshold()).timestamp(now).source("seed").build(),
                Measurement.builder().location(loc2).parameter(so2).value(5.0)
                    .threshold(so2.getDefaultThreshold()).timestamp(now).source("seed").build(),
                Measurement.builder().location(loc2).parameter(o3).value(60.0)
                    .threshold(o3.getDefaultThreshold()).timestamp(now).source("seed").build()
            ));
            System.out.println("Initialized: 2 locations, 6 parameters, 6 measurements");

            dataSources.save(DataSource.builder()
            .name("Open-Meteo Ulyanovsk")
            .type("OPEN_METEO")
            .url("54.3142,48.4031")
            .intervalMinutes(60)
            .active(true)
            .status("ACTIVE")
            .build());
        };
    }
}