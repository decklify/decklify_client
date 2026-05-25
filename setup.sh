#!/usr/bin/env bash

###############################################################################
# Decklify setup script
#
# Usage:
#   curl -fsSL https://raw.githubusercontent.com/decklify/decklify_client/master/setup.sh | sudo bash
#
# Testing:
#   BASE=/tmp/decklify-test sudo ./setup.sh
###############################################################################

set -euo pipefail

# -----------------------------------------------------------------------------
# CONFIG
# -----------------------------------------------------------------------------

BASE="${BASE:-/opt/decklify}"
TMP="${BASE}.tmp"
SERVICE_NAME="decklify"

# -----------------------------------------------------------------------------
# PRECONDITIONS
# -----------------------------------------------------------------------------

[[ "$EUID" -eq 0 ]] || { echo "Please run as root (sudo)"; exit 1; }
[[ -n "${SUDO_USER:-}" ]]  || { echo "SUDO_USER is not set"; exit 1; }

echo "✅ Running as root"

# -----------------------------------------------------------------------------
# CLEANUP
# -----------------------------------------------------------------------------

cleanup() { echo "❌ Setup failed — cleaning up"; rm -rf "$TMP"; }
trap cleanup ERR INT

# -----------------------------------------------------------------------------
# DEPENDENCIES
# -----------------------------------------------------------------------------

echo "📦 Installing dependencies"

apt update
apt install -y \
  curl \
  jq \
  unzip \
  zip

if [[ ! -d "/home/$SUDO_USER/.sdkman" ]]; then
  sudo -u "$SUDO_USER" bash -c "curl -s 'https://get.sdkman.io?ci=true' | bash"
else
  echo "✅ SDKman already installed"
fi

sudo -u "$SUDO_USER" bash -c "
  source '/home/$SUDO_USER/.sdkman/bin/sdkman-init.sh'
  sdk install java 23.0.2-tem
"

# -----------------------------------------------------------------------------
# STAGING
# -----------------------------------------------------------------------------

echo "📁 Preparing staging directory"

rm -rf "$TMP"
mkdir -p "$TMP/app/current" "$TMP/log"
touch "$TMP/app/version.txt" "$TMP/log/app.log"

# -----------------------------------------------------------------------------
# DOWNLOAD
# -----------------------------------------------------------------------------

echo "⬇️  Downloading launch script"

curl -fsSL \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "application/vnd.github.raw" \
  "https://raw.githubusercontent.com/decklify/decklify_client/master/launch.sh" \
  -o "$TMP/launch.sh"

chmod +x "$TMP/launch.sh"

# -----------------------------------------------------------------------------
# SYSTEMD SERVICE
# -----------------------------------------------------------------------------

echo "🧠 Creating systemd service"

cat > "$TMP/${SERVICE_NAME}.service" <<EOF
[Unit]
Description=Decklify
After=network-online.target graphical.target
Wants=network-online.target

[Service]
Type=simple
User=$SUDO_USER
ExecStartPre=/bin/bash -c 'until ping -c1 -W1 224.0.0.251 >/dev/null 2>&1; do sleep 1; done'
ExecStart=${BASE}/launch.sh
Restart=always
RestartSec=2
Environment=DISPLAY=:0

[Install]
WantedBy=graphical.target
EOF

# -----------------------------------------------------------------------------
# COMMIT
# -----------------------------------------------------------------------------

echo "🚀 Installing"

rm -rf "$BASE"
mv "$TMP" "$BASE"
chown -R "$SUDO_USER:$SUDO_USER" "$BASE"
chmod -R 755 "$BASE"

mv "$BASE/${SERVICE_NAME}.service" "/etc/systemd/system/${SERVICE_NAME}.service"

# -----------------------------------------------------------------------------
# ACTIVATE
# -----------------------------------------------------------------------------

echo "🔧 Enabling service"

systemctl daemon-reload
systemctl enable "$SERVICE_NAME"
systemctl start "$SERVICE_NAME"

# -----------------------------------------------------------------------------
# DONE
# -----------------------------------------------------------------------------

trap - ERR INT
echo "✅ Decklify successfully installed! It will now start automatically on boot"