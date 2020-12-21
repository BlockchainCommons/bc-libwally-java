package com.bc.libwally.script;

import com.bc.libwally.address.PubKey;

import java.util.Objects;

public class ScriptSigType implements Cloneable {
    public enum Type {
        PAY_TO_PUBKEY_HASH, // P2PKH
        PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH // P2SH-P2WPKH
    }

    private final Type type;

    private final PubKey pubKey;

    public static ScriptSigType payToPubKeyHash(PubKey pubKey) {
        return new ScriptSigType(Type.PAY_TO_PUBKEY_HASH, pubKey);
    }

    public static ScriptSigType payToScriptHashPayToWitnessPubKeyHash(PubKey pubKey) {
        return new ScriptSigType(Type.PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH, pubKey);
    }

    private ScriptSigType(Type type, PubKey pubKey) {
        this.type = type;
        this.pubKey = pubKey;
    }

    public Type getType() {
        return type;
    }

    public PubKey getPubKey() {
        return pubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptSigType that = (ScriptSigType) o;
        return type == that.type && pubKey.equals(that.pubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pubKey);
    }

    @Override
    protected ScriptSigType clone() throws CloneNotSupportedException {
        return (ScriptSigType) super.clone();
    }
}
