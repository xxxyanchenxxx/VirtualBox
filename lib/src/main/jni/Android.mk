LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_MODULE := ft_64
LOCAL_CFLAGS := -DCORE_SO_NAME=\"libft_64.so\"
else
LOCAL_MODULE := ft
LOCAL_CFLAGS := -DCORE_SO_NAME=\"libft.so\"
endif

LOCAL_CFLAGS += -Wno-error=format-security -fpermissive -O2
LOCAL_CFLAGS += -DLOG_TAG=\"FT++\"
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CPPFLAGS += -std=c++11

LOCAL_C_INCLUDES += $(MAIN_LOCAL_PATH)
LOCAL_C_INCLUDES += $(MAIN_LOCAL_PATH)/Foundation
LOCAL_C_INCLUDES += $(MAIN_LOCAL_PATH)/Jni

LOCAL_SRC_FILES := Jni/VAJni.cpp \
				   Jni/Helper.cpp \
				   Foundation/syscall/BinarySyscallFinder.cpp \
				   Foundation/fake_dlfcn.cpp \
				   Foundation/canonicalize_md.c \
				   Foundation/MapsRedirector.cpp \
				   Foundation/IORelocator.cpp \
				   Foundation/VMHook.cpp \
				   Foundation/Symbol.cpp \
				   Foundation/SandboxFs.cpp \
				   Substrate/hde64.c \
                   Substrate/SubstrateDebug.cpp \
                   Substrate/SubstrateHook.cpp \
                   Substrate/SubstratePosixMemory.cpp \
                   Substrate/And64InlineHook.cpp

LOCAL_LDLIBS := -llog -latomic

include $(BUILD_SHARED_LIBRARY)