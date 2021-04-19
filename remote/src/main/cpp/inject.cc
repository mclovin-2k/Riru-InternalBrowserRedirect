#include <cstdlib>
#include <unistd.h>
#include <fcntl.h>
#include <cerrno>
#include <cstring>
#include <sys/stat.h>
#include "inject.h"
#include "log.h"
#include "dlfcn.h"
#include <cstdint>

#define INJECT_CLASS_PATH "com/github/kr328/ibr/remote/Injector"
#define INJECT_METHOD_NAME "inject"
#define INJECT_METHOD_SIGNATURE "(Ljava/lang/String;)V"

static void *dex_data = nullptr;
static size_t dex_data_length = 0u;

static int catch_exception(JNIEnv *env) {
    int result = env->ExceptionCheck();

    // check status
    if (result) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    return result;
}

void preloadDex(const char *dex_path) {
    if (dex_data != nullptr) return;
    int fd = open(dex_path, O_RDONLY);
    if (fd < 0) {
        LOGE("Open dex file: %s", strerror(errno));
        return;
    }

    struct stat stat{};

    if (fstat(fd, &stat) < 0) {
        LOGE("fetch size of dex file: %s", strerror(errno));

        close(fd);

        return;
    }


    dex_data = malloc(stat.st_size);
    dex_data_length = stat.st_size;

    auto *ptr = (uint8_t *) dex_data;
    int count = 0;

    while (count < stat.st_size) {
        int r = read(fd, ptr, stat.st_size - count);

        if (r < 0) {
            LOGE("read dex: %s", strerror(errno));

            free(dex_data);
            close(fd);

            dex_data = nullptr;
            dex_data_length = 0;

            return;
        }

        count += r;
        ptr += r;
    }

    close(fd);

}

int load_and_invoke_dex(JNIEnv *env, const char *argument) {
    if (dex_data == nullptr) return 1;
    // get system class loader
    jclass cClassLoader = env->FindClass("java/lang/ClassLoader");
    jmethodID mSystemClassLoader = env->GetStaticMethodID(cClassLoader,
                                                             "getSystemClassLoader",
                                                             "()Ljava/lang/ClassLoader;");
    jobject oSystemClassLoader = env->CallStaticObjectMethod(cClassLoader,
                                                                mSystemClassLoader);

    // load dex
    jobject bufferDex = env->NewDirectByteBuffer(dex_data, dex_data_length);
    jclass cDexClassLoader = env->FindClass("dalvik/system/InMemoryDexClassLoader");
    jmethodID mDexClassLoaderInit = env->GetMethodID(cDexClassLoader, "<init>",
                                                        "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    jobject oDexClassLoader = env->NewObject(cDexClassLoader,
                                                mDexClassLoaderInit,
                                                bufferDex,
                                                oSystemClassLoader);

    if (catch_exception(env)) return 1;

    // get loaded dex inject method
    jmethodID mFindClass = env->GetMethodID(cDexClassLoader, "loadClass",
                                               "(Ljava/lang/String;Z)Ljava/lang/Class;");
    jstring sInjectClassName = env->NewStringUTF(INJECT_CLASS_PATH);
    auto cInject = (jclass) env->CallObjectMethod(oDexClassLoader,
                                                       mFindClass, sInjectClassName, (jboolean) 0);

    if (catch_exception(env)) return 1;

    // find method
    jmethodID mLoaded = env->GetStaticMethodID(cInject, INJECT_METHOD_NAME,
                                                  INJECT_METHOD_SIGNATURE);

    if (catch_exception(env)) return 1;

    // invoke inject method
    jstring stringArgument = env->NewStringUTF(argument);

    env->CallStaticVoidMethod(cInject, mLoaded, stringArgument);

    return catch_exception(env);
}

