package com.rocket.api.domain;

import com.rocket.api.common.exceptions.exceptions.ValidationException;
import lombok.NonNull;
import java.util.Map;
import java.util.UUID;

public record RocketEvent(
        @NonNull UUID channel,
        @NonNull Integer messageNumber,
        @NonNull UtcDateTime messageTime,
        @NonNull MessageType messageType,
        @NonNull Map<String, Object> payload
) {
    public RocketEvent {
        if (messageNumber < 1) {
            throw new ValidationException("rocket.message.should.be.positive", "Message number must be a positive integer starting from 1");
        }
    }

    // RocketLaunched payload accessors
    public String getRocketType() {
        return getString("type");
    }

    public Integer getLaunchSpeed() {
        return getInt("launchSpeed");
    }

    public String getMission() {
        return getString("mission");
    }

    public Integer getSpeedDelta() {
        return getInt("by");
    }

    public String getNewMission() {
        return getString("newMission");
    }

    public String getExplodedReason() {
        return getString("reason");
    }

    private String getString(String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getInt(String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}