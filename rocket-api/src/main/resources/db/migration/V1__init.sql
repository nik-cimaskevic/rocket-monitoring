-- Rocket Events Table (source of truth - all incoming messages)
CREATE TABLE rocket_event (
    channel         UUID        NOT NULL,
    message_number  INTEGER     NOT NULL,
    message_time    TIMESTAMPTZ NOT NULL,
    message_type    TEXT        NOT NULL,
    payload         JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (channel, message_number)
);

CREATE INDEX idx_rocket_event_channel ON rocket_event (channel);

-- Rocket State Table (materialized current state for fast reads)
CREATE TABLE rocket_state (
    channel                    UUID        PRIMARY KEY,
    rocket_type                TEXT,
    mission                    TEXT,
    speed                      INTEGER     NOT NULL DEFAULT 0,
    status                     TEXT        NOT NULL DEFAULT 'launched',
    exploded_reason            TEXT,
    launched_at                TIMESTAMPTZ,
    last_updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_processed_msg_number  INTEGER     NOT NULL DEFAULT 0,
    stale                      BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_rocket_state_status ON rocket_state (status);
CREATE INDEX idx_rocket_state_rocket_type ON rocket_state (rocket_type);
CREATE INDEX idx_rocket_state_speed ON rocket_state (speed);
CREATE INDEX idx_rocket_state_mission ON rocket_state (mission);

-- Rocket Snapshot Table (periodic state snapshots for efficient replay)
CREATE TABLE rocket_snapshot (
    channel             UUID        NOT NULL,
    at_message_number   INTEGER     NOT NULL,
    rocket_type         TEXT,
    mission             TEXT,
    speed               INTEGER     NOT NULL,
    status              TEXT        NOT NULL,
    exploded_reason     TEXT,
    launched_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (channel, at_message_number)
);

-- Index for finding latest snapshot efficiently
CREATE INDEX idx_rocket_snapshot_channel_msg ON rocket_snapshot (channel, at_message_number DESC);