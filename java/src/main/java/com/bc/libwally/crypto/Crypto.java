package com.bc.libwally.crypto;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.crypto.CryptoConstants.EC_FLAG_RECOVERABLE;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_UNCOMPRESSED_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_DER_MAX_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_RECOVERABLE_LEN;
import static com.bc.libwally.crypto.CryptoConstants.HASH160_LEN;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_private_key_verify;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_public_key_decompress;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_public_key_from_private_key;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_sig_from_bytes;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_sig_normalize;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_sig_to_der;
import static com.bc.libwally.crypto.CryptoJni.wally_ec_sig_verify;
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

    public static boolean ecPrvKeyVerify(byte[] privKey) {
        return wally_ec_private_key_verify(privKey) == WALLY_OK;
    }

    public static byte[] ecSigFromBytes(byte[] privKey, byte[] message, long flags) {
        int outputLen = flags == EC_FLAG_RECOVERABLE
                        ? EC_SIGNATURE_RECOVERABLE_LEN
                        : EC_SIGNATURE_LEN;
        byte[] output = new byte[outputLen];
        if (wally_ec_sig_from_bytes(privKey, message, flags, output) != WALLY_OK) {
            throw new CryptoException("wally_ec_sig_from_bytes error");
        }

        return output;
    }

    public static boolean ecSigVerify(byte[] pubKey, byte[] message, long flags, byte[] sig) {
        return wally_ec_sig_verify(pubKey, message, flags, sig) == WALLY_OK;
    }

    public static byte[] ecSigNormalize(byte[] sig) {
        byte[] output = new byte[EC_SIGNATURE_LEN];
        if (wally_ec_sig_normalize(sig, output) != WALLY_OK) {
            throw new CryptoException("wally_ec_sig_normalize error");
        }

        return output;
    }

    public static byte[] ecSig2Der(byte[] sig) {
        int[] written = new int[1];
        byte[] output = new byte[EC_SIGNATURE_DER_MAX_LEN];
        if (wally_ec_sig_to_der(sig, output, written) != WALLY_OK) {
            throw new CryptoException("wally_ec_sig_to_der error");
        }
        return slice(output, 0, written[0]);
    }


}
