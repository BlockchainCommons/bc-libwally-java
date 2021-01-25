# Installation for Blockchain Commons Libwally Java library
This document gives the instruction for installing the Blockchain Commons Libwally Java library.

## Dependencies
> We add utility script for installing all system dependencies, you can find it at `<android|java>/scripts/install_deps.sh`
If you want to do it manually by yourself, make sure all of following dependencies are installed correctly. 

- [Adopt Open JDK 1.8](https://github.com/AdoptOpenJDK/openjdk8-binaries/releases) is recommended for both MacOS and Linux.
- Android NDK r19c ([Linux](https://dl.google.com/android/repository/android-ndk-r19c-linux-x86_64.zip) and [Mac](https://dl.google.com/android/repository/android-ndk-r19c-darwin-x86_64.zip)) or above is recommended for building Android library.

### Linux
> Well tested on Ubuntu 16.04, 18.04 and Debian 8,9,10.

> Following packages can be installed via `apt-get`

- automake
- make
- libtool
- clang (Build for java only)

### MacOS
> Following packages can be installed via `brew`

- automake
- make
- libtool
- gnu-sed

## Build native libraries
> Native libraries for this project includes `libwallycore` and the other jni wrapper libraries.
You only need to build `libwallycore` once since it's not frequently changed but you have to rebuild the jni libraries each time there is any changes in the jni codes.

> Environment variables is required for building native libraries. For android, `NDK_HOME="your/ndk/home"` is needed. For Java, you have to set`CC="your/clang"` before each build command.
`JAVA_HOME="your/java/home"` also need for both.

Build `libwallycore` only
```console
$ ./scripts/build-host.sh
```

Build jni libraries only
```console
$ ./scripts/build-jni.sh
```

Run following command for building all native libraries, includes `libwallycore` and jni libraries
```console
$ ./scripts/build.sh
```

## Android
> Working directory: `/android`

### Testing
```console
$ ANDROID_SDK_ROOT="your/android-sdk/home" ./gradlew clean connectedDebugAndroidTest
```

### Bundling
```console
$ ANDROID_SDK_ROOT="your/android-sdk/home" ./gradlew clean assembleRelease
```

> The `app-release.aar` file would be found in `app/build/outputs/aar`. You can compile it as a library in your project.


## Java (Web app/ Desktop app)
> Working directory: `/java`

> You need to install native libraries into `java.library.path` for JVM can load it at runtime.

### Testing
The test tasks automatically points JVM `java.library.path` to native libraries path so make sure you already built the native libraries before executing the tests.

Run following command for executing test cases.
```console
$ ./gradlew test
```

### Bundling
The `jar` file will be bundled by running
```console
$ ./gradlew assemble
```

> `jar` file just contain all `.class` files for running pure Java, no dynamic library is carried with.