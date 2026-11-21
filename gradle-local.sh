#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"

if [ -x "$ROOT/gradlew" ]; then
    exec "$ROOT/gradlew" -p "$ROOT" "$@"
fi
if command -v gradle >/dev/null 2>&1; then
    exec gradle -p "$ROOT" "$@"
fi
echo "Gradle haipatikani. Tumia ./gradlew au install Gradle 9.9.1." >&2
exit 1
