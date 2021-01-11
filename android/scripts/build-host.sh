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
BUILD_LOG_DIR="log"
BUILD_LOG="$BUILD_LOG_DIR/$(date +%s)-build-host-log.txt"
EXTERNAL_BUILD_LOG=$1
if [[ -n $EXTERNAL_BUILD_LOG ]]; then
  BUILD_LOG=$EXTERNAL_BUILD_LOG
fi

config_libwally_core() {
  pushd "$ROOT_DIR/deps/libwally-core"
  export LDFLAGS="$LDFLAGS -avoid-version"
  ./tools/cleanup.sh
  ./tools/autogen.sh
  popd
}

build_libwally_core() {
  pushd "$ROOT_DIR/deps/libwally-core"
  ARCH=$1

  # build libwally-core
  android_build_wally "$ARCH" "$TOOLCHAIN_DIR" "$API" "$USEROPTS"

  # strip libwally-core binary file
  LIBWALLY_CORE_FILE=libwallycore.so
  LIBWALLY_CORE_DIR="$PWD/src/.libs"

  STRIP_TOOL=$(android_get_build_tool "$ARCH" "$TOOLCHAIN_DIR" "$API" "strip")
  $STRIP_TOOL -o "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE"

  # copy binary files
  OUT_DIR=$ROOT_DIR/android/app/src/main/jniLibs/$ARCH
  mkdir -p "$OUT_DIR"

  cp "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$OUT_DIR"

  popd
}

(
  exec 2>&1
  mkdir -p ${BUILD_LOG_DIR}
  touch "$BUILD_LOG"

  config_libwally_core

  for ARCH in $ARCH_LIST; do
    API=19
    if [[ $ARCH == *"64"* ]]; then
      API=21
    fi

    echo "Building libwally-core for '$ARCH'..."
    build_libwally_core "$ARCH"
    echo "Done! $OUT_DIR/$LIBWALLY_CORE_FILE"

  done
) | tee -a "${BUILD_LOG}"
