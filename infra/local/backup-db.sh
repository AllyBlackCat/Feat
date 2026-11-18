#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

BACKUP_DIR="infra/local/backups"
mkdir -p "$BACKUP_DIR"

BACKUP_FILE="$BACKUP_DIR/feat_$(date +%Y%m%d_%H%M%S).dump"

docker compose -f infra/local/compose.yaml exec -T db \
  pg_dump -U feat -d feat -Fc > "$BACKUP_FILE"

echo "Backup completed: $BACKUP_FILE"