#!/usr/bin/env bash

set -euo pipefail

port_is_available() {
    python3 - "$1" <<'PY'
import socket
import sys

port = int(sys.argv[1])
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as probe:
    probe.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        probe.bind(("127.0.0.1", port))
    except OSError:
        raise SystemExit(1)
PY
}

next_available_port() {
    local port="$1"
    while ! port_is_available "$port"; do
        ((port += 1))
        if ((port > 65535)); then
            printf 'No available port found from %s through 65535.\n' "$1" >&2
            return 1
        fi
    done
    printf '%s\n' "$port"
}

POSTGRES_PORT="$(next_available_port "${POSTGRES_PORT:-5433}")"
SERVER_PORT="$(next_available_port "${SERVER_PORT:-8081}")"
export POSTGRES_PORT SERVER_PORT

printf 'PostgreSQL: localhost:%s\n' "$POSTGRES_PORT"
printf 'Dashboard:  http://localhost:%s/\n' "$SERVER_PORT"

if [[ "${1:-}" == "--print-only" ]]; then
    exit 0
fi

docker compose up -d --wait postgres
exec ./gradlew bootRun
