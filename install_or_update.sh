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

LATEST_VERSIONED_APK=$(ls -t TDLxHT.*.apk 2>/dev/null | head -n 1 || true)
if [ -n "$LATEST_VERSIONED_APK" ] && [ -f "$LATEST_VERSIONED_APK" ]; then
  APK_PATH="./$LATEST_VERSIONED_APK"
elif [ -f "./TDlxHT.apk" ]; then
  APK_PATH="./TDlxHT.apk"
elif [ -f "./TDlxHT-release.apk" ]; then
  APK_PATH="./TDlxHT-release.apk"
else
  echo "Error: No APK found in current directory. Run ./gradlew syncReleaseApk first."
  exit 1
fi

echo ""
echo "Installing/Updating $APK_PATH with -r (reinstall preserving app data)..."
$ADB install -r "$APK_PATH"
echo ""
echo "✓ Success! TDlxHT is installed/updated."
