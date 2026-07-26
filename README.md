# BlackBox Pro - Virtual Engine

<p align="center">
  <img src="assets/usage.gif" alt="BlackBox Pro Banner" width="100%"/>
</p>

**BlackBox Pro** is a premium virtual engine that allows you to clone and run virtual applications on Android devices without installing APKs. This project works on Android 5.0 to 14.0+ and supports multiple architectures (ARM64, ARMv7, x86).

## Overview

This enhanced edition includes bug fixes, stability improvements, Android 14+ compatibility, a modern dark-themed UI, and multi-language support.

### Key Features

*   **Virtual App Cloning**: Run multiple instances of applications.
*   **Sandboxed Environment**: Isolated process execution.
*   **No Root Required**: Runs entirely in userspace.
*   **Multi-Architecture**: Support for 32-bit and 64-bit apps.
*   **Device Spoofing**: Modify device information for virtual apps.
*   **Fake Location**: Spoof GPS coordinates.
*   **Modern Dark UI**: Premium dark-themed interface with smooth animations.
*   **Multi-Language Support**: English, Arabic, Spanish, French, Chinese (Simplified & Traditional).
*   **Language Switching**: Change app language from Settings.
*   **Xposed Framework**: Built-in Xposed module support.
*   **GMS Support**: Google Mobile Services management.

## Requirements

*   **Android Version**: Android 5.0 (API 21) or higher.
*   **RAM**: 2GB minimum recommended.
*   **Architecture**: ARMv7a, ARM64-v8a.

## Build Instructions

### Prerequisites
*   Android Studio (Arctic Fox or newer)
*   JDK 21
*   Android SDK 35+
*   NDK (Version 29.0.13846066)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/modmin25/NewBlackbox.git
cd NewBlackbox

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

## CI/CD

This project uses GitHub Actions for automated builds. Push to `main` or use workflow_dispatch to trigger a build.

The workflow produces `app-debug.apk` artifacts available for download from the Actions tab.

## Credits

*   **Main Developer**: ALEX502
*   **Original Framework**: VirtualApp, VirtualAPK
*   **Native Hooks**: Dobby, xDL
*   **Reflection**: BlackReflection, FreeReflection

## License

Copyright 2024 BlackBox Pro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
