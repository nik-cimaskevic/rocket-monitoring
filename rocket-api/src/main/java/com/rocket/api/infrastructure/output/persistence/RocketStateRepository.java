package com.rocket.api.infrastructure.output.persistence;

import com.rocket.api.domain.RocketState;
import com.rocket.api.domain.RocketStatus;
import com.rocket.api.domain.UtcDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RocketStateRepository {

    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("asc", "desc");

    private final JdbcTemplate jdbcTemplate;

    public void save(RocketState state) {
        jdbcTemplate.update("""
                INSERT INTO rocket_state (channel, rocket_type, mission, speed, status, exploded_reason, launched_at, last_updated_at, last_processed_msg_number, stale)
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (channel) DO UPDATE SET
                    rocket_type = EXCLUDED.rocket_type,
                    mission = EXCLUDED.mission,
                    speed = EXCLUDED.speed,
                    status = EXCLUDED.status,
                    exploded_reason = EXCLUDED.exploded_reason,
                    launched_at = EXCLUDED.launched_at,
                    last_updated_at = EXCLUDED.last_updated_at,
                    last_processed_msg_number = EXCLUDED.last_processed_msg_number,
                    stale = EXCLUDED.stale
                """,
                state.channel().toString(),
                state.rocketType(),
                state.mission(),
                state.speed(),
                state.status().name(),
                state.explodedReason(),
                state.launchedAt() != null ? state.launchedAt().toOffsetDateTime() : null,
                state.lastUpdatedAt() != null ? state.lastUpdatedAt().toOffsetDateTime() : null,
                state.lastProcessedMsgNumber(),
                state.stale()
        );
    }

    public Optional<RocketState> findByChannel(UUID channel) {
        List<RocketState> results = jdbcTemplate.query("""
                SELECT channel, rocket_type, mission, speed, status, exploded_reason, launched_at, last_updated_at, last_processed_msg_number, stale
                FROM rocket_state
                WHERE channel = ?::uuid
                """,
                new RocketStateRowMapper(),
                channel.toString()
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public List<RocketState> findAll(String sortBy, String sortOrder) {
        String column = mapSortColumn(sortBy);
        String order = ALLOWED_SORT_ORDERS.contains(sortOrder.toLowerCase()) ? sortOrder.toUpperCase() : "ASC";

        String sql = String.format("""
                SELECT channel, rocket_type, mission, speed, status, exploded_reason, launched_at, last_updated_at, last_processed_msg_number, stale
                FROM rocket_state
                ORDER BY %s %s NULLS LAST
                """, column, order);

        return jdbcTemplate.query(sql, new RocketStateRowMapper());
    }

    private String mapSortColumn(String sortBy) {
        return switch (sortBy) {
            case "type" -> "rocket_type";
            case "speed" -> "speed";
            case "mission" -> "mission";
            case "status" -> "status";
            default -> "rocket_type";
        };
    }

    private static class RocketStateRowMapper implements RowMapper<RocketState> {
        @Override
        public RocketState mapRow(ResultSet rs, int rowNum) throws SQLException {
            OffsetDateTime launchedAt = rs.getObject("launched_at", OffsetDateTime.class);
            OffsetDateTime lastUpdatedAt = rs.getObject("last_updated_at", OffsetDateTime.class);
            return new RocketState(
                    UUID.fromString(rs.getString("channel")),
                    rs.getString("rocket_type"),
                    rs.getString("mission"),
                    rs.getInt("speed"),
                    RocketStatus.valueOf(rs.getString("status")),
                    rs.getString("exploded_reason"),
                    launchedAt != null ? UtcDateTime.of(launchedAt) : null,
                    lastUpdatedAt != null ? UtcDateTime.of(lastUpdatedAt) : null,
                    rs.getInt("last_processed_msg_number"),
                    rs.getBoolean("stale")
            );
        }
    }
}