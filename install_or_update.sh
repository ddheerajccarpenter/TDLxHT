#!/usr/bin/env bash
set -e

# Path to ADB
ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
if ! command -v "$ADB" &> /dev/null; then
  ADB="adb"
fi

echo "=================================================="
echo "TDlxHT - Install or Update App"
echo "=================================================="
echo "Checking connected Android device/emulator..."
$ADB devices

APK_PATH="./TDLxHT.2.3.8.apk"
if [ ! -f "$APK_PATH" ]; then
  APK_PATH="./TDlxHT.apk"
fi
if [ ! -f "$APK_PATH" ]; then
  APK_PATH="./TDlxHT-release.apk"
fi

echo ""
echo "Installing/Updating $APK_PATH with -r (reinstall preserving app data)..."
$ADB install -r "$APK_PATH"
echo ""
echo "✓ Success! TDlxHT is installed/updated."
