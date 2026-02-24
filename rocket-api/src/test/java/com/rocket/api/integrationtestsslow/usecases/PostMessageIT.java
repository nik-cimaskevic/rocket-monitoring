package com.rocket.api.integrationtestsslow.usecases;

import com.rocket.api.domain.RocketState;
import com.rocket.api.infrastructure.output.persistence.RocketEventRepository;
import com.rocket.api.infrastructure.output.persistence.RocketStateRepository;
import com.rocket.api.setup.AbstractIntegrationTest;
import com.rocket.api.setup.annotations.CleanDBState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CleanDBState
class PostMessageIT extends AbstractIntegrationTest {

    @Autowired
    private RocketEventRepository rocketEventRepository;

    @Autowired
    private RocketStateRepository rocketStateRepository;


    @Test
    void shouldReceiveRocketLaunchedMessage() throws Exception {
        // Given
        String jsonRequest = """
                {
                    "metadata": {
                        "channel": "193270a9-c9cf-404a-8f83-838e71d9ae67",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:39:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Falcon-9",
                        "launchSpeed": 500,
                        "mission": "ARTEMIS"
                    }
                }
                """;

        // When
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // Then - verify event is stored
        UUID channel = UUID.fromString("193270a9-c9cf-404a-8f83-838e71d9ae67");
        int eventCount = rocketEventRepository.countByChannel(channel);
        assertThat(eventCount).isEqualTo(1);

        // Then - verify state is computed
        RocketState state = rocketStateRepository.findByChannel(channel).orElseThrow();
        assertThat(state.rocketType()).isEqualTo("Falcon-9");
        assertThat(state.speed()).isEqualTo(500);
        assertThat(state.mission()).isEqualTo("ARTEMIS");
        assertThat(state.status().name()).isEqualTo("launched");
    }

    @Test
    void shouldHandleDuplicateMessages() throws Exception {
        // Given
        String jsonRequest = """
                {
                    "metadata": {
                        "channel": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:39:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Saturn-V",
                        "launchSpeed": 600,
                        "mission": "APOLLO"
                    }
                }
                """;

        // When - send same message twice
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // Then - only one event stored
        UUID channel = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        int eventCount = rocketEventRepository.countByChannel(channel);
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void shouldHandleOutOfOrderMessages() throws Exception {
        // Given - send message 2 first
        String speedIncreaseMessage = """
                {
                    "metadata": {
                        "channel": "b2c3d4e5-f678-90ab-cdef-123456789012",
                        "messageNumber": 2,
                        "messageTime": "2022-02-02T19:40:05.86337+01:00",
                        "messageType": "RocketSpeedIncreased"
                    },
                    "message": {
                        "by": 1000
                    }
                }
                """;

        String launchMessage = """
                {
                    "metadata": {
                        "channel": "b2c3d4e5-f678-90ab-cdef-123456789012",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:39:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Falcon-9",
                        "launchSpeed": 500,
                        "mission": "ARTEMIS"
                    }
                }
                """;

        // When - send out of order
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(speedIncreaseMessage))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(launchMessage))
                .andExpect(status().isOk());

        // Then - state should be correctly computed
        UUID channel = UUID.fromString("b2c3d4e5-f678-90ab-cdef-123456789012");
        RocketState state = rocketStateRepository.findByChannel(channel).orElseThrow();
        assertThat(state.rocketType()).isEqualTo("Falcon-9");
        assertThat(state.speed()).isEqualTo(1500); // 500 + 1000
        assertThat(state.mission()).isEqualTo("ARTEMIS");
    }

    @Test
    void shouldHandleRocketExplosion() throws Exception {
        // Given
        String launchMessage = """
                {
                    "metadata": {
                        "channel": "c3d4e5f6-7890-abcd-ef12-345678901234",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:39:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Falcon-9",
                        "launchSpeed": 500,
                        "mission": "ARTEMIS"
                    }
                }
                """;

        String explosionMessage = """
                {
                    "metadata": {
                        "channel": "c3d4e5f6-7890-abcd-ef12-345678901234",
                        "messageNumber": 2,
                        "messageTime": "2022-02-02T19:45:00.00000+01:00",
                        "messageType": "RocketExploded"
                    },
                    "message": {
                        "reason": "PRESSURE_VESSEL_FAILURE"
                    }
                }
                """;

        // When
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(launchMessage))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(explosionMessage))
                .andExpect(status().isOk());

        // Then
        UUID channel = UUID.fromString("c3d4e5f6-7890-abcd-ef12-345678901234");
        RocketState state = rocketStateRepository.findByChannel(channel).orElseThrow();
        assertThat(state.status().name()).isEqualTo("exploded");
        assertThat(state.explodedReason()).isEqualTo("PRESSURE_VESSEL_FAILURE");
    }
}