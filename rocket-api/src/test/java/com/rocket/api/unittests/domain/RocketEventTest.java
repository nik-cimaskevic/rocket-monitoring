package com.rocket.api.unittests.domain;

import com.rocket.api.common.exceptions.exceptions.ValidationException;
import com.rocket.api.domain.MessageType;
import com.rocket.api.domain.RocketEvent;
import com.rocket.api.domain.UtcDateTime;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RocketEventTest {

    private static final UUID CHANNEL = UUID.fromString("193270a9-c9cf-404a-8f83-838e71d9ae67");
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2022-02-02T20:00:00Z"), ZoneOffset.UTC);
    private static final UtcDateTime NOW = UtcDateTime.now(FIXED_CLOCK);

    @Test
    void shouldRejectMessageNumberLessThanOne() {
        assertThatThrownBy(() -> new RocketEvent(
                CHANNEL,
                0,
                NOW,
                MessageType.RocketLaunched,
                Map.of()
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Message number must be a positive integer starting from 1");
    }

    @Test
    void shouldRejectNegativeMessageNumber() {
        assertThatThrownBy(() -> new RocketEvent(
                CHANNEL,
                -1,
                NOW,
                MessageType.RocketLaunched,
                Map.of()
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Message number must be a positive integer starting from 1");
    }

    @Test
    void shouldAcceptMessageNumberOne() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of()
        );

        assertThat(event.messageNumber()).isEqualTo(1);
    }

    @Test
    void shouldExtractRocketTypeFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of("type", "Falcon-9")
        );

        assertThat(event.getRocketType()).isEqualTo("Falcon-9");
    }

    @Test
    void shouldReturnNullForMissingRocketType() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of()
        );

        assertThat(event.getRocketType()).isNull();
    }

    @Test
    void shouldExtractLaunchSpeedFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of("launchSpeed", 500)
        );

        assertThat(event.getLaunchSpeed()).isEqualTo(500);
    }

    @Test
    void shouldReturnNullForMissingLaunchSpeed() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of()
        );

        assertThat(event.getLaunchSpeed()).isNull();
    }

    @Test
    void shouldExtractMissionFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of("mission", "ARTEMIS")
        );

        assertThat(event.getMission()).isEqualTo("ARTEMIS");
    }

    @Test
    void shouldExtractSpeedDeltaFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketSpeedIncreased,
                Map.of("by", 1000)
        );

        assertThat(event.getSpeedDelta()).isEqualTo(1000);
    }

    @Test
    void shouldExtractNewMissionFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketMissionChanged,
                Map.of("newMission", "APOLLO")
        );

        assertThat(event.getNewMission()).isEqualTo("APOLLO");
    }

    @Test
    void shouldExtractExplodedReasonFromPayload() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketExploded,
                Map.of("reason", "PRESSURE_VESSEL_FAILURE")
        );

        assertThat(event.getExplodedReason()).isEqualTo("PRESSURE_VESSEL_FAILURE");
    }

    @Test
    void shouldHandleNumericStringConversion() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketLaunched,
                Map.of("type", 123)  // Number instead of String
        );

        assertThat(event.getRocketType()).isEqualTo("123");
    }

    @Test
    void shouldHandleLongAsSpeed() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketSpeedIncreased,
                Map.of("by", 1000L)  // Long instead of Integer
        );

        assertThat(event.getSpeedDelta()).isEqualTo(1000);
    }

    @Test
    void shouldHandleDoubleAsSpeed() {
        RocketEvent event = new RocketEvent(
                CHANNEL,
                1,
                NOW,
                MessageType.RocketSpeedIncreased,
                Map.of("by", 1000.7)  // Double truncated to int
        );

        assertThat(event.getSpeedDelta()).isEqualTo(1000);
    }
}