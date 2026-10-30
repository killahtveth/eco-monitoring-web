package ru.coursework.eco_monitoring_backend.service.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.coursework.eco_monitoring_backend.model.DataSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OpenMeteoCollector implements Collector {

    private static final String[] API_PARAMS = {
        "pm10", "pm2_5", "carbon_monoxide", "nitrogen_dioxide", "sulphur_dioxide", "ozone"
    };

    private static final Map<String, String> CODE_MAP = Map.of(
        "pm10",             "PM10",
        "pm2_5",            "PM2.5",
        "carbon_monoxide",  "CO",
        "nitrogen_dioxide", "NO2",
        "sulphur_dioxide",  "SO2",
        "ozone",            "O3"
    );

    private final RestClient http;

    public OpenMeteoCollector(RestClient.Builder builder) {
        this.http = builder.baseUrl("https://air-quality-api.open-meteo.com").build();
    }

    @Override
    public String supports() {
        return "OPEN_METEO";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RawMeasurement> fetch(DataSource source) {
        String coords = (source.getUrl() == null || source.getUrl().isBlank())
            ? "54.3142,48.4031"
            : source.getUrl();
        String[] parts = coords.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("DataSource.url must be 'lat,lon'");
        }
        String lat = parts[0].trim();
        String lon = parts[1].trim();
        String locationName = source.getName() != null ? source.getName() : "Open-Meteo";

        Map<String, Object> body;
        try {
            body = http.get()
                .uri(uri -> uri.path("/v1/air-quality")
                    .queryParam("latitude", lat)
                    .queryParam("longitude", lon)
                    .queryParam("current", String.join(",", API_PARAMS))
                    .queryParam("timezone", "UTC")
                    .build())
                .retrieve()
                .body(Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Open-Meteo request failed: " + e.getMessage(), e);
        }

        if (body == null || !(body.get("current") instanceof Map<?, ?>)) {
            throw new RuntimeException("Open-Meteo: empty response");
        }
        Map<String, Object> current = (Map<String, Object>) body.get("current");
        Map<String, Object> units = (Map<String, Object>) body.getOrDefault("current_units", Map.of());

        LocalDateTime ts;
        try {
            ts = LocalDateTime.parse(String.valueOf(current.get("time")))
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        } catch (Exception e) {
            ts = LocalDateTime.now();
        }

        List<RawMeasurement> out = new ArrayList<>();
        for (String key : API_PARAMS) {
            Object v = current.get(key);
            if (v instanceof Number n) {
                String unit = String.valueOf(units.getOrDefault(key, "μg/m³"));
                out.add(new RawMeasurement(locationName, CODE_MAP.get(key), n.doubleValue(), unit, ts));
            }
        }
        log.info("Open-Meteo: fetched {} measurements for {}", out.size(), locationName);
        return out;
    }
}