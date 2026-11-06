package ru.coursework.eco_monitoring_backend.config;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JacksonComponent
public class LocalDateTimeOffsetSerializer extends ValueSerializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) {
        gen.writeString(value.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(FORMATTER));
    }
}
