#!/bin/bash

echo 'Cleanup...'
./scripts/cleanup.sh

echo 'Building libwally-core...'
./scripts/build-host.sh || exit

echo 'Building jni libs...'
./scripts/build-jni.sh || exit

