package com.bc.libwally.psbt;

import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Derivation;
import com.bc.libwally.bip32.Bip32Exception;
import com.bc.libwally.bip32.Bip32Path;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.psbt.raw.WallyPsbtOutput;
import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.tx.TxOutput;
import com.bc.libwally.tx.WallyTxOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.bc.libwally.ArrayUtils.reversed;

public class PsbtOutput {

    private final TxOutput txOutput;

    private final Map<PubKey, KeyOrigin> originMap;

    PsbtOutput(WallyPsbtOutput psbtOutput, WallyTxOutput txOutput, Network network) {
        if (psbtOutput.getKeyPaths().hasValue()) {
            this.originMap = KeyOrigin.getOriginMap(psbtOutput.getKeyPaths(), network);
        } else {
            this.originMap = null;
        }

        ScriptPubKey scriptPubKey = new ScriptPubKey(psbtOutput.getWitnessScript() != null
                                                     ? psbtOutput.getWitnessScript()
                                                     : txOutput.getScript());

        this.txOutput = new TxOutput(scriptPubKey, txOutput.getSatoshi(), network);
    }

    private static boolean commonOriginChecks(KeyOrigin origin,
                                              PubKey pubKey,
                                              HDKey signer,
                                              HDKey[] cosigners) {
        // Check that origin ends with 0/* or 1/*
        Bip32Derivation[] components = origin.getPath().getComponents();
        if (components.length < 2 ||
            !(Bip32Derivation.newNormal(0).equals(reversed(components)[1]) ||
              Bip32Derivation.newNormal(1).equals(reversed(components)[1]))) {
            return false;
        }

        if (signer.getMasterFingerprint() == null) {
            return false;
        }

        HDKey hdKey = null;
        if (Arrays.equals(signer.getMasterFingerprint(), origin.getFingerprint())) {
            hdKey = signer;
        } else {
            for (HDKey cosigner : cosigners) {
                if (cosigner.getMasterFingerprint() == null) {
                    return false;
                }

                if (Arrays.equals(cosigner.getMasterFingerprint(), origin.getFingerprint())) {
                    hdKey = cosigner;
                    break;
                }
            }
        }

        if (hdKey == null) {
            return false;
        }

        HDKey childKey;
        try {
            childKey = hdKey.derive(origin.getPath());
        } catch (Bip32Exception ignore) {
            return false;
        }

        return childKey.getPubKey().equals(pubKey);
    }

    public boolean isChange(HDKey signer, PsbtInput[] inputs, HDKey cosigner, int threshold) {
        return isChange(signer, inputs, new HDKey[]{cosigner}, threshold);
    }

    public boolean isChange(HDKey signer, PsbtInput[] inputs, HDKey[] cosigners, int threshold) {
        // Transaction must have at least one input
        if (inputs.length < 1) {
            return false;
        }

        // All inputs must have origin info
        for (PsbtInput input : inputs) {
            if (input.getOriginMap() == null) {
                return false;
            }
        }

        // Skip key derivation root
        Bip32Path keyPath = new ArrayList<>(inputs[0].getOriginMap().entrySet()).get(0)
                                                                                .getValue()
                                                                                .getPath();
        if (keyPath.getComponents().length < 2) {
            return false;
        }

        for (PsbtInput input : inputs) {
            // Check that we can sign all inputs (TODO: relax assumption for e.g. coinjoin)
            if (!input.canSign(signer)) {
                return false;
            }

            if (input.getOriginMap() == null) {
                return false;
            }

            for (Map.Entry<PubKey, KeyOrigin> e : input.getOriginMap().entrySet()) {
                if (!commonOriginChecks(e.getValue(), e.getKey(), signer, cosigners)) {
                    return false;
                }
            }
        }

        // Check outputs
        if (originMap == null) {
            return false;
        }

        Long changeIndex = null;
        for (Map.Entry<PubKey, KeyOrigin> e : originMap.entrySet()) {
            if (!commonOriginChecks(e.getValue(), e.getKey(), signer, cosigners)) {
                return false;
            }

            // Check that the output index is reasonable
            // When combined with the above constraints, change "hijacked" to an extreme index can
            // be covered by importing keys using Bitcoin Core's maximum range [0,999999].
            // This needs less than 1 GB of RAM, but is fairly slow.

            Bip32Derivation component = (Bip32Derivation) reversed(e.getValue()
                                                                    .getPath()
                                                                    .getComponents())[0];
            if (component.getIndex() > 999999) {
                return false;
            }

            // Change index must be the same for all origins
            if (changeIndex != null && component.getIndex() != changeIndex) {
                return false;
            } else {
                changeIndex = component.getIndex();
            }
        }

        // Check scriptPubKey
        if (txOutput.getScriptPubKey().getType() == ScriptPubKey.ScriptType.MULTI_SIG) {
            PubKey[] keys = originMap.keySet().toArray(new PubKey[0]);
            ScriptPubKey expectedScriptPubKey = new ScriptPubKey(keys, threshold);
            return expectedScriptPubKey.equals(txOutput.getScriptPubKey());
        } else {
            return false;
        }
    }

    public TxOutput getTxOutput() {
        return txOutput;
    }

    public Map<PubKey, KeyOrigin> getOriginMap() {
        return originMap;
    }
}
