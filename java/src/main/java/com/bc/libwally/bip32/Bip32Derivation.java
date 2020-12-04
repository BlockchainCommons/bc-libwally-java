package com.bc.libwally.bip32;

import java.util.Objects;

public class Bip32Derivation {

    private long index;

    private Type type;

    private Bip32Derivation(long index, Type type) {
        if (index < 0)
            throw new Bip32Exception("index must be greater or equals to zero");
        this.index = index;
        this.type = type;
    }

    public static Bip32Derivation newNormal(long value) {
        return new Bip32Derivation(value, Type.NORMAL);
    }

    public static Bip32Derivation newHardened(long value) {
        return new Bip32Derivation(value, Type.HARDENED);
    }

    public long getIndex() {
        return index;
    }

    public Type getType() {
        return type;
    }

    public boolean isHardened() {
        return type == Type.HARDENED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bip32Derivation that = (Bip32Derivation) o;
        return index == that.index && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, type);
    }

    public enum Type {
        NORMAL,
        HARDENED
    }
}
