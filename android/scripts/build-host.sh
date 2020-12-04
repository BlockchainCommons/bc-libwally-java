#!/bin/bash

set -e

source scripts/helper.sh
source ../deps/libwally-core/tools/android_helpers.sh

# Set env var
. ./scripts/setenv.sh

ARCH_LIST=$(android_get_arch_list)
TOOLCHAIN_DIR=$(android_get_build_tools_dir)
USEROPTS="--disable-swig-java --enable-debug"

pushd ../deps/libwally-core

./tools/cleanup.sh
./tools/autogen.sh

for ARCH in $ARCH_LIST; do

  API=19
  if [[ $ARCH == *"64"* ]]; then
    API=21
  fi

  # build libwally-core
  echo "Building libwally-core for '$ARCH'..."
  android_build_wally "$ARCH" "$TOOLCHAIN_DIR" $API "$USEROPTS"

  # strip libwally-core binary file
  LIBWALLY_CORE_FILE=libwallycore.so
  LIBWALLY_CORE_DIR="$PWD/release/lib/$ARCH"
  mkdir -p "$LIBWALLY_CORE_DIR"

  STRIP_TOOL=$(android_get_build_tool "$ARCH" "$TOOLCHAIN_DIR" $API "strip")
  $STRIP_TOOL -o "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$PWD/src/.libs/$LIBWALLY_CORE_FILE"

  # copy binany files
  echo "Copying libwally-core binary file..."
  OUT_DIR=../../android/app/src/main/jniLibs/$ARCH
  mkdir -p "$OUT_DIR"
  if is_osx; then
    cp "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$OUT_DIR/$LIBWALLY_CORE_FILE"
  else
    find "$LIBWALLY_CORE_DIR" -name "$LIBWALLY_CORE_FILE*" -exec cp '{}' "$OUT_DIR" ';'
  fi

  echo "Done! $OUT_DIR/$LIBWALLY_CORE_FILE"

done

popd
