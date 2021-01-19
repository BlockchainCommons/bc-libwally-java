#!/bin/bash

set -e

BUILD_LOG_DIR="log"
BUILD_LOG="$BUILD_LOG_DIR/$(date +%s)-build-log.txt"

(
  exec 2>&1
  mkdir -p ${BUILD_LOG_DIR}
  touch "$BUILD_LOG"

  echo 'Cleanup...'
  ./scripts/cleanup.sh

  echo 'Building libwally-core...'
  ./scripts/build-host.sh "$BUILD_LOG"

  echo 'Building jni libs...'
  ./scripts/build-jni.sh "$BUILD_LOG"
)
