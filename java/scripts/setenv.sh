#!/bin/bash

source scripts/helper.sh

export CC="clang"
export CXX="clang++"

J_HOME="/usr/local/java/jdk8u265-b01"
if is_osx; then
  J_HOME=$(/usr/libexec/java_home 2>/dev/null)
fi

if [ -z "$JAVA_HOME" ]; then
  export JAVA_HOME=$J_HOME
fi
echo "${JAVA_HOME:?}"