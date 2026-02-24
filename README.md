# Rocket Monitoring System

A production-ready rocket state monitoring system demonstrating modern distributed systems patterns including event sourcing, 
snapshot-based state recomputation, and full observability with the Grafana stack.

## Monorepo Structure

This is a **monorepo** containing all services, infrastructure configuration, and documentation:

```
rocket-monitoring/
├── rocket-api/           # Java Spring Boot backend service
├── rocket-dashboard/     # Nginx-based web dashboard
├── rockets-launcher/     # Go-based test data generator
├── infrastructure/       # Observability stack configs
│   ├── grafana/         # Dashboards & datasources
│   ├── prometheus/      # Metrics collection
│   ├── tempo/           # Distributed tracing
│   ├── loki/            # Log aggregation
│   └── alloy/           # Log collection agent
├── docs/                # Architecture documentation
│   ├── adr/             # Architecture Decision Records
│   └── diagrams/        # C4 & sequence diagrams
├── docker-compose.yml   # Complete infrastructure definition
└── Makefile             # Project automation
```

## High-Level Architecture

![System Context Diagram](docs/diagrams/rendered/diagrams/c4/context/c4-context.png)

For detailed component breakdown, see the [Container Diagram](docs/diagrams/rendered/diagrams/c4/container/c4-container.png).

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Make (optional, but recommended)

### Run Everything

```bash
# Build and start all services (without test data generator)
make all

# Build and start all services WITH the launcher (generates test data)
make all-with-launcher

# Or without Make:
docker compose --profile infra --profile app build rocket-api
docker compose --profile infra --profile app up -d                      # without launcher
docker compose --profile infra --profile app --profile launcher up -d   # with launcher
```

The launcher (`rockets-launcher`) is a test data generator that continuously sends rocket state events to the API. Run with `all-with-launcher` to see the system in action with simulated data.

### Available Endpoints

| Service | URL | Description |
|---------|-----|-------------|
| **Rocket Dashboard** | http://localhost:3001 | Real-time rocket monitoring UI |
| **Rocket API** | http://localhost:8088 | REST API for rocket data |
| **Swagger UI** | http://localhost:8088/swagger-ui.html | Interactive API documentation |
| **OpenAPI Spec** | http://localhost:8088/api-docs | API specification (JSON) |
| **Grafana** | http://localhost:3000 | Observability dashboards (admin/admin) |
| **Prometheus** | http://localhost:9090 | Metrics explorer |
| **Tempo** | http://localhost:3200 | Trace backend |
| **Loki** | http://localhost:3100 | Log backend |

### API Endpoints

```
POST /messages              # Ingest rocket state change events
GET  /rockets               # List all rockets (supports sorting)
GET  /rockets/{rocketId}    # Get specific rocket state
```

Query parameters for `/rockets`:
- `sortBy`: type, speed, mission, status (default: type)
- `sortOrder`: asc, desc (default: asc)

### Useful Commands

```bash
make start               # Start all services (without launcher)
make start-with-launcher # Start all services with test data generator
make stop                # Stop all services
make logs                # Tail all container logs
make logs-api            # Tail rocket-api logs only
make logs-launcher       # Tail launcher logs only
make status              # Show running services
make health              # Check health of all services
make infra               # Start only infrastructure (for local dev)
make rebuild-api         # Rebuild and restart API only
make clean               # Full cleanup with volume removal
```

## Docker Setup (Windows Users)

If you're running on **Windows**, you'll need Docker Desktop installed with WSL 2 backend.

### Setup Instructions for Windows

1. **Install Docker Desktop for Windows**
   - Download from: https://www.docker.com/products/docker-desktop
   - Enable WSL 2 backend during installation

2. **Enable WSL 2 Integration**
   - Open Docker Desktop Settings
   - Go to Resources > WSL Integration
   - Enable integration with your WSL distro

3. **Clone and Run**
   ```powershell
   # In PowerShell or WSL terminal
   git clone <repository-url>
   cd rocket-monitoring

   # Using Docker Compose directly (Make may not be available)
   docker compose --profile infra --profile app build rocket-api
   docker compose --profile infra --profile app up -d

   # To include the test data generator (launcher):
   docker compose --profile infra --profile app --profile launcher up -d
   ```

4. **Verify Services**
   ```powershell
   docker compose --profile infra --profile app --profile launcher ps
   ```

> **Note**: All `make` commands have equivalent `docker compose` commands shown above.

## Documentation

### Architecture

- [Architecture Documentation](docs/architecture.md) - Comprehensive guide to the Clean Architecture implementation, layer responsibilities, data flow, and testing strategy

### Architecture Decision Records

- [ADR-0001: Snapshot-Based State Recomputation](docs/adr/0001-snapshot-based-state-recomputation.md) - Core architectural decision explaining how we handle out-of-order events at scale

### Diagrams

PlantUML diagrams are located in `docs/diagrams/`:

- **C4 Context Diagram**: `docs/diagrams/c4/context/c4-context.puml`
- **C4 Container Diagram**: `docs/diagrams/c4/container/c4-container.puml`
- **Sequence Diagrams**: `docs/diagrams/sequence/`

### Render Documentation

Generate PNG images from all PlantUML diagrams:

```bash
make docs
```

This uses Docker to run PlantUML and outputs rendered PNGs to `docs/diagrams/rendered/`.

### API Documentation

- **Swagger UI**: http://localhost:8088/swagger-ui.html (interactive)
- **OpenAPI YAML**: `rocket-api/src/main/resources/openapi/rocket-api.yaml`

## Observability Dashboards

Access Grafana at http://localhost:3000

### Pre-configured Dashboards

| Dashboard | What You Can See |
|-----------|------------------|
| **RED Metrics** | Rate, Error, Duration metrics for the API. Monitor request throughput, error rates, and latency percentiles (p50, p95, p99). |
| **Loki Logs** | Aggregated logs from all services. Search by service name, log level, or trace ID. Correlate logs with traces. |
| **Infrastructure Metrics** | System-level monitoring: CPU usage, memory consumption, I/O operations across containers. |

### Observability Features

**Metrics (Prometheus)**
- Request rate and error rates
- Response time histograms with percentiles
- JVM metrics (memory, GC, threads)
- Custom business metrics

**Logs (Loki)**
- Centralized logging from all containers
- Full-text search
- Label-based filtering (service, level)
- TraceID extraction for correlation

**Traces (Tempo)**
- Distributed tracing with OpenTelemetry
- Service dependency maps
- Request flow visualization
- Automatic span correlation with logs and metrics

### Correlation

All three pillars are integrated:
- Click a trace ID in logs to jump to the trace
- Click a trace to see related logs
- View exemplars on metrics to drill down to traces

## Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 24, Spring Boot 3.4.1, Gradle |
| Database | PostgreSQL 16 |
| Frontend | HTML/CSS/JS, Nginx |
| Test Generator | Go |
| Metrics | Prometheus, Micrometer |
| Tracing | OpenTelemetry, Tempo |
| Logging | Loki, Alloy |
| Visualization | Grafana |
| Containerization | Docker, Docker Compose |
