#include <jni.h>
#include <stdbool.h>
#include <wally_crypto.h>
#include <wally_core.h>
#include "jni-utils.c"

// com/bc/libwally/crypto/CryptoException
static bool throw_new_crypto_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/crypto/CryptoException", msg);
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1public_1key_1from_1private_1key(JNIEnv *env,
                                                                                 jclass clazz,
                                                                                 jbyteArray priv_key,
                                                                                 jbyteArray output) {
    if (priv_key == NULL) {
        throw_new_crypto_exception(env, "priv_key is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jint priv_key_len = (*env)->GetArrayLength(env, priv_key);
    if (priv_key_len != EC_PRIVATE_KEY_LEN) {
        throw_new_crypto_exception(env, "invalid priv_key len");
        return WALLY_ERROR;
    }

    unsigned char *c_priv_key = (unsigned char *) to_unsigned_char_array(env, priv_key);
    unsigned char *c_output = (unsigned char *) calloc(EC_PUBLIC_KEY_LEN, sizeof(unsigned char));

    int ret = wally_ec_public_key_from_private_key(c_priv_key,
                                                   priv_key_len,
                                                   c_output,
                                                   EC_PUBLIC_KEY_LEN);
    if (ret != WALLY_OK) {
        free(c_priv_key);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, EC_PUBLIC_KEY_LEN);

    free(c_priv_key);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1public_1key_1decompress(JNIEnv *env,
                                                                         jclass clazz,
                                                                         jbyteArray pub_key,
                                                                         jbyteArray output) {
    if (pub_key == NULL) {
        throw_new_crypto_exception(env, "pub_key is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jint pub_key_len = (*env)->GetArrayLength(env, pub_key);
    if (pub_key_len != EC_PUBLIC_KEY_LEN) {
        throw_new_crypto_exception(env, "invalid pub_key len");
        return WALLY_ERROR;
    }

    unsigned char *c_pub_key = (unsigned char *) to_unsigned_char_array(env, pub_key);
    unsigned char *c_output = (unsigned char *) calloc(EC_PUBLIC_KEY_UNCOMPRESSED_LEN,
                                                       sizeof(unsigned char));

    int ret = wally_ec_public_key_decompress(c_pub_key,
                                             pub_key_len,
                                             c_output,
                                             EC_PUBLIC_KEY_UNCOMPRESSED_LEN);
    if (ret != WALLY_OK) {
        free(c_pub_key);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, EC_PUBLIC_KEY_UNCOMPRESSED_LEN);

    free(c_pub_key);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1hash160(JNIEnv *env,
                                                     jclass clazz,
                                                     jbyteArray bytes,
                                                     jbyteArray output) {
    if (bytes == NULL) {
        throw_new_crypto_exception(env, "bytes is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    jint bytes_len = (*env)->GetArrayLength(env, bytes);

    unsigned char *c_bytes = (unsigned char *) to_unsigned_char_array(env, bytes);
    unsigned char *c_output = (unsigned char *) calloc(HASH160_LEN, sizeof(unsigned char));

    int ret = wally_hash160(c_bytes, bytes_len, c_output, HASH160_LEN);
    if (ret != WALLY_OK) {
        free(c_bytes);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, HASH160_LEN);

    free(c_bytes);
    free(c_output);

    return WALLY_OK;
}

