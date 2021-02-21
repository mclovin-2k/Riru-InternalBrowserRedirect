#include <stdio.h>
#include <jni.h>
#include <dlfcn.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <sys/system_properties.h>
#include <riru.h>

#include "hook.h"
#include "log.h"
#include "inject.h"

#define DEX_PATH    "/system/framework/ibr.dex"
#define RULES_PATH  "/data/misc/internal_browser_redirect/userdata/rules.%s.json"
#define SERVICE_STATUE_KEY "sys.ibr.status"

static int enable_inject;

static void on_app_fork(JNIEnv *env, jstring jAppDataDir) {
    char path_buffer[1024];
    char package_name[256] = {0};

    if (jAppDataDir) {
        const char *appDataDir = (*env)->GetStringUTFChars(env, jAppDataDir, NULL);
        int user = 0;
        while (appDataDir) {
            // /data/user/<user_id>/<package>
            if (sscanf(appDataDir, "/data/%*[^/]/%d/%s", &user, package_name) == 2)
                break;

            // /mnt/expand/<id>/user/<user_id>/<package>
            if (sscanf(appDataDir, "/mnt/expand/%*[^/]/%*[^/]/%d/%s", &user, package_name) == 2)
                break;

            // /data/data/<package>
            if (sscanf(appDataDir, "/data/%*[^/]/%s", package_name) == 1)
                break;

            package_name[0] = '\0';
            break;
        }
        (*env)->ReleaseStringUTFChars(env, jAppDataDir, appDataDir);
    }

    sprintf(path_buffer, RULES_PATH, package_name);

    enable_inject = access(path_buffer, F_OK) == 0;
    LOGD("should hook %s: %d", package_name, enable_inject);
}

static void nativeForkAndSpecializePre(
        JNIEnv *env, jclass clazz, jint *_uid, jint *gid, jintArray *gids, jint *runtimeFlags,
        jobjectArray *rlimits, jint *mountExternal, jstring *seInfo, jstring *niceName,
        jintArray *fdsToClose, jintArray *fdsToIgnore, jboolean *is_child_zygote,
        jstring *instructionSet, jstring *appDataDir, jboolean *isTopApp,
        jobjectArray *pkgDataInfoList,
        jobjectArray *whitelistedDataInfoList, jboolean *bindMountAppDataDirs,
        jboolean *bindMountAppStorageDirs) {
    on_app_fork(env, *appDataDir);
}

static void nativeForkAndSpecializePost(JNIEnv *env, jclass clazz, jint res) {
    if (res == 0 && enable_inject) {
        load_and_invoke_dex(env, "app_forked");
    }
}

static void nativeForkSystemServerPost(JNIEnv *env, jclass clazz, jint res) {
    if (res == 0) {
        __system_property_set(SERVICE_STATUE_KEY, "system_server_forked");
        load_and_invoke_dex(env, "system_server_forked");
    }
}

static void onModuleLoaded() {
    LOGI("welcome to ibr");
    preloadDex(DEX_PATH);
//    __system_property_set(SERVICE_STATUE_KEY, "riru_loaded");
}


int riru_api_version;
RiruApiV9 *riru_api_v9;

/*
 * Init will be called three times.
 *
 * The first time:
 *   Returns the highest version number supported by both Riru and the module.
 *
 *   arg: (int *) Riru's API version
 *   returns: (int *) the highest possible API version
 *
 * The second time:
 *   Returns the RiruModuleX struct created by the module.
 *   (X is the return of the first call)
 *
 *   arg: (RiruApiVX *) RiruApi strcut, this pointer can be saved for further use
 *   returns: (RiruModuleX *) RiruModule strcut
 *
 * The second time:
 *   Let the module to cleanup (such as RiruModuleX struct created before).
 *
 *   arg: null
 *   returns: (ignored)
 *
 */
RIRU_EXPORT void *init(void *arg) {
    static int step = 0;
    step += 1;

    static void *_module;

    switch (step) {
        case 1: {
            int core_max_api_version = *(int *) arg;
            riru_api_version =
                    core_max_api_version <= RIRU_MODULE_API_VERSION ? core_max_api_version
                                                                    : RIRU_MODULE_API_VERSION;
            return &riru_api_version;
        }
        case 2: {
            switch (riru_api_version) {
                // RiruApiV10 and RiruModuleInfoV10 are equal to V9
                case 10:
                case 9: {
                    riru_api_v9 = (RiruApiV9 *) arg;

                    RiruModuleInfoV9 *module = (RiruModuleInfoV9 *) malloc(
                            sizeof(RiruModuleInfoV9));
                    memset(module, 0, sizeof(RiruModuleInfoV9));
                    _module = module;

                    module->supportHide = true;

                    module->version = 1;
                    module->versionName = RIRU_MODULE_VERSION_NAME;
                    module->onModuleLoaded = onModuleLoaded;
                    module->shouldSkipUid = NULL;
                    module->forkAndSpecializePre = nativeForkAndSpecializePre;
                    module->forkAndSpecializePost = nativeForkAndSpecializePost;
                    module->specializeAppProcessPre = NULL;
                    module->specializeAppProcessPost = NULL;
                    module->forkSystemServerPre = NULL;
                    module->forkSystemServerPost = nativeForkSystemServerPost;
                    return module;
                }
                default: {
                    return NULL;
                }
            }
        }
        case 3: {
            free(_module);
            return NULL;
        }
        default: {
            return NULL;
        }
    }
}
