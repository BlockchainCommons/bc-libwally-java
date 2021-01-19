package com.bc.libwally.crypto;

public class CryptoConstants {

    public static final int EC_PRIVATE_KEY_LEN = 32;

    public static final int EC_PUBLIC_KEY_LEN = 33;

    public static final int EC_PUBLIC_KEY_UNCOMPRESSED_LEN = 65;

    public static final int EC_MESSAGE_HASH_LEN = 32;

    public static final int EC_SIGNATURE_LEN = 64;

    public static final int EC_SIGNATURE_RECOVERABLE_LEN = 65;

    public static final int EC_SIGNATURE_DER_MAX_LEN = 72;

    public static final int EC_SIGNATURE_DER_MAX_LOW_R_LEN = 71;

    public static final int EC_FLAG_ECDSA = 0x1;

    public static final int EC_FLAG_SCHNORR = 0x2;

    public static final int EC_FLAG_GRIND_R = 0x4;

    public static final int EC_FLAG_RECOVERABLE = 0x8;

    public static final int EC_FLAGS_ALL = (0x1 | 0x2 | 0x4 | 0x8);

    public static final int BITCOIN_MESSAGE_MAX_LEN = 64 * 1024 - 64;

    public static final int BITCOIN_MESSAGE_FLAG_HASH = 1;

    public static final int PBKDF2_HMAC_SHA256_LEN = 32;

    public static final int PBKDF2_HMAC_SHA512_LEN = 64;

    public static final int HASH160_LEN = 20;

    public static final int HMAC_SHA256_LEN = 32;

    public static final int HMAC_SHA512_LEN = 64;

    public static final int SHA256_LEN = 32;

    public static final int SHA512_LEN = 64;

    public static final int AES_BLOCK_LEN = 16;

    public static final int AES_KEY_LEN_128 = 16;

    public static final int AES_KEY_LEN_192 = 24;

    public static final int AES_KEY_LEN_256 = 32;

    public static final int AES_FLAG_ENCRYPT = 1;

    public static final int AES_FLAG_DECRYPT = 2;
}
