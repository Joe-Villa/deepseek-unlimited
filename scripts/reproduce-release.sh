#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"

# The maintainer's local vendor/ cache is intentionally not committed (it is large).
# Prefer it when present for an offline, byte-identical build; otherwise fall back
# to the Gradle wrapper and the Android SDK on this machine.
GRADLE_FLAGS=()
if [[ -x "$PWD/vendor/gradle-9.4.1/bin/gradle" && -d "$PWD/vendor/.gradle" ]]; then
  export GRADLE_USER_HOME="$PWD/vendor/.gradle"
  GRADLE="$PWD/vendor/gradle-9.4.1/bin/gradle"
  GRADLE_FLAGS=(--offline)
  APKSIGNER="$PWD/vendor/android-sdk/build-tools/37.0.0/apksigner"
else
  GRADLE="$PWD/gradlew"
  SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
  if [[ -z "$SDK" && -f local.properties ]]; then
    SDK="$(grep '^sdk.dir=' local.properties | cut -d= -f2- || true)"
  fi
  APKSIGNER="${SDK}/build-tools/37.0.0/apksigner"
  if [[ ! -x "$APKSIGNER" ]]; then
    APKSIGNER="$(command -v apksigner || true)"
  fi
fi

if [[ ! -x "$APKSIGNER" ]]; then
  echo "apksigner not found. Set ANDROID_HOME/ANDROID_SDK_ROOT or install build-tools 37.0.0." >&2
  exit 1
fi

RELEASE="${1:-$PWD/dist/deepseek-unlimited-0.11.3.apk}"
if [[ ! -f "$RELEASE" ]]; then
  echo "Release APK not found: $RELEASE" >&2
  echo "Download deepseek-unlimited-0.11.3.apk from GitHub Releases and pass its path:" >&2
  echo "  ./scripts/reproduce-release.sh /path/to/deepseek-unlimited-0.11.3.apk" >&2
  exit 1
fi

echo "== 1. verify source manifest =="
sha256sum -c SOURCE-MANIFEST.sha256

echo "== 2. clean build =="
"$GRADLE" clean :app:assembleRelease "${GRADLE_FLAGS[@]}" --no-daemon --console=plain

echo "== 3. canonical v2-only re-sign (same debug key as release) =="
mkdir -p build/verify
"$APKSIGNER" sign \
  --ks "$PWD/keystore/debug.keystore" \
  --ks-pass pass:android \
  --key-pass pass:android \
  --ks-key-alias androiddebugkey \
  --v1-signing-enabled false \
  --v2-signing-enabled true \
  --v3-signing-enabled false \
  --out build/verify/deepseek-reproduced.apk \
  app/build/outputs/apk/release/app-release.apk

echo "== 4. compare =="
sha256sum build/verify/deepseek-reproduced.apk "$RELEASE"
if cmp -s build/verify/deepseek-reproduced.apk "$RELEASE"; then
  echo "OK: rebuilt APK is byte-identical to the published APK."
else
  echo "MISMATCH: rebuilt APK differs from the published APK." >&2
  exit 1
fi
"$APKSIGNER" verify --print-certs build/verify/deepseek-reproduced.apk
