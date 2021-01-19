package com.bc.libwally.address;

import com.bc.libwally.bip32.WallyHDKey;

class AddressJni {

    static {
        System.loadLibrary("bc-libwally-address-jni");
    }

    static native int wally_addr_segwit_to_bytes(String addr,
                                                 String addrFamily,
                                                 byte[] output,
                                                 int[] written);

    static native int wally_address_to_scriptpubkey(String addr,
                                                    long network,
                                                    byte[] output,
                                                    int[] written);

    static native String wally_bip32_key_to_address(WallyHDKey key, long flags, long version);

    static native String wally_bip32_key_to_addr_segwit(WallyHDKey key, String addrFamily);


    static native String wally_scriptpubkey_to_address(byte[] scriptPubKey, long network);

    static native String wally_addr_segwit_from_bytes(byte[] bytes, String addrFamily);

    static native int wally_wif_to_bytes(byte[] wif, long prefix, long flags, byte[] output);

    static native String wally_wif_from_bytes(byte[] privKey, long prefix, long flags);

}
