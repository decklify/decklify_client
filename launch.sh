#!/usr/bin/env bash

set -e

VERSION_PATH="/opt/decklify/app/version.txt"
APP_PATH="/opt/decklify/app/current"

echo "⬇️ Fetching latest release..."

RELEASE_JSON=$(curl -fsSL -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/decklify/decklify_client/releases/latest)

ASSET_ID=$(jq -r '.assets[] | select(.name | endswith(".jar")) | .id' <<< "$RELEASE_JSON")

CURRENT_TAG=$(cat $VERSION_PATH)
LATEST_TAG=$(jq -r '.tag_name' <<< "$RELEASE_JSON")

CURRENT_TAG=${CURRENT_TAG#v}
LATEST_TAG=${LATEST_TAG#v}

echo "🔧 Checking if installed app is up to date"

if [[ "$(printf '%s\n' "$CURRENT_TAG" "$LATEST_TAG" | sort -V | head -n1)" != "$LATEST_TAG" ]]; then
    echo "⬇️ Downloading latest version..."

    curl -L -H "Accept: application/octet-stream" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        -o "$APP_PATH/app.jar.new" \
        https://api.github.com/repos/decklify/decklify_client/releases/assets/$ASSET_ID

    if [[ ! -s "$APP_PATH/app.jar.new" ]]; then
        echo "Download failed"
        exit 1
    fi

    echo "$LATEST_TAG" > "$VERSION_PATH"
    mv "$APP_PATH/app.jar.new" "$APP_PATH/app.jar"
fi

echo "🚀 Launching app!"

exec /home/$SUDO_USER/.sdkman/candidates/java/current/bin/java -jar "$APP_PATH/app.jar"
