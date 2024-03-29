//
// VirtualApp Native Project
//

#ifndef NDK_HOOK_H
#define NDK_HOOK_H


#include <string>
#include <map>
#include <list>
#include <jni.h>
#include <dlfcn.h>
#include <stddef.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/syscall.h>

#include "Jni/Helper.h"


#define HOOK_SYMBOL(handle, func) hook_function(handle, #func, (void*) new_##func, (void**) &orig_##func)
#define HOOK_DEF(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__); \
  ret new_##func(__VA_ARGS__)


namespace IOUniformer {

    void init_env_before_all();

    void startUniformer(const char *pkgName, const char *so_path, const char *so_path_64, const char *native_path,
                        int api_level, int preview_api_level);

    void relocate(const char *orig_path, const char *new_path);

    void whitelist(const char *path);

    const char *query(const char *orig_path, char *const buffer, const size_t size);

    const char *reverse(const char *redirected_path, char *const buffer, const size_t size);

    void forbid(const char *path);

    void readOnly(const char *path);
}

#endif //NDK_HOOK_H
