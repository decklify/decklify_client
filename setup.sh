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
  zip \
  avahi-utils

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

cat > "$TMP/wait-mdns.sh" <<'EOF'
#!/bin/bash
for i in $(seq 1 30); do
  avahi-browse -t _decklify._tcp 2>/dev/null | grep -q "+" && exit 0
  sleep 1
done
exit 1
EOF
chmod +x "$TMP/wait-mdns.sh"

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
# NETWORK MANAGER
# -----------------------------------------------------------------------------

echo "Configuring network manager..."

systemctl disable systemd-networkd-wait-online 2>/dev/null || true

mkdir -p /etc/systemd/system/NetworkManager-wait-online.service.d
cat > /etc/systemd/system/NetworkManager-wait-online.service.d/override.conf <<EOF
[Service]
ExecStart=
ExecStart=/usr/bin/nm-online -s --timeout=30
EOF

systemctl enable NetworkManager-wait-online 2>/dev/null || true

echo "Network manager configured"

# -----------------------------------------------------------------------------
# SYSTEMD SERVICE
# -----------------------------------------------------------------------------

echo "🧠 Creating systemd service"

cat > "$TMP/${SERVICE_NAME}.service" <<EOF
[Unit]
Description=Decklify
After=avahi-daemon.service network-online.target graphical.target
Wants=avahi-daemon.service network-online.target

[Service]
Type=simple
User=$SUDO_USER
ExecStartPre=${BASE}/wait-mdns.sh
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