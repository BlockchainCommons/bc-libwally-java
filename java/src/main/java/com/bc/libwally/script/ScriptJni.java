package com.bc.libwally.script;

class ScriptJni {

    static {
        System.loadLibrary("bc-libwally-script-jni");
    }

    static native int wally_scriptpubkey_get_type(byte[] bytes);

    static native int wally_scriptpubkey_multisig_from_bytes(byte[] bytes,
                                                             long threshold,
                                                             long flags,
                                                             byte[] output,
                                                             int[] written);

    static native int wally_witness_program_from_bytes(byte[] bytes,
                                                       long flags,
                                                       byte[] output,
                                                       int[] written);
}
