#!/bin/bash

source scripts/helper.sh

DEPS=(automake make libtool)

if is_osx; then
  DEPS+=(gnu-sed)
else
  DEPS+=(wget sudo clang unzip)
fi

echo "Checking and installing dependencies '${DEPS[*]}'..."

# Check and install missing dependencies
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
  install_java
else
  echo "JDK 8 has been installed at $JAVA_HOME"
fi

# Check for NDK
NDK_VERSION="r19c"
NDK_PATH=$(check_ndk_path $NDK_VERSION)
if [ "$NDK_PATH" == "" ]; then
  echo "Installing NDK..."
  pushd "$HOME" || exit
  install_ndk $NDK_VERSION
  popd || exit
else
  echo "NDK has been installed at $NDK_PATH"
fi
