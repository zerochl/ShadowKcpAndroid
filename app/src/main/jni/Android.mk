cflags:= -Wall -Wextra -std=c++11
extra_ldlibs :=

ifeq ($(NDK_DEBUG),1)
	cflags+= -DDEBUG
	extra_ldlibs= -llog
else
	cflags+= -fvisibility=hidden
endif

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := ZeroVPN
LOCAL_CFLAGS := $(cflags)
LOCAL_LDLIBS := -ljnigraphics -landroid -lGLESv2 $(extra_ldlibs)

LOCAL_SRC_FILES := \
	util.cpp \

include $(BUILD_SHARED_LIBRARY)