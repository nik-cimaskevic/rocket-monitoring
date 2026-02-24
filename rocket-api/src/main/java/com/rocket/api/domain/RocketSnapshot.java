package com.rocket.api.domain;

import java.time.Clock;
import java.util.UUID;

public record RocketSnapshot(
        UUID channel,
        int atMessageNumber,
        String rocketType,
        String mission,
        int speed,
        RocketStatus status,
        String explodedReason,
        UtcDateTime launchedAt,
        UtcDateTime createdAt
) {
    public static RocketSnapshot fromState(RocketState state, Clock clock) {
        return new RocketSnapshot(
                state.channel(),
                state.lastProcessedMsgNumber(),
                state.rocketType(),
                state.mission(),
                state.speed(),
                state.status(),
                state.explodedReason(),
                state.launchedAt(),
                UtcDateTime.now(clock)
        );
    }

    public RocketState toState() {
        return new RocketState(
                channel,
                rocketType,
                mission,
                speed,
                status,
                explodedReason,
                launchedAt,
                createdAt,
                atMessageNumber,
                false
        );
    }
}