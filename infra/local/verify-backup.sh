#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

if [[ $# -ne 1 || ! -f "$1" ]]; then
  echo "Usage: bash infra/local/verify-backup.sh <backup-file>"
  exit 1
fi

BACKUP_FILE="$1"
TEMP_DB="feat_verify_$(date +%Y%m%d%H%M%S)_$$"
DB_CREATED=false

cleanup() {
  if [[ "$DB_CREATED" == true ]]; then
    docker compose -f infra/local/compose.yaml exec -T db \
      dropdb -U feat --if-exists --force "$TEMP_DB"
  fi
}

trap cleanup EXIT

# バックアップ形式の検証
docker compose -f infra/local/compose.yaml exec -T db \
  pg_restore -l < "$BACKUP_FILE" > /dev/null

# 復元確認専用の一時データベースを作成
docker compose -f infra/local/compose.yaml exec -T db \
  createdb -U feat "$TEMP_DB"

DB_CREATED=true

# 一時データベースへ復元
docker compose -f infra/local/compose.yaml exec -T db \
  pg_restore -U feat -d "$TEMP_DB" \
  --no-owner --no-acl --exit-on-error < "$BACKUP_FILE"

# Flywayの履歴を確認
docker compose -f infra/local/compose.yaml exec -T db \
  psql -U feat -d "$TEMP_DB" -v ON_ERROR_STOP=1 \
  -c "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;"

echo "Backup verification completed."
