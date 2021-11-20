//
// VirtualApp Native Project
//
#include <unistd.h>
#include <stdlib.h>
#include <sys/ptrace.h>
#include <Substrate/CydiaSubstrate.h>
#include <Jni/VAJni.h>
#include <sys/stat.h>
#include <syscall.h>
#include <Foundation/syscall/BinarySyscallFinder.h>
#include <limits.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <sys/user.h>

#include "IORelocator.h"
#include "SandboxFs.h"
#include "canonicalize_md.h"
#include "Symbol.h"
#include "Log.h"
#include "mono_func.h"
#include "MapsRedirector.h"

#if defined(__LP64__)
#define LINKER_PATH "/system/bin/linker64"
#else
#define LINKER_PATH "/system/bin/linker"
#endif

void startIOHook(int api_level);

bool need_load_env = true;

bool execve_process = false;

void startIOHook(int api_level);

char *get_process_name() {
    char *cmdline = (char *) calloc(0x400u, 1u);
    if (cmdline) {
        FILE *file = fopen("/proc/self/cmdline", "r");
        if (file) {
            int count = fread(cmdline, 1u, 0x400u, file);
            if (count) {
                if (cmdline[count - 1] == '\n') {
                    cmdline[count - 1] = '\0';
                }
            }
            fclose(file);
        } else {
            ALOGE("fail open cmdline.");
        }
    }
    return cmdline;
}

void IOUniformer::init_env_before_all() {
    if (!need_load_env) {
        return;
    }
    need_load_env = false;
    char *ld_preload = getenv("LD_PRELOAD");
    if (!ld_preload || !strstr(ld_preload, CORE_SO_NAME)) {
        return;
    }
    execve_process = true;
    char *process_name = get_process_name();
    ALOGI("Start init env : %s", process_name);
    free(process_name);
    char src_key[KEY_MAX];
    char dst_key[KEY_MAX];
    int i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        memset(dst_key, 0, sizeof(dst_key));
        sprintf(src_key, "V_REPLACE_ITEM_SRC_%d", i);
        sprintf(dst_key, "V_REPLACE_ITEM_DST_%d", i);
        char *src_value = getenv(src_key);
        if (!src_value) {
            break;
        }
        char *dst_value = getenv(dst_key);
        add_replace_item(src_value, dst_value);
        i++;
    }
    i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        sprintf(src_key, "V_KEEP_ITEM_%d", i);
        char *keep_value = getenv(src_key);
        if (!keep_value) {
            break;
        }
        add_keep_item(keep_value);
        i++;
    }
    i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        sprintf(src_key, "V_FORBID_ITEM_%d", i);
        char *forbid_value = getenv(src_key);
        if (!forbid_value) {
            break;
        }
        add_forbidden_item(forbid_value);
        i++;
    }
    char *api_level_char = getenv("V_API_LEVEL");
    if (api_level_char != NULL) {
        int api_level = atoi(api_level_char);
        startIOHook(api_level);
    }
}

static inline void
hook_function(void *handle, const char *symbol, void *new_func, void **old_func) {
    void *addr = dlsym(handle, symbol);
    if (addr == NULL) {
        ALOGE("Not found symbol : %s", symbol);
        return;
    }
    MSHookFunction(addr, new_func, old_func);
}

void IOUniformer::relocate(const char *orig_path, const char *new_path) {
    add_replace_item(orig_path, new_path);
}

const char *IOUniformer::query(const char *orig_path, char *const buffer, const size_t size) {
    return relocate_path(orig_path, buffer, size);
}

void IOUniformer::whitelist(const char *_path) {
    add_keep_item(_path);
}

void IOUniformer::forbid(const char *_path) {
    add_forbidden_item(_path);
}

void IOUniformer::readOnly(const char *_path) {
    add_readonly_item(_path);
}

const char *IOUniformer::reverse(const char *_path, char *const buffer, const size_t size) {
    return reverse_relocate_path(_path, buffer, size);
}


__BEGIN_DECLS

// int faccessat(int dirfd, const char *pathname, int mode, int flags);
HOOK_DEF(int, faccessat, int dirfd, const char *pathname, int mode, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !(mode & W_OK && isReadOnly(relocated_path))) {
        return syscall(__NR_faccessat, dirfd, relocated_path, mode, flags);
    }
    errno = EACCES;
    return -1;
}

// int fchmodat(int dirfd, const char *pathname, mode_t mode, int flags);
HOOK_DEF(int, fchmodat, int dirfd, const char *pathname, mode_t mode, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchmodat, dirfd, relocated_path, mode, flags);
    }
    errno = EACCES;
    return -1;
}

// int fstatat64(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat64, int dirfd, const char *pathname, struct stat *buf, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret;
#if defined(__arm__) || defined(__i386__)
        ret = syscall(__NR_fstatat64, dirfd, relocated_path, buf, flags);
#else
        ret = syscall(__NR_newfstatat, dirfd, relocated_path, buf, flags);
#endif
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int kill(pid_t pid, int sig);
HOOK_DEF(int, kill, pid_t pid, int sig) {
    ALOGE("kill >>> pid : %d, sig : %d", pid, sig);
    return syscall(__NR_kill, pid, sig);
}

#ifndef __LP64__

// int __statfs64(const char *path, size_t size, struct statfs *stat);
HOOK_DEF(int, __statfs64, const char *pathname, size_t size, struct statfs *stat) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_statfs64, relocated_path, size, stat);
    }
    errno = EACCES;
    return -1;
}

// int __open(const char *pathname, int flags, int mode);
HOOK_DEF(int, __open, const char *pathname, int flags, int mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !((flags & O_WRONLY || flags & O_RDWR) && isReadOnly(relocated_path))) {
        int fake_fd = redirect_proc_maps(relocated_path, flags, mode);
        if (fake_fd != 0) {
            return fake_fd;
        }
        return syscall(__NR_open, relocated_path, flags, mode);
    }
    errno = EACCES;
    return -1;
}

// ssize_t readlink(const char *path, char *buf, size_t bufsiz);
HOOK_DEF(ssize_t, readlink, const char *pathname, char *buf, size_t bufsiz) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret = syscall(__NR_readlink, relocated_path, buf, bufsiz);
        if (ret < 0) {
            return ret;
        } else {
            // relocate link content
            if (reverse_relocate_path_inplace(buf, bufsiz) != -1) {
                return ret;
            }
        }
    }
    errno = EACCES;
    return -1;
}

// int mkdir(const char *pathname, mode_t mode);
HOOK_DEF(int, mkdir, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mkdir, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int rmdir(const char *pathname);
HOOK_DEF(int, rmdir, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_rmdir, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int lchown(const char *pathname, uid_t owner, gid_t group);
HOOK_DEF(int, lchown, const char *pathname, uid_t owner, gid_t group) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_lchown, relocated_path, owner, group);
    }
    errno = EACCES;
    return -1;
}

// int utimes(const char *filename, const struct timeval *tvp);
HOOK_DEF(int, utimes, const char *pathname, const struct timeval *tvp) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_utimes, relocated_path, tvp);
    }
    errno = EACCES;
    return -1;
}

// int link(const char *oldpath, const char *newpath);
HOOK_DEF(int, link, const char *oldpath, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_link, relocated_path_old, newpath);
    }
    errno = EACCES;
    return -1;
}

// int access(const char *pathname, int mode);
HOOK_DEF(int, access, const char *pathname, int mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !(mode & W_OK && isReadOnly(relocated_path))) {
        return syscall(__NR_access, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int chmod(const char *path, mode_t mode);
HOOK_DEF(int, chmod, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chmod, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int chown(const char *path, uid_t owner, gid_t group);
HOOK_DEF(int, chown, const char *pathname, uid_t owner, gid_t group) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chown, relocated_path, owner, group);
    }
    errno = EACCES;
    return -1;
}

// int lstat(const char *path, struct stat *buf);
HOOK_DEF(int, lstat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_lstat64, relocated_path, buf);
    }
    errno = EACCES;
    return -1;
}

// int stat(const char *path, struct stat *buf);
HOOK_DEF(int, stat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret = syscall(__NR_stat64, relocated_path, buf);
        if (isReadOnly(relocated_path)) {
            buf->st_mode &= ~S_IWGRP;
        }
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int symlink(const char *oldpath, const char *newpath);
HOOK_DEF(int, symlink, const char *oldpath, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_symlink, relocated_path_old, newpath);
    }
    errno = EACCES;
    return -1;
}

// int unlink(const char *pathname);
HOOK_DEF(int, unlink, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !isReadOnly(relocated_path)) {
        return syscall(__NR_unlink, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int fchmod(const char *pathname, mode_t mode);
HOOK_DEF(int, fchmod, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchmod, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}


// int fstatat(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat, int dirfd, const char *pathname, struct stat *buf, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fstatat64, dirfd, relocated_path, buf, flags);
    }
    errno = EACCES;
    return -1;
}

// int fstat(const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fstat64, relocated_path, buf);
    }
    errno = EACCES;
    return -1;
}

// int mknod(const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknod, const char *pathname, mode_t mode, dev_t dev) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mknod, relocated_path, mode, dev);
    }
    errno = EACCES;
    return -1;
}

// int rename(const char *oldpath, const char *newpath);
HOOK_DEF(int, rename, const char *oldpath, const char *newpath) {
    char temp_old[PATH_MAX], temp_new[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp_old, sizeof(temp_old));
    const char *relocated_path_new = relocate_path(newpath, temp_new, sizeof(temp_new));
    if (relocated_path_old && relocated_path_new) {
        return syscall(__NR_rename, relocated_path_old, relocated_path_new);
    }
    errno = EACCES;
    return -1;
}

#endif


// int mknodat(int dirfd, const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknodat, int dirfd, const char *pathname, mode_t mode, dev_t dev) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mknodat, dirfd, relocated_path, mode, dev);
    }
    errno = EACCES;
    return -1;
}

// int utimensat(int dirfd, const char *pathname, const struct timespec times[2], int flags);
HOOK_DEF(int, utimensat, int dirfd, const char *pathname, const struct timespec times[2],
         int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_utimensat, dirfd, relocated_path, times, flags);
    }
    errno = EACCES;
    return -1;
}

// int fchownat(int dirfd, const char *pathname, uid_t owner, gid_t group, int flags);
HOOK_DEF(int, fchownat, int dirfd, const char *pathname, uid_t owner, gid_t group, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchownat, dirfd, relocated_path, owner, group, flags);
    }
    errno = EACCES;
    return -1;
}

// int chroot(const char *pathname);
HOOK_DEF(int, chroot, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chroot, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, renameat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    char temp_old[PATH_MAX], temp_new[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp_old, sizeof(temp_old));
    const char *relocated_path_new = relocate_path(newpath, temp_new, sizeof(temp_new));
    if (relocated_path_old && relocated_path_new) {
        return syscall(__NR_renameat, olddirfd, relocated_path_old, newdirfd,
                       relocated_path_new);
    }
    errno = EACCES;
    return -1;
}

// int statfs64(const char *__path, struct statfs64 *__buf) __INTRODUCED_IN(21);
HOOK_DEF(int, statfs64, const char *filename, struct statfs64 *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(filename, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_statfs, relocated_path, buf);
    }
    errno = EACCES;
    return -1;
}

// int unlinkat(int dirfd, const char *pathname, int flags);
HOOK_DEF(int, unlinkat, int dirfd, const char *pathname, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !isReadOnly(relocated_path)) {
        return syscall(__NR_unlinkat, dirfd, relocated_path, flags);
    }
    errno = EACCES;
    return -1;
}

// int symlinkat(const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, symlinkat, const char *oldpath, int newdirfd, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_symlinkat, relocated_path_old, newdirfd, newpath);
    }
    errno = EACCES;
    return -1;
}

// int linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags);
HOOK_DEF(int, linkat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath,
         int flags) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_linkat, olddirfd, relocated_path_old, newdirfd, newpath,
                       flags);
    }
    errno = EACCES;
    return -1;
}

// int mkdirat(int dirfd, const char *pathname, mode_t mode);
HOOK_DEF(int, mkdirat, int dirfd, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mkdirat, dirfd, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz);
HOOK_DEF(int, readlinkat, int dirfd, const char *pathname, char *buf, size_t bufsiz) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret = syscall(__NR_readlinkat, dirfd, relocated_path, buf, bufsiz);
        if (ret < 0) {
            return ret;
        } else {
            // relocate link content
            if (reverse_relocate_path_inplace(buf, bufsiz) != -1) {
                return ret;
            }
        }
    }
    errno = EACCES;
    return -1;
}


// int truncate(const char *path, off_t length);
HOOK_DEF(int, truncate, const char *pathname, off_t length) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_truncate, relocated_path, length);
    }
    errno = EACCES;
    return -1;
}

// int chdir(const char *path);
HOOK_DEF(int, chdir, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chdir, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int __getcwd(char *buf, size_t size);
HOOK_DEF(int, __getcwd, char *buf, size_t size) {
    long ret = syscall(__NR_getcwd, buf, size);
    if (!ret) {
        if (reverse_relocate_path_inplace(buf, size) < 0) {
            errno = EACCES;
            return -1;
        }
    }
    return ret;
}

// int __openat(int fd, const char *pathname, int flags, int mode);
HOOK_DEF(int, __openat, int fd, const char *pathname, int flags, int mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        int fake_fd = redirect_proc_maps(relocated_path, flags, mode);
        if (fake_fd != 0) {
            return fake_fd;
        }
        return syscall(__NR_openat, fd, relocated_path, flags, mode);
    }
    errno = EACCES;
    return -1;
}

// int __statfs (__const char *__file, struct statfs *__buf);
HOOK_DEF(int, __statfs, __const char *__file, struct statfs *__buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(__file, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_statfs, relocated_path, __buf);
    }
    errno = EACCES;
    return -1;
}

int inline getArrayItemCount(char *const array[]) {
    int i;
    for (i = 0; array[i]; ++i);
    return i;
}

char **build_new_env(char *const envp[]) {
    char *provided_ld_preload = NULL;
    int provided_ld_preload_index = -1;
    int orig_envp_count = getArrayItemCount(envp);

    for (int i = 0; i < orig_envp_count; i++) {
        if (strstr(envp[i], "LD_PRELOAD")) {
            provided_ld_preload = envp[i];
            provided_ld_preload_index = i;
        }
    }
    char ld_preload[200];
    char *so_path = getenv("V_SO_PATH");
    if (provided_ld_preload) {
        sprintf(ld_preload, "LD_PRELOAD=%s:%s", so_path, provided_ld_preload + 11);
    } else {
        sprintf(ld_preload, "LD_PRELOAD=%s", so_path);
    }
    int new_envp_count = orig_envp_count
                         + get_keep_item_count()
                         + get_forbidden_item_count()
                         + get_replace_item_count() * 2 + 1;
    if (provided_ld_preload) {
        new_envp_count--;
    }
    char **new_envp = (char **) malloc(new_envp_count * sizeof(char *));
    int cur = 0;
    new_envp[cur++] = ld_preload;
    for (int i = 0; i < orig_envp_count; ++i) {
        if (i != provided_ld_preload_index) {
            new_envp[cur++] = envp[i];
        }
    }
    for (int i = 0; environ[i]; ++i) {
        if (environ[i][0] == 'V' && environ[i][1] == '_') {
            new_envp[cur++] = environ[i];
        }
    }
    new_envp[cur] = NULL;
    return new_envp;
}

char **build_new_argv(char *const envp[]) {
    char *provided_ld_preload = NULL;
    int provided_ld_preload_index = -1;
    int orig_envp_count = getArrayItemCount(envp);

    for (int i = 0; i < orig_envp_count; i++) {
        if (strstr(envp[i], "compiler-filter")) {
            provided_ld_preload = envp[i];
            provided_ld_preload_index = i;
        }
    }
    char ld_preload[40];
    if (provided_ld_preload) {
        sprintf(ld_preload, "--compiler-filter=%s", "everything");
    }

    char *api_level_char = getenv("V_API_LEVEL");
    int api_level = atoi(api_level_char);

    int new_envp_count = orig_envp_count + 4;
    char **new_envp = (char **) malloc(new_envp_count * sizeof(char *));
    int cur = 0;
    for (int i = 0; i < orig_envp_count; ++i) {
        if (i != provided_ld_preload_index) {
            new_envp[cur++] = envp[i];
        } else {
            new_envp[i] = ld_preload;
            cur++;
        }
    }

    if (api_level >= 22) {
        new_envp[cur++] = (char *) "--compile-pic";
    }
    if (api_level >= 23) {
        new_envp[cur++] = (char *) (api_level > 25 ? "--inline-max-code-units=0" : "--inline-depth-limit=0");
    }
    if (api_level >= 28) {
        new_envp[cur++] = (char *) "--debuggable";
    }
    new_envp[cur] = NULL;

//    int n = getArrayItemCount(new_envp);
//    for (int i = 0; i < n; i++) {
//        ALOGE("dex2oat : %s", new_envp[i]);
//    }

    return new_envp;
}

// int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
HOOK_DEF(int, execve, const char *pathname, char *argv[], char *const envp[]) {
    /**
     * CANNOT LINK EXECUTABLE "/system/bin/cat": "/data/app/io.virtualapp-1/lib/arm/libva-native.so" is 32-bit instead of 64-bit.
     *
     * We will support 64Bit to adopt it.
     */
    // ALOGE("execve : %s", pathname); // any output can break exec. See bug: https://issuetracker.google.com/issues/109448553
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (!relocated_path) {
        errno = EACCES;
        return -1;
    }

    char *ld = getenv("LD_PRELOAD");
    if (ld) {
        if (strstr(ld, "libNimsWrap.so") || strstr(ld, "stamina.so")) {
            int ret = syscall(__NR_execve, relocated_path, argv, envp);
            return ret;
        }
    }
    if (strstr(pathname, "dex2oat")) {
        char **new_envp = build_new_env(envp);
        char **new_argv = build_new_argv(argv);
        int ret = syscall(__NR_execve, relocated_path, new_argv, new_envp);
        free(new_envp);
        free(new_argv);
        return ret;
    }
    int ret = syscall(__NR_execve, relocated_path, argv, envp);
    return ret;
}

//void *dlsym(void *handle, const char *symbol)
HOOK_DEF(void*, dlsym, void *handle, char *symbol) {
    return orig_dlsym(handle, symbol);
}

HOOK_DEF(pid_t, vfork) {
    return fork();
}

__END_DECLS
// end IO DEF

HOOK_DEF(void *, dlopen, const char *filename, int flag) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(filename, temp, sizeof(temp));
    if (relocated_path != NULL && strstr(relocated_path, "/data/data/free.game.video.box.fun/virtual/data/user/0/com.tencent.wework/hotload/libso/libtxmapengine.so")) {
        relocated_path = "/data/data/free.game.video.box.fun/virtual/data/app/com.tencent"
                         ".wework/lib/libtxmapengine.so";
        ALOGE("do_dlopen relocated_path:%s", relocated_path);
    }
    void *ret = orig_dlopen(relocated_path, flag);
    return ret;
}

HOOK_DEF(void *, do_dlopen_V24, const char *filename, int flags, const void *extinfo,
         void *caller_addr) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(filename, temp, sizeof(temp));
    if (relocated_path != NULL && strstr(relocated_path, "/data/data/free.game.video.box"
                                                             ".fun/virtual/data/user/0/com.tencent.wework/hotload/libso/libtxmapengine.so")) {
            relocated_path = "/data/data/free.game.video.box.fun/virtual/data/app/com.tencent"
                             ".wework/lib/libtxmapengine.so";
            ALOGE("do_dlopen_V24 relocated_path:%s", relocated_path);
    }
    void *ret = orig_do_dlopen_V24(relocated_path, flags, extinfo, caller_addr);
    return ret;
}

HOOK_DEF(void *, dlopen_CI, const char *filename, int flag) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(filename, temp, sizeof(temp));
    void *ret = orig_dlopen_CI(redirect_path, flag);
    return ret;
}

HOOK_DEF(void*, do_dlopen_CIV, const char *filename, int flag, const void *extinfo) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(filename, temp, sizeof(temp));
    void *ret = orig_do_dlopen_CIV(redirect_path, flag, extinfo);
    return ret;
}

HOOK_DEF(void*, do_dlopen_CIVV, const char *name, int flags, const void *extinfo,
         void *caller_addr) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(name, temp, sizeof(temp));
    void *ret = orig_do_dlopen_CIVV(redirect_path, flags, extinfo, caller_addr);
    return ret;
}

#if defined(__aarch64__)

bool hook_dlopen(int api_level) {
    intptr_t linker_addr, dlopen_off, symbol;
    const char * link_path;
    if (api_level <= 28) {
        link_path = "/system/bin/linker64";
    } else {
        link_path = "/apex/com.android.runtime/bin/linker64";
    }
    if ((linker_addr = get_addr(link_path)) == 0) {
        ALOGE("Cannot found linker addr.");
        return false;
    }
    if (resolve_symbol(LINKER_PATH, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPKv",
                       &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIVV,
                       (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPv",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIVV,
                       (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl__ZL10dlopen_extPKciPK17android_dlextinfoPv",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIVV,
                       (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (
            resolve_symbol(LINKER_PATH, "__dl__Z20__android_dlopen_extPKciPK17android_dlextinfoPKv",
                           &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIVV,
                       (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (
            resolve_symbol(LINKER_PATH, "__dl___loader_android_dlopen_ext",
                           &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIVV,
                       (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl__Z9do_dlopenPKciPK17android_dlextinfo",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIV,
                       (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl__Z8__dlopenPKciPKv",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIV,
                       (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl___loader_dlopen",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_do_dlopen_CIV,
                       (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(LINKER_PATH, "__dl_dlopen",
                              &dlopen_off) == 0) {
        symbol = linker_addr + dlopen_off;
        MSHookFunction((void *) symbol, (void *) new_dlopen_CI,
                       (void **) &orig_dlopen_CI);
        return true;
    }
    return false;
}

bool on_found_syscall_aarch64(const char *path, int num, void *func) {
    static int pass = 0;
    switch (num) {
        case __NR_fchmodat:
            MSHookFunction(func, (void *) new_fchmodat, (void **) &orig_fchmodat);
            pass++;
            break;
        case __NR_faccessat:
            MSHookFunction(func, (void *) new_faccessat, (void **) &orig_faccessat);
            pass++;
            break;
        case __NR_statfs:
            MSHookFunction(func, (void *) new___statfs, (void **) &orig___statfs);
            pass++;
            break;
        case __NR_getcwd:
            MSHookFunction(func, (void *) new___getcwd, (void **) &orig___getcwd);
            pass++;
            break;
        case __NR_openat:
            MSHookFunction(func, (void *) new___openat, (void **) &orig___openat);
            pass++;
            break;
    }
    if (pass == 5) {
        return BREAK_FIND_SYSCALL;
    }
    return CONTINUE_FIND_SYSCALL;
}

bool on_found_linker_syscall_arch64(const char *path, int num, void *func) {
    switch (num) {
        case __NR_openat:
            MSHookFunction(func, (void *) new___openat, (void **) &orig___openat);
            return BREAK_FIND_SYSCALL;
    }
    return CONTINUE_FIND_SYSCALL;
}
#else

bool on_found_linker_syscall_arm(const char *path, int num, void *func) {
    switch (num) {
        case __NR_openat:
            MSHookFunction(func, (void *) new___openat, (void **) &orig___openat);
            break;
        case __NR_open:
            MSHookFunction(func, (void *) new___open, (void **) &orig___open);
            break;
    }
    return CONTINUE_FIND_SYSCALL;
}

void hook_dlopen() {
    void *symbol = NULL;
    if (find_name("__dl__Z9do_dlopenPKciPK17android_dlextinfoPKv", "linker",
                  (unsigned long *) &symbol, NULL) == 0) {
        MSHookFunction(symbol, (void *) new_do_dlopen_V24, (void **) &orig_do_dlopen_V24);
    } else if (find_name("__dl__Z9do_dlopenPKciPK17android_dlextinfoPv", "linker",
                         (unsigned long *) &symbol, NULL) == 0) {
        MSHookFunction(symbol, (void *) new_do_dlopen_V24, (void **) &orig_do_dlopen_V24);
    } else if (find_name("__dl__Z9do_dlopenPKciPK17android_dlextinfo", "linker",
                         (unsigned long *) &symbol, NULL) == 0) {
        MSHookFunction(symbol, (void *) new_do_dlopen_V24, (void **) &orig_do_dlopen_V24);
    } else if (find_name("__dl_dlopen", "linker", (unsigned long *) &symbol, NULL) == 0) {
        MSHookFunction(symbol, (void *) new_dlopen, (void **) &orig_dlopen);
    } else {
        MSHookFunction((void *) dlopen, (void *) new_dlopen, (void **) &orig_dlopen);
    }
}

#endif

void startIOHook(int api_level) {
    void *handle;
    const char* lib_path;

#if defined(__aarch64__)
    if (api_level <= 28) {
        lib_path = "/system/lib64/libc.so";
    } else {
        lib_path = "/apex/com.android.runtime/lib64/bionic/libc.so";
    }
#else
    lib_path = "libc.so";
#endif

    handle = dlopen(lib_path, RTLD_NOW);
    if (handle) {
#if defined(__aarch64__)
        if ( !(findSyscalls(lib_path, (bool (__cdecl *)(const char *, int, void *))
                on_found_syscall_aarch64)) ) {
            HOOK_SYMBOL(handle, faccessat);
            HOOK_SYMBOL(handle, __openat);
            HOOK_SYMBOL(handle, fchownat);
            HOOK_SYMBOL(handle, renameat);
            HOOK_SYMBOL(handle, mkdirat);
            HOOK_SYMBOL(handle, mknodat);
            HOOK_SYMBOL(handle, truncate);
            HOOK_SYMBOL(handle, linkat);
            HOOK_SYMBOL(handle, readlinkat);
            HOOK_SYMBOL(handle, unlinkat);
            HOOK_SYMBOL(handle, symlinkat);
            HOOK_SYMBOL(handle, utimensat);
            HOOK_SYMBOL(handle, chdir);
            HOOK_SYMBOL(handle, execve);
            HOOK_SYMBOL(handle, statfs64);
            HOOK_SYMBOL(handle, kill);
            HOOK_SYMBOL(handle, vfork);
            HOOK_SYMBOL(handle, fstatat64);
        }
        hook_dlopen(api_level);
        const char * link_path;
        if (api_level <= 28) {
            link_path = "/system/bin/linker64";
        } else {
            link_path = "/apex/com.android.runtime/bin/linker64";
        }
        findSyscalls(link_path, (bool (__cdecl *)(const char *, int, void *))on_found_linker_syscall_arch64);
#else
        HOOK_SYMBOL(handle, faccessat);
        HOOK_SYMBOL(handle, __openat);
        HOOK_SYMBOL(handle, fchmodat);
        HOOK_SYMBOL(handle, fchownat);
        HOOK_SYMBOL(handle, renameat);
        HOOK_SYMBOL(handle, fstatat64);
        HOOK_SYMBOL(handle, __statfs);
        HOOK_SYMBOL(handle, __statfs64);
        HOOK_SYMBOL(handle, mkdirat);
        HOOK_SYMBOL(handle, mknodat);
        HOOK_SYMBOL(handle, truncate);
        HOOK_SYMBOL(handle, linkat);
        HOOK_SYMBOL(handle, readlinkat);
        HOOK_SYMBOL(handle, unlinkat);
        HOOK_SYMBOL(handle, symlinkat);
        HOOK_SYMBOL(handle, utimensat);
        HOOK_SYMBOL(handle, __getcwd);
        HOOK_SYMBOL(handle, chdir);
        HOOK_SYMBOL(handle, execve);
        HOOK_SYMBOL(handle, kill);
        HOOK_SYMBOL(handle, vfork);
        if (api_level <= 20) {
            HOOK_SYMBOL(handle, access);
            HOOK_SYMBOL(handle, stat);
            HOOK_SYMBOL(handle, lstat);
            HOOK_SYMBOL(handle, fstatat);
            HOOK_SYMBOL(handle, __open);
            HOOK_SYMBOL(handle, chmod);
            HOOK_SYMBOL(handle, chown);
            HOOK_SYMBOL(handle, rename);
            HOOK_SYMBOL(handle, rmdir);
            HOOK_SYMBOL(handle, mkdir);
            HOOK_SYMBOL(handle, mknod);
            HOOK_SYMBOL(handle, link);
            HOOK_SYMBOL(handle, unlink);
            HOOK_SYMBOL(handle, readlink);
            HOOK_SYMBOL(handle, symlink);
        }
        hook_dlopen();
#endif
        dlclose(handle);
    }
}


void
IOUniformer::startUniformer(const char *app_pkg_name, const char *so_path, const char *so_path_64, const char *native_path,
                            int api_level,
                            int preview_api_level) {
    ALOGE("startUniformer %s", so_path);
#if defined(__aarch64__)
    setenv("V_SO_PATH", so_path_64, 1);
#else
    setenv("V_SO_PATH", so_path, 1);
#endif
    char api_level_chars[56];
    sprintf(api_level_chars, "%i", api_level);
    setenv("V_API_LEVEL", api_level_chars, 1);
    sprintf(api_level_chars, "%i", preview_api_level);
    setenv("V_PREVIEW_API_LEVEL", api_level_chars, 1);
    setenv("V_NATIVE_PATH", native_path, 1);
    startIOHook(api_level);
}