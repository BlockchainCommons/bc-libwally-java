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
