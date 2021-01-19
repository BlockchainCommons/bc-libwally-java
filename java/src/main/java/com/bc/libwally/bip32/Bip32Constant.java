package com.bc.libwally.bip32;

public class Bip32Constant {

    public static long BIP32_INITIAL_HARDENED_CHILD = 0x80000000L;

    public static int BIP32_ENTROPY_LEN_128 = 16;

    public static int BIP32_ENTROPY_LEN_256 = 32;

    public static int BIP32_ENTROPY_LEN_512 = 64;

    public static int BIP32_KEY_FINGERPRINT_LEN = 4;

    public static int BIP32_SERIALIZED_LEN = 78;

    public static int BIP32_FLAG_KEY_PRIVATE = 0x0;

    public static int BIP32_FLAG_KEY_PUBLIC = 0x1;

    public static int BIP32_FLAG_SKIP_HASH = 0x2;

    public static int BIP32_FLAG_KEY_TWEAK_SUM = 0x4;

    public static int BIP32_VER_MAIN_PUBLIC = 0x0488B21E;

    public static int BIP32_VER_MAIN_PRIVATE = 0x0488ADE4;

    public static int BIP32_VER_TEST_PUBLIC = 0x043587CF;

    public static int BIP32_VER_TEST_PRIVATE = 0x04358394;

}
