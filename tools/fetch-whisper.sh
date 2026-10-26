#!/usr/bin/env bash
# Fetch whisper.cpp at a known-good version for Android build
set -euo pipefail

WHISPER_VERSION="v1.7.4"
WHISPER_DIR="$(dirname "$0")/../app/src/main/cpp/whisper.cpp"
STAMP_FILE="${WHISPER_DIR}.stamp"

# Already fetched?
if [[ -f "$STAMP_FILE" ]] && [[ "$(cat "$STAMP_FILE")" == "$WHISPER_VERSION" ]]; then
    echo "whisper.cpp $WHISPER_VERSION already present"
    exit 0
fi

echo "Fetching whisper.cpp $WHISPER_VERSION..."
cd "$(dirname "$0")/../app/src/main/cpp"
rm -rf whisper.cpp whisper.cpp.tar.gz

# Download and extract
wget -q "https://github.com/ggerganov/whisper.cpp/archive/refs/tags/${WHISPER_VERSION}.tar.gz" -O whisper.cpp.tar.gz
tar -xzf whisper.cpp.tar.gz
mv "whisper.cpp-${WHISPER_VERSION#v}" whisper.cpp
rm whisper.cpp.tar.gz

echo "$WHISPER_VERSION" > "$STAMP_FILE"
echo "Done. Sources at $WHISPER_DIR"