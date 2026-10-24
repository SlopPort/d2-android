# Building

## Toolchain

- Android SDK (compileSdk 35) with NDK **27.2.12479018** and CMake **3.22.1**
  (`sdkmanager "ndk;27.2.12479018" "cmake;3.22.1"`).
- Gradle 8.10 with Android Gradle Plugin 8.8 (no wrapper is committed yet; use a local Gradle or
  open the project in Android Studio).
- `local.properties` with `sdk.dir=...` (not committed).
- JDK 17+ (AGP 8.8 refuses Java 8); Android Studio's bundled `jbr` works:
  `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`.

```bash
gradle assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The native part is large (all of D2Common and D2Game, ~250k lines); a clean build takes a few
minutes, incremental ones seconds. Only `armeabi-v7a` is built (see ARCHITECTURE.md).

## Game data on the device

You need your own Diablo II: Lord of Destruction installation (patch 1.14d). The app keeps its
own copy of the MPQs in its private storage. For development, copy the archives to the app's
external folder and start the app; they are imported and put in the game's load order
automatically (`DataImport.importPending`).

```bash
adb shell mkdir -p /sdcard/Android/data/pl.d2port/files/import
adb push patch_d2.mpq d2exp.mpq d2char.mpq d2data.mpq /sdcard/Android/data/pl.d2port/files/import/
```

## Command-line test (no APK)

`app/src/main/cpp/d2test.cpp` is a phone-side test executable (tables, DRLG, collision, the D2Game
server). Build it with the NDK toolchain directly:

```bash
cmake -S app/src/main/cpp -B build-native -G Ninja \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=armeabi-v7a -DANDROID_PLATFORM=26 -DANDROID_STL=c++_static \
  -DD2NATIVE_TESTS=ON -DCMAKE_BUILD_TYPE=Debug
cmake --build build-native
```

It reads extracted files (lower-case paths) from `/data/local/tmp/d2data`.
`tools/d2data_extract.py` extracts them from the MPQs of the Diablo II installation in the game's
load order:

```bash
python tools/d2data_extract.py OUT_DIR @list.txt     # one archive path per line, e.g. DATA\GLOBAL\AnimData.d2
adb push OUT_DIR/. /data/local/tmp/d2data
```

(`--mods DIR` searches every `.mpq` in DIR first, for a mod; not needed for the vanilla game.)
See DEVELOPMENT.md for running the test.
