#!/bin/bash

set -e

CMD_LINE_DIR=cmdline-tools/bin

check_dep() {
  DEP=$1
  echo "$DEP"
  if is_osx; then
    if brew ls --versions "$DEP" >/dev/null; then
      echo "Package '$DEP' already installed"
    else
      echo "Installing '$DEP'..."
      echo y | brew install "$DEP"
    fi
  else
    if dpkg -s "$DEP" >/dev/null; then
      echo "Package '$DEP' already installed"
    else
      echo "Installing '$DEP'..."
      echo y | apt-get install "$DEP"
    fi
  fi
}

is_osx() {
  [[ "$(uname)" == "Darwin" ]]
}

install_java() {
  FILE=OpenJDK8U-jdk_x64_linux_hotspot_8u265b01.tar.gz
  if is_osx; then
    FILE=OpenJDK8U-jdk_x64_mac_hotspot_8u265b01.pkg
  fi
  wget -O "$FILE" -q "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/$FILE"

  if is_osx; then
    sudo installer -pkg $FILE -target /
  else
    JAVA_DIR=/usr/local/java
    sudo mkdir -p $JAVA_DIR
    mv $FILE $JAVA_DIR
    cd $JAVA_DIR || exit
    tar -xzvf $FILE
    sudo update-alternatives --install "/usr/bin/java" "java" "$JAVA_DIR/jdk8u265-b01/bin/java" 1
    sudo update-alternatives --install "/usr/bin/javac" "javac" "$JAVA_DIR/jdk8u265-b01/bin/javac" 1
  fi
  rm -f "$FILE"
}

check_ndk() {
  SDK_DIR=$1
  NDK_VERSION=$2
  NDK_DIR="$SDK_DIR"/ndk/"$NDK_VERSION"

  if [ -f "$NDK_DIR" ]; then
    echo "$NDK_DIR"
  fi
}

install_cmdline_tool() {
  FILE=commandlinetools-linux-6858069_latest.zip
  if is_osx; then
    FILE=commandlinetools-mac-6858069_latest.zip
  fi
  wget -O "$FILE" -q "https://dl.google.com/android/repository/$FILE"

  unzip $FILE >/dev/null
  rm -f $FILE
}

install_android_ndk() {
  NDK_VERSION=$1
  pushd "$CMD_LINE_DIR"
  echo y | ./sdkmanager --sdk_root="$SDK_DIR" "ndk;$NDK_VERSION"
  popd
}

install_android_sdk() {
  SDK_DIR=$1
  COMPILE_SDK_VERSION=$2
  BUILD_TOOL_VERSION=$3
  NDK_VERSION=$4

  if [ ! -f "$CMD_LINE_DIR/sdkmanager" ]; then
    echo "Installing command line tool..."
    install_cmdline_tool
  fi

  pushd "$CMD_LINE_DIR"

  echo y | ./sdkmanager --sdk_root="$SDK_DIR" "platforms;android-$COMPILE_SDK_VERSION"
  echo y | ./sdkmanager --sdk_root="$SDK_DIR" "build-tools;$BUILD_TOOL_VERSION"
  echo y | ./sdkmanager --sdk_root="$SDK_DIR" "ndk;$NDK_VERSION"

  popd
}
