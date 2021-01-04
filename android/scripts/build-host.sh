#!/bin/bash

set -e

ROOT_DIR=$(
  cd ..
  pwd
)

source scripts/helper.sh
source "$ROOT_DIR"/deps/libwally-core/tools/android_helpers.sh

echo "${JAVA_HOME:?}"
echo "${ANDROID_NDK:?}"
echo "${CC:?}"

ARCH_LIST=$(android_get_arch_list)
TOOLCHAIN_DIR=$(android_get_build_tools_dir)
USEROPTS="--disable-swig-java --enable-debug"

pushd "$ROOT_DIR/deps/libwally-core"

ROOT_DIR=$(
  cd ../..
  pwd
)

export LDFLAGS="$LDFLAGS -avoid-version"

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
  LIBWALLY_CORE_DIR="$PWD/src/.libs"

  STRIP_TOOL=$(android_get_build_tool "$ARCH" "$TOOLCHAIN_DIR" $API "strip")
  $STRIP_TOOL -o "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE"

  # copy binany files
  echo "Copying libwally-core binary file..."
  OUT_DIR=$ROOT_DIR/android/app/src/main/jniLibs/$ARCH
  mkdir -p "$OUT_DIR"

  cp "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$OUT_DIR"

  echo "Done! $OUT_DIR/$LIBWALLY_CORE_FILE"

done

popd
