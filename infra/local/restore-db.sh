#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

if [[ $# -ne 1 || ! -f "$1" ]]; then
  echo "Usage: bash infra/local/restore-db.sh <backup-file>"
  exit 1
fi

BACKUP_FILE="$1"

echo "Restore target: feat (local PostgreSQL)"
echo "Backup file: $BACKUP_FILE"
echo "WARNING: Existing database objects and data may be overwritten."
read -r -p "Type RESTORE to continue: " CONFIRM

if [[ "$CONFIRM" != "RESTORE" ]]; then
  echo "Cancelled."
  exit 0
fi

# 先にバックアップ形式を検証する
docker compose -f infra/local/compose.yaml exec -T db \
  pg_restore -l < "$BACKUP_FILE" > /dev/null

# アプリケーションからのDB接続を停止
docker compose -f infra/local/compose.yaml stop backend

# 復元後、バックエンドを起動する
trap 'docker compose -f infra/local/compose.yaml start backend' EXIT

docker compose -f infra/local/compose.yaml exec -T db \
  pg_restore \
    -U feat \
    -d feat \
    --clean \
    --if-exists \
    --no-owner \
    --no-acl \
    --exit-on-error < "$BACKUP_FILE"

echo "Restore completed."
