#!/usr/bin/env bash

###############################################################################
# Decklify uninstall script
#
# Usage:
#   curl -fsSL https://raw.githubusercontent.com/decklify/decklify_client/master/uninstall.sh | sudo bash
###############################################################################

set -euo pipefail

SERVICE_NAME="decklify"
BASE="/opt/decklify"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

# -----------------------------------------------------------------------------
# PRECONDITIONS
# -----------------------------------------------------------------------------

[[ "$EUID" -eq 0 ]] || { echo "Please run as root (sudo)"; exit 1; }
[[ -n "${SUDO_USER:-}" ]] || { echo "SUDO_USER is not set"; exit 1; }

read -r -p "⚠️  This will uninstall Decklify. Continue? [y/N] " confirm </dev/tty
[[ "${confirm,,}" == "y" ]] || { echo "Aborted"; exit 0; }

# -----------------------------------------------------------------------------
# STOP & DISABLE SERVICE
# -----------------------------------------------------------------------------

if systemctl is-active --quiet "$SERVICE_NAME"; then
  echo "🛑 Stopping service..."
  systemctl stop "$SERVICE_NAME"
fi

if systemctl is-enabled --quiet "$SERVICE_NAME"; then
  echo "🔧 Disabling service..."
  systemctl disable "$SERVICE_NAME"
fi

if [[ -f "$SERVICE_FILE" ]]; then
  echo "🗑️  Removing service file..."
  rm -f "$SERVICE_FILE"
  systemctl daemon-reload
fi

# -----------------------------------------------------------------------------
# REMOVE APP FILES
# -----------------------------------------------------------------------------

if [[ -d "$BASE" ]]; then
  echo "🗑️  Removing $BASE..."
  rm -rf "$BASE"
fi

# -----------------------------------------------------------------------------
# OPTIONAL: REMOVE JAVA
# -----------------------------------------------------------------------------

read -r -p "Remove Java 23 (installed via SDKman)? [y/N] " remove_java </dev/tty
if [[ "${remove_java,,}" == "y" ]]; then
  echo "Removing Java 23..."
  sudo -u "$SUDO_USER" bash -c "source /home/$SUDO_USER/.sdkman/bin/sdkman-init.sh && sdk uninstall java 23.0.2-tem --force"
  echo "Java 23 removed"
fi

# -----------------------------------------------------------------------------
# OPTIONAL: REMOVE SDKMAN
# -----------------------------------------------------------------------------

read -r -p "Remove SDKman entirely? [y/N] " remove_sdkman </dev/tty
if [[ "${remove_sdkman,,}" == "y" ]]; then
  read -r -p "Backup SDKman first? [y/N] " backup_sdkman </dev/tty
  if [[ "${backup_sdkman,,}" == "y" ]]; then
    sudo -u "$SUDO_USER" bash -c "
      tar zcf \"/home/$SUDO_USER/sdkman-backup_\$(date +%F-%kh%M).tar.gz\" \
        -C \"/home/$SUDO_USER\" .sdkman
    "
    echo "✅ Backup saved to home directory"
  fi

  rm -rf "/home/$SUDO_USER/.sdkman"

  for RC in "/home/$SUDO_USER/.bashrc" \
            "/home/$SUDO_USER/.bash_profile" \
            "/home/$SUDO_USER/.profile" \
            "/home/$SUDO_USER/.zshrc"; do
    if [[ -f "$RC" ]]; then
      sed -i '/THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK/d' "$RC"
      sed -i '/sdkman-init\.sh/d' "$RC"
      echo "✅ Cleaned $RC"
    fi
  done

  echo "✅ SDKman removed"
fi

# -----------------------------------------------------------------------------
# DONE
# -----------------------------------------------------------------------------

echo "✅ Decklify uninstalled"