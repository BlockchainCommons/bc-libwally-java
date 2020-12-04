package com.bc.libwally.bip39;

import java.util.Arrays;

import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;

public class Bip39Seed {

    private final byte[] data;

    public Bip39Seed(String hex) {
        this(hex2Bytes(hex), null);
    }

    public Bip39Seed(byte[] data, Integer len) {
        if (data == null || data.length == 0)
            throw new Bip39Exception("invalid data");
        this.data = len != null ? Arrays.copyOfRange(data, 0, len) : data;
    }

    public Bip39Seed(byte[] data) {
        this(data, null);
    }

    public String getHexData() {
        return bytes2Hex(this.data);
    }

    public int getLength() {
        return this.data.length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bip39Seed bip39Seed = (Bip39Seed) o;
        return Arrays.equals(data, bip39Seed.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
