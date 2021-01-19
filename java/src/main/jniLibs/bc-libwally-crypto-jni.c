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

    jsize priv_key_len = (*env)->GetArrayLength(env, priv_key);
    if (priv_key_len != EC_PRIVATE_KEY_LEN) {
        throw_new_crypto_exception(env, "invalid priv_key len");
        return WALLY_ERROR;
    }

    unsigned char *c_priv_key = to_unsigned_char_array(env, priv_key);
    unsigned char *c_output = (unsigned char *) calloc(EC_PUBLIC_KEY_LEN, sizeof(unsigned char));

    int ret = wally_ec_public_key_from_private_key(c_priv_key,
                                                   (size_t) priv_key_len,
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

    jsize pub_key_len = (*env)->GetArrayLength(env, pub_key);
    if (pub_key_len != EC_PUBLIC_KEY_LEN) {
        throw_new_crypto_exception(env, "invalid pub_key len");
        return WALLY_ERROR;
    }

    unsigned char *c_pub_key = to_unsigned_char_array(env, pub_key);
    unsigned char *c_output = (unsigned char *) calloc(EC_PUBLIC_KEY_UNCOMPRESSED_LEN,
                                                       sizeof(unsigned char));

    int ret = wally_ec_public_key_decompress(c_pub_key,
                                             (size_t) pub_key_len,
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

    jsize bytes_len = (*env)->GetArrayLength(env, bytes);

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    unsigned char *c_output = (unsigned char *) calloc(HASH160_LEN, sizeof(unsigned char));

    int ret = wally_hash160(c_bytes, (size_t) bytes_len, c_output, HASH160_LEN);
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

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1private_1key_1verify(JNIEnv *env,
                                                                      jclass clazz,
                                                                      jbyteArray priv_key) {

    if (priv_key == NULL) {
        throw_new_crypto_exception(env, "priv_key is NULL");
        return WALLY_ERROR;
    }

    unsigned char *c_priv_key = to_unsigned_char_array(env, priv_key);
    jsize priv_key_len = (*env)->GetArrayLength(env, priv_key);

    int ret = wally_ec_private_key_verify(c_priv_key, (size_t) priv_key_len);

    free(c_priv_key);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1sig_1from_1bytes(JNIEnv *env,
                                                                  jclass clazz,
                                                                  jbyteArray priv_key,
                                                                  jbyteArray message,
                                                                  jlong flags,
                                                                  jbyteArray output) {
    if (priv_key == NULL) {
        throw_new_crypto_exception(env, "priv_key is NULL");
        return WALLY_ERROR;
    }

    if (message == NULL) {
        throw_new_crypto_exception(env, "message is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    if (flags > UINT32_MAX) {
        throw_new_crypto_exception(env, "flags is too large");
        return WALLY_ERROR;
    }

    unsigned char *c_priv_key = to_unsigned_char_array(env, priv_key);
    jsize priv_key_len = (*env)->GetArrayLength(env, priv_key);
    unsigned char *c_message = to_unsigned_char_array(env, message);
    jsize message_len = (*env)->GetArrayLength(env, message);
    uint32_t out_len =
            flags == EC_FLAG_RECOVERABLE ? EC_SIGNATURE_RECOVERABLE_LEN : EC_SIGNATURE_LEN;
    unsigned char *c_output = (unsigned char *) calloc(out_len, sizeof(unsigned char));

    int ret = wally_ec_sig_from_bytes(c_priv_key,
                                      (size_t) priv_key_len,
                                      c_message,
                                      (size_t) message_len,
                                      (uint32_t) flags,
                                      c_output,
                                      (size_t) out_len);

    copy_to_jbyteArray(env, output, c_output, out_len);

    free(c_priv_key);
    free(c_message);
    free(c_output);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1sig_1verify(JNIEnv *env,
                                                             jclass clazz,
                                                             jbyteArray pub_key,
                                                             jbyteArray message,
                                                             jlong flags,
                                                             jbyteArray sig) {
    if (pub_key == NULL) {
        throw_new_crypto_exception(env, "pub_key is NULL");
        return WALLY_ERROR;
    }

    if (message == NULL) {
        throw_new_crypto_exception(env, "message is NULL");
        return WALLY_ERROR;
    }

    if (sig == NULL) {
        throw_new_crypto_exception(env, "sig is NULL");
        return WALLY_ERROR;
    }

    if (flags > UINT32_MAX) {
        throw_new_crypto_exception(env, "flags is too large");
        return WALLY_ERROR;
    }

    unsigned char *c_pub_key = to_unsigned_char_array(env, pub_key);
    jsize pub_key_len = (*env)->GetArrayLength(env, pub_key);
    unsigned char *c_message = to_unsigned_char_array(env, message);
    jsize message_len = (*env)->GetArrayLength(env, message);
    unsigned char *c_sig = to_unsigned_char_array(env, sig);

    int ret = wally_ec_sig_verify(c_pub_key,
                                  (size_t) pub_key_len,
                                  c_message,
                                  (size_t) message_len,
                                  (uint32_t) flags,
                                  c_sig,
                                  EC_SIGNATURE_LEN);

    free(c_pub_key);
    free(c_message);
    free(c_sig);

    return ret;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1sig_1normalize(JNIEnv *env,
                                                                jclass clazz,
                                                                jbyteArray sig,
                                                                jbyteArray output) {
    if (sig == NULL) {
        throw_new_crypto_exception(env, "sig is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    unsigned char *c_sig = to_unsigned_char_array(env, sig);
    jsize sig_len = (*env)->GetArrayLength(env, sig);
    unsigned char *c_output = calloc(EC_SIGNATURE_LEN, sizeof(unsigned char));

    int ret = wally_ec_sig_normalize(c_sig, (size_t) sig_len, c_output, EC_SIGNATURE_LEN);

    copy_to_jbyteArray(env, output, c_output, EC_SIGNATURE_LEN);

    free(c_sig);
    free(c_output);

    return ret;

}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_crypto_CryptoJni_wally_1ec_1sig_1to_1der(JNIEnv *env,
                                                              jclass clazz,
                                                              jbyteArray sig,
                                                              jbyteArray output,
                                                              jintArray written) {
    if (sig == NULL) {
        throw_new_crypto_exception(env, "sig is NULL");
        return WALLY_ERROR;
    }

    if (output == NULL) {
        throw_new_crypto_exception(env, "output is NULL");
        return WALLY_ERROR;
    }

    if (written == NULL) {
        throw_new_crypto_exception(env, "written is NULL");
        return WALLY_ERROR;
    }

    jsize written_len = (*env)->GetArrayLength(env, written);
    if (written_len != 1) {
        throw_new_crypto_exception(env, "written len must be 1");
        return WALLY_ERROR;
    }

    unsigned char *c_sig = to_unsigned_char_array(env, sig);
    jsize sig_len = (*env)->GetArrayLength(env, sig);
    unsigned char *c_output = calloc(EC_SIGNATURE_DER_MAX_LEN, sizeof(unsigned char));
    size_t c_written = 0;

    int ret = wally_ec_sig_to_der(c_sig,
                                  (size_t) sig_len,
                                  c_output,
                                  EC_SIGNATURE_DER_MAX_LEN,
                                  &c_written);

    copy_to_jbyteArray(env, output, c_output, EC_SIGNATURE_DER_MAX_LEN);
    copy_to_jintArray(env, written, &c_written, 1);

    free(c_sig);
    free(c_output);

    return ret;
}

