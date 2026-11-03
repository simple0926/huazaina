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
OUT="花在哪-v${VERSION}.apk"

rm -rf "$ROOT/build" "$ROOT/release"
mkdir -p "$ROOT/build/classes" "$ROOT/build/dex" "$ROOT/build/gen" "$ROOT/release"

echo "→ 生成 HTML 单文件"
node "$ROOT/../build.mjs"

echo "→ 编译资源"
"$BT/aapt2" compile --dir "$ROOT/res" -o "$ROOT/build/res.zip"
"$BT/aapt2" link -o "$ROOT/build/app-unsigned.apk" \
  -I "$JAR" --manifest "$ROOT/AndroidManifest.xml" \
  -R "$ROOT/build/res.zip" --java "$ROOT/build/gen" --auto-add-overlay

echo "→ 编译 Java"
find "$ROOT/java" -name "*.java" > "$ROOT/build/sources.txt"
find "$ROOT/build/gen" -name "*.java" >> "$ROOT/build/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -classpath "$JAR" -d "$ROOT/build/classes" @"$ROOT/build/sources.txt"

echo "→ dex"
cd "$ROOT/build/classes" && jar cf "$ROOT/build/classes.jar" . && cd "$ROOT"
"$BT/d8" --release --lib "$JAR" --min-api 29 --output "$ROOT/build/dex" "$ROOT/build/classes.jar"

echo "→ 打包 assets"
( cd "$ROOT/build/dex" && zip -q "$ROOT/build/app-unsigned.apk" classes.dex )
( cd "$ROOT" && zip -qr "$ROOT/build/app-unsigned.apk" assets )
"$BT/zipalign" -f 4 "$ROOT/build/app-unsigned.apk" "$ROOT/build/app-aligned.apk"

# 签名
KS="${KEYSTORE_FILE:-$ROOT/huazaina-release.keystore}"
PROPS="$ROOT/keystore.properties"
if [ -f "$PROPS" ]; then
  set -a; . "$PROPS"; set +a
fi
STORE_PASS="${KEYSTORE_PASS:-${storePassword:-}}"
KEY_PASS="${KEYSTORE_KEY_PASS:-${keyPassword:-$STORE_PASS}}"
ALIAS="${KEYSTORE_ALIAS:-${keyAlias:-huazaina}}"

if [ ! -f "$KS" ]; then
  if [ -z "$STORE_PASS" ]; then
    echo "缺少签名密钥，且没有提供 KEYSTORE_PASS。" >&2
    exit 1
  fi
  echo "→ 生成新的签名密钥"
  keytool -genkeypair -v -keystore "$KS" -alias "$ALIAS" -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass "$STORE_PASS" -keypass "$KEY_PASS" \
    -dname "CN=Huazaina, OU=Personal, O=Huazaina, L=Shanghai, ST=Shanghai, C=CN" >/dev/null 2>&1
fi

echo "→ 签名"
"$BT/apksigner" sign --ks "$KS" --ks-key-alias "$ALIAS" \
  --ks-pass "pass:$STORE_PASS" --key-pass "pass:$KEY_PASS" \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out "$ROOT/release/$OUT" "$ROOT/build/app-aligned.apk"

"$BT/apksigner" verify --verbose "$ROOT/release/$OUT" | head -5
echo "---"
"$BT/aapt" dump badging "$ROOT/release/$OUT" | head -6
ls -lh "$ROOT/release/$OUT"
