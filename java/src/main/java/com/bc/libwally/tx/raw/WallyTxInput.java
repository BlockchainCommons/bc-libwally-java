package com.bc.libwally.tx.raw;

public class WallyTxInput implements Cloneable {

    private final byte[] txHash;

    private final long index;

    private final long sequence;

    private final byte[] script;

    private final WallyTxWitnessStack witness;

    private final short features;

    WallyTxInput(byte[] txHash,
                 long index,
                 long sequence,
                 byte[] script,
                 WallyTxWitnessStack witness,
                 short features) {
        this.txHash = txHash;
        this.index = index;
        this.sequence = sequence;
        this.script = script;
        this.witness = witness;
        this.features = features;
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public long getIndex() {
        return index;
    }

    public long getSequence() {
        return sequence;
    }

    public byte[] getScript() {
        return script;
    }

    public WallyTxWitnessStack getWitness() {
        return witness;
    }

    public short getFeatures() {
        return features;
    }

    @Override
    protected WallyTxInput clone() throws CloneNotSupportedException {
        WallyTxInput input = (WallyTxInput) super.clone();
        return new WallyTxInput(input.txHash,
                                input.index,
                                input.sequence,
                                input.script,
                                witness != null ? witness.clone() : null,
                                input.features);
    }
}
