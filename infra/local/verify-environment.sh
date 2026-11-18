#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

COMPOSE_FILE="infra/local/compose.yaml"

echo "Checking PostgreSQL..."
docker compose -f "$COMPOSE_FILE" exec -T db \
  pg_isready -U feat -d feat

echo "Checking Spring Boot..."
docker compose -f "$COMPOSE_FILE" exec -T backend \
  curl -fsS http://localhost:8080/actuator/health

echo
echo "Checking Vite..."
docker compose -f "$COMPOSE_FILE" exec -T frontend \
  node -e "fetch('http://127.0.0.1:5173/').then(r => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"

echo "Checking Nginx..."
curl -fsS http://localhost:8080/ -o /dev/null

echo "All environment checks passed."
