package com.bc.libwally.crypto;

class CryptoJni {

    static {
        System.loadLibrary("bc-libwally-crypto-jni");
    }

    static native int wally_ec_public_key_from_private_key(byte[] privKey, byte[] output);

    static native int wally_ec_public_key_decompress(byte[] pubKey, byte[] output);

    static native int wally_hash160(byte[] bytes, byte[] output);

    static native int wally_ec_private_key_verify(byte[] privKey);

    static native int wally_ec_sig_from_bytes(byte[] privKey,
                                              byte[] message,
                                              long flags,
                                              byte[] output);

    static native int wally_ec_sig_verify(byte[] pubKey, byte[] message, long flags, byte[] sig);

    static native int wally_ec_sig_normalize(byte[] sig, byte[] output);

    static native int wally_ec_sig_to_der(byte[] sig, byte[] output, int[] written);
}
