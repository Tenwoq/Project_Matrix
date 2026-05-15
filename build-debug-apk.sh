#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

JAVA_21_HOME="$(/usr/libexec/java_home -v 21)"
JAVA_HOME="$JAVA_21_HOME" PATH="$JAVA_21_HOME/bin:$PATH" ./gradlew :app:assembleDebug

echo "APK: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
