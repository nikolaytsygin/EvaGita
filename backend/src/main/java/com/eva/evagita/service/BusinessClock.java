package com.eva.evagita.service;

import com.eva.evagita.config.BusinessTimeProperties;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class BusinessClock {

    private final Clock clock;
    private final ZoneId zoneId;

    public BusinessClock(Clock clock, BusinessTimeProperties properties) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.zoneId = Objects.requireNonNull(properties, "properties must not be null")
                .getZoneId();
    }

    public LocalDate currentDate() {
        return LocalDate.now(clock.withZone(zoneId));
    }

    public LocalDateTime currentDateTime() {
        return LocalDateTime.now(clock.withZone(zoneId));
    }
}
