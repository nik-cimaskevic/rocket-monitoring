.PHONY: all build start stop clean logs infra restart status

# Default target - build and start everything
all: build start

# Build the rocket-api Docker image
build:
	@echo "Building rocket-api Docker image..."
	docker compose build rocket-api

# Start all services
start:
	@echo "Starting all services..."
	docker compose up -d
	@echo "Services started. Access points:"
	@echo "  - Rocket API:  http://localhost:8088"
	@echo "  - Prometheus:  http://localhost:9090"
	@echo "  - Grafana:     http://localhost:3000 (admin/admin)"
	@echo "  - Tempo:       http://localhost:3200"
	@echo "  - Loki:        http://localhost:3100"

# Stop all services
stop:
	@echo "Stopping all services..."
	docker compose down

# Full cleanup - stop services and remove volumes
clean:
	@echo "Stopping services and removing volumes..."
	docker compose down -v
	@echo "Removing built images..."
	docker compose down --rmi local

# Tail logs from all services
logs:
	docker compose logs -f

# Start only infrastructure services (for local development)
infra:
	@echo "Starting infrastructure services only..."
	docker compose up -d rocket-postgres prometheus grafana tempo loki alloy
	@echo "Infrastructure started. Run 'make start' to start rocket-api as well."

# Restart all services
restart: stop start

# Show status of all services
status:
	docker compose ps

# Show logs for rocket-api only
logs-api:
	docker compose logs -f rocket-api

# Rebuild and restart rocket-api only
rebuild-api:
	docker compose up -d --build rocket-api

# Health check
health:
	@echo "Checking service health..."
	@curl -sf http://localhost:8088/actuator/health | jq . || echo "rocket-api: not healthy"
	@curl -sf http://localhost:9090/-/healthy && echo "prometheus: healthy" || echo "prometheus: not healthy"
	@curl -sf http://localhost:3000/api/health && echo "grafana: healthy" || echo "grafana: not healthy"
	@curl -sf http://localhost:3200/ready && echo "tempo: healthy" || echo "tempo: not healthy"
	@curl -sf http://localhost:3100/ready && echo "loki: healthy" || echo "loki: not healthy"
