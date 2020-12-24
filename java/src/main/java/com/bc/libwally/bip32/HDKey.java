package com.bc.libwally.bip32;

import com.bc.libwally.address.Key;
import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip39.Bip39Seed;

import java.util.Arrays;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_FLAG_KEY_PRIVATE;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_FLAG_KEY_PUBLIC;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_VER_MAIN_PRIVATE;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_VER_MAIN_PUBLIC;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_VER_TEST_PRIVATE;
import static com.bc.libwally.bip32.Bip32Constant.BIP32_VER_TEST_PUBLIC;
import static com.bc.libwally.bip32.Bip32Error.HARDENED_DERIVATION_WITHOUT_PRIV_KEY;
import static com.bc.libwally.bip32.Bip32Jni.bip32_key_from_base58_alloc;
import static com.bc.libwally.bip32.Bip32Jni.bip32_key_from_parent_path_alloc;
import static com.bc.libwally.bip32.Bip32Jni.bip32_key_from_seed_alloc;
import static com.bc.libwally.bip32.Bip32Jni.bip32_key_get_fingerprint;
import static com.bc.libwally.bip32.Bip32Jni.bip32_key_to_base58;

public class HDKey {

    private final WallyHDKey key;

    private byte[] masterFingerprint;
    // TODO: https://github.com/ElementsProject/libwally-core/issues/164

    public HDKey(WallyHDKey key, byte[] masterFingerprint) {
        this.key = key;
        this.masterFingerprint = masterFingerprint;
    }

    public HDKey(String base58) {
        this(base58, null);
    }

    public HDKey(String base58, byte[] masterFingerprint) {
        this.key = bip32_key_from_base58_alloc(base58);
        this.masterFingerprint = masterFingerprint;

        if (key.getDepth() == 0) {
            byte[] fingerprint = getFingerprint();
            if (this.masterFingerprint == null) {
                this.masterFingerprint = fingerprint;
            } else if (Arrays.equals(this.masterFingerprint, fingerprint)) {
                throw new Bip32Exception("Invalid masterFingerprint");
            }
        }
    }

    public HDKey(Bip39Seed seed, Network network) {
        long version = network == Network.MAINNET ? BIP32_VER_MAIN_PRIVATE : BIP32_VER_TEST_PRIVATE;

        this.key = bip32_key_from_seed_alloc(seed.getData(), version, 0);
        this.masterFingerprint = getFingerprint();
    }

    public HDKey(Bip39Seed seed) {
        this(seed, Network.MAINNET);
    }

    public byte[] getFingerprint() {
        return bip32_key_get_fingerprint(this.key);
    }

    public Network getNetwork() {
        if (this.key.getVersion() == BIP32_VER_MAIN_PRIVATE ||
            this.key.getVersion() == BIP32_VER_MAIN_PUBLIC) {
            return Network.MAINNET;
        } else {
            return Network.TESTNET;
        }
    }

    public boolean isNeutered() {
        return this.key.getVersion() == BIP32_VER_MAIN_PUBLIC ||
               this.key.getVersion() == BIP32_VER_TEST_PUBLIC;
    }

    public String getDescription() {
        return isNeutered() ? getXpub() : getXprv();
    }

    public String getXpub() {
        return bip32_key_to_base58(key, BIP32_FLAG_KEY_PUBLIC);
    }

    public String getXprv() {
        return bip32_key_to_base58(key, BIP32_FLAG_KEY_PRIVATE);
    }

    public PubKey getPubKey() {
        return new PubKey(key.getPubKey(), getNetwork(), true);
    }

    public Key getPrivKey() {
        if (isNeutered())
            return null;

        // skip prefix byte 0
        byte[] data = slice(key.getPrivKey(), 1, key.getPrivKey().length);

        return new Key(data, getNetwork(), true);
    }

    public HDKey derive(Bip32Path path) {
        short depth = key.getDepth();

        Bip32Path tmpPath = path;
        if (!path.isRelative()) {
            tmpPath = path.chop(depth);
        }

        boolean containHardened = false;
        for (Bip32Derivation component : tmpPath.getComponents()) {
            if (!component.isHardened())
                continue;
            containHardened = true;
            break;
        }

        if (isNeutered() && containHardened) {
            throw new Bip32Exception(HARDENED_DERIVATION_WITHOUT_PRIV_KEY);
        }

        long flags = isNeutered() ? BIP32_FLAG_KEY_PUBLIC : BIP32_FLAG_KEY_PRIVATE;
        WallyHDKey key = bip32_key_from_parent_path_alloc(this.key, tmpPath.getRawPath(), flags);
        return new HDKey(key, this.masterFingerprint);
    }

    public byte[] getMasterFingerprint() {
        return masterFingerprint;
    }

    public WallyHDKey getKey() {
        return key;
    }
}
