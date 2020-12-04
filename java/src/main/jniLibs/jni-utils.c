#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdint.h>
#include <limits.h>
#include <wally_address.h>
#include <wally_bip32.h>

// -------------- Common JNI methods ---------------- //
static jclass find_jclass(JNIEnv *env, char *className) {
    jclass clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        fprintf(stderr, "JNIEnv::FindClass error");
        return NULL;
    }

    return clazz;
}

static jmethodID get_methodID(JNIEnv *env, jclass clazz, char *methodName, char *methodSig) {
    jmethodID methodID = (*env)->GetMethodID(env, clazz, methodName, methodSig);
    if (methodID == NULL) {
        fprintf(stderr, "JNIEnv::GetMethodID error");
        return NULL;
    }

    return methodID;
}

static bool throw_new(JNIEnv *env, char *className, char *msg) {
    jclass clazz = find_jclass(env, className);
    const jint rs = (*env)->ThrowNew(env, clazz, msg);
    if (rs != JNI_OK) {
        fprintf(stderr, "throw_new error");
        return (*env)->ExceptionCheck(env);
    }

    return true;
}

static unsigned char *to_unsigned_char_array(JNIEnv *env, jbyteArray array) {
    jint count = (*env)->GetArrayLength(env, array);
    jbyte *elements = (*env)->GetByteArrayElements(env, array, JNI_FALSE);
    unsigned char *ret = (unsigned char *) calloc(count, sizeof(unsigned char));
    memcpy(ret, elements, count);
    (*env)->ReleaseByteArrayElements(env, array, elements, JNI_ABORT);
    return ret;
}

static unsigned char **to_unsigned_char_2dimension_array(JNIEnv *env, jobjectArray array) {
    jint count = (*env)->GetArrayLength(env, array);
    unsigned char **ret = (uint8_t **) calloc(count, sizeof(unsigned char *));
    for (int i = 0; i < count; i++) {
        jbyteArray obj = (jbyteArray) (*env)->GetObjectArrayElement(env, array, i);
        ret[i] = to_unsigned_char_array(env, obj);
    }
    return ret;
}

static uint32_t *to_uint32_t_array(JNIEnv *env, jlongArray array) {
    jint count = (*env)->GetArrayLength(env, array);
    jlong *elements = (*env)->GetLongArrayElements(env, array, JNI_FALSE);
    uint32_t *ret = (uint32_t *) calloc(count, sizeof(uint32_t));
    for (jint i = 0; i < count; i++) {
        ret[i] = (uint32_t) elements[i];
    }
    (*env)->ReleaseLongArrayElements(env, array, elements, JNI_ABORT);
    return ret;
}

static void
copy_to_jbyteArray(JNIEnv *env, jbyteArray dst, const unsigned char *src, size_t src_len) {
    jint count = (*env)->GetArrayLength(env, dst);
    if (count != src_len) {
        fprintf(stderr, "the length between src and dst is different");
        return;
    }

    jbyte *jbytes = (*env)->GetByteArrayElements(env, dst, JNI_FALSE);
    for (int i = 0; i < count; ++i) {
        jbytes[i] = src[i];
    }
    (*env)->ReleaseByteArrayElements(env, dst, jbytes, 0);
}

static void copy_to_jintArray(JNIEnv *env, jintArray dst, const size_t *src, size_t src_len) {
    jint count = (*env)->GetArrayLength(env, dst);
    if (count != src_len) {
        fprintf(stderr, "the length between src and dst is different");
        return;
    }

    jint *jints = (*env)->GetIntArrayElements(env, dst, JNI_FALSE);
    for (int i = 0; i < count; ++i) {
        jints[i] = src[i];
    }
    (*env)->ReleaseIntArrayElements(env, dst, jints, 0);
}

static jbyteArray create_jbyteArray(JNIEnv *env, const unsigned char *src, size_t src_len) {
    jbyteArray dst = (*env)->NewByteArray(env, src_len);
    copy_to_jbyteArray(env, dst, src, src_len);
    return dst;
}

static jobject to_jobject(JNIEnv *env, void *ptr) {
    jclass clazz = find_jclass(env, "com/bc/libwally/NativeWrapper$JniObject");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID construct_mid = (*env)->GetMethodID(env, clazz, "<init>", "(J)V");
    if (construct_mid == NULL) {
        return NULL;
    }

    return (*env)->NewObject(env, clazz, construct_mid, (jlong) (uintptr_t) ptr);
}

static void *to_c_obj_ptr(JNIEnv *env, jobject obj) {
    jclass clazz = find_jclass(env, "com/bc/libwally/NativeWrapper$JniObject");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID get_ptr_mid = (*env)->GetMethodID(env, clazz, "getPtr", "()J");
    if (get_ptr_mid == NULL) {
        return NULL;
    }

    void *ret;
    ret = (void *) (uintptr_t) ((*env)->CallLongMethod(env, obj, get_ptr_mid));
    return ret;
}

static bool verify_network(uint32_t network) {
    if (network == WALLY_NETWORK_BITCOIN_MAINNET || network == WALLY_NETWORK_BITCOIN_TESTNET ||
        network == WALLY_NETWORK_LIQUID || network == WALLY_NETWORK_LIQUID_REGTEST) {
        return true;
    }
    return false;
}

// -------------- END Common JNI methods ---------------- //

// -------------- Bip32 JNI methods --------------------//

static jobject to_jHDKey(JNIEnv *env, struct ext_key *key) {
    jclass clazz = find_jclass(env, "com/bc/libwally/bip32/WallyHDKey");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_mid = get_methodID(env, clazz, "<init>", "([B[BS[B[BJ[BJ[B[B)V");
    if (constructor_mid == NULL) {
        return NULL;
    }


    jbyteArray j_chain_code = create_jbyteArray(env, (uint8_t *) key->chain_code, 32);
    jbyteArray j_parent160 = create_jbyteArray(env, (uint8_t *) key->parent160, 20);
    jbyteArray j_pad1 = create_jbyteArray(env, (uint8_t *) key->pad1, 10);
    jbyteArray j_priv_key = create_jbyteArray(env, (uint8_t *) key->priv_key, 33);
    jbyteArray j_hash160 = create_jbyteArray(env, (uint8_t *) key->hash160, 20);
    jbyteArray j_pad2 = create_jbyteArray(env, (uint8_t *) key->pad2, 3);
    jbyteArray j_pub_key = create_jbyteArray(env, (uint8_t *) key->pub_key, 33);

    jobject result = (*env)->NewObject(env, clazz, constructor_mid, j_chain_code,
                                       j_parent160, (jshort) key->depth, j_pad1, j_priv_key,
                                       (jlong) key->child_num, j_hash160, (jlong) key->version,
                                       j_pad2,
                                       j_pub_key);

    return result;
}

static struct ext_key *to_cHDKey(JNIEnv *env, jobject jHDKey) {
    jclass clazz = find_jclass(env, "com/bc/libwally/bip32/WallyHDKey");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID get_chain_code_mid = get_methodID(env, clazz, "getChainCode", "()[B");
    jbyteArray j_chain_code = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                    jHDKey,
                                                                    get_chain_code_mid);
    jmethodID get_parent160_mid = get_methodID(env, clazz, "getParent160", "()[B");
    jbyteArray j_parent160 = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_parent160_mid);
    jmethodID get_depth_mid = get_methodID(env, clazz, "getDepth", "()S");
    jshort j_depth = (*env)->CallShortMethod(env, jHDKey, get_depth_mid);
    jmethodID get_pad1_mid = get_methodID(env, clazz, "getPad1", "()[B");
    jbyteArray j_pad1 = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_pad1_mid);
    jmethodID get_priv_key_mid = get_methodID(env, clazz, "getPrivKey", "()[B");
    jbyteArray j_priv_key = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_priv_key_mid);
    jmethodID get_child_num_mid = get_methodID(env, clazz, "getChildNum", "()J");
    jlong j_child_num = (*env)->CallLongMethod(env, jHDKey, get_child_num_mid);
    jmethodID get_hash160_mid = get_methodID(env, clazz, "getHash160", "()[B");
    jbyteArray j_hash160 = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_hash160_mid);
    jmethodID get_version_mid = get_methodID(env, clazz, "getVersion", "()J");
    jlong j_version = (*env)->CallLongMethod(env, jHDKey, get_version_mid);
    jmethodID get_pad2_mid = get_methodID(env, clazz, "getPad2", "()[B");
    jbyteArray j_pad2 = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_pad2_mid);
    jmethodID get_pub_key_mid = get_methodID(env, clazz, "getPubKey", "()[B");
    jbyteArray j_pub_key = (jbyteArray) (*env)->CallObjectMethod(env, jHDKey, get_pub_key_mid);

    struct ext_key *key = (struct ext_key *) malloc(sizeof(struct ext_key));
    memcpy(key->chain_code, (unsigned char *) to_unsigned_char_array(env, j_chain_code), 32);
    memcpy(key->parent160, (unsigned char *) to_unsigned_char_array(env, j_parent160), 20);
    key->depth = (uint8_t) j_depth;
    memcpy(key->pad1, (unsigned char *) to_unsigned_char_array(env, j_pad1), 10);
    memcpy(key->priv_key, (unsigned char *) to_unsigned_char_array(env, j_priv_key), 33);
    key->child_num = (uint32_t) j_child_num;
    memcpy(key->hash160, (unsigned char *) to_unsigned_char_array(env, j_hash160), 20);
    key->version = (uint32_t) j_version;
    memcpy(key->pad2, (unsigned char *) to_unsigned_char_array(env, j_pad2), 3);
    memcpy(key->pub_key, (unsigned char *) to_unsigned_char_array(env, j_pub_key), 33);
    return key;
}

// -------------- END Bip32 JNI methods --------------------//