package com.bc.libwally.psbt.raw;

import com.bc.libwally.tx.WallyTx;
import com.bc.libwally.tx.WallyTxOutput;
import com.bc.libwally.tx.WallyTxWitnessStack;

public class WallyPsbtInput {

    private final WallyTx utxo;

    private final WallyTxOutput witnessUtxo;

    private final byte[] redeemScript;

    private final byte[] witnessScript;

    private final byte[] finalScriptSig;

    private final WallyTxWitnessStack finalWitness;

    private final WallyMap keyPaths;

    private final WallyMap signatures;

    private final WallyMap unknowns;

    private long sigHash;

    WallyPsbtInput(WallyTx utxo,
                   WallyTxOutput witnessUtxo,
                   byte[] redeemScript,
                   byte[] witnessScript,
                   byte[] finalScriptSig,
                   WallyTxWitnessStack finalWitness,
                   WallyMap keyPaths,
                   WallyMap signatures,
                   WallyMap unknowns) {
        this.utxo = utxo;
        this.witnessUtxo = witnessUtxo;
        this.redeemScript = redeemScript;
        this.witnessScript = witnessScript;
        this.finalScriptSig = finalScriptSig;
        this.finalWitness = finalWitness;
        this.keyPaths = keyPaths;
        this.signatures = signatures;
        this.unknowns = unknowns;
    }

    public WallyTx getUtxo() {
        return utxo;
    }

    public WallyTxOutput getWitnessUtxo() {
        return witnessUtxo;
    }

    public byte[] getRedeemScript() {
        return redeemScript;
    }

    public byte[] getWitnessScript() {
        return witnessScript;
    }

    public byte[] getFinalScriptSig() {
        return finalScriptSig;
    }

    public WallyTxWitnessStack getFinalWitness() {
        return finalWitness;
    }

    public WallyMap getKeyPaths() {
        return keyPaths;
    }

    public WallyMap getSignatures() {
        return signatures;
    }

    public WallyMap getUnknowns() {
        return unknowns;
    }

    public long getSigHash() {
        return sigHash;
    }
}
