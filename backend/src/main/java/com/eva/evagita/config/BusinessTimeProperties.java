package com.eva.evagita.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DateTimeException;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "app")
public class BusinessTimeProperties {

    public static final String DEFAULT_TIMEZONE = "Europe/Moscow";

    private String timezone = DEFAULT_TIMEZONE;

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ZoneId getZoneId() {
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("app.timezone must not be blank");
        }

        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid app.timezone configuration",
                    exception
            );
        }
    }
}
