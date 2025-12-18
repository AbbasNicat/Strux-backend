package com.strux.unit_service.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            // Try to parse as LocalDateTime first (with time)
            return LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
        } catch (Exception e1) {
            try {
                // If that fails, try to parse as LocalDate (without time) and convert to LocalDateTime at start of day
                LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
                return localDate.atStartOfDay();
            } catch (Exception e2) {
                throw new IOException("Unable to parse date: " + dateString, e2);
            }
        }
    }
}