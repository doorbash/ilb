APP_ABI := armeabi-v7a x86 arm64-v8a x86_64
APP_CFLAGS := -Wall -Oz -fomit-frame-pointer -flto \
-D__MVSTR=${MAGISK_VERSION} -D__MCODE=${MAGISK_VER_CODE}
APP_LDFLAGS := -flto
APP_CPPFLAGS := -std=c++17
APP_STL := none
APP_PLATFORM := android-21
APP_SHORT_COMMANDS := true
