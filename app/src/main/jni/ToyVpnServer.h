
#define _GNU_SOURCE 1
#ifdef __clang__
#pragma clang system_header
#pragma clang diagnostic ignored "-Wgnu"
#elif __GNUC__
#pragma GCC system_header
#pragma GCC diagnostic ignored "-Wgnu"
#endif

#include <stdio.h>

#ifdef DEBUG
#include <android/log.h>
#define  LOG_TAG    "ToyVpnServer"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#endif

#define TEMP_FAILURE_RETRY(exp) ({         \
    __typeof__(exp) _rc;                   \
    do {                                   \
        _rc = (exp);                       \
    } while (_rc == -1 && errno == EINTR); \
    _rc; })
#define THROW_ON_NONZERO_RESULT(fun, message) if (fun !=0) throwException(env, RUNTIME_EXCEPTION_ERRNO, message)
#define GET_ADDR(bm, width, left, top) bm + top * width + left
#define OOME_MESSAGE "Failed to allocate native memory"
#define DEFAULT_FRAME_DURATION_MS 100

int startServer(int argc, char **argv);