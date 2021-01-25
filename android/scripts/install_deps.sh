#!/bin/bash

set -e

source scripts/helper.sh

# Check and install missing dependencies
DEPS=(automake make libtool)
if is_osx; then
  DEPS+=(gnu-sed)
else
  DEPS+=(wget sudo unzip)
fi

echo "Checking and installing dependencies '${DEPS[*]}'..."
if ! is_osx; then
  apt-get update
fi
for DEP in "${DEPS[@]}"; do
  check_dep "$DEP"
done

# Check for JDK
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ $JAVA_VERSION < "1.8.0" ]]; then
  echo "Installing JDK 8..."
  pushd "$HOME"
  install_java
  popd
else
  echo "JDK 8 has been installed $JAVA_HOME"
fi

# Check for Android SDK
ANDROID_SDK_DIR=$HOME/Android
COMPILE_SDK_VERSION=30
BUILD_TOOL_VERSION=30.0.2
NDK_VERSION=21.0.6113669

if [[ -z $ANDROID_SDK_ROOT ]]; then
  pushd "$HOME"
  echo "Installing Android SDK..."
  mkdir -p "$ANDROID_SDK_DIR"
  install_android_sdk "$ANDROID_SDK_DIR" $COMPILE_SDK_VERSION $BUILD_TOOL_VERSION $NDK_VERSION
  echo "Android SDK has been installed at '$ANDROID_SDK_DIR'"
  popd
else
  echo "Android SDK has been installed at '$ANDROID_SDK_ROOT'"

  if [ -z $(check_ndk "$ANDROID_SDK_ROOT" "$NDK_VERSION") ]; then
    echo "Installing ndk..."
    install_ndk "$NDK_VERSION"
  fi

fi
