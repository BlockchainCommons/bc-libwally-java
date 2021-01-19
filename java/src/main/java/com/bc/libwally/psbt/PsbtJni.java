package com.bc.libwally.psbt;

import com.bc.libwally.psbt.raw.WallyPsbt;
import com.bc.libwally.tx.raw.WallyTx;

class PsbtJni {

    static {
        System.loadLibrary("bc-libwally-psbt-jni");
    }

    static native WallyPsbt wally_psbt_clone_alloc(WallyPsbt psbt, long flags);

    static native WallyPsbt wally_psbt_from_bytes(byte[] bytes);

    static native int wally_psbt_get_length(WallyPsbt psbt, long flags);

    static native int wally_psbt_to_bytes(WallyPsbt psbt, long flags, byte[] output, int[] written);

    static native WallyTx wally_psbt_extract(WallyPsbt psbt);

    static native WallyPsbt wally_psbt_sign(WallyPsbt psbt, byte[] key, long flags);

    static native WallyPsbt wally_psbt_finalize(WallyPsbt psbt);

    static native boolean wally_psbt_is_finalized(WallyPsbt psbt);

    static native WallyPsbt wally_psbt_from_base64(String base64);

    static native String wally_psbt_to_base64(WallyPsbt psbt, long flags);

}
