#include <jni.h>
#include <stdbool.h>
#include "jni-utils.c"
#include <wally_bip39.h>
#include <wally_core.h>
#include "../../../../deps/libwally-core/src/wordlist.h"

// com/bc/libwally/bip39/Bip39Exception
static bool throw_new_bip39_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/bip39/Bip39Exception", msg);
}

typedef struct words bip39_words;

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1get_1wordlist(JNIEnv *env, jclass clazz, jstring lang) {

    const char *c_lang = NULL;
    if (lang != NULL) {
        c_lang = (*env)->GetStringUTFChars(env, lang, 0);
    }

    bip39_words *output = (bip39_words *) calloc(1, sizeof(bip39_words));

    int ret = bip39_get_wordlist(c_lang, &output);
    if (ret != WALLY_OK) {
        if (lang != NULL) {
            (*env)->ReleaseStringUTFChars(env, lang, c_lang);
        }
        throw_new_bip39_exception(env, "bip39_get_wordlist error");
        return NULL;
    }
    jobject result = to_jobject(env, (void *) output);

    if (lang != NULL) {
        (*env)->ReleaseStringUTFChars(env, lang, c_lang);
    }

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1get_1word(JNIEnv *env,
                                                     jclass clazz,
                                                     jobject words,
                                                     jint index) {
    bip39_words *c_words = NULL;

    if (words != NULL) {
        c_words = (bip39_words *) to_c_obj_ptr(env, words);
        if (c_words == NULL) {
            throw_new_bip39_exception(env, "c_words is NULL");
            return NULL;
        }
    }

    char *output = "";

    int ret = bip39_get_word(c_words, (size_t) index, &output);
    if (ret != WALLY_OK) {
        if (words != NULL)free(output);
        throw_new_bip39_exception(env, "bip39_get_word error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    if (words != NULL) free(output);
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1mnemonic_1from_1bytes(JNIEnv *env,
                                                                 jclass clazz,
                                                                 jobject words,
                                                                 jbyteArray bytes) {

    if (bytes == NULL) {
        throw_new_bip39_exception(env, "bytes is NULL");
        return NULL;
    }

    bip39_words *c_words = NULL;
    if (words != NULL) {
        c_words = (bip39_words *) to_c_obj_ptr(env, words);
        if (c_words == NULL) {
            throw_new_bip39_exception(env, "c_words is NULL");
            return NULL;
        }
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize byte_len = (*env)->GetArrayLength(env, bytes);
    char *output = "";

    int ret = bip39_mnemonic_from_bytes(c_words, c_bytes, (size_t) byte_len, &output);
    if (ret != WALLY_OK) {
        free(c_bytes);
        if (words != NULL)free(output);
        throw_new_bip39_exception(env, "bip39_mnemonic_from_bytes error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_bytes);
    if (words != NULL)free(output);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1mnemonic_1to_1bytes(JNIEnv *env,
                                                               jclass clazz,
                                                               jobject words,
                                                               jstring mnemonic,
                                                               jbyteArray output,
                                                               jintArray written) {
    if (output == NULL) {
        throw_new_bip39_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_bip39_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    bip39_words *c_words = NULL;
    if (words != NULL) {
        c_words = (bip39_words *) to_c_obj_ptr(env, words);
        if (c_words == NULL) {
            throw_new_bip39_exception(env, "c_words is NULL");
            return WALLY_ERROR;
        }
    }

    const char *c_mnemonic = (*env)->GetStringUTFChars(env, mnemonic, 0);
    jsize output_len = (*env)->GetArrayLength(env, output);
    unsigned char *c_output = calloc(output_len, sizeof(char));
    size_t c_written = 0;

    int ret = bip39_mnemonic_to_bytes(c_words, c_mnemonic, c_output, output_len, &c_written);
    if (ret != WALLY_OK) {
        (*env)->ReleaseStringUTFChars(env, mnemonic, c_mnemonic);
        free(c_output);
        if (words != NULL) free(c_words);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    (*env)->ReleaseStringUTFChars(env, mnemonic, c_mnemonic);
    free(c_output);
    if (words != NULL) free(c_words);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1mnemonic_1to_1seed(JNIEnv *env,
                                                              jclass clazz,
                                                              jstring mnemonic,
                                                              jstring passphrase,
                                                              jbyteArray output,
                                                              jintArray written) {
    if (output == NULL) {
        throw_new_bip39_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_bip39_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    const char *c_mnemonic = (*env)->GetStringUTFChars(env, mnemonic, 0);
    const char *c_pass_phrase = (*env)->GetStringUTFChars(env, passphrase, 0);
    jsize output_len = (*env)->GetArrayLength(env, output);
    unsigned char *c_output = calloc(output_len, sizeof(char));
    size_t c_written = 0;

    int ret = bip39_mnemonic_to_seed(c_mnemonic, c_pass_phrase, c_output, output_len, &c_written);

    if (ret != WALLY_OK) {
        (*env)->ReleaseStringUTFChars(env, mnemonic, c_mnemonic);
        (*env)->ReleaseStringUTFChars(env, passphrase, c_pass_phrase);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    (*env)->ReleaseStringUTFChars(env, mnemonic, c_mnemonic);
    (*env)->ReleaseStringUTFChars(env, passphrase, c_pass_phrase);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_bip39_Bip39Jni_bip39_1mnemonic_1validate(JNIEnv *env,
                                                              jclass clazz,
                                                              jobject words,
                                                              jstring mnemonic) {
    bip39_words *c_words = NULL;
    if (words != NULL) {
        c_words = (bip39_words *) to_c_obj_ptr(env, words);
        if (c_words == NULL) {
            throw_new_bip39_exception(env, "c_words is NULL");
            return WALLY_ERROR;
        }
    }

    const char *c_mnemonic = (*env)->GetStringUTFChars(env, mnemonic, 0);

    int ret = bip39_mnemonic_validate(c_words, c_mnemonic);

    (*env)->ReleaseStringUTFChars(env, mnemonic, c_mnemonic);
    if (c_words != NULL) free(c_words);

    return ret;
}