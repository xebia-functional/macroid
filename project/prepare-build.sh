#!/usr/bin/env bash

set -eu 

# caching based on tar file name to allow easy upgrading when version changes
if [[ ! -f $HOME/android-sdk_r24-linux.tgz || ! -d $HOME/android-sdk-linux ]]; then
  curl http://dl.google.com/android/android-sdk_r24-linux.tgz > $HOME/android-sdk_r24-linux.tgz 
  tar xf $HOME/android-sdk_r24-linux.tgz -C $HOME
fi 

export ANDROID_HOME=$HOME/android-sdk-linux
export ANDROID_SDK_HOME=$HOME/android-sdk-linux
export PATH=${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

echo "yes" | android update sdk --all --filter platform-tools --no-ui
echo "yes" | android update sdk --all --filter build-tools-23.0.1 --no-ui
echo "yes" | android update sdk --all --filter android-23 --no-ui
echo "yes" | android update sdk --all --filter extra-android-support --no-ui
echo "yes" | android update sdk --all --filter extra-android-m2repository --no-ui

