#!/usr/bin/env bash
# Reproducible dev toolchain bootstrap (Linux/macOS).
# Pinned, repository-owned setup per docs/security/DEPENDENCY_POLICY.md supply-chain rules.
set -euo pipefail

echo "==> JDK 21 (via mise)"
command -v mise >/dev/null || { echo "mise not found: https://mise.jdx.dev"; exit 1; }
mise use -g java@temurin-21

if [[ ! -d android-sdk/cmdline-tools/latest ]]; then
  echo "==> Android SDK command-line tools"
  mkdir -p android-sdk/cmdline-tools
  curl -sL -o /tmp/clt.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q /tmp/clt.zip -d android-sdk/cmdline-tools
  rm /tmp/clt.zip
  mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest
fi

echo "==> SDK packages (platform 35, build-tools 35.0.0)"
yes | android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses >/dev/null
android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

echo "local.properties"
echo "sdk.dir=$(cd android-sdk && pwd)" > local.properties

echo "==> ./gradlew assembleDebug"
./gradlew :core:domain:jvmTest :android:app:assembleDebug
