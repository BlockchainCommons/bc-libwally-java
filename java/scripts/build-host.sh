#!/bin/bash

set -e

source scripts/helper.sh

echo "${JAVA_HOME:?}"
echo "${CC:?}"

ROOT_DIR=$(
  cd ..
  pwd
)

BUILD_LOG_DIR="log"
BUILD_LOG="$BUILD_LOG_DIR/$(date +%s)-build-host-log.txt"
EXTERNAL_BUILD_LOG=$1
if [[ -n $EXTERNAL_BUILD_LOG ]]; then
  BUILD_LOG=$EXTERNAL_BUILD_LOG
fi

config_libwally_core() {
  pushd "$ROOT_DIR/deps/libwally-core"
  ./tools/cleanup.sh
  ./tools/autogen.sh
  popd
}

build_libwally_core() {
  # build libwally-core
  pushd "$ROOT_DIR/deps/libwally-core"
  ./configure --disable-swig-java --enable-debug
  make
  popd

  # Copy binary file
  LIBWALLY_CORE_DIR=$ROOT_DIR/deps/libwally-core/src/.libs
  LIBWALLY_CORE_FILE=libwallycore.so
  OUT_DIR=src/main/libs
  mkdir -p "$OUT_DIR"

  if is_osx; then
    LIBWALLY_CORE_FILE=libwallycore.dylib
    cp "$LIBWALLY_CORE_DIR/$LIBWALLY_CORE_FILE" "$OUT_DIR"
  else
    find "$LIBWALLY_CORE_DIR" -name "$LIBWALLY_CORE_FILE*" -exec cp '{}' "$OUT_DIR" ';'
  fi

}

(
  exec 2>&1
  mkdir -p ${BUILD_LOG_DIR}
  touch "$BUILD_LOG"

  config_libwally_core

  build_libwally_core

) | tee -a "${BUILD_LOG}"
