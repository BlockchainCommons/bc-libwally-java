package com.bc.libwally.crypto;

import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_UNCOMPRESSED_LEN;
import static com.bc.libwally.crypto.CryptoConstants.HASH160_LEN;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_public_key_decompress;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_public_key_from_private_key;
import static com.bc.libwally.crypto.CryptoJni.wally_hash160;

public class Crypto {

    public static byte[] ecPubKeyFromPrvKey(byte[] prvKey) {
        byte[] output = new byte[EC_PUBLIC_KEY_LEN];
        if (wally_ec_public_key_from_private_key(prvKey, output) != WALLY_OK) {
            throw new CryptoException("wally_ec_public_key_from_private_key error");
        }
        return output;
    }

    public static byte[] ecPubKeyDecompress(byte[] pubKey) {
        byte[] output = new byte[EC_PUBLIC_KEY_UNCOMPRESSED_LEN];
        if (wally_ec_public_key_decompress(pubKey, output) != WALLY_OK) {
            throw new CryptoException("wally_ec_public_key_decompress error");
        }
        return output;
    }

    public static byte[] hash160(byte[] bytes) {
        byte[] output = new byte[HASH160_LEN];
        if (wally_hash160(bytes, output) != WALLY_OK) {
            throw new CryptoException("wally_hash160 error");
        }
        return output;
    }
}
