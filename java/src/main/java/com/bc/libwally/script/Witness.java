package com.bc.libwally.script;

import java.util.Arrays;
import java.util.Objects;

import static com.bc.libwally.ArrayUtils.append;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.Crypto.hash160;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_DER_MAX_LOW_R_LEN;

public class Witness {

    private final WitnessType type;

    private final byte[] signature;

    private boolean dummy;

    public Witness(WitnessType type, byte[] signature, boolean dummy) {
        this.type = type;
        this.signature = signature;
        this.dummy = dummy;
    }

    public Witness(WitnessType type, byte[] signature) {
        this(type, signature, false);
    }

    public Witness(WitnessType type) {
        byte[] dummySig = new byte[EC_SIGNATURE_DER_MAX_LOW_R_LEN];
        this.type = type;
        this.signature = dummySig;
        this.dummy = true;
    }

    public Witness signed(byte[] signature) {
        return new Witness(type, signature);
    }

    public byte[] getScriptCode() {
        byte[] pubKeyHashBytes = hash160(type.getPubKey().getData());
        return append(hex2Bytes("76a914"), pubKeyHashBytes, hex2Bytes("88ac"));
    }

    public byte[] getSignature() {
        return signature;
    }

    public WitnessType getType() {
        return type;
    }

    public boolean isDummy() {
        return dummy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Witness witness = (Witness) o;
        return dummy == witness.dummy &&
               type.equals(witness.type) &&
               Arrays.equals(signature, witness.signature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, dummy);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}
