package com.bc.libwally.tx;

public class WallyTxOutput implements Cloneable {

    private final long satoshi;

    private final byte[] script;

    private final short features;

    WallyTxOutput(long satoshi, byte[] script, short features) {
        this.satoshi = satoshi;
        this.script = script;
        this.features = features;
    }

    public long getSatoshi() {
        return satoshi;
    }

    public byte[] getScript() {
        return script;
    }

    public short getFeatures() {
        return features;
    }

    @Override
    protected WallyTxOutput clone() throws CloneNotSupportedException {
        return (WallyTxOutput) super.clone();
    }
}
