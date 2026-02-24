package com.rocket.api.domain;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/*
 * Represents a UTC-normalized datetime value object.
 * Automatically converts any timezone to UTC on construction.
 */
public record UtcDateTime(ZonedDateTime value) {

    public UtcDateTime {
        if (!value.getZone().equals(ZoneOffset.UTC)) {
            value = value.withZoneSameInstant(ZoneOffset.UTC);
        }
    }

    public static UtcDateTime now(Clock clock) {
        return new UtcDateTime(ZonedDateTime.now(clock).withZoneSameInstant(ZoneOffset.UTC));
    }

    public static UtcDateTime of(OffsetDateTime offsetDateTime) {
        return new UtcDateTime(offsetDateTime.toZonedDateTime());
    }

    public UtcDateTime plusMinutes(long minutes) {
        return new UtcDateTime(value.plusMinutes(minutes));
    }

    public OffsetDateTime toOffsetDateTime() {
        return value.toOffsetDateTime();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}