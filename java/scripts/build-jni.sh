#!/bin/bash

set -e

ROOT_DIR=$(
  cd ..
  pwd
)

source scripts/helper.sh

echo "${JAVA_HOME:?}"
echo "${CC:?}"

OUT_DIR=src/main/libs
LIBWALLY_CORE_FILE=libwallycore.so
JNI_MD_DIR="linux"
JNI_LIBS=(bc-libwally-address-jni bc-libwally-bip32-jni bc-libwally-bip39-jni bc-libwally-crypto-jni bc-libwally-core-jni bc-libwally-script-jni bc-libwally-tx-jni bc-libwally-psbt-jni)
BUILD_LOG_DIR="log"
BUILD_LOG="$BUILD_LOG_DIR/$(date +%s)-build-jni-log.txt"
if [[ -n $1 ]]; then
  BUILD_LOG=$1
fi

if is_osx; then
  JNI_MD_DIR="darwin"
  LIBWALLY_CORE_FILE=libwallycore.dylib
fi

build_jni() {
  LIB=$1
  LIB_NAME="lib${LIB}.so"
  if is_osx; then
    LIB_NAME="lib${LIB}.dylib"
  fi

  SRC_ROOT_DIR=src/main
  WALLY_ROOT_DIR=$ROOT_DIR/deps/libwally-core

  echo "Building $LIB_NAME..."

  $CC -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/$JNI_MD_DIR" \
    -I"$WALLY_ROOT_DIR/include" \
    -I"$WALLY_ROOT_DIR/src" \
    -L$OUT_DIR \
    -shared -fPIC \
    "$SRC_ROOT_DIR/jniLibs/${LIB}.c" \
    "$SRC_ROOT_DIR/jniLibs/jni-utils.c" \
    "$WALLY_ROOT_DIR/src/ccan/ccan/base64/base64.c" \
    -o \
    "$OUT_DIR/$LIB_NAME" \
    "$OUT_DIR/$LIBWALLY_CORE_FILE"

  echo "Done! Checkout the release file at $OUT_DIR/$LIB_NAME"
}

(
  exec 2>&1
  mkdir -p ${BUILD_LOG_DIR}
  touch "$BUILD_LOG"

  mkdir -p "$OUT_DIR"
  for LIB in "${JNI_LIBS[@]}"; do
    build_jni "$LIB"
  done

) | tee -a "${BUILD_LOG}"
