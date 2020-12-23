package com.bc.libwally.psbt;

import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Exception;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.psbt.raw.WallyMap;
import com.bc.libwally.psbt.raw.WallyPsbtInput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PsbtInput {

    private final Map<PubKey, KeyOrigin> originMap;

    private final Map<PubKey, byte[]> signatureMap;

    private final byte[] witnessScript;

    private final boolean isSegwit;

    private final Long amount;

    PsbtInput(WallyPsbtInput wallyPsbtInput, Network network) {
        if (wallyPsbtInput.getKeyPaths().hasValue()) {
            this.originMap = KeyOrigin.getOriginMap(wallyPsbtInput.getKeyPaths(), network);
        } else {
            this.originMap = null;
        }

        if (wallyPsbtInput.getSignatures().hasValue()) {
            this.signatureMap = getSignatureMap(wallyPsbtInput.getSignatures(), network);
        } else {
            this.signatureMap = null;
        }

        this.witnessScript = wallyPsbtInput.getWitnessScript();
        if (wallyPsbtInput.getWitnessUtxo() != null) {
            isSegwit = true;
            amount = wallyPsbtInput.getWitnessUtxo().getSatoshi();
        } else {
            isSegwit = false;
            amount = null;
        }
    }

    private static Map<PubKey, byte[]> getSignatureMap(WallyMap signature, Network network) {
        Map<PubKey, byte[]> sigMap = new HashMap<>();
        for (int i = 0; i < signature.size(); i++) {
            WallyMap.WallyMapItem item = signature.getItems()[i];
            PubKey pubKey = new PubKey(item.getKey(), network);
            byte[] sig = item.getValue();
            sigMap.put(pubKey, sig);
        }
        return sigMap;
    }

    public Map<PubKey, KeyOrigin> getCanSignOriginMap(HDKey key) {
        Map<PubKey, KeyOrigin> originMap = new HashMap<>();
        for (Map.Entry<PubKey, KeyOrigin> entry : this.originMap.entrySet()) {
            byte[] masterKeyFingerprint = key.getMasterFingerprint();
            if (masterKeyFingerprint == null)
                break;

            PubKey pubKey = entry.getKey();
            KeyOrigin keyOrigin = entry.getValue();
            if (Arrays.equals(masterKeyFingerprint, keyOrigin.getFingerprint())) {
                try {
                    HDKey childKey = key.derive(keyOrigin.getPath());
                    if (childKey.getPubKey().equals(pubKey)) {
                        originMap.put(pubKey, keyOrigin);
                    }
                } catch (Bip32Exception ignore) {
                }
            }
        }

        return originMap.size() > 0 ? originMap : null;
    }

    public boolean canSign(HDKey key) {
        return getCanSignOriginMap(key) != null;
    }

    public Map<PubKey, KeyOrigin> getOriginMap() {
        return originMap;
    }

    public Map<PubKey, byte[]> getSignatureMap() {
        return signatureMap;
    }

    public byte[] getWitnessScript() {
        return witnessScript;
    }

    public boolean isSegwit() {
        return isSegwit;
    }

    public Long getAmount() {
        return amount;
    }
}
