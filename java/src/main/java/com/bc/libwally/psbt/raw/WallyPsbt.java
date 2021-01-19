package com.bc.libwally.psbt.raw;

import com.bc.libwally.tx.raw.WallyTx;

public class WallyPsbt {

    private final byte[] magic;

    private final WallyTx tx;

    private final WallyPsbtInput[] inputs;

    private int inputsAllocLength;

    private final WallyPsbtOutput[] outputs;

    private int outputsAllocLength;

    private WallyMap unknowns;

    private long version;

    WallyPsbt(byte[] magic,
              WallyTx tx,
              WallyPsbtInput[] inputs,
              int inputsAllocLength,
              WallyPsbtOutput[] outputs,
              int outputsAllocLength,
              WallyMap unknowns,
              long version) {
        this.magic = magic;
        this.tx = tx;
        this.inputs = inputs;
        this.inputsAllocLength = inputsAllocLength;
        this.outputs = outputs;
        this.outputsAllocLength = outputsAllocLength;
        this.unknowns = unknowns;
        this.version = version;
    }

    public byte[] getMagic() {
        return magic;
    }

    public WallyTx getTx() {
        return tx;
    }

    public WallyPsbtInput[] getInputs() {
        return inputs;
    }

    public int getInputsAllocLength() {
        return inputsAllocLength;
    }

    public WallyPsbtOutput[] getOutputs() {
        return outputs;
    }

    public int getOutputsAllocLength() {
        return outputsAllocLength;
    }

    public WallyMap getUnknowns() {
        return unknowns;
    }

    public long getVersion() {
        return version;
    }
}
