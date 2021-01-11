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

ARCH_LIST=$(android_get_arch_list)
TOOLCHAIN_DIR=$(android_get_build_tools_dir)
JNI_MD_DIR="darwin"
JNI_LIBS=(bc-libwally-address-jni bc-libwally-bip32-jni bc-libwally-bip39-jni bc-libwally-crypto-jni bc-libwally-core-jni bc-libwally-script-jni bc-libwally-tx-jni bc-libwally-psbt-jni)
LIBWALY_CORE_FILE=libwallycore.so
BUILD_LOG_DIR="log"
BUILD_LOG="$BUILD_LOG_DIR/$(date +%s)-build-jni-log.txt"
if [[ -n $1 ]]; then
  BUILD_LOG=$1
fi

if ! is_osx; then
  JNI_MD_DIR="linux"
fi

build_jni() {
  ARCH=$1
  LIB=$2
  OUT_DIR=$3
  LIB_NAME="lib${LIB}.so"

  CC=$(android_get_build_tool "$ARCH" "$TOOLCHAIN_DIR" "$API" "clang")

  SRC_ROOT_DIR=$ROOT_DIR/java/src/main
  WALLY_ROOT_DIR=$ROOT_DIR/deps/libwally-core

  echo "Building $LIB_NAME..."
  $CC -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/$JNI_MD_DIR" \
    -I"$WALLY_ROOT_DIR/include" \
    -I"$WALLY_ROOT_DIR/src" \
    -shared -fPIC \
    "$SRC_ROOT_DIR/jniLibs/${LIB}.c" \
    "$SRC_ROOT_DIR/jniLibs/jni-utils.c" \
    "$WALLY_ROOT_DIR/src/ccan/ccan/base64/base64.c" \
    -o \
    "$OUT_DIR/$LIB_NAME" \
    "$OUT_DIR/$LIBWALY_CORE_FILE"
  echo "'$ARCH/$LIB_NAME' Done!"
}

(
  exec 2>&1
  mkdir -p ${BUILD_LOG_DIR}
  touch "$BUILD_LOG"

  for ARCH in $ARCH_LIST; do
    API=19
    if [[ $ARCH == *"64"* ]]; then
      API=21
    fi

    OUT_DIR=app/src/main/jniLibs/$ARCH
    mkdir -p "$OUT_DIR"

    for LIB in "${JNI_LIBS[@]}"; do
      build_jni "$ARCH" "$LIB" "$OUT_DIR"
    done

  done

) | tee -a "${BUILD_LOG}"
