package com.rocket.api.infrastructure.output.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocket.api.domain.MessageType;
import com.rocket.api.domain.RocketEvent;
import com.rocket.api.domain.UtcDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RocketEventRepository{

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public boolean save(RocketEvent event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event.payload());
            jdbcTemplate.update("""
                INSERT INTO rocket_event (channel, message_number, message_time, message_type, payload)
                VALUES (?::uuid, ?, ?, ?, ?::jsonb)
                """,
                    event.channel().toString(),
                    event.messageNumber(),
                    event.messageTime().toOffsetDateTime(),
                    event.messageType().name(),
                    payloadJson
            );
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    public List<RocketEvent> findByChannelAfterMessageNumber(UUID channel, int afterMessageNumber) {
        return jdbcTemplate.query("""
                SELECT channel, message_number, message_time, message_type, payload
                FROM rocket_event
                WHERE channel = ?::uuid AND message_number > ?
                ORDER BY message_number ASC
                """,
                new RocketEventRowMapper(),
                channel.toString(),
                afterMessageNumber
        );
    }

    public int countByChannel(UUID channel) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rocket_event WHERE channel = ?::uuid",
                Integer.class,
                channel.toString()
        );
        return count != null ? count : 0;
    }

    private class RocketEventRowMapper implements RowMapper<RocketEvent> {
        @Override
        public RocketEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String payloadJson = rs.getString("payload");
                Map<String, Object> payload = objectMapper.readValue(
                        payloadJson,
                        new TypeReference<>() {}
                );

                return new RocketEvent(
                        UUID.fromString(rs.getString("channel")),
                        rs.getInt("message_number"),
                        UtcDateTime.of(rs.getObject("message_time", OffsetDateTime.class)),
                        MessageType.valueOf(rs.getString("message_type")),
                        payload
                );
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to deserialize event payload", e);
            }
        }
    }
}