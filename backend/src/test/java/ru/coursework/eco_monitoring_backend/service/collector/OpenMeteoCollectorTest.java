package ru.coursework.eco_monitoring_backend.service.collector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.coursework.eco_monitoring_backend.model.DataSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenMeteoCollectorTest {

    private MockRestServiceServer server;
    private OpenMeteoCollector collector;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        collector = new OpenMeteoCollector(builder);
    }

    private DataSource ds() {
        return DataSource.builder().name("Test City").url("54.3,48.4").type("OPEN_METEO").build();
    }

    @Test
    void parsesRealApiResponse() {
        String json = """
            {
              "latitude": 54.3, "longitude": 48.4,
              "current_units": {
                "time": "iso8601",
                "pm10": "μg/m³", "pm2_5": "μg/m³",
                "carbon_monoxide": "μg/m³", "nitrogen_dioxide": "μg/m³",
                "sulphur_dioxide": "μg/m³", "ozone": "μg/m³"
              },
              "current": {
                "time": "2026-11-11T15:00",
                "pm10": 25.3, "pm2_5": 12.1,
                "carbon_monoxide": 180.0, "nitrogen_dioxide": 15.2,
                "sulphur_dioxide": 3.2, "ozone": 45.0
              }
            }
            """;

        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Collector.RawMeasurement> result = collector.fetch(ds());

        assertThat(result).hasSize(6);
        assertThat(result).extracting(Collector.RawMeasurement::parameterCode)
            .containsExactlyInAnyOrder("PM10", "PM2.5", "CO", "NO2", "SO2", "O3");
        assertThat(result).filteredOn(r -> r.parameterCode().equals("PM2.5"))
            .first().extracting(Collector.RawMeasurement::value).isEqualTo(12.1);
        assertThat(result.get(0).locationName()).isEqualTo("Test City");
        assertThat(result.get(0).ts()).isEqualTo(
            LocalDateTime.parse("2026-11-11T15:00")
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime());
        server.verify();
    }

    @Test
    void throwsOnHttpError() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withServerError());

        assertThatThrownBy(() -> collector.fetch(ds()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Open-Meteo");
    }

    @Test
    void throwsOnEmptyResponse() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/v1/air-quality")))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> collector.fetch(ds()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("empty response");
    }

    @Test
    void supportsOpenMeteo() {
        assertThat(collector.supports()).isEqualTo("OPEN_METEO");
    }
}