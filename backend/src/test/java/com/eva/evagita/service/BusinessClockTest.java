package com.eva.evagita.service;

import com.eva.evagita.config.BusinessTimeProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessClockTest {

    @Test
    void fixedClockReturnsBusinessDateInConfiguredTimezone() {
        BusinessTimeProperties properties = new BusinessTimeProperties();
        properties.setTimezone("Europe/Moscow");

        BusinessClock businessClock = new BusinessClock(
                Clock.fixed(
                        Instant.parse("2026-11-03T21:15:30Z"),
                        ZoneOffset.UTC
                ),
                properties
        );

        assertEquals(
                LocalDate.of(2026, 11, 4),
                businessClock.currentDate()
        );
    }

    @Test
    void currentDateChangesAtConfiguredTimezoneMidnight() {
        BusinessTimeProperties properties = new BusinessTimeProperties();
        properties.setTimezone("Europe/Moscow");

        BusinessClock beforeMidnight = new BusinessClock(
                Clock.fixed(
                        Instant.parse("2026-11-03T20:59:59Z"),
                        ZoneOffset.UTC
                ),
                properties
        );
        BusinessClock afterMidnight = new BusinessClock(
                Clock.fixed(
                        Instant.parse("2026-11-03T21:00:00Z"),
                        ZoneOffset.UTC
                ),
                properties
        );

        assertEquals(
                LocalDate.of(2026, 11, 3),
                beforeMidnight.currentDate()
        );
        assertEquals(
                LocalDate.of(2026, 11, 4),
                afterMidnight.currentDate()
        );
    }

    @Test
    void currentDateTimeUsesConfiguredTimezone() {
        BusinessTimeProperties properties = new BusinessTimeProperties();
        properties.setTimezone("Europe/Moscow");

        BusinessClock businessClock = new BusinessClock(
                Clock.fixed(
                        Instant.parse("2026-11-03T21:15:30Z"),
                        ZoneOffset.UTC
                ),
                properties
        );

        assertEquals(
                LocalDateTime.of(2026, 11, 4, 0, 15, 30),
                businessClock.currentDateTime()
        );
    }

    @Test
    void fixedClockCanUseAnotherConfiguredTimezone() {
        BusinessTimeProperties properties = new BusinessTimeProperties();
        properties.setTimezone("Asia/Tokyo");

        BusinessClock businessClock = new BusinessClock(
                Clock.fixed(
                        Instant.parse("2026-11-03T15:00:00Z"),
                        ZoneId.of("UTC")
                ),
                properties
        );

        assertEquals(
                LocalDate.of(2026, 11, 4),
                businessClock.currentDate()
        );
    }
}
