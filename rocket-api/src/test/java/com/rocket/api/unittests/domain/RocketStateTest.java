package com.rocket.api.unittests.domain;

import com.rocket.api.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RocketStateTest {

    private static final UUID CHANNEL = UUID.fromString("193270a9-c9cf-404a-8f83-838e71d9ae67");
    private static final Clock LAUNCH_CLOCK = Clock.fixed(Instant.parse("2022-02-02T18:39:05Z"), ZoneOffset.UTC);
    private static final UtcDateTime LAUNCH_TIME = UtcDateTime.now(LAUNCH_CLOCK);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2022-02-02T20:00:00Z"), ZoneOffset.UTC);
    private static final UtcDateTime NOW = UtcDateTime.now(FIXED_CLOCK);

    @Test
    void shouldCreateEmptyState() {
        RocketState state = RocketState.empty(CHANNEL, NOW);

        assertThat(state.channel()).isEqualTo(CHANNEL);
        assertThat(state.rocketType()).isNull();
        assertThat(state.mission()).isNull();
        assertThat(state.speed()).isZero();
        assertThat(state.status()).isEqualTo(RocketStatus.pending);
        assertThat(state.explodedReason()).isNull();
        assertThat(state.lastProcessedMsgNumber()).isZero();
        assertThat(state.stale()).isFalse();
    }

    @Test
    void shouldApplyLaunchedEvent() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        RocketEvent launched = rocketLaunched(1, "Falcon-9", 500, "ARTEMIS");

        RocketState newState = state.applyAll(List.of(launched), NOW);

        assertThat(newState.channel()).isEqualTo(CHANNEL);
        assertThat(newState.rocketType()).isEqualTo("Falcon-9");
        assertThat(newState.mission()).isEqualTo("ARTEMIS");
        assertThat(newState.speed()).isEqualTo(500);
        assertThat(newState.status()).isEqualTo(RocketStatus.launched);
        assertThat(newState.launchedAt()).isEqualTo(LAUNCH_TIME);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(1);
        assertThat(newState.stale()).isFalse();
    }

    @Test
    void shouldIncreaseSpeed() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                speedIncreased(2, 3000)
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.speed()).isEqualTo(3500);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(2);
    }

    @Test
    void shouldDecreaseSpeed() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 3500, "ARTEMIS"),
                speedDecreased(2, 2500)
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.speed()).isEqualTo(1000);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(2);
    }

    @Test
    void shouldNotDecreaseSpeedBelowZero() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                speedDecreased(2, 1000)
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.speed()).isZero();
    }

    @Test
    void shouldChangeMission() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                missionChanged(2, "SHUTTLE_MIR")
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.mission()).isEqualTo("SHUTTLE_MIR");
        assertThat(newState.rocketType()).isEqualTo("Falcon-9");
        assertThat(newState.speed()).isEqualTo(500);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(2);
    }

    @Test
    void shouldExplode() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                exploded(2, "PRESSURE_VESSEL_FAILURE")
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.status()).isEqualTo(RocketStatus.exploded);
        assertThat(newState.explodedReason()).isEqualTo("PRESSURE_VESSEL_FAILURE");
        assertThat(newState.rocketType()).isEqualTo("Falcon-9");
        assertThat(newState.speed()).isEqualTo(500);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(2);
    }

    @Test
    void shouldApplyMultipleEventsInSequence() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                speedIncreased(2, 1000),
                speedIncreased(3, 500),
                missionChanged(4, "APOLLO"),
                speedDecreased(5, 200)
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.channel()).isEqualTo(CHANNEL);
        assertThat(newState.rocketType()).isEqualTo("Falcon-9");
        assertThat(newState.mission()).isEqualTo("APOLLO");
        assertThat(newState.speed()).isEqualTo(1800); // 500 + 1000 + 500 - 200
        assertThat(newState.status()).isEqualTo(RocketStatus.launched);
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(5);
        assertThat(newState.stale()).isFalse();
    }

    @Test
    void shouldMarkStateAsStaleWhenGapDetected() {
        RocketState state = RocketState.empty(CHANNEL, NOW);
        List<RocketEvent> events = List.of(
                rocketLaunched(1, "Falcon-9", 500, "ARTEMIS"),
                speedIncreased(3, 1000)  // Gap: missing message 2
        );

        RocketState newState = state.applyAll(events, NOW);

        assertThat(newState.stale()).isTrue();
        assertThat(newState.lastProcessedMsgNumber()).isEqualTo(1); // Stopped at last valid
        assertThat(newState.speed()).isEqualTo(500); // Speed increase not applied
    }

    @Test
    void shouldNotSnapshotWhenStale() {
        RocketState state = RocketState.empty(CHANNEL, NOW)
                .withLastProcessedMsgNumber(300)
                .withStale(true);

        assertThat(state.shouldSnapshot()).isFalse();
    }

    @Test
    void shouldNotSnapshotWhenNotAtInterval() {
        RocketState state = RocketState.empty(CHANNEL, NOW)
                .withLastProcessedMsgNumber(299)
                .withStale(false);

        assertThat(state.shouldSnapshot()).isFalse();
    }

    @Test
    void shouldSnapshotAtInterval() {
        RocketState state = RocketState.empty(CHANNEL, NOW)
                .withLastProcessedMsgNumber(80)
                .withStale(false);

        assertThat(state.shouldSnapshot()).isTrue();
    }

    // Event factory methods

    private RocketEvent rocketLaunched(int msgNumber, String type, int launchSpeed, String mission) {
        return new RocketEvent(
                CHANNEL,
                msgNumber,
                LAUNCH_TIME,
                MessageType.RocketLaunched,
                Map.of("type", type, "launchSpeed", launchSpeed, "mission", mission)
        );
    }

    private RocketEvent speedIncreased(int msgNumber, int by) {
        return new RocketEvent(
                CHANNEL,
                msgNumber,
                NOW,
                MessageType.RocketSpeedIncreased,
                Map.of("by", by)
        );
    }

    private RocketEvent speedDecreased(int msgNumber, int by) {
        return new RocketEvent(
                CHANNEL,
                msgNumber,
                NOW,
                MessageType.RocketSpeedDecreased,
                Map.of("by", by)
        );
    }

    private RocketEvent missionChanged(int msgNumber, String newMission) {
        return new RocketEvent(
                CHANNEL,
                msgNumber,
                NOW,
                MessageType.RocketMissionChanged,
                Map.of("newMission", newMission)
        );
    }

    private RocketEvent exploded(int msgNumber, String reason) {
        return new RocketEvent(
                CHANNEL,
                msgNumber,
                NOW,
                MessageType.RocketExploded,
                Map.of("reason", reason)
        );
    }
}