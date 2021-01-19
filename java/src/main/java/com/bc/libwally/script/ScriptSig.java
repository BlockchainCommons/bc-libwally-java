package com.bc.libwally.script;

import java.util.Arrays;
import java.util.Objects;

import static com.bc.libwally.ArrayUtils.append;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.Crypto.hash160;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_DER_MAX_LOW_R_LEN;
import static com.bc.libwally.tx.TxConstant.WALLY_SIGHASH_ALL;

public class ScriptSig implements Cloneable {

    public enum Purpose {
        SIGNED,
        FEE_WORST_CASE
    }

    private final ScriptSigType type;

    private byte[] signature;

    public ScriptSig(ScriptSigType type) {
        this.type = type;
    }

    public byte[] render(Purpose purpose) {
        switch (type.getType()) {
            case PAY_TO_PUBKEY_HASH:
                byte[] sigHashBytes = new byte[]{WALLY_SIGHASH_ALL};
                byte[] lengthPushPubKey = new byte[]{(byte) type.getPubKey().getData().length};
                byte[] pubKeyData = type.getPubKey().getData();

                switch (purpose) {
                    case FEE_WORST_CASE:
                        byte[] dummySig = new byte[EC_SIGNATURE_DER_MAX_LOW_R_LEN];
                        byte[] lengthPushSig = new byte[]{(byte) (dummySig.length + 1)};

                        return append(lengthPushSig,
                                      dummySig,
                                      sigHashBytes,
                                      lengthPushPubKey,
                                      pubKeyData);

                    case SIGNED:
                        if (signature != null) {
                            lengthPushSig = new byte[]{(byte) (signature.length + 1)};

                            return append(lengthPushSig,
                                          signature,
                                          sigHashBytes,
                                          lengthPushPubKey,
                                          pubKeyData);
                        } else {
                            return null;
                        }

                }
            case PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH:
                byte[] pubKeyHashBytes = hash160(type.getPubKey().getData());
                byte[] redeemScript = append(hex2Bytes("0014"), pubKeyHashBytes);
                return append(new byte[]{(byte) redeemScript.length}, redeemScript);
        }

        return null;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setSignature(String hex) {
        setSignature(hex2Bytes(hex));
    }

    public ScriptSigType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptSig scriptSig = (ScriptSig) o;
        return type.equals(scriptSig.type) && Arrays.equals(signature, scriptSig.signature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public ScriptSig clone() throws CloneNotSupportedException {
        return (ScriptSig) super.clone();
    }
}
