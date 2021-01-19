#include <jni.h>
#include <limits.h>
#include <stdbool.h>
#include "jni-utils.c"
#include <wally_bip32.h>
#include <wally_core.h>

// com/bc/libwally/bip32/Bip32Exception
static bool throw_new_bip32_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/bip32/Bip32Exception", msg);
}

static bool verify_seed_version(uint32_t version) {
    if (version == BIP32_VER_MAIN_PRIVATE || version == BIP32_VER_TEST_PRIVATE) {
        return true;
    }
    return false;
}

static bool verify_seed_flags(uint32_t flags) {
    if (flags == BIP32_FLAG_SKIP_HASH || flags == 0) {
        return true;
    }
    return false;
}

static bool verify_key_flags(uint32_t flags) {
    if (flags == BIP32_FLAG_KEY_PRIVATE || flags == BIP32_FLAG_KEY_PUBLIC) {
        return true;
    }
    return false;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_bip32_Bip32Jni_bip32_1key_1from_1base58_1alloc(JNIEnv *env,
                                                                    jclass clazz,
                                                                    jstring base58) {
    const char *c_base58 = (*env)->GetStringUTFChars(env, base58, 0);
    struct ext_key *output = (struct ext_key *) calloc(1, sizeof(struct ext_key));

    int ret = bip32_key_from_base58_alloc(c_base58, &output);
    if (ret != WALLY_OK) {
        free(output);
        (*env)->ReleaseStringUTFChars(env, base58, c_base58);
        throw_new_bip32_exception(env, "bip32_key_from_base58_alloc error");
        return NULL;
    }

    jobject result = to_jWallyHDKey(env, output);

    free(output);
    (*env)->ReleaseStringUTFChars(env, base58, c_base58);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_bip32_Bip32Jni_bip32_1key_1from_1seed_1alloc(JNIEnv *env,
                                                                  jclass clazz,
                                                                  jbyteArray seed,
                                                                  jlong version,
                                                                  jlong flags) {

    if (seed == NULL) {
        throw_new_bip32_exception(env, "seed is NULL");
        return NULL;
    }

    if (!verify_seed_version((uint32_t) version)) {
        throw_new_bip32_exception(env, "invalid version");
        return NULL;
    }

    if (!verify_seed_flags((uint32_t) flags)) {
        throw_new_bip32_exception(env, "invalid flags");
        return NULL;
    }

    unsigned char *c_seed = to_unsigned_char_array(env, seed);
    jsize seed_len = (*env)->GetArrayLength(env, seed);
    struct ext_key *output = (struct ext_key *) calloc(1, sizeof(struct ext_key));

    int ret = bip32_key_from_seed_alloc(c_seed,
                                        (size_t) seed_len,
                                        (uint32_t) version,
                                        (uint32_t) flags,
                                        &output);
    if (ret != WALLY_OK) {
        free((unsigned char *) c_seed);
        free(output);
        throw_new_bip32_exception(env, "bip32_key_from_seed_alloc error");
        return NULL;
    }

    jobject result = to_jWallyHDKey(env, output);

    free(c_seed);
    free(output);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_bip32_Bip32Jni_bip32_1key_1to_1base58(JNIEnv *env,
                                                           jclass clazz,
                                                           jobject key,
                                                           jlong flags) {
    if (key == NULL) {
        throw_new_bip32_exception(env, "key is NULL");
        return NULL;
    }

    if (!verify_key_flags((uint32_t) flags)) {
        throw_new_bip32_exception(env, "invalid flags");
        return NULL;
    }

    struct ext_key *c_key = to_c_ext_key(env, key);
    char *output = "";

    int ret = bip32_key_to_base58(c_key, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_key);
        throw_new_bip32_exception(env, "bip32_key_to_base58 error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_key);

    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_bc_libwally_bip32_Bip32Jni_bip32_1key_1get_1fingerprint(JNIEnv *env,
                                                                 jclass clazz,
                                                                 jobject key) {
    if (key == NULL) {
        throw_new_bip32_exception(env, "key is NULL");
        return NULL;
    }

    struct ext_key *c_key = to_c_ext_key(env, key);
    unsigned char *c_output = (unsigned char *) calloc(BIP32_KEY_FINGERPRINT_LEN,
                                                       sizeof(unsigned char));

    int ret = bip32_key_get_fingerprint(c_key, c_output, BIP32_KEY_FINGERPRINT_LEN);
    if (ret != WALLY_OK) {
        free(c_output);
        free(c_key);
        throw_new_bip32_exception(env, "bip32_key_get_fingerprint error");
        return NULL;
    }

    jbyteArray result = create_jbyteArray(env, c_output, BIP32_KEY_FINGERPRINT_LEN);

    free(c_output);
    free(c_key);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_bip32_Bip32Jni_bip32_1key_1from_1parent_1path_1alloc(JNIEnv *env,
                                                                          jclass clazz,
                                                                          jobject key,
                                                                          jlongArray child_path,
                                                                          jlong flags) {
    if (key == NULL) {
        throw_new_bip32_exception(env, "key is NULL");
        return NULL;
    }

    if (!verify_key_flags((uint32_t) flags)) {
        throw_new_bip32_exception(env, "invalid flags");
        return NULL;
    }

    struct ext_key *c_key = to_c_ext_key(env, key);
    uint32_t *c_child_path = to_uint32_t_array(env, child_path);
    jsize child_path_len = (*env)->GetArrayLength(env, child_path);
    struct ext_key *output = calloc(1, sizeof(struct ext_key));

    int ret = bip32_key_from_parent_path_alloc(c_key,
                                               c_child_path,
                                               (size_t) child_path_len,
                                               (uint32_t) flags,
                                               &output);

    if (ret != WALLY_OK) {
        free(c_key);
        free(c_child_path);
        free(output);
        throw_new_bip32_exception(env, "bip32_key_from_parent_path_alloc error");
        return NULL;
    }

    jobject result = to_jWallyHDKey(env, output);

    free(c_key);
    free(c_child_path);
    free(output);

    return result;
}
