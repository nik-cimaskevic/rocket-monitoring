package com.rocket.api.infrastructure.output.persistence;

import com.rocket.api.domain.RocketSnapshot;
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
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RocketSnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(RocketSnapshot snapshot) {
        jdbcTemplate.update("""
                INSERT INTO rocket_snapshot (channel, at_message_number, rocket_type, mission, speed, status, exploded_reason, launched_at)
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (channel, at_message_number) DO NOTHING
                """,
                snapshot.channel().toString(),
                snapshot.atMessageNumber(),
                snapshot.rocketType(),
                snapshot.mission(),
                snapshot.speed(),
                snapshot.status().name(),
                snapshot.explodedReason(),
                snapshot.launchedAt() != null ? snapshot.launchedAt().toOffsetDateTime() : null
        );
    }

    public Optional<RocketSnapshot> findLatestByChannel(UUID channel) {
        List<RocketSnapshot> results = jdbcTemplate.query("""
                SELECT channel, at_message_number, rocket_type, mission, speed, status, exploded_reason, launched_at, created_at
                FROM rocket_snapshot
                WHERE channel = ?::uuid
                ORDER BY at_message_number DESC
                LIMIT 1
                """,
                new RocketSnapshotRowMapper(),
                channel.toString()
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    private static class RocketSnapshotRowMapper implements RowMapper<RocketSnapshot> {
        @Override
        public RocketSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            OffsetDateTime launchedAt = rs.getObject("launched_at", OffsetDateTime.class);
            OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
            return new RocketSnapshot(
                    UUID.fromString(rs.getString("channel")),
                    rs.getInt("at_message_number"),
                    rs.getString("rocket_type"),
                    rs.getString("mission"),
                    rs.getInt("speed"),
                    RocketStatus.valueOf(rs.getString("status")),
                    rs.getString("exploded_reason"),
                    launchedAt != null ? UtcDateTime.of(launchedAt) : null,
                    createdAt != null ? UtcDateTime.of(createdAt) : null
            );
        }
    }
}