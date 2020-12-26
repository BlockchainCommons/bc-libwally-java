package com.bc.libwally.tx;

import com.bc.libwally.address.Address;
import com.bc.libwally.Network;
import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.tx.raw.WallyTxOutput;

import static com.bc.libwally.tx.TxJni.wally_tx_output_init_alloc;

public class TxOutput implements Cloneable {

    private final ScriptPubKey scriptPubKey;

    private final long amount;

    private final Network network;

    public TxOutput(ScriptPubKey scriptPubKey, long amount, Network network) {
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        this.network = network;
    }

    public String getAddress() {
        return new Address(scriptPubKey, network).getAddress();
    }

    public Network getNetwork() {
        return network;
    }

    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public long getAmount() {
        return amount;
    }

    public WallyTxOutput createWallyTxOutput() {
        return wally_tx_output_init_alloc(amount, scriptPubKey.getData());
    }

    @Override
    public TxOutput clone() throws CloneNotSupportedException {
        TxOutput output = (TxOutput) super.clone();
        return new TxOutput(scriptPubKey.clone(), output.amount, output.network);
    }
}
