APP_ABI := armeabi-v7a arm64-v8a armeabi mips mips64 x86 x86_64
APP_PLATFORM := android-19

ifeq ($(NDK_DEBUG),1)
	APP_OPTIM := debug
else
	APP_OPTIM := release
endif

NDK_TOOLCHAIN_VERSION := clang