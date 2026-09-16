#!/usr/bin/env bash
set -Eeuo pipefail

if [[ "${1:-}" != '--confirm' || "$#" -ne 1 ]]; then
  cat >&2 <<'USAGE'
Usage: ./scripts/reset-seed.sh --confirm

This permanently removes the local Compose Postgres volume and its data.
Stop the backend before running this command. After it completes, restart the
backend so Flyway recreates the schema and loads the deterministic seed rows.
USAGE
  exit 64
fi

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
modernized_dir="$(cd -- "${script_dir}/.." && pwd)"

printf 'Removing the local Postgres container and volume...\n'
docker compose --project-directory "$modernized_dir" \
  --file "$modernized_dir/compose.yaml" down --volumes

printf 'Starting a clean Postgres instance...\n'
docker compose --project-directory "$modernized_dir" \
  --file "$modernized_dir/compose.yaml" up --detach db

cat <<'NEXT'
Postgres is clean. Restart the backend now. Flyway will recreate the schema and
load the deterministic V2/V4/V6/V8-V13 fixture data during backend startup.
NEXT
