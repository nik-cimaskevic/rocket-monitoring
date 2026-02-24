# Architecture Documentation

This document describes the architecture of the Rocket Monitoring System, focusing on the Clean Architecture implementation in the backend service.

## Table of Contents

- [System Overview](#system-overview)
- [Clean Architecture](#clean-architecture)
- [Layer Details](#layer-details)
- [Testing Architecture](#testing-architecture)
- [Observability](#observability)

---

## System Overview

The Rocket Monitoring System is a distributed application for tracking rocket state changes in real-time. It handles event ingestion, state computation, and provides REST APIs for querying current rocket states.

## Clean Architecture

The backend (`rocket-api`) follows Clean Architecture principles, organizing code into concentric layers with dependencies pointing inward.

### Layer Diagram

```
┌───────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                       │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    Use Case Layer                        │  │
│  │  ┌─────────────────────────────────────────────────────┐│  │
│  │  │                  Domain Layer                       ││  │
│  │  │                                                     ││  │
│  │  │   RocketState, RocketEvent, RocketSnapshot          ││  │
│  │  │   MessageType, RocketStatus, UtcDateTime            ││  │
│  │  │                                                     ││  │
│  │  └─────────────────────────────────────────────────────┘│  │
│  │                                                          │  │
│  │   ReceiveMessageUseCase, GetRocketUseCase                │  │
│  │   ListRocketsUseCase                                     │  │
│  │                                                          │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                               │
│   Controllers (HTTP Adapters)    Repositories (DB Adapters)   │
│   MessagesController             RocketEventRepository        │
│   RocketsController              RocketStateRepository        │
│                                  RocketSnapshotRepository     │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

### Dependency Rule

Dependencies **always point inward**:

| Layer | Depends On | Never Depends On |
|-------|------------|------------------|
| Domain | Nothing | Use Cases, Infrastructure |
| Use Cases | Domain only | Infrastructure |
| Infrastructure | Use Cases, Domain | Nothing (outermost) |

### Package Structure

```
com.rocket.api/
├── domain/                      # Innermost layer - pure Java
│   ├── RocketState.java         # Aggregate root
│   ├── RocketEvent.java         # Domain event
│   ├── RocketSnapshot.java      # Optimization artifact
│   ├── MessageType.java         # Event type enum
│   ├── RocketStatus.java        # Status enum
│   └── UtcDateTime.java         # Value object
│
├── usecase/                     # Application business rules
│   ├── ReceiveMessageUseCase.java
│   ├── GetRocketUseCase.java
│   └── ListRocketsUseCase.java
│
├── infrastructure/              # Outermost layer - frameworks
│   ├── input/http/              # Driving adapters
│   │   ├── MessagesController.java
│   │   └── RocketsController.java
│   └── output/persistence/      # Driven adapters
│       ├── RocketEventRepository.java
│       ├── RocketStateRepository.java
│       └── RocketSnapshotRepository.java
│
└── common/                      # Cross-cutting concerns
    ├── exceptions/              # Exception hierarchy
    ├── beans/clock/             # Clock configuration
    ├── jackson/                 # JSON serialization
    └── uuid/                    # UUID utilities
```

---

## Layer Details

### Domain Layer

The domain layer contains **enterprise business rules** with zero external dependencies. All objects are immutable Java records.

### Use Case Layer

Use cases orchestrate domain logic and coordinate with io like repositories. They define **application business rules**.

### Infrastructure Layer

The infrastructure layer contains **adapters** that connect the application to external systems.

#### Input Adapters (HTTP Controllers, Event listeners or Jobs)

Controllers implement OpenAPI-generated interfaces and delegate to use cases:

#### Output Adapters (Repositories)

Repositories use `JdbcTemplate` for raw SQL access

## Testing Architecture

### Three-Tier Strategy

```
┌───────────────────────────────────────────────────────────────┐
│                    Slow Integration Tests                      │
│            (Real DB, MockMvc, Testcontainers)                 │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                Fast Integration Tests                    │  │
│  │              (Mockito only, no Spring)                  │  │
│  │                                                          │  │
│  │  ┌─────────────────────────────────────────────────────┐│  │
│  │  │                   Unit Tests                        ││  │
│  │  │            (Pure Java, domain logic)                ││  │
│  │  └─────────────────────────────────────────────────────┘│  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

### Test Types

| Type | Location | Dependencies | Speed | Coverage |
|------|----------|--------------|-------|----------|
| Unit | `unittests/domain/` | None | ~ms | Domain logic |
| Fast Integration | `integrationtestsfast/` | Mockito | ~100ms | Use case orchestration |
| Slow Integration | `integrationtestsslow/` | Testcontainers, MockMvc | ~seconds | Full request/response |

## Observability

### Three Pillars

| Pillar | Tool | Purpose |
|--------|------|---------|
| Metrics | Prometheus + Grafana | Request rates, latencies, errors |
| Traces | OpenTelemetry + Tempo | Request flow visualization |
| Logs | Loki + Alloy | Centralized log aggregation |

### Correlation

All three pillars are integrated:
- Logs include `traceId` for correlation
- Grafana dashboards link metrics → traces → logs
- Exemplars on metrics link directly to traces