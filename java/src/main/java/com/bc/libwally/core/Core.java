package com.bc.libwally.core;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.core.CoreConstant.BASE58_CHECKSUM_LEN;
import static com.bc.libwally.core.CoreConstant.BASE58_FLAG_CHECKSUM;
import static com.bc.libwally.core.CoreJni.wally_base58_from_bytes;
import static com.bc.libwally.core.CoreJni.wally_base58_to_bytes;
import static com.bc.libwally.core.CoreJni.wally_base64_from_bytes;
import static com.bc.libwally.core.CoreJni.wally_base64_to_bytes;
import static com.bc.libwally.core.CoreJni.wally_hex_from_bytes;
import static com.bc.libwally.core.CoreJni.wally_hex_to_bytes;

public class Core {

    public static String bytes2Hex(byte[] bytes) {
        return wally_hex_from_bytes(bytes);
    }

    public static byte[] hex2Bytes(String hex) {
        int hexLen = hex.length();
        int bytesLen = hexLen % 2 == 0 ? hexLen / 2 : (hexLen / 2) + 1;
        byte[] output = new byte[bytesLen];
        int[] written = new int[1];
        if (wally_hex_to_bytes(hex, output, written) != WALLY_OK) {
            throw new CoreException("wally_hex_to_bytes error");
        }

        return slice(output, written[0]);
    }

    public static String bytes2Base58(byte[] bytes) {
        return wally_base58_from_bytes(bytes, BASE58_FLAG_CHECKSUM);
    }

    public static byte[] base582Bytes(String base58) {
        int len = base58.length() + BASE58_CHECKSUM_LEN;
        byte[] output = new byte[len];
        int[] written = new int[1];
        if (wally_base58_to_bytes(base58, BASE58_FLAG_CHECKSUM, output, written) != WALLY_OK) {
            throw new CoreException("wally_base58_to_bytes error");
        }

        return slice(output, written[0]);
    }

    public static String bytes2Base64(byte[] bytes) {
        return wally_base64_from_bytes(bytes);
    }

    public static byte[] base642Bytes(String base64) {
        return wally_base64_to_bytes(base64);
    }
}
