# Native D2 engine core: D2MOO (MIT, https://github.com/ThePhrozenKeep/D2MOO) compiled for Android.
cmake_minimum_required(VERSION 3.22)
project(d2native C CXX)
set(CMAKE_CXX_STANDARD 17)
set(D2MOO ${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/D2MOO/source)

file(GLOB_RECURSE D2COMMON_SRC ${D2MOO}/D2Common/src/*.cpp)
list(FILTER D2COMMON_SRC EXCLUDE REGEX "DllMain\.cpp$")
file(GLOB_RECURSE FOG_SRC ${D2MOO}/Fog/src/*.cpp)
list(FILTER FOG_SRC EXCLUDE REGEX "(DllMain|QServer|SafeSock|D2QS|System|Fog)[^/]*\.cpp$")
file(GLOB_RECURSE D2LANG_SRC ${D2MOO}/D2Lang/src/*.cpp)
list(FILTER D2LANG_SRC EXCLUDE REGEX "(DllMain|D2LangStubs)\.cpp$")
file(GLOB_RECURSE D2CMP_SRC ${D2MOO}/D2CMP/src/*.cpp)
list(FILTER D2CMP_SRC EXCLUDE REGEX "(DllMain|D2CMP)\.cpp$")

file(GLOB_RECURSE D2GAME_SRC ${D2MOO}/D2Game/src/*.cpp)
# Task.cpp is the Windows server's worker-thread queue; the Android game runs frames itself.
list(FILTER D2GAME_SRC EXCLUDE REGEX "(DllMain|Task)\.cpp$")

add_library(d2native SHARED ${D2GAME_SRC} ${D2COMMON_SRC} ${FOG_SRC} ${D2LANG_SRC} ${D2CMP_SRC} d2native.cpp storm_android.cpp fog_android.cpp lang_android.cpp cmp_android.cpp game.cpp dcc.cpp server_android.cpp loopback.cpp sgame.cpp route.cpp
  ${D2MOO}/../external/squall/storm/thread/CCritSect.cpp)
target_include_directories(d2native PRIVATE compat
  ${D2MOO}/D2Game/include ${D2MOO}/D2Net/include ${D2MOO}/../external/squall
  ${D2MOO}/D2Common/include ${D2MOO}/D2CommonDefinitions/include ${D2MOO}/Fog/include
  ${D2MOO}/Storm/include ${D2MOO}/D2Lang/include ${D2MOO}/D2CMP/include ${D2MOO}/D2Hell/include)
target_compile_definitions(d2native PRIVATE WHOA_SYSTEM_LINUX=1 D2GAME_DLL_DECL= D2NET_DLL_DECL= D2COMMON_DLL_DECL= FOG_DLL_DECL= STORM_DLL_DECL= D2CMP_DLL_DECL= D2LANG_DLL_DECL= D2HELL_DLL_DECL=)
# Game version D2MOO is compiled for. D2MOO reproduces 1.10f and has switches for later behaviour
# (1.11 Uber Tristram, 1.13c changes); the target is 1.14d, which contains them. "110F" builds 1.10f.
set(D2_GAME_VERSION "114D" CACHE STRING "Diablo II version to build: 114D or 110F")
if(D2_GAME_VERSION STREQUAL "114D")
  set(D2_VERSION_DEFS D2_VERSION_MAJOR=1 D2_VERSION_MINOR=14 D2_VERSION_PATCH='D' D2_VERSION_114D D2_VERSION_113C)
else()
  set(D2_VERSION_DEFS)
endif()
target_compile_definitions(d2native PRIVATE ${D2_VERSION_DEFS})
target_compile_options(d2native PRIVATE -fms-extensions -Wno-everything -fno-strict-aliasing -fshort-wchar)
target_link_options(d2native PRIVATE -Wl,--no-undefined -Wl,--error-limit=0)
target_link_libraries(d2native log)

# Command-line test, run on the phone with adb shell (not packaged in the APK).
if(D2NATIVE_TESTS)
  add_executable(d2test d2test.cpp)
  target_include_directories(d2test PRIVATE compat ${D2MOO}/D2Game/include ${D2MOO}/D2Net/include ${D2MOO}/../external/squall ${D2MOO}/D2Common/include ${D2MOO}/D2CommonDefinitions/include ${D2MOO}/Fog/include ${D2MOO}/Storm/include ${D2MOO}/D2Lang/include ${D2MOO}/D2CMP/include ${D2MOO}/D2Hell/include)
  target_compile_definitions(d2test PRIVATE WHOA_SYSTEM_LINUX=1 D2GAME_DLL_DECL= D2NET_DLL_DECL= D2COMMON_DLL_DECL= FOG_DLL_DECL= STORM_DLL_DECL= D2CMP_DLL_DECL= D2LANG_DLL_DECL= D2HELL_DLL_DECL=)
  target_compile_definitions(d2test PRIVATE ${D2_VERSION_DEFS})
  target_compile_options(d2test PRIVATE -fms-extensions -Wno-everything -fshort-wchar)
  target_link_libraries(d2test d2native)
endif()
