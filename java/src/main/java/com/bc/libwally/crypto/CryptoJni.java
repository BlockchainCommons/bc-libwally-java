package com.bc.libwally.crypto;

class CryptoJni {

    static {
        System.loadLibrary("bc-libwally-crypto-jni");
    }

    static native int wally_ec_public_key_from_private_key(byte[] privKey, byte[] output);

    static native int wally_ec_public_key_decompress(byte[] pubKey, byte[] output);

    static native int wally_hash160(byte[] bytes, byte[] output);
}
