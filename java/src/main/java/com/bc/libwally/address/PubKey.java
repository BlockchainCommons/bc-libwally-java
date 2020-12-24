package com.bc.libwally.address;

import com.bc.libwally.bip32.Network;

import java.util.Arrays;
import java.util.Objects;

import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_LEN;
import static com.bc.libwally.crypto.CryptoConstants.EC_PUBLIC_KEY_UNCOMPRESSED_LEN;

public class PubKey {

    private final boolean compressed;

    private final byte[] data;

    private final Network network;

    public PubKey(byte[] data, Network network, boolean compressed) {
        if (data.length != (compressed ? EC_PUBLIC_KEY_LEN : EC_PUBLIC_KEY_UNCOMPRESSED_LEN)) {
            throw new AddressException("Invalid data");
        }

        this.data = data;
        this.compressed = compressed;
        this.network = network;
    }

    public PubKey(byte[] data, Network network) {
        this(data, network, true);
    }

    public PubKey(String hex, Network network) {
        this(hex2Bytes(hex), network);
    }

    public Network getNetwork() {
        return network;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isCompressed() {
        return compressed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PubKey pubKey = (PubKey) o;
        return compressed == pubKey.compressed &&
               Arrays.equals(data, pubKey.data) &&
               network == pubKey.network;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(compressed, network);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
