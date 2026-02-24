package com.rocket.api.integrationtestsslow.usecases;

import com.rocket.api.setup.AbstractIntegrationTest;
import com.rocket.api.setup.annotations.CleanDBState;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CleanDBState
class GetRocketsIT extends AbstractIntegrationTest {

    @Test
    void shouldReturnRocketById() throws Exception {
        // Given - create a rocket
        String launchMessage = """
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

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(launchMessage))
                .andExpect(status().isOk());

        // When
        var result = mockMvc.perform(get("/rockets/193270a9-c9cf-404a-8f83-838e71d9ae67"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseBody = result.getResponse().getContentAsString();
        String expectedResponse = """
                {
                    "id": "193270a9-c9cf-404a-8f83-838e71d9ae67",
                    "type": "Falcon-9",
                    "speed": 500,
                    "mission": "ARTEMIS",
                    "status": "launched"
                }
                """;
        JSONAssert.assertEquals(expectedResponse, responseBody, JSONCompareMode.LENIENT);
    }

    @Test
    void shouldReturn404ForNonExistentRocket() throws Exception {
        // When & Then
        mockMvc.perform(get("/rockets/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListAllRockets() throws Exception {
        // Given - create multiple rockets
        String rocket1 = """
                {
                    "metadata": {
                        "channel": "11111111-1111-1111-1111-111111111111",
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

        String rocket2 = """
                {
                    "metadata": {
                        "channel": "22222222-2222-2222-2222-222222222222",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:40:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Saturn-V",
                        "launchSpeed": 1000,
                        "mission": "APOLLO"
                    }
                }
                """;

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rocket1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rocket2))
                .andExpect(status().isOk());

        // When
        var result = mockMvc.perform(get("/rockets"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseBody = result.getResponse().getContentAsString();
        String expectedResponse = """
                [
                    {
                        "id": "11111111-1111-1111-1111-111111111111",
                        "type": "Falcon-9",
                        "speed": 500,
                        "mission": "ARTEMIS",
                        "status": "launched"
                    },
                    {
                        "id": "22222222-2222-2222-2222-222222222222",
                        "type": "Saturn-V",
                        "speed": 1000,
                        "mission": "APOLLO",
                        "status": "launched"
                    }
                ]
                """;
        JSONAssert.assertEquals(expectedResponse, responseBody, JSONCompareMode.LENIENT);
    }

    @Test
    void shouldListRocketsSortedBySpeed() throws Exception {
        // Given - create rockets with different speeds
        String slowRocket = """
                {
                    "metadata": {
                        "channel": "33333333-3333-3333-3333-333333333333",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:39:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Slow-Rocket",
                        "launchSpeed": 100,
                        "mission": "SLOW"
                    }
                }
                """;

        String fastRocket = """
                {
                    "metadata": {
                        "channel": "44444444-4444-4444-4444-444444444444",
                        "messageNumber": 1,
                        "messageTime": "2022-02-02T19:40:05.86337+01:00",
                        "messageType": "RocketLaunched"
                    },
                    "message": {
                        "type": "Fast-Rocket",
                        "launchSpeed": 9000,
                        "mission": "FAST"
                    }
                }
                """;

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(slowRocket))
                .andExpect(status().isOk());

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fastRocket))
                .andExpect(status().isOk());

        // When - sort by speed descending
        var result = mockMvc.perform(get("/rockets")
                        .param("sortBy", "speed")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        // Then - fast rocket should be first
        String responseBody = result.getResponse().getContentAsString();
        String expectedResponse = """
                [
                    {
                        "id": "44444444-4444-4444-4444-444444444444",
                        "type": "Fast-Rocket",
                        "speed": 9000,
                        "mission": "FAST",
                        "status": "launched",
                        "stale": false
                    },
                    {
                        "id": "33333333-3333-3333-3333-333333333333",
                        "type": "Slow-Rocket",
                        "speed": 100,
                        "mission": "SLOW",
                        "status": "launched",
                        "stale": false
                    }
                ]
                """;
        JSONAssert.assertEquals(expectedResponse, responseBody, JSONCompareMode.STRICT);
    }

    @Test
    void shouldReturnEmptyListWhenNoRockets() throws Exception {
        // When
        var result = mockMvc.perform(get("/rockets"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseBody = result.getResponse().getContentAsString();
        JSONAssert.assertEquals("[]", responseBody, JSONCompareMode.STRICT);
    }
}