package com.bc.libwally.tx;

import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.script.ScriptSig;
import com.bc.libwally.script.ScriptSigType;
import com.bc.libwally.script.Witness;
import com.bc.libwally.tx.raw.WallyTxInput;

import static com.bc.libwally.tx.TxJni.wally_tx_input_init_alloc;

public class TxInput implements Cloneable {

    private final byte[] txHash;

    private final long vout;

    private final long sequence;

    private final long amount;

    private ScriptSig scriptSig;

    private Witness witness;

    private ScriptPubKey scriptPubKey;

    public TxInput(byte[] txHash,
                   long vout,
                   long sequence,
                   long amount,
                   ScriptSig scriptSig,
                   ScriptPubKey scriptPubKey) {
        this(txHash, vout, sequence, amount, scriptPubKey);
        this.scriptSig = scriptSig;
    }

    public TxInput(byte[] txHash,
                   long vout,
                   long amount,
                   ScriptSig scriptSig,
                   ScriptPubKey scriptPubKey) {
        this(txHash, vout, 0xffffffff, amount, scriptSig, scriptPubKey);
    }

    public TxInput(byte[] txHash,
                   long vout,
                   long sequence,
                   long amount,
                   Witness witness,
                   ScriptPubKey scriptPubKey) {
        this(txHash, vout, sequence, amount, scriptPubKey);
        this.witness = witness;
        switch (witness.getType().getType()) {
            case PAY_TO_WITNESS_PUBKEY_HASH:
                this.scriptSig = null;
                break;
            case PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH:
                this.scriptSig = new ScriptSig(ScriptSigType.payToScriptHashPayToWitnessPubKeyHash(
                        witness.getType().getPubKey()));
                break;
        }
    }

    public TxInput(byte[] txHash,
                   long vout,
                   long amount,
                   Witness witness,
                   ScriptPubKey scriptPubKey) {
        this(txHash, vout, 0xffffffff, amount, witness, scriptPubKey);
    }

    private TxInput(byte[] txHash,
                    long vout,
                    long sequence,
                    long amount,
                    ScriptPubKey scriptPubKey) {
        this.txHash = txHash;
        this.vout = vout;
        this.sequence = sequence;
        this.amount = amount;
        this.scriptPubKey = scriptPubKey;
    }

    public WallyTxInput createWallyTxInput() {
        return wally_tx_input_init_alloc(txHash,
                                         vout,
                                         sequence,
                                         null,
                                         witness == null
                                         ? null
                                         : witness.createWallyTxWitnessStack());
    }

    public boolean isSigned() {
        return (this.scriptSig != null && this.scriptSig.getSignature() != null) ||
               (this.witness != null && !this.witness.isDummy());
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public long getVout() {
        return vout;
    }

    public long getSequence() {
        return sequence;
    }

    public long getAmount() {
        return amount;
    }

    public ScriptSig getScriptSig() {
        return scriptSig;
    }

    public Witness getWitness() {
        return witness;
    }

    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public void setWitness(Witness witness) {
        this.witness = witness;
    }

    public void setScriptSig(ScriptSig scriptSig) {
        this.scriptSig = scriptSig;
    }

    @Override
    protected TxInput clone() throws CloneNotSupportedException {
        TxInput input = (TxInput) super.clone();
        input.scriptPubKey = scriptPubKey.clone();
        if (scriptSig != null) {
            input.scriptSig = scriptSig.clone();
        }
        if (witness != null) {
            input.witness = witness.clone();
        }

        return input;
    }
}
