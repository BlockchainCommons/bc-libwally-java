#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdint.h>
#include <limits.h>
#include <wally_address.h>
#include <wally_bip32.h>
#include <wally_transaction.h>
#include <stdio.h>

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
    jsize count = (*env)->GetArrayLength(env, array);
    jbyte *elements = (*env)->GetByteArrayElements(env, array, JNI_FALSE);
    unsigned char *ret = (unsigned char *) calloc(count, sizeof(unsigned char));
    memcpy(ret, elements, count);
    (*env)->ReleaseByteArrayElements(env, array, elements, JNI_ABORT);
    return ret;
}

static unsigned char **to_unsigned_char_2dimension_array(JNIEnv *env, jobjectArray array) {
    jsize count = (*env)->GetArrayLength(env, array);
    unsigned char **ret = (uint8_t **) calloc(count, sizeof(unsigned char *));
    for (int i = 0; i < count; i++) {
        jbyteArray obj = (jbyteArray) (*env)->GetObjectArrayElement(env, array, i);
        *(ret + i) = to_unsigned_char_array(env, obj);
    }
    return ret;
}

static uint32_t *to_uint32_t_array(JNIEnv *env, jlongArray array) {
    jsize count = (*env)->GetArrayLength(env, array);
    jlong *elements = (*env)->GetLongArrayElements(env, array, JNI_FALSE);
    uint32_t *ret = (uint32_t *) calloc(count, sizeof(uint32_t));
    for (int i = 0; i < count; i++) {
        *(ret + i) = (uint32_t) *(elements + i);
    }
    (*env)->ReleaseLongArrayElements(env, array, elements, JNI_ABORT);
    return ret;
}

static void
copy_to_jbyteArray(JNIEnv *env, jbyteArray dst, const unsigned char *src, size_t src_len) {
    jsize count = (*env)->GetArrayLength(env, dst);
    if (count != src_len) {
        fprintf(stderr, "the length between src and dst is different");
        return;
    }

    jbyte *jbytes = (*env)->GetByteArrayElements(env, dst, JNI_FALSE);
    for (int i = 0; i < count; ++i) {
        *(jbytes + i) = *(src + i);
    }
    (*env)->ReleaseByteArrayElements(env, dst, jbytes, 0);
}

static void copy_to_jintArray(JNIEnv *env, jintArray dst, const size_t *src, size_t src_len) {
    jsize count = (*env)->GetArrayLength(env, dst);
    if (count != src_len) {
        fprintf(stderr, "the length between src and dst is different");
        return;
    }

    jint *jints = (*env)->GetIntArrayElements(env, dst, JNI_FALSE);
    for (int i = 0; i < count; ++i) {
        *(jints + i) = *(src + i);
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

    return (*env)->NewObject(env, clazz, construct_mid, (jlong) (uintptr_t)
            ptr);
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

static jstring to_jstring(JNIEnv *env, char *input) {
    return (*env)->NewStringUTF(env, input);
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

static jobject to_jWallyHDKey(JNIEnv *env, struct ext_key *key) {
    jclass clazz = find_jclass(env, "com/bc/libwally/bip32/WallyHDKey");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_mid = get_methodID(env, clazz, "<init>", "([B[BS[B[BJ[BJ[B[B)V");
    if (constructor_mid == NULL) {
        return NULL;
    }


    jbyteArray j_chain_code = create_jbyteArray(env, key->chain_code, 32);
    jbyteArray j_parent160 = create_jbyteArray(env, key->parent160, 20);
    jbyteArray j_pad1 = create_jbyteArray(env, key->pad1, 10);
    jbyteArray j_priv_key = create_jbyteArray(env, key->priv_key, 33);
    jbyteArray j_hash160 = create_jbyteArray(env, key->hash160, 20);
    jbyteArray j_pad2 = create_jbyteArray(env, key->pad2, 3);
    jbyteArray j_pub_key = create_jbyteArray(env, key->pub_key, 33);

    jobject result = (*env)->NewObject(env, clazz, constructor_mid, j_chain_code,
                                       j_parent160, (jshort) key->depth, j_pad1, j_priv_key,
                                       (jlong) key->child_num, j_hash160, (jlong) key->version,
                                       j_pad2,
                                       j_pub_key);

    return result;
}

static struct ext_key *to_c_ext_key(JNIEnv *env, jobject jHDKey) {
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

    struct ext_key *key = (struct ext_key *) calloc(1, sizeof(struct ext_key));
    unsigned char *c_chain_code = to_unsigned_char_array(env, j_chain_code);
    memcpy(key->chain_code, c_chain_code, 32);
    free(c_chain_code);

    unsigned char *c_parent160 = to_unsigned_char_array(env, j_parent160);
    memcpy(key->parent160, c_parent160, 20);
    free(c_parent160);

    key->depth = (uint8_t) j_depth;

    unsigned char *c_pad1 = to_unsigned_char_array(env, j_pad1);
    memcpy(key->pad1, c_pad1, 10);
    free(c_pad1);

    unsigned char *c_priv_key = to_unsigned_char_array(env, j_priv_key);
    memcpy(key->priv_key, c_priv_key, 33);
    free(c_priv_key);

    key->child_num = (uint32_t) j_child_num;

    unsigned char *c_hash160 = to_unsigned_char_array(env, j_hash160);
    memcpy(key->hash160, c_hash160, 20);
    free(c_hash160);

    key->version = (uint32_t) j_version;

    unsigned char *c_pad2 = to_unsigned_char_array(env, j_pad2);
    memcpy(key->pad2, c_pad2, 3);
    free(c_pad2);

    unsigned char *c_pub_key = to_unsigned_char_array(env, j_pub_key);
    memcpy(key->pub_key, c_pub_key, 33);
    free(c_pub_key);

    return key;
}

// -------------- END Bip32 JNI methods --------------------//

// -------------- Tx JNI methods -----------------------//

static jobject to_jWallyTxWitnessItem(JNIEnv *env, struct wally_tx_witness_item *item) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxWitnessStack$WallyTxWitnessItem");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_mid = get_methodID(env, clazz, "<init>", "([B)V");
    if (constructor_mid == NULL) {
        return NULL;
    }

    jbyteArray j_witness = create_jbyteArray(env, item->witness, item->witness_len);
    return (*env)->NewObject(env, clazz, constructor_mid, j_witness);
}

static struct wally_tx_witness_item *
to_c_wally_tx_witness_item(JNIEnv *env, jobject jWallyTxWitnessItem) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxWitnessStack$WallyTxWitnessItem");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID get_witness_mid = get_methodID(env, clazz, "getWitness", "()[B");
    jbyteArray j_witness = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                 jWallyTxWitnessItem,
                                                                 get_witness_mid);

    struct wally_tx_witness_item *item = (struct wally_tx_witness_item *) calloc(1,
                                                                                 sizeof(struct wally_tx_witness_item));

    if (j_witness != NULL) {
        jsize len = (*env)->GetArrayLength(env, j_witness);
        item->witness_len = (size_t) len;

        unsigned char *c_witness = to_unsigned_char_array(env, j_witness);
        item->witness = c_witness;
    }

    return item;
}

static jobject to_jWallyTxWitnessStack(JNIEnv *env, struct wally_tx_witness_stack *stack) {
    jclass stack_clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxWitnessStack");
    if (stack_clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_stack_mid = get_methodID(env,
                                                   stack_clazz,
                                                   "<init>",
                                                   "([Lcom/bc/libwally/tx/WallyTxWitnessStack$WallyTxWitnessItem;I)V");
    if (constructor_stack_mid == NULL) {
        return NULL;
    }

    jclass item_clazz = find_jclass(env,
                                    "com/bc/libwally/tx/WallyTxWitnessStack$WallyTxWitnessItem");
    if (item_clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_item_mid = get_methodID(env, item_clazz, "<init>", "([B)V");
    if (constructor_item_mid == NULL) {
        return NULL;
    }

    size_t num_items = stack->num_items;
    jobjectArray j_witness_items = (*env)->NewObjectArray(env,
                                                          (jsize) num_items,
                                                          item_clazz,
                                                          NULL);

    for (int i = 0; i < num_items; i++) {
        jobject j_item = to_jWallyTxWitnessItem(env, stack->items + i);
        (*env)->SetObjectArrayElement(env, j_witness_items, i, j_item);
    }

    jobject j_stack = (*env)->NewObject(env,
                                        stack_clazz,
                                        constructor_stack_mid,
                                        j_witness_items,
                                        (jint) stack->items_allocation_len);

    return j_stack;
}

static struct wally_tx_witness_stack *
to_c_wally_tx_witness_stack(JNIEnv *env, jobject jWallyTxWitnessStack) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxWitnessStack");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID get_tx_witness_item_mid = get_methodID(env,
                                                     clazz,
                                                     "getItems",
                                                     "()[Lcom/bc/libwally/tx/WallyTxWitnessStack$WallyTxWitnessItem;");

    jobjectArray j_items = (jobjectArray) (*env)->CallObjectMethod(env,
                                                                   jWallyTxWitnessStack,
                                                                   get_tx_witness_item_mid);
    jsize j_num_items = (*env)->GetArrayLength(env, j_items);
    jmethodID get_items_alloc_len_mid = get_methodID(env, clazz, "getItemsAllocLength", "()I");
    size_t j_items_alloc_len = (size_t) (*env)->CallIntMethod(env,
                                                              jWallyTxWitnessStack,
                                                              get_items_alloc_len_mid);

    struct wally_tx_witness_stack *stack = (struct wally_tx_witness_stack *) calloc(1,
                                                                                    sizeof(struct wally_tx_witness_stack));
    stack->num_items = (size_t) j_num_items;
    stack->items_allocation_len = j_items_alloc_len;
    struct wally_tx_witness_item *items = calloc(stack->items_allocation_len,
                                                 sizeof(struct wally_tx_witness_item));

    for (int i = 0; i < j_num_items; ++i) {
        jobject item = (*env)->GetObjectArrayElement(env, j_items, i);
        struct wally_tx_witness_item *c_item = to_c_wally_tx_witness_item(env, item);
        *(items + i) = *c_item;
    }

    stack->items = items;
    return stack;
}

static jobject to_jWallyTxOutput(JNIEnv *env, struct wally_tx_output *output) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxOutput");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_mid = get_methodID(env, clazz, "<init>", "(J[BS)V");
    if (constructor_mid == NULL) {
        return NULL;
    }

    jlong j_amount = (jlong) output->satoshi;
    jbyteArray j_script = create_jbyteArray(env, output->script, output->script_len);
    jshort j_features = (jshort) output->features;
    return (*env)->NewObject(env,
                             clazz,
                             constructor_mid,
                             j_amount,
                             j_script,
                             j_features);
}

static struct wally_tx_output *to_c_wally_tx_output(JNIEnv *env, jobject jWallyTxOutput) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxOutput");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID get_amount_mid = get_methodID(env, clazz, "getSatoshi", "()J");
    jlong j_amount = (*env)->CallLongMethod(env, jWallyTxOutput, get_amount_mid);
    jmethodID get_script_mid = get_methodID(env, clazz, "getScript", "()[B");
    jbyteArray j_script = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                jWallyTxOutput,
                                                                get_script_mid);
    jmethodID get_features_mid = get_methodID(env, clazz, "getFeatures", "()S");
    jshort j_features = (*env)->CallShortMethod(env, jWallyTxOutput, get_features_mid);

    struct wally_tx_output *output = calloc(1, sizeof(struct wally_tx_output));
    output->satoshi = (uint64_t) j_amount;
    unsigned char *c_script = to_unsigned_char_array(env, j_script);
    jsize script_len = (*env)->GetArrayLength(env, j_script);
    output->script = c_script;
    output->script_len = (size_t) script_len;
    output->features = (uint8_t) j_features;
    return output;
}

static jobject to_jWallyTxInput(JNIEnv *env, struct wally_tx_input *input) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxInput");
    if (clazz == NULL) {
        return NULL;
    }

    jmethodID constructor_mid = get_methodID(env,
                                             clazz,
                                             "<init>",
                                             "([BJJ[BLcom/bc/libwally/tx/WallyTxWitnessStack;S)V");
    if (constructor_mid == NULL) {
        return NULL;
    }

    jbyteArray j_tx_hash = create_jbyteArray(env, (unsigned char *) input->txhash, 32);
    jlong j_index = (jlong) input->index;
    jlong j_sequence = (jlong) input->sequence;
    jbyteArray j_script = NULL;
    if (input->script != NULL) {
        j_script = create_jbyteArray(env,
                                     input->script,
                                     input->script_len);
    }

    jobject j_witnessStack = NULL;
    if (input->witness != NULL) {
        j_witnessStack = to_jWallyTxWitnessStack(env, input->witness);
    }

    jshort j_feafures = (jshort) input->features;

    return (*env)->NewObject(env,
                             clazz,
                             constructor_mid,
                             j_tx_hash,
                             j_index,
                             j_sequence,
                             j_script,
                             j_witnessStack,
                             j_feafures);
}

static struct wally_tx_input *to_c_wally_tx_input(JNIEnv *env, jobject jWallyTxInput) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxInput");
    if (clazz == NULL) {
        return NULL;
    }

    // get values from Java objects
    jmethodID get_tx_hash_mid = get_methodID(env, clazz, "getTxHash", "()[B");
    jbyteArray j_tx_hash = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                 jWallyTxInput,
                                                                 get_tx_hash_mid);
    jmethodID get_index_mid = get_methodID(env, clazz, "getIndex", "()J");
    jlong j_index = (*env)->CallLongMethod(env, jWallyTxInput, get_index_mid);
    jmethodID get_sequence_mid = get_methodID(env, clazz, "getSequence", "()J");
    jlong j_sequence = (*env)->CallLongMethod(env, jWallyTxInput, get_sequence_mid);
    jmethodID get_script_mid = get_methodID(env, clazz, "getScript", "()[B");
    jbyteArray j_script = (jbyteArray) (*env)->CallObjectMethod(env,
                                                                jWallyTxInput,
                                                                get_script_mid);
    jmethodID get_witness_mid = get_methodID(env,
                                             clazz,
                                             "getWitness",
                                             "()Lcom/bc/libwally/tx/WallyTxWitnessStack;");
    jobject j_witness = (*env)->CallObjectMethod(env, jWallyTxInput, get_witness_mid);
    jmethodID get_features_mid = get_methodID(env, clazz, "getFeatures", "()S");
    jshort j_features = (*env)->CallShortMethod(env, jWallyTxInput, get_features_mid);

    // assign to C struct
    struct wally_tx_input *input = (struct wally_tx_input *) calloc(1,
                                                                    sizeof(struct wally_tx_input));

    if (j_tx_hash != NULL) {
        unsigned char *c_tx_hash = to_unsigned_char_array(env, j_tx_hash);
        jsize hash_len = (*env)->GetArrayLength(env, j_tx_hash);
        memcpy(input->txhash, c_tx_hash, (size_t) hash_len);
        free(c_tx_hash);
    }

    input->index = (uint32_t) j_index;
    input->sequence = (uint32_t) j_sequence;

    if (j_script != NULL) {
        jsize script_len = (*env)->GetArrayLength(env, j_script);
        unsigned char *c_script = to_unsigned_char_array(env, j_script);
        input->script = c_script;
        input->script_len = (size_t) script_len;
    }

    if (j_witness != NULL) {
        struct wally_tx_witness_stack *witness = to_c_wally_tx_witness_stack(env, j_witness);
        input->witness = witness;
    }

    input->features = (uint8_t) j_features;

    return input;
}

static jobject to_jWallyTx(JNIEnv *env, struct wally_tx *tx) {
    jclass wally_tx_clazz = find_jclass(env, "com/bc/libwally/tx/WallyTx");
    if (wally_tx_clazz == NULL) {
        return NULL;
    }

    jmethodID wally_tx_constructor_mid = get_methodID(env,
                                                      wally_tx_clazz,
                                                      "<init>",
                                                      "(JJ[Lcom/bc/libwally/tx/WallyTxInput;[Lcom/bc/libwally/tx/WallyTxOutput;II)V");
    if (wally_tx_constructor_mid == NULL) {
        return NULL;
    }

    jclass wally_tx_input_clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxInput");
    if (wally_tx_input_clazz == NULL) {
        return NULL;
    }

    jclass wally_tx_output_clazz = find_jclass(env, "com/bc/libwally/tx/WallyTxOutput");
    if (wally_tx_output_clazz == NULL) {
        return NULL;
    }

    jlong j_version = (jlong) tx->version;
    jlong j_locktime = (jlong) tx->locktime;
    jint j_inputs_alloc_len = tx->inputs_allocation_len;
    jint j_output_alloc_len = tx->outputs_allocation_len;

    jsize tx_input_count = (jsize) tx->num_inputs;
    jobjectArray j_tx_inputs = (*env)->NewObjectArray(env,
                                                      tx_input_count,
                                                      wally_tx_input_clazz,
                                                      NULL);
    for (int i = 0; i < tx_input_count; ++i) {
        jobject input = to_jWallyTxInput(env, tx->inputs + i);
        (*env)->SetObjectArrayElement(env, j_tx_inputs, i, input);
    }

    jsize tx_output_count = (jsize) tx->num_outputs;
    jobjectArray j_tx_outputs = (*env)->NewObjectArray(env,
                                                       tx_output_count,
                                                       wally_tx_output_clazz,
                                                       NULL);
    for (int i = 0; i < tx_output_count; ++i) {
        jobject output = to_jWallyTxOutput(env, tx->outputs + i);
        (*env)->SetObjectArrayElement(env, j_tx_outputs, i, output);
    }

    return (*env)->NewObject(env,
                             wally_tx_clazz,
                             wally_tx_constructor_mid,
                             j_version,
                             j_locktime,
                             j_tx_inputs,
                             j_tx_outputs,
                             j_inputs_alloc_len,
                             j_output_alloc_len);

}

static struct wally_tx *to_c_wally_tx(JNIEnv *env, jobject jWallyTx) {
    jclass clazz = find_jclass(env, "com/bc/libwally/tx/WallyTx");
    if (clazz == NULL) {
        return NULL;
    }

    // get values from Java object
    jmethodID get_version_mid = get_methodID(env, clazz, "getVersion", "()J");
    jlong j_version = (*env)->CallLongMethod(env, jWallyTx, get_version_mid);
    jmethodID get_lock_time_mid = get_methodID(env, clazz, "getLocktime", "()J");
    jlong j_lock_time = (*env)->CallLongMethod(env, jWallyTx, get_lock_time_mid);
    jmethodID get_inputs_mid = get_methodID(env,
                                            clazz,
                                            "getInputs",
                                            "()[Lcom/bc/libwally/tx/WallyTxInput;");
    jobjectArray j_inputs = (jobjectArray) (*env)->CallObjectMethod(env, jWallyTx, get_inputs_mid);
    jmethodID get_outputs_mid = get_methodID(env,
                                             clazz,
                                             "getOutputs",
                                             "()[Lcom/bc/libwally/tx/WallyTxOutput;");
    jobjectArray j_outputs = (jobjectArray) (*env)->CallObjectMethod(env,
                                                                     jWallyTx,
                                                                     get_outputs_mid);
    jmethodID get_inputs_alloc_len_mid = get_methodID(env, clazz, "getInputsAllocLength", "()I");
    jint j_inputs_alloc_len = (*env)->CallIntMethod(env, jWallyTx, get_inputs_alloc_len_mid);
    jmethodID get_output_alloc_len_mid = get_methodID(env, clazz, "getOutputsAllocLength", "()I");
    jint j_outputs_alloc_len = (*env)->CallIntMethod(env, jWallyTx, get_output_alloc_len_mid);

    // assign to C struct
    struct wally_tx *tx = (struct wally_tx *) calloc(1, sizeof(struct wally_tx));
    tx->version = (uint32_t) j_version;
    tx->locktime = (uint32_t) j_lock_time;
    tx->inputs_allocation_len = (size_t) j_inputs_alloc_len;
    tx->outputs_allocation_len = (size_t) j_outputs_alloc_len;

    // copy `wally_tx_input`s
    jsize num_inputs = (*env)->GetArrayLength(env, j_inputs);
    struct wally_tx_input *c_inputs = calloc(tx->inputs_allocation_len,
                                             sizeof(struct wally_tx_input));
    for (int i = 0; i < num_inputs; ++i) {
        jobject j_input = (*env)->GetObjectArrayElement(env, j_inputs, i);
        struct wally_tx_input *c_input = to_c_wally_tx_input(env, j_input);
        *(c_inputs + i) = *c_input;
    }
    tx->inputs = c_inputs;
    tx->num_inputs = (size_t) num_inputs;

    // copy `wally_tx_output`s
    jsize num_outputs = (*env)->GetArrayLength(env, j_outputs);
    struct wally_tx_output *c_outputs = (struct wally_tx_output *) calloc(tx->outputs_allocation_len,
                                                                          sizeof(struct wally_tx_output));
    for (int i = 0; i < num_outputs; ++i) {
        jobject j_output = (*env)->GetObjectArrayElement(env, j_outputs, i);
        struct wally_tx_output *c_output = to_c_wally_tx_output(env, j_output);
        *(c_outputs + i) = *c_output;
    }
    tx->outputs = c_outputs;
    tx->num_outputs = (size_t) num_outputs;

    return tx;
}

// -------------- END Tx JNI methods -----------------------//