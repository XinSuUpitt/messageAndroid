# TODO (message_cube): Transfer Android.bp to Android.mk

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SDK_VERSION := 9

LOCAL_SRC_FILES := \
    dgif_lib.c     \
    egif_lib.c     \
    gifalloc.c     \
    gif_err.c      \
	gif_font.c     \
    gif_hash.c     \
    openbsd-reallocarray.c     \
    quantize.c

LOCAL_CFLAGS += -Wno-format -Wno-sign-compare -Wno-unused-parameter -DHAVE_CONFIG_H
LOCAL_MODULE:= libgif

include $(BUILD_STATIC_LIBRARY)