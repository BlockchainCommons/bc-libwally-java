#!/bin/bash

set -e

source scripts/helper.sh

# Set env var
. ./scripts/setenv.sh

# Install libwally-core binary
pushd ../deps/libwally-core
./tools/cleanup.sh
./tools/autogen.sh
./configure --disable-swig-jav --enable-debug
make
popd

# Copy binary file
echo "Copying libwally-core binary file..."
LIBWALLY_CORE_DIR=../deps/libwally-core/src/.libs
LIBWALLY_CORE_FILE=libwallycore.so
OUT_DIR=src/main/libs
mkdir -p "$OUT_DIR"

if is_osx; then
  LIBWALLY_CORE_FILE=libwallycore.dylib
  cp "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$OUT_DIR"
else
  find "$LIBWALLY_CORE_DIR" -name "$LIBWALLY_CORE_FILE*" -exec cp '{}' "$OUT_DIR" ';'
fi

echo "Done!"
