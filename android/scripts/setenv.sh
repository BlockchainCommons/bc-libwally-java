#!/bin/bash

source scripts/helper.sh

J_HOME="/usr/local/java/jdk8u265-b01"
if is_osx; then
  J_HOME=$(/usr/libexec/java_home 2>/dev/null)
fi

if [ -z "$JAVA_HOME" ]; then
  export JAVA_HOME=$J_HOME
fi
echo "${JAVA_HOME:?}"

if [ -z "${ANDROID_NDK}" ]; then
  export ANDROID_NDK=$HOME/android-ndk-r19c
  export ANDROID_NDK_HOME=$ANDROID_NDK
fi
echo "${ANDROID_NDK:?}"
export CC=clang