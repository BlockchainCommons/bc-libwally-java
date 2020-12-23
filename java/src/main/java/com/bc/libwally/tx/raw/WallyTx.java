package com.bc.libwally.tx.raw;


public class WallyTx implements Cloneable {

    private final long version;

    private final long locktime;

    private final WallyTxInput[] inputs;

    private final WallyTxOutput[] outputs;

    // for keeping the pre-allocation `inputs_allocation_len` from native `wally_tx`
    private final int inputsAllocLength;

    // for keeping the pre-allocation `outputs_allocation_len` from native `wally_tx`
    private final int outputsAllocLength;

    WallyTx(long version,
            long locktime,
            WallyTxInput[] inputs,
            WallyTxOutput[] outputs,
            int inputsAllocLength,
            int outputsAllocLength) {
        this.version = version;
        this.locktime = locktime;
        this.inputs = inputs;
        this.outputs = outputs;
        this.inputsAllocLength = inputsAllocLength;
        this.outputsAllocLength = outputsAllocLength;
    }

    public long getVersion() {
        return version;
    }

    public long getLocktime() {
        return locktime;
    }

    public WallyTxInput[] getInputs() {
        return inputs;
    }

    public WallyTxOutput[] getOutputs() {
        return outputs;
    }

    public int getInputsAllocLength() {
        return inputsAllocLength;
    }

    public int getOutputsAllocLength() {
        return outputsAllocLength;
    }

    @Override
    protected WallyTx clone() throws CloneNotSupportedException {
        WallyTx tx = (WallyTx) super.clone();
        WallyTxInput[] clonedInputs = new WallyTxInput[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            clonedInputs[i] = inputs[i].clone();
        }

        WallyTxOutput[] clonedOutputs = new WallyTxOutput[inputs.length];
        for (int i = 0; i < outputs.length; i++) {
            clonedOutputs[i] = outputs[i].clone();
        }

        return new WallyTx(tx.version,
                           tx.locktime,
                           clonedInputs,
                           clonedOutputs,
                           tx.inputsAllocLength,
                           tx.outputsAllocLength);
    }
}
