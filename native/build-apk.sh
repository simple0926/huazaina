#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="${ANDROID_SDK_ROOT:-/tmp/android-build/sdk}"
JDK="${JAVA_HOME:-/tmp/android-build/amazon-corretto-17.jdk/Contents/Home}"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-34/android.jar"
export JAVA_HOME="$JDK"
export PATH="$JDK/bin:$PATH"
VERSION="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$ROOT/AndroidManifest.xml")"
OUT="花在哪-原生-v${VERSION}.apk"
rm -rf "$ROOT/build" "$ROOT/release"
mkdir -p "$ROOT/build/classes" "$ROOT/build/dex" "$ROOT/build/gen" "$ROOT/release"
echo "→ resources"
"$BT/aapt2" compile --dir "$ROOT/res" -o "$ROOT/build/res.zip"
"$BT/aapt2" link -o "$ROOT/build/app-unsigned.apk" -I "$JAR" \
  --manifest "$ROOT/AndroidManifest.xml" -R "$ROOT/build/res.zip" \
  --java "$ROOT/build/gen" --auto-add-overlay
echo "→ java"
find "$ROOT/java" -name '*.java' > "$ROOT/build/sources.txt"
find "$ROOT/build/gen" -name '*.java' >> "$ROOT/build/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -classpath "$JAR" -d "$ROOT/build/classes" @"$ROOT/build/sources.txt"
echo "→ dex"
cd "$ROOT/build/classes" && jar cf "$ROOT/build/classes.jar" . && cd "$ROOT"
"$BT/d8" --release --lib "$JAR" --min-api 29 --output "$ROOT/build/dex" "$ROOT/build/classes.jar"
(cd "$ROOT/build/dex" && zip -q "$ROOT/build/app-unsigned.apk" classes.dex)
"$BT/zipalign" -f 4 "$ROOT/build/app-unsigned.apk" "$ROOT/build/app-aligned.apk"
KS="${KEYSTORE_FILE:-$ROOT/../android/huazaina-release.keystore}"
PROPS="$ROOT/../android/keystore.properties"
if [ -f "$PROPS" ]; then set -a; . "$PROPS"; set +a; fi
STORE_PASS="${KEYSTORE_PASS:-${storePassword:-}}"
KEY_PASS="${KEYSTORE_KEY_PASS:-${keyPassword:-$STORE_PASS}}"
ALIAS="${KEYSTORE_ALIAS:-${keyAlias:-huazaina}}"
if [ ! -f "$KS" ] || [ -z "$STORE_PASS" ]; then echo "缺少签名密钥/密码" >&2; exit 1; fi
"$BT/apksigner" sign --ks "$KS" --ks-key-alias "$ALIAS" \
  --ks-pass "pass:$STORE_PASS" --key-pass "pass:$KEY_PASS" \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out "$ROOT/release/$OUT" "$ROOT/build/app-aligned.apk"
"$BT/apksigner" verify --verbose "$ROOT/release/$OUT" | head -5
"$BT/aapt" dump badging "$ROOT/release/$OUT" | head -6
ls -lh "$ROOT/release/$OUT"
