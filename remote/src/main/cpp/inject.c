#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>
#include "inject.h"
#include "log.h"
#include "dlfcn.h"
#include "stdint.h"

#define INJECT_CLASS_PATH "com/github/kr328/ibr/remote/Injector"
#define INJECT_METHOD_NAME "inject"
#define INJECT_METHOD_SIGNATURE "(Ljava/lang/String;)V"

static void *dex_data = NULL;
static size_t dex_data_length = 0u;

static int catch_exception(JNIEnv *env) {
    int result = (*env)->ExceptionCheck(env);

    // check status
    if (result) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }

    return result;
}

void preloadDex(const char *dex_path) {
    if (dex_data != NULL) return;
    int fd = open(dex_path, O_RDONLY);
    if (fd < 0) {
        LOGE("Open dex file: %s", strerror(errno));
        return;
    }

    struct stat stat;

    if (fstat(fd, &stat) < 0) {
        LOGE("fetch size of dex file: %s", strerror(errno));

        close(fd);

        return;
    }


    dex_data = malloc(stat.st_size);
    dex_data_length = stat.st_size;

    uint8_t *ptr = (uint8_t *) dex_data;
    int count = 0;

    while (count < stat.st_size) {
        int r = read(fd, ptr, stat.st_size - count);

        if (r < 0) {
            LOGE("read dex: %s", strerror(errno));

            free(dex_data);
            close(fd);

            dex_data = NULL;
            dex_data_length = 0;

            return;
        }

        count += r;
        ptr += r;
    }

    close(fd);

}

int load_and_invoke_dex(JNIEnv *env, const char *argument) {
    if (dex_data == NULL) return 1;
    // get system class loader
    jclass cClassLoader = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID mSystemClassLoader = (*env)->GetStaticMethodID(env, cClassLoader,
                                                             "getSystemClassLoader",
                                                             "()Ljava/lang/ClassLoader;");
    jobject oSystemClassLoader = (*env)->CallStaticObjectMethod(env, cClassLoader,
                                                                mSystemClassLoader);

    // load dex
    jobject bufferDex = (*env)->NewDirectByteBuffer(env, dex_data, dex_data_length);
    jclass cDexClassLoader = (*env)->FindClass(env, "dalvik/system/InMemoryDexClassLoader");
    jmethodID mDexClassLoaderInit = (*env)->GetMethodID(env, cDexClassLoader, "<init>",
                                                        "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    jobject oDexClassLoader = (*env)->NewObject(env, cDexClassLoader,
                                                mDexClassLoaderInit,
                                                bufferDex,
                                                oSystemClassLoader);

    if (catch_exception(env)) return 1;

    // get loaded dex inject method
    jmethodID mFindClass = (*env)->GetMethodID(env, cDexClassLoader, "loadClass",
                                               "(Ljava/lang/String;Z)Ljava/lang/Class;");
    jstring sInjectClassName = (*env)->NewStringUTF(env, INJECT_CLASS_PATH);
    jclass cInject = (jclass) (*env)->CallObjectMethod(env, oDexClassLoader,
                                                       mFindClass, sInjectClassName, (jboolean) 0);

    if (catch_exception(env)) return 1;

    // find method
    jmethodID mLoaded = (*env)->GetStaticMethodID(env, cInject, INJECT_METHOD_NAME,
                                                  INJECT_METHOD_SIGNATURE);

    if (catch_exception(env)) return 1;

    // invoke inject method
    jstring stringArgument = (*env)->NewStringUTF(env, argument);

    (*env)->CallStaticVoidMethod(env, cInject, mLoaded, stringArgument);

    return catch_exception(env);
}

