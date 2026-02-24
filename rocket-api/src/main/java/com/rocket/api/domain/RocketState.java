package com.rocket.api.domain;

import lombok.With;
import java.util.List;
import java.util.UUID;

@With
public record RocketState(
        UUID channel,
        String rocketType,
        String mission,
        int speed,
        RocketStatus status,
        String explodedReason,
        UtcDateTime launchedAt,
        UtcDateTime lastUpdatedAt,
        int lastProcessedMsgNumber,
        boolean stale
) {

    private static final int SNAPSHOT_INTERVAL = 80;

    public static RocketState empty(UUID channel, UtcDateTime now) {
        return new RocketState(channel, null, null, 0, RocketStatus.pending, null, null, now, 0, false);
    }

    public RocketState applyAll(List<RocketEvent> events, UtcDateTime now) {
        RocketState current = this;
        int expected = this.lastProcessedMsgNumber + 1;

        for (RocketEvent event : events) {
            if (event.messageNumber() != expected) {
                return current.withStale(true);
            }
            expected = event.messageNumber() + 1;
            current = current.apply(event, now);
        }
        return current;
    }

    public boolean shouldSnapshot() {
        return !stale
                && lastProcessedMsgNumber > 0
                && lastProcessedMsgNumber % SNAPSHOT_INTERVAL == 0;
    }

    private RocketState apply(RocketEvent event, UtcDateTime now) {
        int msgNumber = event.messageNumber();
        return switch (event.messageType()) {
            case RocketLaunched -> this
                    .withRocketType(event.getRocketType())
                    .withMission(event.getMission())
                    .withSpeed(event.getLaunchSpeed())
                    .withStatus(RocketStatus.launched)
                    .withExplodedReason(null)
                    .withLaunchedAt(event.messageTime())
                    .withLastUpdatedAt(now)
                    .withLastProcessedMsgNumber(msgNumber);
            case RocketSpeedIncreased -> this
                    .withSpeed(this.speed + event.getSpeedDelta())
                    .withLastUpdatedAt(now)
                    .withLastProcessedMsgNumber(msgNumber);
            case RocketSpeedDecreased -> this
                    .withSpeed(Math.max(0, this.speed - event.getSpeedDelta()))
                    .withLastUpdatedAt(now)
                    .withLastProcessedMsgNumber(msgNumber);
            case RocketMissionChanged -> this
                    .withMission(event.getNewMission())
                    .withLastUpdatedAt(now)
                    .withLastProcessedMsgNumber(msgNumber);
            case RocketExploded -> this
                    .withStatus(RocketStatus.exploded)
                    .withExplodedReason(event.getExplodedReason())
                    .withLastUpdatedAt(now)
                    .withLastProcessedMsgNumber(msgNumber);
        };
    }
}