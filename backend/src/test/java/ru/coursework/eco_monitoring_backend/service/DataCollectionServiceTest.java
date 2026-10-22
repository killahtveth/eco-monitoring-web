package ru.coursework.eco_monitoring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.coursework.eco_monitoring_backend.model.DataSource;
import ru.coursework.eco_monitoring_backend.model.Location;
import ru.coursework.eco_monitoring_backend.model.Measurement;
import ru.coursework.eco_monitoring_backend.model.Parameter;
import ru.coursework.eco_monitoring_backend.repository.DataSourceRepository;
import ru.coursework.eco_monitoring_backend.repository.LocationRepository;
import ru.coursework.eco_monitoring_backend.repository.MeasurementRepository;
import ru.coursework.eco_monitoring_backend.repository.ParameterRepository;
import ru.coursework.eco_monitoring_backend.service.collector.OpenMeteoCollector;
import ru.coursework.eco_monitoring_backend.service.NotificationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DataCollectionServiceTest {

    private DataCollectionService service;
    private MeasurementRepository measurements;
    private DataSourceRepository dataSources;
    private LocationRepository locations;
    private ParameterRepository parameters;
    private MockRestServiceServer server;
    private DataSource ds;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        dataSources = mock(DataSourceRepository.class);
        locations = mock(LocationRepository.class);
        parameters = mock(ParameterRepository.class);
        measurements = mock(MeasurementRepository.class);
        notificationService = mock(NotificationService.class);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        OpenMeteoCollector collector = new OpenMeteoCollector(builder);

        // ← ГЛАВНОЕ ИЗМЕНЕНИЕ: добавлен DataValidationService
        service = new DataCollectionService(
            dataSources, locations, parameters, measurements,
            List.of(collector), new DataValidationService(), notificationService);

        ds = DataSource.builder()
            .id(1L).name("Test").type("OPEN_METEO")
            .url("54.3,48.4").intervalMinutes(60).active(true).build();
        when(dataSources.findByActiveTrue()).thenReturn(List.of(ds));
        when(dataSources.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(locations.findByName("Test")).thenReturn(null);
        when(locations.save(any())).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(parameters.findByCode(any())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return Optional.of(Parameter.builder()
                .id(1L).code(code).unit("μg/m³")
                .minValid(0.0).maxValid(10000.0)
                .defaultThreshold(50.0).build());
        });
        when(measurements.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void collectAll_savesAllMeasurementsFromRealApi() {
        String json = """
            {
              "current_units": {"pm10":"μg/m³","pm2_5":"μg/m³","carbon_monoxide":"μg/m³",
                                "nitrogen_dioxide":"μg/m³","sulphur_dioxide":"μg/m³","ozone":"μg/m³"},
              "current": {"time":"2026-11-11T15:00","pm10":25.0,"pm2_5":12.0,
                          "carbon_monoxide":180.0,"nitrogen_dioxide":15.0,
                          "sulphur_dioxide":3.0,"ozone":45.0}
            }
            """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        int saved = service.collectAll();

        assertThat(saved).isEqualTo(6);
        verify(measurements, times(6)).save(any(Measurement.class));
        verify(dataSources).save(argThat(d -> "ACTIVE".equals(d.getStatus())));
    }

    @Test
    void collectAll_marksErrorWhenApiFails() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withServerError());

        int saved = service.collectAll();

        assertThat(saved).isZero();
        verify(measurements, never()).save(any());
        verify(dataSources).save(argThat(d ->
            "ERROR".equals(d.getStatus()) && d.getLastError() != null));
    }

    @Test
    void collectAll_skipsSourceWhenIntervalNotElapsed() {
        ds.setLastFetchAt(java.time.LocalDateTime.now());
        ds.setIntervalMinutes(60);

        int saved = service.collectAll();

        assertThat(saved).isZero();
        server.verify();
    }

    @Test
    void collectAll_dropsInvalidValues() {
        // Переопределяем параметр: допустимый диапазон 0..10, значение 100 — невалидно
        when(parameters.findByCode(any())).thenAnswer(inv -> Optional.of(
            Parameter.builder()
                .id(1L).code(inv.getArgument(0)).unit("μg/m³")
                .minValid(0.0).maxValid(10.0)
                .defaultThreshold(5.0).build()));

        String json = """
            {
              "current_units": {"pm10":"μg/m³"},
              "current": {"time":"2026-11-11T15:00","pm10":100.0}
            }
            """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        int saved = service.collectAll();

        // pm10=100 выходит за maxValid=10 → отбрасывается
        assertThat(saved).isZero();
        verify(measurements, never()).save(any());
    }

    @Test
void collectAll_callsNotificationServiceAfterSave() {
    String json = """
        {"current_units":{"pm2_5":"μg/m³"},"current":{"time":"2026-11-11T15:00","pm2_5":12.0}}
        """;
    server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    service.collectAll();

    verify(notificationService, atLeastOnce()).check(any(Measurement.class));
}
}