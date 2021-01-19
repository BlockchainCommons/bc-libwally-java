package com.bc.libwally.psbt;

import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Path;
import com.bc.libwally.Network;
import com.bc.libwally.psbt.raw.WallyMap;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.bc.libwally.ArrayUtils.bytes2Int;
import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.ArrayUtils.toUnsignedLong;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_KEY_FINGERPRINT_LEN;

public class KeyOrigin {

    private final byte[] fingerprint;

    private final Bip32Path path;

    static Map<PubKey, KeyOrigin> getOriginMap(WallyMap keyPaths, Network network) {
        Map<PubKey, KeyOrigin> originMap = new HashMap<>();
        for (int i = 0; i < keyPaths.size(); i++) {
            // TODO: simplify after https://github.com/ElementsProject/libwally-core/issues/241
            WallyMap.WallyMapItem item = keyPaths.getItems()[i];

            PubKey pubKey = new PubKey(item.getKey(), network);
            byte[] fingerprint = slice(item.getValue(), BIP32_KEY_FINGERPRINT_LEN);
            byte[] keyPath = slice(item.getValue(),
                                   BIP32_KEY_FINGERPRINT_LEN,
                                   item.getValue().length);

            int len = keyPath.length / 4;
            long[] components = new long[len];
            for (int j = 0; j < len; j++) {
                long data = toUnsignedLong(bytes2Int(slice(keyPath, j * 4, (j + 1) * 4),
                                                     ByteOrder.LITTLE_ENDIAN));
                components[j] = data;
            }
            Bip32Path path = new Bip32Path(components, false);
            originMap.put(pubKey, new KeyOrigin(fingerprint, path));
        }

        return originMap;
    }

    public KeyOrigin(byte[] fingerprint, Bip32Path path) {
        this.fingerprint = fingerprint;
        this.path = path;
    }

    public Bip32Path getPath() {
        return path;
    }

    public byte[] getFingerprint() {
        return fingerprint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KeyOrigin keyOrigin = (KeyOrigin) o;
        return Arrays.equals(fingerprint, keyOrigin.fingerprint) &&
               Objects.equals(path, keyOrigin.path);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(path);
        result = 31 * result + Arrays.hashCode(fingerprint);
        return result;
    }
}
