#include <jni.h>
#include <stdbool.h>
#include <limits.h>
#include <wally_script.h>
#include <wally_core.h>
#include "jni-utils.c"

// com/bc/libwally/script/ScriptException
static bool throw_new_script_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/script/ScriptException", msg);
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_script_ScriptJni_wally_1scriptpubkey_1get_1type(JNIEnv *env,
                                                                     jclass clazz,
                                                                     jbyteArray bytes) {
    if (bytes == NULL) {
        throw_new_script_exception(env, "bytes is NULL");
        return WALLY_ERROR;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jint bytes_len = (*env)->GetArrayLength(env, bytes);
    size_t written = 0;

    int ret = wally_scriptpubkey_get_type(c_bytes, bytes_len, &written);
    if (ret != WALLY_OK) {
        free(c_bytes);
        throw_new_script_exception(env, "wally_scriptpubkey_get_type error");
        return ret;
    }

    free(c_bytes);
    return written;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_script_ScriptJni_wally_1scriptpubkey_1multisig_1from_1bytes(JNIEnv *env,
                                                                                 jclass clazz,
                                                                                 jbyteArray bytes,
                                                                                 jlong threshold,
                                                                                 jlong flags,
                                                                                 jbyteArray output,
                                                                                 jintArray written) {
    if (bytes == NULL) {
        throw_new_script_exception(env, "bytes is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_script_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jint written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_script_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    if (flags != 0 && flags != WALLY_SCRIPT_MULTISIG_SORTED) {
        throw_new_script_exception(env, "invalid flags");
        return WALLY_ERROR;
    }

    if (threshold < 0 || threshold > UINT32_MAX) {
        throw_new_script_exception(env, "invalid threshold");
        return WALLY_ERROR;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jint bytes_len = (*env)->GetArrayLength(env, bytes);
    jint output_len = (*env)->GetArrayLength(env, output);
    unsigned char *c_output = (unsigned char *) calloc(output_len, sizeof(unsigned char));
    size_t c_written = 0;

    int ret = wally_scriptpubkey_multisig_from_bytes(c_bytes,
                                                     bytes_len,
                                                     (uint32_t) threshold,
                                                     (uint32_t) flags,
                                                     c_output,
                                                     output_len,
                                                     &c_written);

    if (ret != WALLY_OK) {
        free(c_bytes);
        free(c_output);
        throw_new_script_exception(env, "wally_scriptpubkey_multisig_from_bytes error");
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_bytes);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_script_ScriptJni_wally_1witness_1program_1from_1bytes(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jbyteArray bytes,
                                                                           jlong flags,
                                                                           jbyteArray output,
                                                                           jintArray written) {
    if (bytes == NULL) {
        throw_new_script_exception(env, "bytes is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_script_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jint written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_script_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    if (flags != WALLY_SCRIPT_HASH160 && flags != WALLY_SCRIPT_SHA256 &&
        flags != WALLY_SCRIPT_AS_PUSH) {
        throw_new_script_exception(env, "invalid flags");
        return WALLY_ERROR;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jint bytes_len = (*env)->GetArrayLength(env, bytes);
    jint output_len = (*env)->GetArrayLength(env, output);
    unsigned char *c_output = (unsigned char *) calloc(output_len, sizeof(unsigned char));
    size_t c_written = 0;

    int ret = wally_witness_program_from_bytes(c_bytes,
                                               bytes_len,
                                               (uint32_t) flags,
                                               c_output,
                                               output_len,
                                               &c_written);

    if (ret != WALLY_OK) {
        free(c_bytes);
        free(c_output);
        throw_new_script_exception(env, "wally_witness_program_from_bytes error");
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_bytes);
    free(c_output);

    return WALLY_OK;
}