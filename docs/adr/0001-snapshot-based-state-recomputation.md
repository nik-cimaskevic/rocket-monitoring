# ADR-0001: Snapshot-Based State Recomputation for Rocket Events

- **Status:** Accepted
- **Date:** 2026-02-22
- **Owners:** Backend Team

## Context

We need to build a service that consumes rocket state change messages and exposes current rocket state via REST API.

**Requirements:**
- Handle out-of-order message delivery
- Handle duplicate messages (at-least-once guarantee)
- Provide current state of rockets for dashboard consumption
- Support up to **100,000 events per rocket**
- Time constraint: 6-hour implementation window

**Constraints:**
- Limited number of rockets (tens, not thousands)
- Short-lived test scenarios
- Single PostgreSQL instance available
- No existing message queue infrastructure

**Problem:**
Speed changes are delta-based (`+3000`, `-2500`), meaning events must be applied in correct order to compute accurate state. Out-of-order delivery complicates incremental updates.

## Decision

Implement **snapshot-based state recomputation** with snapshots every 80 events:

- Store all events in `rocket_event` table with `(channel, message_number)` as primary key
- Store periodic snapshots in `rocket_snapshot` table
- Track `last_processed_msg_number` in both state and snapshots
- On recomputation: load latest snapshot, replay only events since snapshot
- Create new snapshot when `lastProcessedMsgNumber % 80 == 0`
- Duplicate handling via primary key constraint (INSERT fails silently)
- Single Spring `@Transactional` boundary for consistency

### Schema

```sql
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

ALTER TABLE rocket_state
ADD COLUMN last_processed_msg_number INTEGER NOT NULL DEFAULT 0;
```

### Recomputation Flow

```
1. Load latest snapshot for channel (or empty state)
2. Query events WHERE message_number > snapshot.at_message_number
3. Replay events sequentially
4. Save updated state
5. If lastProcessedMsgNumber % 80 == 0 -> save new snapshot
```

## Alternatives Considered

### 1. Full State Recomputation (No Snapshots)

Replay all events from the beginning on every incoming message.

**Pros:**
- Simple implementation (~300 lines of business logic)
- Easy to understand
- Correctness guaranteed

**Cons:**
- O(n) per message — 100,000 events x 50us/event = **5 seconds** per recomputation
- At 2 msg/sec, system cannot keep up

**Rejected:** Unacceptable latency at 100K events scale.

---

### 2. Incremental State Updates with Gap Tracking

```java
if (event.messageNumber == state.lastProcessed + 1) {
    applyDirectly(event);
} else {
    trackGap(event);
    // recompute only when gap is filled
}
```

**Pros:**
- O(1) for in-order messages
- Efficient at scale

**Cons:**
- Complex gap tracking logic
- Edge cases with multiple gaps
- More code, more bugs, more testing

**Rejected:** Complexity not justified; snapshot approach is simpler and sufficient.

---

### 3. Async Processing with Kafka

```
HTTP -> Kafka -> Worker -> PostgreSQL
```

**Pros:**
- Decouples ingestion from processing
- Horizontal scaling of workers
- Durable message buffer

**Cons:**
- Requires Kafka infrastructure (not available)
- Operational complexity (ZooKeeper/KRaft, topics, consumers)
- Eventual consistency on reads
- 6-hour time constraint makes this infeasible

**Rejected:** No existing Kafka; setup time exceeds budget.

---

### 4. CQRS with Separate Read Model

Separate write (events) and read (projections) databases.

**Pros:**
- Optimized read performance
- Scalable independently

**Cons:**
- Two data stores to maintain
- Eventual consistency between write/read
- Significant architectural overhead

**Rejected:** Architectural overkill for current requirements.

---

## Consequences

### Pros

- **Bounded replay cost** — Maximum 79 events to replay (vs 100,000)
- **Simple implementation** — ~350 lines of business logic
- **Backward compatible** — Works with existing events; snapshots build incrementally
- **Easy rollback** — Delete snapshots and `rocket_state`, recompute from events

### Cons / Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Snapshot storage growth | Disk usage | Snapshots are small (~200 bytes each) |
| Snapshot query overhead | Extra DB round-trip | Single indexed lookup; negligible |
| Snapshot inconsistency | Wrong state computed | Snapshots derived from immutable events; always recomputable |
| Single DB bottleneck | Limited throughput | Sufficient for 2 msg/sec |
| No async buffer | Message loss on crash | Acceptable for test scenario |


### When to Revisit

This decision should be **superseded** if:

- [ ] Load exceeds **1000 msg/sec sustained**
- [ ] Snapshot storage exceeds available disk
- [ ] Snapshot lookup becomes a bottleneck (unlikely with indexed PK)

### Observability

Monitor these metrics to validate decision:

```
rocket_event_count{channel} — events per rocket
rocket_state_recompute_duration_ms — time to replay
http_request_duration_ms{path="/messages"} — POST latency
```

Alert if:
- `recompute_duration_ms P95 > 200ms`
- `event_count > 5000` for any channel without recent snapshot