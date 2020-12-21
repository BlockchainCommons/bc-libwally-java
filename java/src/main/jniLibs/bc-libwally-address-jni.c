#include <jni.h>
#include <limits.h>
#include <stdbool.h>
#include <string.h>
#include "jni-utils.c"
#include <wally_address.h>
#include <wally_core.h>
#include <wally_crypto.h>

// com/bc/libwally/address/AddressException
static bool throw_new_address_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/address/AddressException", msg);
}

static bool verify_wif_flag(uint32_t flags) {
    if (flags == WALLY_WIF_FLAG_COMPRESSED || flags == WALLY_WIF_FLAG_UNCOMPRESSED) {
        return true;
    }
    return false;
}


JNIEXPORT jint JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1addr_1segwit_1to_1bytes(JNIEnv *env,
                                                                       jclass clazz,
                                                                       jstring addr,
                                                                       jstring addr_family,
                                                                       jbyteArray output,
                                                                       jintArray written) {

    if (output == NULL) {
        throw_new_address_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_address_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    const char *c_addr = (*env)->GetStringUTFChars(env, addr, 0);
    const char *c_addr_family = (*env)->GetStringUTFChars(env, addr_family, 0);
    const size_t addr_len = strlen(c_addr);
    unsigned char *c_output = calloc(addr_len, sizeof(char));
    size_t c_written = 0;

    int ret = wally_addr_segwit_to_bytes(c_addr, c_addr_family, 0, c_output, addr_len, &c_written);
    if (ret != WALLY_OK) {
        (*env)->ReleaseStringUTFChars(env, addr, c_addr);
        (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, addr_len);
    copy_to_jintArray(env, written, &c_written, 1);

    (*env)->ReleaseStringUTFChars(env, addr, c_addr);
    (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1address_1to_1scriptpubkey(JNIEnv *env,
                                                                         jclass clazz,
                                                                         jstring addr,
                                                                         jlong network,
                                                                         jbyteArray output,
                                                                         jintArray written) {

    if (output == NULL) {
        throw_new_address_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_address_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    if (!verify_network((uint32_t) network)) {
        throw_new_address_exception(env, "invalid network");
        return WALLY_ERROR;
    }

    const char *c_addr = (*env)->GetStringUTFChars(env, addr, 0);
    jsize output_len = (*env)->GetArrayLength(env, output);
    unsigned char *c_output = calloc(output_len, sizeof(char));
    size_t c_written = 0;

    int ret = wally_address_to_scriptpubkey(c_addr, (uint32_t) network, c_output, output_len,
                                            &c_written);
    if (ret != WALLY_OK) {
        (*env)->ReleaseStringUTFChars(env, addr, c_addr);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, output_len);
    copy_to_jintArray(env, written, &c_written, 1);

    (*env)->ReleaseStringUTFChars(env, addr, c_addr);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1bip32_1key_1to_1address(JNIEnv *env,
                                                                       jclass clazz,
                                                                       jobject key,
                                                                       jlong flags,
                                                                       jlong version) {
    if (key == NULL) {
        throw_new_address_exception(env, "key is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX || version > UINT32_MAX) {
        throw_new_address_exception(env, "flags or version is too large");
        return NULL;
    }

    struct ext_key *c_key = to_c_ext_key(env, key);
    char *output = "";

    int ret = wally_bip32_key_to_address(c_key, (uint32_t) flags, (uint32_t) version, &output);
    if (ret != WALLY_OK) {
        free(output);
        free(c_key);
        throw_new_address_exception(env, "wally_bip32_key_to_address error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(output);
    free(c_key);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1bip32_1key_1to_1addr_1segwit(JNIEnv *env,
                                                                            jclass clazz,
                                                                            jobject key,
                                                                            jstring addr_family) {
    if (key == NULL) {
        throw_new_address_exception(env, "key is NULL");
        return NULL;
    }

    struct ext_key *c_key = to_c_ext_key(env, key);
    const char *c_addr_family = (*env)->GetStringUTFChars(env, addr_family, 0);
    char *output = "";

    int ret = wally_bip32_key_to_addr_segwit(c_key, c_addr_family, 0, &output);
    if (ret != WALLY_OK) {
        free(output);
        free(c_key);
        (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);
        throw_new_address_exception(env, "wally_bip32_key_to_addr_segwit error");
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, output);

    free(output);
    free(c_key);
    (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1scriptpubkey_1to_1address(JNIEnv *env,
                                                                         jclass clazz,
                                                                         jbyteArray script_pub_key,
                                                                         jlong network) {

    if (script_pub_key == NULL) {
        throw_new_address_exception(env, "script_pub_key is NULL");
        return NULL;
    }

    if (!verify_network((uint32_t) network)) {
        throw_new_address_exception(env, "invalid network");
        return NULL;
    }

    unsigned char *c_script_pubkey = (unsigned char *) to_unsigned_char_array(env,
                                                                              script_pub_key);
    jsize script_pubkey_len = (*env)->GetArrayLength(env, script_pub_key);
    char *output = "";

    int ret = wally_scriptpubkey_to_address(c_script_pubkey,
                                            (size_t) script_pubkey_len,
                                            (uint32_t) network,
                                            &output);
    if (ret != WALLY_OK) {
        free(output);
        free(c_script_pubkey);
        throw_new_address_exception(env, "wally_scriptpubkey_to_address error");
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, output);

    free(output);
    free(c_script_pubkey);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1addr_1segwit_1from_1bytes(JNIEnv *env,
                                                                         jclass clazz,
                                                                         jbyteArray bytes,
                                                                         jstring addr_family) {
    if (bytes == NULL) {
        throw_new_address_exception(env, "bytes is NULL");
        return NULL;
    }

    unsigned char *c_bytes = (unsigned char *) to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    const char *c_addr_family = (*env)->GetStringUTFChars(env, addr_family, 0);
    char *output = "";

    int ret = wally_addr_segwit_from_bytes(c_bytes,
                                           (size_t) bytes_len,
                                           c_addr_family,
                                           0,
                                           &output);
    if (ret != WALLY_OK) {
        free(output);
        free(c_bytes);
        (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);
        throw_new_address_exception(env, "wally_addr_segwit_from_bytes error");
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, output);

    free(output);
    free(c_bytes);
    (*env)->ReleaseStringUTFChars(env, addr_family, c_addr_family);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1wif_1to_1bytes(JNIEnv *env,
                                                              jclass clazz,
                                                              jbyteArray wif,
                                                              jlong prefix,
                                                              jlong flags,
                                                              jbyteArray output) {

    if (wif == NULL) {
        throw_new_address_exception(env, "wif is NULL");
        return WALLY_ERROR;
    }

    if (prefix > UINT32_MAX) {
        throw_new_address_exception(env, "prefix is too large");
        return WALLY_ERROR;
    }

    if (!verify_wif_flag((uint32_t) flags)) {
        throw_new_address_exception(env, "invalid wif flag");
        return WALLY_ERROR;
    }

    char *c_wif = (char *) to_unsigned_char_array(env, wif);
    unsigned char *c_output = (unsigned char *) calloc(EC_PRIVATE_KEY_LEN, sizeof(char));

    int ret = wally_wif_to_bytes(c_wif,
                                 (uint32_t) prefix,
                                 (uint32_t) flags,
                                 c_output,
                                 EC_PRIVATE_KEY_LEN);
    if (ret != WALLY_OK) {
        free(c_output);
        free(c_wif);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, EC_PRIVATE_KEY_LEN);

    free(c_output);
    free(c_wif);

    return WALLY_OK;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_address_AddressJni_wally_1wif_1from_1bytes(JNIEnv *env,
                                                                jclass clazz,
                                                                jbyteArray priv_key,
                                                                jlong prefix,
                                                                jlong flags) {
    if (priv_key == NULL) {
        throw_new_address_exception(env, "priv_key is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX || prefix > UINT32_MAX) {
        throw_new_address_exception(env, "flags or prefix is too large");
        return NULL;
    }

    if (!verify_wif_flag((uint32_t) flags)) {
        throw_new_address_exception(env, "invalid wif flag");
        return NULL;
    }

    unsigned char *c_prv_key = (unsigned char *) to_unsigned_char_array(env, priv_key);
    jsize prv_len = (*env)->GetArrayLength(env, priv_key);

    char *output = "";

    int ret = wally_wif_from_bytes(c_prv_key,
                                   (size_t) prv_len,
                                   (uint32_t) prefix,
                                   (uint32_t) flags,
                                   &output);
    if (ret != WALLY_OK) {
        free(c_prv_key);
        free(output);
        throw_new_address_exception(env, "wally_wif_from_bytes error");
        return NULL;
    }

    jstring result = (*env)->NewStringUTF(env, output);

    free(c_prv_key);
    free(output);

    return result;
}