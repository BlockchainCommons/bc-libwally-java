#include <jni.h>
#include <limits.h>
#include <stdbool.h>
#include <wally_core.h>
#include "jni-utils.c"
#include <ccan/ccan/base64/base64.h>

// com/bc/libwally/core/CoreException
static bool throw_new_core_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/core/CoreException", msg);
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1hex_1from_1bytes(JNIEnv *env,
                                                          jclass clazz,
                                                          jbyteArray bytes) {
    if (bytes == NULL) {
        throw_new_core_exception(env, "bytes is NULL");
        return NULL;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    char *output = "";

    int ret = wally_hex_from_bytes(c_bytes, bytes_len, &output);
    if (ret != WALLY_OK) {
        free(c_bytes);
        free(output);
        throw_new_core_exception(env, "wally_hex_from_bytes error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_bytes);
    free(output);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1hex_1to_1bytes(JNIEnv *env,
                                                        jclass clazz,
                                                        jstring hex,
                                                        jbyteArray output,
                                                        jintArray written) {
    if (output == NULL) {
        throw_new_core_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_core_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    unsigned char *c_output = to_unsigned_char_array(env, output);
    jsize output_len = (*env)->GetArrayLength(env, output);
    const char *c_hex = (*env)->GetStringUTFChars(env, hex, 0);
    size_t c_written = 0;

    int ret = wally_hex_to_bytes(c_hex, c_output, (size_t) output_len, &c_written);
    if (ret != WALLY_OK) {
        free(c_output);
        (*env)->ReleaseStringUTFChars(env, hex, c_hex);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_output);
    (*env)->ReleaseStringUTFChars(env, hex, c_hex);

    return WALLY_OK;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1base58_1from_1bytes(JNIEnv *env,
                                                             jclass clazz,
                                                             jbyteArray bytes,
                                                             jlong flags) {
    if (bytes == NULL) {
        throw_new_core_exception(env, "bytes is NULL");
        return NULL;
    }

    if (flags != BASE58_FLAG_CHECKSUM && flags != 0) {
        throw_new_core_exception(env, "flags is invalid");
        return NULL;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    char *output = "";

    int ret = wally_base58_from_bytes(c_bytes, bytes_len, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_bytes);
        free(output);
        throw_new_core_exception(env, "wally_base58_from_bytes error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_bytes);
    free(output);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1base58_1to_1bytes(JNIEnv *env,
                                                           jclass clazz,
                                                           jstring base58,
                                                           jlong flags,
                                                           jbyteArray output,
                                                           jintArray written) {
    if (output == NULL) {
        throw_new_core_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_core_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    if (flags != BASE58_FLAG_CHECKSUM && flags != 0) {
        throw_new_core_exception(env, "flags is invalid");
        return WALLY_ERROR;
    }

    unsigned char *c_output = to_unsigned_char_array(env, output);
    jsize output_len = (*env)->GetArrayLength(env, output);
    const char *c_base58 = (*env)->GetStringUTFChars(env, base58, 0);
    size_t c_written = 0;

    int ret = wally_base58_to_bytes(c_base58,
                                    (uint32_t) flags,
                                    c_output,
                                    (size_t) output_len,
                                    &c_written);
    if (ret != WALLY_OK) {
        free(c_output);
        (*env)->ReleaseStringUTFChars(env, base58, c_base58);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_output);
    (*env)->ReleaseStringUTFChars(env, base58, c_base58);

    return WALLY_OK;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1base64_1from_1bytes(JNIEnv *env,
                                                             jclass clazz,
                                                             jbyteArray bytes) {
    if (bytes == NULL) {
        throw_new_core_exception(env, "bytes is NULL");
        return NULL;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    size_t output_len = base64_encoded_length(bytes_len);
    char *output = (char *) calloc(bytes_len, sizeof(char));

    size_t written = base64_encode(output, output_len, (char *) c_bytes, bytes_len);
    if (written == 0 || written > output_len) {
        free(c_bytes);
        free(output);
        throw_new_core_exception(env, "base64_encode error");
        return NULL;
    }

    char *c_result = (char *) calloc(written, sizeof(char));
    memcpy(c_result, output, written);

    jstring result = (*env)->NewStringUTF(env, c_result);

    free(c_bytes);
    free(output);
    free(c_result);

    return result;

}

JNIEXPORT jbyteArray JNICALL
Java_com_bc_libwally_core_CoreJni_wally_1base64_1to_1bytes(JNIEnv *env,
                                                           jclass clazz,
                                                           jstring base64) {

    const char *c_base64 = (*env)->GetStringUTFChars(env, base64, 0);
    size_t base64_len = strlen(c_base64);
    size_t output_len = base64_decoded_length(base64_len);
    char *output = (char *) calloc(output_len, sizeof(char));

    size_t written = base64_decode(output, output_len, c_base64, base64_len);
    if (written == 0 || written > output_len) {
        (*env)->ReleaseStringUTFChars(env, base64, c_base64);
        free(output);
        throw_new_core_exception(env, "base64_decode error");
        return NULL;
    }

    jbyteArray result = create_jbyteArray(env, (unsigned char *) output, output_len);

    (*env)->ReleaseStringUTFChars(env, base64, c_base64);
    free(output);

    return result;
}
