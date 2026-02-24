.PHONY: all all-with-launcher build start start-with-launcher stop clean logs infra restart status docs

# Default target - build and start everything (without launcher)
all: build start

# Build and start everything including the launcher
all-with-launcher: build start-with-launcher

# Build the rocket-api Docker image
build:
	@echo "Building rocket-api Docker image..."
	docker compose --profile infra --profile app build rocket-api

# Start all services (without launcher)
start:
	@echo "Starting all services..."
	docker compose --profile infra --profile app up -d
	@echo "Services started. Access points:"
	@echo "  - Rocket API:  http://localhost:8088"
	@echo "  - Dashboard:   http://localhost:3001"
	@echo "  - Prometheus:  http://localhost:9090"
	@echo "  - Grafana:     http://localhost:3000 (admin/admin)"
	@echo "  - Tempo:       http://localhost:3200"
	@echo "  - Loki:        http://localhost:3100"
	@echo ""
	@echo "Tip: Run 'make start-with-launcher' to include the test data generator"

# Start all services including the launcher (test data generator)
start-with-launcher:
	@echo "Starting all services with launcher..."
	docker compose --profile infra --profile app --profile launcher up -d
	@echo "Services started. Access points:"
	@echo "  - Rocket API:  http://localhost:8088"
	@echo "  - Dashboard:   http://localhost:3001"
	@echo "  - Prometheus:  http://localhost:9090"
	@echo "  - Grafana:     http://localhost:3000 (admin/admin)"
	@echo "  - Tempo:       http://localhost:3200"
	@echo "  - Loki:        http://localhost:3100"
	@echo ""
	@echo "Launcher is running and generating test data."

# Stop all services
stop:
	@echo "Stopping all services..."
	docker compose --profile infra --profile app --profile launcher down

# Full cleanup - stop services and remove volumes
clean:
	@echo "Stopping services and removing volumes..."
	docker compose --profile infra --profile app --profile launcher down -v
	@echo "Removing built images..."
	docker compose --profile infra --profile app --profile launcher down --rmi local

# Tail logs from all services
logs:
	docker compose --profile infra --profile app --profile launcher logs -f

# Start only infrastructure services (for local development)
infra:
	@echo "Starting infrastructure services only..."
	docker compose --profile infra up -d
	@echo "Infrastructure started. Run 'make start' to start rocket-api as well."

# Restart all services
restart: stop start

# Show status of all services
status:
	docker compose --profile infra --profile app --profile launcher ps

# Show logs for rocket-api only
logs-api:
	docker compose logs -f rocket-api

# Show logs for launcher only
logs-launcher:
	docker compose logs -f rockets-launcher

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

# Generate PNG diagrams from PlantUML files (using Docker)
# Outputs to docs/diagrams/rendered/ preserving directory structure
docs:
	@echo "Generating PNG diagrams from PlantUML files..."
	@find docs -name "*.puml" | while read f; do \
		relpath=$${f#docs/}; \
		outdir="docs/diagrams/rendered/$$(dirname $$relpath)"; \
		mkdir -p "$$outdir"; \
		docker run --rm -v "$(PWD)/docs:/docs" plantuml/plantuml -tpng -o "/docs/diagrams/rendered/$$(dirname $$relpath)" "/docs/$$relpath"; \
	done
	@echo "Done. PNG files generated in docs/diagrams/rendered/"
