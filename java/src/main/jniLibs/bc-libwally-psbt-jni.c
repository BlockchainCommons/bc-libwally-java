#include <jni.h>
#include "jni-utils.c"
#include <stdbool.h>
#include <wally_psbt.h>
#include <wally_transaction.h>
#include <limits.h>
#include <stdio.h>

// com/bc/libwally/psbt/PsbtException
static bool throw_new_psbt_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/psbt/PsbtException", msg);
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1clone_1alloc(JNIEnv *env,
                                                            jclass clazz,
                                                            jobject psbt,
                                                            jlong flags) {
    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_psbt_exception(env, "flags is too large");
        return NULL;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    struct wally_psbt *output = (struct wally_psbt *) calloc(1, sizeof(struct wally_psbt));

    int ret = wally_psbt_clone_alloc(c_psbt, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_psbt);
        free(output);
        throw_new_psbt_exception(env, "wally_psbt_clone_alloc error");
        return NULL;
    }

    jobject result = to_jWallyPsbt(env, output);

    free(c_psbt);
    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1from_1bytes(JNIEnv *env,
                                                           jclass clazz,
                                                           jbyteArray bytes) {
    if (bytes == NULL) {
        throw_new_psbt_exception(env, "bytes is NULL");
        return NULL;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    struct wally_psbt *output = (struct wally_psbt *) calloc(1, sizeof(struct wally_psbt));

    int ret = wally_psbt_from_bytes(c_bytes, (size_t) bytes_len, &output);
    if (ret != WALLY_OK) {
        free(c_bytes);
        free(output);
        throw_new_psbt_exception(env, "wally_psbt_from_bytes error");
        return NULL;
    }

    jobject result = to_jWallyPsbt(env, output);

    free(c_bytes);
    free(output);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1get_1length(JNIEnv *env,
                                                           jclass clazz,
                                                           jobject psbt,
                                                           jlong flags) {
    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return JNI_ERR;
    }

    if (flags > UINT32_MAX) {
        throw_new_psbt_exception(env, "flags is too large");
        return JNI_ERR;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    size_t written = 0;

    int ret = wally_psbt_get_length(c_psbt, (uint32_t) flags, &written);
    if (ret != WALLY_OK) {
        free(c_psbt);
        throw_new_psbt_exception(env, "wally_psbt_get_length error");
        return ret;
    }

    free(c_psbt);

    return (jint) written;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1to_1bytes(JNIEnv *env,
                                                         jclass clazz,
                                                         jobject psbt,
                                                         jlong flags,
                                                         jbyteArray output,
                                                         jintArray written) {
    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return JNI_ERR;
    }

    if (flags > UINT32_MAX) {
        throw_new_psbt_exception(env, "flags is too large");
        return JNI_ERR;
    }

    if (output == NULL) {
        throw_new_psbt_exception(env, "output is NULL");
        return JNI_ERR;
    }

    if (written == NULL) {
        throw_new_psbt_exception(env, "written is NULL");
        return JNI_ERR;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    unsigned char *c_output = to_unsigned_char_array(env, output);
    jsize output_len = (*env)->GetArrayLength(env, output);
    size_t c_written = 0;

    int ret = wally_psbt_to_bytes(c_psbt,
                                  (uint32_t) flags,
                                  c_output,
                                  (size_t) output_len,
                                  &c_written);
    if (ret != WALLY_OK) {
        free(c_psbt);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_psbt);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1extract(JNIEnv *env, jclass clazz, jobject psbt) {

    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return NULL;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    struct wally_tx *output = (struct wally_tx *) calloc(1, sizeof(struct wally_tx));

    int ret = wally_psbt_extract(c_psbt, &output);
    if (ret != WALLY_OK) {
        free(c_psbt);
        free(output);
        throw_new_psbt_exception(env, "wally_psbt_extract error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, output);

    free(c_psbt);
    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1sign(JNIEnv *env,
                                                    jclass clazz,
                                                    jobject psbt,
                                                    jbyteArray key,
                                                    jlong flags) {
    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return NULL;
    }

    if (key == NULL) {
        throw_new_psbt_exception(env, "key is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_psbt_exception(env, "flags is too large");
        return NULL;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    unsigned char *c_key = to_unsigned_char_array(env, key);
    jsize key_len = (*env)->GetArrayLength(env, key);

    int ret = wally_psbt_sign(c_psbt, c_key, (size_t) key_len, (uint32_t) flags);
    if (ret != WALLY_OK) {
        free(c_psbt);
        free(c_key);
        throw_new_psbt_exception(env, "wally_psbt_sign error");
        return NULL;
    }

    jobject result = to_jWallyPsbt(env, c_psbt);

    free(c_psbt);
    free(c_key);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1finalize(JNIEnv *env, jclass clazz, jobject psbt) {

    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return NULL;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);

    int ret = wally_psbt_finalize(c_psbt);
    if (ret != WALLY_OK) {
        free(c_psbt);
        throw_new_psbt_exception(env, "wally_psbt_finalize error");
        return NULL;
    }

    jobject result = to_jWallyPsbt(env, c_psbt);

    free(c_psbt);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1from_1base64(JNIEnv *env,
                                                            jclass clazz,
                                                            jstring base64) {

    struct wally_psbt *output = (struct wally_psbt *) calloc(1, sizeof(struct wally_psbt));
    const char *c_base64 = (*env)->GetStringUTFChars(env, base64, 0);

    int ret = wally_psbt_from_base64(c_base64, &output);
    if (ret != WALLY_OK) {
        free(output);
        (*env)->ReleaseStringUTFChars(env, base64, c_base64);
        throw_new_psbt_exception(env, "wally_psbt_from_base64 error");
        return NULL;
    }

    jobject result = to_jWallyPsbt(env, output);

    free(output);
    (*env)->ReleaseStringUTFChars(env, base64, c_base64);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1to_1base64(JNIEnv *env,
                                                          jclass clazz,
                                                          jobject psbt,
                                                          jlong flags) {

    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_psbt_exception(env, "flags is too large");
        return NULL;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    char *output = "";

    int ret = wally_psbt_to_base64(c_psbt, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_psbt);
        throw_new_psbt_exception(env, "wally_psbt_to_base64 error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_psbt);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_bc_libwally_psbt_PsbtJni_wally_1psbt_1is_1finalized(JNIEnv *env,
                                                             jclass clazz,
                                                             jobject psbt) {
    if (psbt == NULL) {
        throw_new_psbt_exception(env, "psbt is NULL");
        return JNI_FALSE;
    }

    struct wally_psbt *c_psbt = to_c_wally_psbt(env, psbt);
    size_t written = 0;

    int ret = wally_psbt_is_finalized(c_psbt, &written);
    if (ret != WALLY_OK) {
        free(c_psbt);
        throw_new_psbt_exception(env, "wally_psbt_is_finalized error");
        return JNI_FALSE;
    }

    free(c_psbt);
    return written;
}