package com.bc.libwally.psbt.raw;

public class WallyPsbtOutput {

    private final byte[] redeemScript;

    private final byte[] witnessScript;

    private final WallyMap keyPaths;

    private final WallyMap unknowns;

    WallyPsbtOutput(byte[] redeemScript,
                    byte[] witnessScript,
                    WallyMap keyPaths,
                    WallyMap unknowns) {
        this.redeemScript = redeemScript;
        this.witnessScript = witnessScript;
        this.keyPaths = keyPaths;
        this.unknowns = unknowns;
    }

    public byte[] getRedeemScript() {
        return redeemScript;
    }

    public byte[] getWitnessScript() {
        return witnessScript;
    }

    public WallyMap getKeyPaths() {
        return keyPaths;
    }

    public WallyMap getUnknowns() {
        return unknowns;
    }
}
