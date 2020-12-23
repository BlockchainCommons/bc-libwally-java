#!/bin/bash

set -e

source scripts/helper.sh

# Set env var
. ./scripts/setenv.sh

OUT_DIR=src/main/libs
LIBWALLY_CORE_FILE=libwallycore.so
JNI_MD_DIR="linux"
JNI_LIBS=(bc-libwally-address-jni bc-libwally-bip32-jni bc-libwally-bip39-jni bc-libwally-crypto-jni bc-libwally-core-jni bc-libwally-script-jni bc-libwally-tx-jni bc-libwally-psbt-jni)

if is_osx; then
  JNI_MD_DIR="darwin"
  LIBWALLY_CORE_FILE=libwallycore.dylib
fi

# Install jni libs
mkdir -p "$OUT_DIR"

for LIB in "${JNI_LIBS[@]}"; do
  LIB_NAME="lib${LIB}.so"
  if is_osx; then
    LIB_NAME="lib${LIB}.dylib"
  fi

  echo "Building $LIB_NAME..."
  echo $PWD

  SRC_ROOT_DIR=src/main
  WALLY_ROOT_DIR=../deps/libwally-core
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
done
