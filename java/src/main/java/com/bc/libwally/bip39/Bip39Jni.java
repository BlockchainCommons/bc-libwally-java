package com.bc.libwally.bip39;

import com.bc.libwally.NativeWrapper;

class Bip39Jni {

    static {
        System.loadLibrary("bc-libwally-bip39-jni");
    }

    static native NativeWrapper.JniObject bip39_get_wordlist(String lang);

    static native String bip39_get_word(NativeWrapper.JniObject words, int index);

    static native String bip39_mnemonic_from_bytes(NativeWrapper.JniObject words, byte[] bytes);

    static native int bip39_mnemonic_to_bytes(NativeWrapper.JniObject words,
                                              String mnemonic,
                                              byte[] output,
                                              int[] written);

    static native int bip39_mnemonic_to_seed(String mnemonic,
                                             String passphrase,
                                             byte[] output,
                                             int[] written);

    static native int bip39_mnemonic_validate(NativeWrapper.JniObject words, String mnemonic);

}
