#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

COMPOSE_FILE="infra/local/compose.yaml"

echo "WARNING: Local database 'feat' will be deleted."
echo "All existing data will be lost."
read -r -p "Type RESET to continue: " CONFIRM

if [[ "$CONFIRM" != "RESET" ]]; then
  echo "Cancelled."
  exit 0
fi

# バックエンドを停止
docker compose -f "$COMPOSE_FILE" stop backend

# 処理中にエラーが発生してもバックエンドの再起動を試みる
trap 'docker compose -f "$COMPOSE_FILE" start backend' EXIT

# ローカルDBを削除・再作成
docker compose -f "$COMPOSE_FILE" exec -T db \
  dropdb -U feat --force feat

docker compose -f "$COMPOSE_FILE" exec -T db \
  createdb -U feat feat

echo "Database recreated."

# バックエンドを起動し、Flywayを実行
docker compose -f "$COMPOSE_FILE" start backend

trap - EXIT

echo "Database reset completed."
