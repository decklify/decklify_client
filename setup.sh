#!/usr/bin/env bash

###############################################################################
# Decklify setup script
#
# This script installs Decklify and its dependencies.
#
# Usage:
#   curl https://github.com/you/repo/setup.sh | sudo bash
#
# Testing:
#   BASE=/tmp/decklify-test sudo ./setup.sh
###############################################################################

# -----------------------------------------------------------------------------
# SAFETY RAILS
# -----------------------------------------------------------------------------

set -e

# Optional variables (can be overridden for testing)
BASE="${BASE:-/opt/decklify}"          # Final install location
TMP="${BASE}.tmp"                      # Staging directory
SERVICE_NAME="decklify"

APP="${TMP}/app"
LOG="${TMP}/log"

# -----------------------------------------------------------------------------
# HELPER FUNCTIONS
# -----------------------------------------------------------------------------

# Cleanup function — runs automatically on error or Ctrl+C
cleanup() {
  echo "❌ Setup failed — cleaning up temporary files"
  rm -rf "$TMP"
}

# Register cleanup for:
#   - ERR → command failure
#   - INT → Ctrl+C
trap cleanup ERR INT

# -----------------------------------------------------------------------------
# PRECONDITIONS
# -----------------------------------------------------------------------------

# Ensure we are root (required for /opt and systemd)
if [[ "$EUID" -ne 0 ]]; then
  echo "Please run this script as root (use sudo)"
  exit 1
fi

echo "✅ Running as root"

# -----------------------------------------------------------------------------
# DEPENDENCIES
# -----------------------------------------------------------------------------

echo "📦 Installing required packages"

apt update
apt install -y \
  curl \
  jq \
  unzip \
  zip

if [[ ! -d "/home/$SUDO_USER/.sdkman" ]]; then
    sudo -u "$SUDO_USER" bash -c "curl -s 'https://get.sdkman.io?ci=true' | bash"
  else
    echo "✅ Sdkman is already installed!"
fi

sudo -u "$SUDO_USER" bash -c "
  source '/home/$SUDO_USER/.sdkman/bin/sdkman-init.sh'
  sdk install java 23.0.2-tem
"

# -----------------------------------------------------------------------------
# STAGING PHASE
# -----------------------------------------------------------------------------

echo "📁 Preparing staging directory at $TMP"

# Ensure a clean staging area
rm -rf "$TMP"

mkdir -p "$APP"
mkdir -p "$APP/current"
touch "$APP/version.txt"

mkdir -p "$LOG"
touch "$LOG/app.log"

sudo chown -R $SUDO_USER:$SUDO_USER "$BASE"
sudo chmod -R 755 "$BASE"

# -----------------------------------------------------------------------------
# DOWNLOAD LAUNCHER
# -----------------------------------------------------------------------------

echo "⬇️  Downloading launch script"

curl -fL \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "application/vnd.github.raw" \
  https://raw.githubusercontent.com/decklify/decklify_client/master/launch.sh \
  | sudo tee "$TMP/launch.sh" > /dev/null

chmod +x "$TMP/launch.sh"

# Validate launch exists and is executable
[[ -x "$TMP/launch.sh" ]]

# -----------------------------------------------------------------------------
# CREATE SYSTEMD SERVICE FILE
# -----------------------------------------------------------------------------

echo "🧠 Creating systemd service definition"

cat <<EOF > "$TMP/${SERVICE_NAME}.service"
[Unit]
Description=Decklify
After=network-online.target graphical.target
Wants=network-online.target

[Service]
Type=simple
User=$SUDO_USER
ExecStart=${BASE}/launch.sh
Restart=always
RestartSec=2
Environment=DISPLAY=:0

[Install]
WantedBy=graphical.target
EOF

# Validate service file exists
[[ -f "$TMP/${SERVICE_NAME}.service" ]]

# -----------------------------------------------------------------------------
# COMMIT PHASE
# -----------------------------------------------------------------------------

echo "🚀 Committing installation"

# Remove any previous install (safe because we're committing atomically)
rm -rf "$BASE"

# Atomic move: TMP → FINAL
mv "$TMP" "$BASE"

# Install systemd service
mv "$BASE/${SERVICE_NAME}.service" \
       "/etc/systemd/system/${SERVICE_NAME}.service"

# -----------------------------------------------------------------------------
# ACTIVATE SERVICE
# -----------------------------------------------------------------------------

echo "🔧 Enabling systemd service"

systemctl daemon-reload
systemctl enable "$SERVICE_NAME"
systemctl start "$SERVICE_NAME"

# -----------------------------------------------------------------------------
# SUCCESS
# -----------------------------------------------------------------------------

# Disable cleanup trap, we succeeded
trap - ERR INT

echo "✅ Decklify successfully installed and running"
echo "🔁 It will now start automatically on boot"
