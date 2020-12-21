package com.bc.libwally.tx;

class TxJni {

    static {
        System.loadLibrary("bc-libwally-tx-jni");
    }

    static native WallyTxInput wally_tx_input_init_alloc(byte[] txHash,
                                                         long utxoIndex,
                                                         long sequence,
                                                         byte[] script,
                                                         WallyTxWitnessStack witness);

    static native WallyTx wally_tx_clone_alloc(WallyTx wallyTx, long flags);

    static native WallyTxOutput wally_tx_output_init_alloc(long satoshi, byte[] script);

    static native WallyTxWitnessStack wally_tx_witness_stack_init_alloc(int allocationLength);

    static native WallyTxWitnessStack wally_tx_witness_stack_set(WallyTxWitnessStack stack,
                                                                 int index,
                                                                 byte[] witness);

    static native WallyTx wally_tx_from_bytes(byte[] bytes, long flags);

    static native WallyTx wally_tx_init_alloc(long version,
                                              long locktime,
                                              int inputsAllocLen,
                                              int outputsAllocLen);

    static native WallyTx wally_tx_add_input(WallyTx wallyTx, WallyTxInput input);

    static native WallyTx wally_tx_add_output(WallyTx wallyTx, WallyTxOutput output);

    static native String wally_tx_to_hex(WallyTx wallyTx, long flags);

    static native long wally_tx_get_total_output_satoshi(WallyTx wallyTx);

    static native WallyTx wally_tx_set_input_script(WallyTx wallyTx, int index, byte[] script);

    static native int wally_tx_get_vsize(WallyTx wallyTx);

    static native int wally_tx_get_btc_signature_hash(WallyTx wallyTx,
                                                      int index,
                                                      byte[] script,
                                                      long satoshi,
                                                      long sigHash,
                                                      long flags,
                                                      byte[] output);

    static native WallyTx wally_tx_set_input_witness(WallyTx wallyTx,
                                                     int index,
                                                     WallyTxWitnessStack stack);
}
