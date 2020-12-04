package com.bc.libwally.script;

import com.bc.libwally.address.PubKey;

import java.util.Objects;

public class WitnessType {

    public enum Type {
        PAY_TO_WITNESS_PUBKEY_HASH, // P2WPKH
        PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH // P2SH-P2WPKH
    }

    private final Type type;

    private final PubKey pubKey;

    public static WitnessType payToWitnessPubKeyHash(PubKey pubKey) {
        return new WitnessType(Type.PAY_TO_WITNESS_PUBKEY_HASH, pubKey);
    }

    public static WitnessType payToScriptHashPayToWitnessPubKeyHash(PubKey pubKey) {
        return new WitnessType(Type.PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH, pubKey);
    }

    private WitnessType(Type type, PubKey pubKey) {
        this.type = type;
        this.pubKey = pubKey;
    }

    public PubKey getPubKey() {
        return pubKey;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WitnessType that = (WitnessType) o;
        return type == that.type && pubKey.equals(that.pubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pubKey);
    }
}
