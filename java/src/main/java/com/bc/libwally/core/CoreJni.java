package com.bc.libwally.core;

class CoreJni {

    static {
        System.loadLibrary("bc-libwally-core-jni");
    }

    static native String wally_hex_from_bytes(byte[] bytes);

    static native int wally_hex_to_bytes(String hex, byte[] output, int[] written);

    static native String wally_base58_from_bytes(byte[] bytes, long flags);

    static native int wally_base58_to_bytes(String base58,
                                            long flags,
                                            byte[] output,
                                            int[] written);
}
