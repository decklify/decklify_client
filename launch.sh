#!/usr/bin/env bash

set -euo pipefail

VERSION_FILE="/opt/decklify/app/version.txt"
APP_JAR="/opt/decklify/app/current/app.jar"
JAVA="/home/$USER/.sdkman/candidates/java/current/bin/java"
REPO="decklify/decklify_client"

# -----------------------------------------------------------------------------
# FETCH RELEASE INFO
# -----------------------------------------------------------------------------

echo "⬇️  Fetching latest release..."

RELEASE_JSON=$(curl -fsSL \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "https://api.github.com/repos/$REPO/releases/latest")

LATEST_TAG=$(jq -r '.tag_name' <<< "$RELEASE_JSON")
ASSET_ID=$(jq -r '.assets[] | select(.name | endswith(".jar")) | .id' <<< "$RELEASE_JSON")
CURRENT_TAG=$(cat "$VERSION_FILE")

# Strip leading 'v' for comparison
CURRENT_TAG="${CURRENT_TAG#v}"
LATEST_TAG="${LATEST_TAG#v}"

# -----------------------------------------------------------------------------
# UPDATE IF NEEDED
# -----------------------------------------------------------------------------

if [[ "$(printf '%s\n' "$CURRENT_TAG" "$LATEST_TAG" | sort -V | head -n1)" != "$LATEST_TAG" ]]; then
  echo "⬇️  Downloading $LATEST_TAG..."

  curl -fsSL \
    -H "Accept: application/octet-stream" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -o "${APP_JAR}.new" \
    "https://api.github.com/repos/$REPO/releases/assets/$ASSET_ID"

  mv "${APP_JAR}.new" "$APP_JAR"
  echo "$LATEST_TAG" > "$VERSION_FILE"
  echo "✅ Updated to $LATEST_TAG"
else
  echo "✅ Already on latest ($CURRENT_TAG)"
fi

# -----------------------------------------------------------------------------
# LAUNCH
# -----------------------------------------------------------------------------

echo "🚀 Launching..."

exec "$JAVA" \
  -Dprism.order=es2 \
  -Dprism.forceGPU=true \
  -Dmonocle.platform=DRM \
  -jar "$APP_JAR"
