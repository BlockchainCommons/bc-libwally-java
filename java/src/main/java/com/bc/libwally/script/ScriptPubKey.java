package com.bc.libwally.script;

import com.bc.libwally.address.PubKey;

import java.util.Arrays;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_LEN;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_MULTISIG_SORTED;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_SHA256;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_MULTISIG;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_OP_RETURN;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_P2PKH;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_P2SH;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_P2WPKH;
import static com.bc.libwally.script.ScriptConstant.WALLY_SCRIPT_TYPE_P2WSH;
import static com.bc.libwally.script.ScriptJni.wally_scriptpubkey_get_type;
import static com.bc.libwally.script.ScriptJni.wally_scriptpubkey_multisig_from_bytes;
import static com.bc.libwally.script.ScriptJni.wally_witness_program_from_bytes;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.MULTI_SIG;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.OP_RETURN;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_PUBKEY_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_SCRIPT_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_WITNESS_PUBKEY_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_WITNESS_SCRIPT_HASH;

public class ScriptPubKey implements Cloneable {

    public enum ScriptType {
        OP_RETURN, // OP_RETURN
        PAY_TO_PUBKEY_HASH, // P2PKH
        PAY_TO_SCRIPT_HASH, // P2SH
        PAY_TO_WITNESS_PUBKEY_HASH, // P2WPKH
        PAY_TO_WITNESS_SCRIPT_HASH, // P2WS
        MULTI_SIG
    }

    private final byte[] data;

    public ScriptPubKey(byte[] data, Integer len) {
        this.data = len != null ? slice(data, len) : data;
    }

    public ScriptPubKey(byte[] data) {
        this(data, null);
    }

    public ScriptPubKey(String hex) {
        this(hex2Bytes(hex), null);
    }

    public ScriptPubKey(PubKey[] pubKeys, long threshold, boolean bip67) {
        if (pubKeys == null || pubKeys.length == 0)
            throw new ScriptException("invalid pubKeys");
        int pubKeysBytesLen = EC_PUBLIC_KEY_LEN * pubKeys.length;
        byte[] pubKeysBytes = new byte[pubKeysBytesLen];
        int offset = 0;
        for (PubKey pubKey : pubKeys) {
            System.arraycopy(pubKey.getData(), 0, pubKeysBytes, offset, EC_PUBLIC_KEY_LEN);
            offset += EC_PUBLIC_KEY_LEN;
        }

        int scriptLength = 3 + pubKeys.length * (EC_PUBLIC_KEY_LEN + 1);
        byte[] scriptBytes = new byte[scriptLength];
        long flags = bip67 ? WALLY_SCRIPT_MULTISIG_SORTED : 0;
        int[] written = new int[1];
        if (wally_scriptpubkey_multisig_from_bytes(pubKeysBytes,
                                                   threshold,
                                                   flags,
                                                   scriptBytes,
                                                   written) != WALLY_OK) {
            throw new ScriptException("wally_scriptpubkey_multisig_from_bytes error");
        }

        this.data = slice(scriptBytes, written[0]);
    }

    public ScriptPubKey(PubKey[] pubKeys, long threshold) {
        this(pubKeys, threshold, true);
    }

    public byte[] getData() {
        return data;
    }

    public String getHexData() {
        return bytes2Hex(this.data);
    }

    public ScriptType getType() {
        int type = wally_scriptpubkey_get_type(this.data);
        switch (type) {
            case WALLY_SCRIPT_TYPE_OP_RETURN:
                return OP_RETURN;
            case WALLY_SCRIPT_TYPE_P2PKH:
                return PAY_TO_PUBKEY_HASH;
            case WALLY_SCRIPT_TYPE_P2SH:
                return PAY_TO_SCRIPT_HASH;
            case WALLY_SCRIPT_TYPE_P2WPKH:
                return PAY_TO_WITNESS_PUBKEY_HASH;
            case WALLY_SCRIPT_TYPE_P2WSH:
                return PAY_TO_WITNESS_SCRIPT_HASH;
            case WALLY_SCRIPT_TYPE_MULTISIG:
                return MULTI_SIG;
            default:
                throw new ScriptException("invalid script type");
        }
    }

    public byte[] getWitnessProgram() {
        int scriptBytesLen = 34; // 00 20 HASH256
        byte[] scriptByte = new byte[scriptBytesLen];
        int[] written = new int[1];
        if (wally_witness_program_from_bytes(this.data, WALLY_SCRIPT_SHA256, scriptByte, written) !=
            WALLY_OK) {
            throw new ScriptException("wally_witness_program_from_bytes error");
        }

        return slice(scriptByte, written[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptPubKey that = (ScriptPubKey) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public ScriptPubKey clone() throws CloneNotSupportedException {
        return (ScriptPubKey) super.clone();
    }
}
