package com.bc.libwally.bip32;

class Bip32Jni {
    static {
        System.loadLibrary("bc-libwally-bip32-jni");
    }

    static native WallyHDKey bip32_key_from_base58_alloc(String base58);

    static native WallyHDKey bip32_key_from_seed_alloc(byte[] seed, long version, long flags);

    static native String bip32_key_to_base58(WallyHDKey key, long flags);

    static native byte[] bip32_key_get_fingerprint(WallyHDKey key);

    static native WallyHDKey bip32_key_from_parent_path_alloc(WallyHDKey key,
                                                              long[] childPath,
                                                              long flags);

}
