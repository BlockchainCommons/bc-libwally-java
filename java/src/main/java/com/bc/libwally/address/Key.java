package com.bc.libwally.address;

import com.bc.libwally.Network;

import java.util.Arrays;
import java.util.Objects;

import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.address.AddressConstant.WALLY_ADDRESS_VERSION_WIF_MAINNET;
import static com.bc.libwally.address.AddressConstant.WALLY_ADDRESS_VERSION_WIF_TESTNET;
import static com.bc.libwally.address.AddressConstant.WALLY_WIF_FLAG_COMPRESSED;
import static com.bc.libwally.address.AddressConstant.WALLY_WIF_FLAG_UNCOMPRESSED;
import static com.bc.libwally.address.AddressJni.wally_wif_from_bytes;
import static com.bc.libwally.address.AddressJni.wally_wif_to_bytes;
import static com.bc.libwally.crypto.Crypto.ecPubKeyDecompress;
import static com.bc.libwally.crypto.Crypto.ecPubKeyFromPrvKey;
import static com.bc.libwally.crypto.CryptoConstants.EC_PRIVATE_KEY_LEN;

public class Key {

    private final boolean compressed;

    private final byte[] data;

    private final Network network;

    public Key(String wif, Network network, boolean compressed) {
        byte[] output = new byte[EC_PRIVATE_KEY_LEN];
        if (wally_wif_to_bytes(wif.getBytes(), getPrefix(network), getFlags(compressed), output) !=
            WALLY_OK) {
            throw new AddressException("wally_wif_to_bytes error");
        }

        this.data = output;
        this.network = network;
        this.compressed = compressed;
    }

    public Key(String wif, Network network) {
        this(wif, network, true);
    }

    public Key(byte[] data, Network network, boolean compressed) {
        if (data == null || data.length != EC_PRIVATE_KEY_LEN)
            throw new AddressException("Invalid data");
        this.data = data;
        this.network = network;
        this.compressed = compressed;
    }

    public Key(byte[] data, Network network) {
        this(data, network, true);
    }

    public byte[] getData() {
        return data;
    }

    public Network getNetwork() {
        return network;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public String getWif() {
        return wally_wif_from_bytes(data, getPrefix(network), getFlags(this.compressed));
    }

    private long getFlags(boolean compressed) {
        return compressed ? WALLY_WIF_FLAG_COMPRESSED : WALLY_WIF_FLAG_UNCOMPRESSED;
    }

    private long getPrefix(Network network) {
        return network == Network.MAINNET
               ? WALLY_ADDRESS_VERSION_WIF_MAINNET
               : WALLY_ADDRESS_VERSION_WIF_TESTNET;
    }

    public PubKey getPubKey() {
        byte[] pubKeyData = ecPubKeyFromPrvKey(this.data);
        if (compressed) {
            return new PubKey(pubKeyData, network, true);
        } else {
            byte[] pubKeyDataUncompressed = ecPubKeyDecompress(pubKeyData);
            return new PubKey(pubKeyDataUncompressed, network, false);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Key key = (Key) o;
        return compressed == key.compressed &&
               Arrays.equals(data, key.data) &&
               network == key.network;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(compressed, network);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
