#include <jni.h>
#include "jni-utils.c"
#include <stdbool.h>
#include <wally_transaction.h>
#include <limits.h>
#include <wally_crypto.h>
#include <stdio.h>

// com/bc/libwally/tx/TxException
static bool throw_new_tx_exception(JNIEnv *env, char *msg) {
    return throw_new(env, "com/bc/libwally/tx/TxException", msg);
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1input_1init_1alloc(JNIEnv *env,
                                                            jclass clazz,
                                                            jbyteArray tx_hash,
                                                            jlong utxo_index,
                                                            jlong sequence,
                                                            jbyteArray script,
                                                            jobject witness) {

    if (tx_hash == NULL) {
        throw_new_tx_exception(env, "tx_hash is NULL");
        return NULL;
    }

    if (utxo_index > UINT32_MAX) {
        throw_new_tx_exception(env, "utxo_index is too large");
        return NULL;
    }

    if (sequence > UINT32_MAX) {
        throw_new_tx_exception(env, "sequence is too large");
        return NULL;
    }

    unsigned char *c_tx_hash = to_unsigned_char_array(env, tx_hash);
    jsize tx_hash_len = (*env)->GetArrayLength(env, tx_hash);
    unsigned char *c_script = NULL;
    jsize script_len = 0;
    if (script != NULL) {
        c_script = to_unsigned_char_array(env, script);
        script_len = (*env)->GetArrayLength(env, script);
    }
    struct wally_tx_witness_stack *c_witness = NULL;
    if (witness != NULL) {
        c_witness = to_c_wally_tx_witness_stack(env, witness);
    }

    struct wally_tx_input *output = calloc(1, sizeof(struct wally_tx_input));

    int ret = wally_tx_input_init_alloc(c_tx_hash,
                                        (size_t) tx_hash_len,
                                        (uint32_t) utxo_index,
                                        (uint32_t) sequence,
                                        c_script,
                                        (size_t) script_len,
                                        c_witness,
                                        &output);
    if (ret != WALLY_OK) {
        free(c_tx_hash);
        free(output);
        if (c_script != NULL) {
            free(c_script);
        }
        if (c_witness != NULL) {
            free(c_witness);
        }
        throw_new_tx_exception(env, "wally_tx_input_init_alloc error");
        return NULL;
    }

    jobject result = to_jWallyTxInput(env, output);

    free(c_tx_hash);
    free(output);
    if (c_script != NULL) {
        free(c_script);
    }
    if (c_witness != NULL) {
        free(c_witness);
    }

    return result;

}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1output_1init_1alloc(JNIEnv *env,
                                                             jclass clazz,
                                                             jlong satoshi,
                                                             jbyteArray script) {

    if (script == NULL) {
        throw_new_tx_exception(env, "script is NULL");
        return NULL;
    }

    if (satoshi > UINT64_MAX) {
        throw_new_tx_exception(env, "satoshi is too large");
        return NULL;
    }

    unsigned char *c_script = to_unsigned_char_array(env, script);
    jsize script_len = (*env)->GetArrayLength(env, script);
    struct wally_tx_output *output = calloc(1, sizeof(struct wally_tx_output));

    int ret = wally_tx_output_init_alloc((uint64_t) satoshi,
                                         c_script,
                                         (size_t) script_len,
                                         &output);
    if (ret != WALLY_OK) {
        free(c_script);
        free(output);
        throw_new_tx_exception(env, "wally_tx_output_init_alloc error");
        return NULL;
    }

    jobject result = to_jWallyTxOutput(env, output);

    free(c_script);
    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1witness_1stack_1init_1alloc(JNIEnv *env,
                                                                     jclass clazz,
                                                                     jint allocation_length) {

    struct wally_tx_witness_stack *output = calloc(1, sizeof(struct wally_tx_witness_stack));
    int ret = wally_tx_witness_stack_init_alloc((size_t) allocation_length, &output);
    if (ret != WALLY_OK) {
        free(output);
        throw_new_tx_exception(env, "wally_tx_witness_stack_init_alloc error");
        return NULL;
    }

    jobject result = to_jWallyTxWitnessStack(env, output);

    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1witness_1stack_1set(JNIEnv *env,
                                                             jclass clazz,
                                                             jobject stack,
                                                             jint index,
                                                             jbyteArray witness) {

    if (stack == NULL) {
        throw_new_tx_exception(env, "stack is NULL");
        return NULL;
    }

    if (witness == NULL) {
        throw_new_tx_exception(env, "witness is NULL");
        return NULL;
    }

    struct wally_tx_witness_stack *c_stack = to_c_wally_tx_witness_stack(env, stack);
    unsigned char *c_witness = to_unsigned_char_array(env, witness);
    jsize witness_len = (*env)->GetArrayLength(env, witness);

    int ret = wally_tx_witness_stack_set(c_stack, (size_t) index, c_witness, (size_t) witness_len);
    if (ret != WALLY_OK) {
        free(c_stack);
        free(c_witness);
        throw_new_tx_exception(env, "wally_tx_witness_stack_set error");
        return NULL;
    }

    jobject result = to_jWallyTxWitnessStack(env, c_stack);

    free(c_stack);
    free(c_witness);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1from_1bytes(JNIEnv *env,
                                                     jclass clazz,
                                                     jbyteArray bytes,
                                                     jlong flags) {

    if (bytes == NULL) {
        throw_new_tx_exception(env, "bytes is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_tx_exception(env, "flags is too large");
        return NULL;
    }

    unsigned char *c_bytes = to_unsigned_char_array(env, bytes);
    jsize bytes_len = (*env)->GetArrayLength(env, bytes);
    struct wally_tx *output = calloc(1, sizeof(struct wally_tx));

    int ret = wally_tx_from_bytes(c_bytes, (size_t) bytes_len, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_bytes);
        free(output);
        throw_new_tx_exception(env, "wally_tx_from_bytes error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, output);

    free(c_bytes);
    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1init_1alloc(JNIEnv *env,
                                                     jclass clazz,
                                                     jlong version,
                                                     jlong locktime,
                                                     jint inputs_alloc_len,
                                                     jint outputs_alloc_len) {
    if (version > UINT32_MAX) {
        throw_new_tx_exception(env, "version is too large");
        return NULL;
    }

    if (locktime > UINT32_MAX) {
        throw_new_tx_exception(env, "locktime is too large");
        return NULL;
    }

    struct wally_tx *output = calloc(1, sizeof(struct wally_tx));

    int ret = wally_tx_init_alloc((uint32_t) version,
                                  (uint32_t) locktime,
                                  (size_t) inputs_alloc_len,
                                  (size_t) outputs_alloc_len,
                                  &output);

    if (ret != WALLY_OK) {
        free(output);
        throw_new_tx_exception(env, "wally_tx_init_alloc error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, output);

    free(output);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1add_1input(JNIEnv *env,
                                                    jclass clazz,
                                                    jobject wally_tx,
                                                    jobject input) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (input == NULL) {
        throw_new_tx_exception(env, "input is NULL");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    struct wally_tx_input *c_tx_input = to_c_wally_tx_input(env, input);

    int ret = wally_tx_add_input(c_tx, c_tx_input);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(c_tx_input);
        throw_new_tx_exception(env, "wally_tx_add_input error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, c_tx);

    free(c_tx);
    free(c_tx_input);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1add_1output(JNIEnv *env,
                                                     jclass clazz,
                                                     jobject wally_tx,
                                                     jobject output) {
    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (output == NULL) {
        throw_new_tx_exception(env, "output is NULL");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    struct wally_tx_output *c_tx_output = to_c_wally_tx_output(env, output);

    int ret = wally_tx_add_output(c_tx, c_tx_output);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(c_tx_output);
        throw_new_tx_exception(env, "wally_tx_add_output error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, c_tx);

    free(c_tx);
    free(c_tx_output);

    return result;
}

JNIEXPORT jstring JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1to_1hex(JNIEnv *env,
                                                 jclass clazz,
                                                 jobject wally_tx,
                                                 jlong flags) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_tx_exception(env, "flags is too large");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    char *output = "";

    int ret = wally_tx_to_hex(c_tx, (uint32_t) flags, &output);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(output);
        throw_new_tx_exception(env, "wally_tx_to_hex error");
        return NULL;
    }

    jstring result = to_jstring(env, output);

    free(c_tx);
    free(output);

    return result;
}

JNIEXPORT jlong JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1get_1total_1output_1satoshi(JNIEnv *env,
                                                                     jclass clazz,
                                                                     jobject wally_tx) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return JNI_ERR;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    uint64_t output = 0;

    int ret = wally_tx_get_total_output_satoshi(c_tx, &output);
    if (ret != WALLY_OK) {
        free(c_tx);
        throw_new_tx_exception(env, "wally_tx_get_total_output_satoshi error");
        return JNI_ERR;
    }

    jlong result = (jlong) output;

    free(c_tx);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1set_1input_1script(JNIEnv *env,
                                                            jclass clazz,
                                                            jobject wally_tx,
                                                            jint index,
                                                            jbyteArray script) {
    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (script == NULL) {
        throw_new_tx_exception(env, "script is NULL");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    unsigned char *c_script = to_unsigned_char_array(env, script);
    jsize script_len = (*env)->GetArrayLength(env, script);

    int ret = wally_tx_set_input_script(c_tx, (size_t) index, c_script, (size_t) script_len);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(c_script);
        throw_new_tx_exception(env, "wally_tx_set_input_script error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, c_tx);

    free(c_tx);
    free(c_script);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1get_1vsize(JNIEnv *env, jclass clazz, jobject wally_tx) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return JNI_ERR;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    size_t written = 0;

    int ret = wally_tx_get_vsize(c_tx, &written);
    if (ret != WALLY_OK) {
        free(c_tx);
        throw_new_tx_exception(env, "wally_tx_get_vsize error");
        return ret;
    }

    jint result = (jint) written;

    free(c_tx);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1get_1btc_1signature_1hash(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jobject wally_tx,
                                                                   jint index,
                                                                   jbyteArray script,
                                                                   jlong satoshi,
                                                                   jlong sig_hash,
                                                                   jlong flags,
                                                                   jbyteArray output) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return JNI_ERR;
    }

    if (script == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return JNI_ERR;
    }

    if (output == NULL) {
        throw_new_tx_exception(env, "output is NULL");
        return JNI_ERR;
    }

    if (satoshi > UINT64_MAX) {
        throw_new_tx_exception(env, "satoshi is too large");
        return JNI_ERR;
    }

    if (sig_hash > UINT32_MAX) {
        throw_new_tx_exception(env, "sig_hash is too large");
        return JNI_ERR;
    }

    if (flags > UINT32_MAX) {
        throw_new_tx_exception(env, "flags is too large");
        return JNI_ERR;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    unsigned char *c_script = to_unsigned_char_array(env, script);
    jsize script_len = (*env)->GetArrayLength(env, script);
    unsigned char *c_output = calloc(SHA256_LEN, sizeof(unsigned char));

    int ret = wally_tx_get_btc_signature_hash(c_tx,
                                              (size_t) index,
                                              c_script,
                                              (size_t) script_len,
                                              (uint64_t) satoshi,
                                              (uint32_t) sig_hash,
                                              (uint32_t) flags,
                                              c_output,
                                              SHA256_LEN);

    if (ret != WALLY_OK) {
        free(c_tx);
        free(c_script);
        free(c_output);
        return ret;
    }

    copy_to_jbyteArray(env, output, c_output, SHA256_LEN);

    free(c_tx);
    free(c_script);
    free(c_output);

    return WALLY_OK;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1set_1input_1witness(JNIEnv *env,
                                                             jclass clazz,
                                                             jobject wally_tx,
                                                             jint index,
                                                             jobject stack) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (stack == NULL) {
        throw_new_tx_exception(env, "stack is NULL");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    struct wally_tx_witness_stack *c_witness = to_c_wally_tx_witness_stack(env, stack);

    int ret = wally_tx_set_input_witness(c_tx, (size_t) index, c_witness);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(c_witness);
        throw_new_tx_exception(env, "wally_tx_set_input_witness error");
        return NULL;
    }

    jobject result = to_jWallyTx(env, c_tx);

    free(c_tx);
    free(c_witness);

    return result;
}

JNIEXPORT jobject JNICALL
Java_com_bc_libwally_tx_TxJni_wally_1tx_1clone_1alloc(JNIEnv *env,
                                                      jclass clazz,
                                                      jobject wally_tx,
                                                      jlong flags) {

    if (wally_tx == NULL) {
        throw_new_tx_exception(env, "wally_tx is NULL");
        return NULL;
    }

    if (flags > UINT32_MAX) {
        throw_new_tx_exception(env, "flags is too large");
        return NULL;
    }

    struct wally_tx *c_tx = to_c_wally_tx(env, wally_tx);
    struct wally_tx *cloned_tx = (struct wally_tx *) calloc(1, sizeof(struct wally_tx));

    int ret = wally_tx_clone_alloc(c_tx, (uint32_t) flags, &cloned_tx);
    if (ret != WALLY_OK) {
        free(c_tx);
        free(cloned_tx);
        throw_new_tx_exception(env, "wally_tx_clone_alloc error");
        return NULL;
    }

    jobject j_tx = to_jWallyTx(env, cloned_tx);

    free(c_tx);
    free(cloned_tx);

    return j_tx;
}