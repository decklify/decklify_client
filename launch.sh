#!/usr/bin/env bash

set -euo pipefail

VERSION_FILE="/opt/decklify/app/version.txt"
APP_JAR="/opt/decklify/app/current/app.jar"
JAVA="/home/$USER/.sdkman/candidates/java/current/bin/java"
REPO="decklify/decklify_client"

# -----------------------------------------------------------------------------
# BACKGROUND UPDATE CHECK
# -----------------------------------------------------------------------------

update_if_needed() {
  RELEASE_JSON=$(curl -fsSL --max-time 10 \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "https://api.github.com/repos/$REPO/releases/latest") || return 0

  LATEST_TAG=$(jq -r '.tag_name' <<< "$RELEASE_JSON")
  ASSET_ID=$(jq -r '.assets[] | select(.name | endswith(".jar")) | .id' <<< "$RELEASE_JSON")
  CURRENT_TAG=$(cat "$VERSION_FILE")

  CURRENT_TAG="${CURRENT_TAG#v}"
  LATEST_TAG="${LATEST_TAG#v}"

  if [[ "$(printf '%s\n' "$CURRENT_TAG" "$LATEST_TAG" | sort -V | head -n1)" != "$LATEST_TAG" ]]; then
    echo "⬇️  Downloading $LATEST_TAG..."
    curl -fsSL --max-time 60 \
      -H "Accept: application/octet-stream" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      -o "${APP_JAR}.new" \
      "https://api.github.com/repos/$REPO/releases/assets/$ASSET_ID"
    mv "${APP_JAR}.new" "$APP_JAR"
    echo "$LATEST_TAG" > "$VERSION_FILE"
    echo "✅ Updated to $LATEST_TAG — restart required"
  fi
}

update_if_needed &

# -----------------------------------------------------------------------------
# LAUNCH IMMEDIATELY
# -----------------------------------------------------------------------------

echo "🚀 Launching..."

exec "$JAVA" \
  -XX:TieredStopAtLevel=1 \
  -Dprism.order=es2 \
  -Dprism.forceGPU=true \
  -Dmonocle.platform=DRM \
  -jar "$APP_JAR"
