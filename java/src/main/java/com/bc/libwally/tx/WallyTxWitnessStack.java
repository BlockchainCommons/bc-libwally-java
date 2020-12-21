package com.bc.libwally.tx;

import com.bc.libwally.script.Witness;

import static com.bc.libwally.ArrayUtils.append;
import static com.bc.libwally.tx.TxConstant.WALLY_SIGHASH_ALL;
import static com.bc.libwally.tx.TxJni.wally_tx_witness_stack_init_alloc;
import static com.bc.libwally.tx.TxJni.wally_tx_witness_stack_set;

public class WallyTxWitnessStack implements Cloneable {

    public static WallyTxWitnessStack create(Witness witness) {
        WallyTxWitnessStack stack = wally_tx_witness_stack_init_alloc(2);
        byte[] sigHashBytes = new byte[]{WALLY_SIGHASH_ALL};
        stack = wally_tx_witness_stack_set(stack, 0, append(witness.getSignature(), sigHashBytes));
        stack = wally_tx_witness_stack_set(stack, 1, witness.getType().getPubKey().getData());
        return stack;
    }

    private final WallyTxWitnessItem[] items;

    private final int itemsAllocLength;

    WallyTxWitnessStack(WallyTxWitnessItem[] items, int itemsAllocLength) {
        this.items = items;
        this.itemsAllocLength = itemsAllocLength;
    }

    public WallyTxWitnessItem[] getItems() {
        return items;
    }

    public int getItemsAllocLength() {
        return itemsAllocLength;
    }

    @Override
    protected WallyTxWitnessStack clone() throws CloneNotSupportedException {
        WallyTxWitnessStack stack = (WallyTxWitnessStack) super.clone();
        WallyTxWitnessItem[] items = new WallyTxWitnessItem[this.items.length];
        for (int i = 0; i < this.items.length; i++) {
            items[i] = this.items[i].clone();
        }
        return new WallyTxWitnessStack(items, stack.itemsAllocLength);
    }

    public static class WallyTxWitnessItem implements Cloneable {

        private final byte[] witness;

        WallyTxWitnessItem(byte[] witness) {
            this.witness = witness;
        }

        public byte[] getWitness() {
            return witness;
        }

        @Override
        protected WallyTxWitnessItem clone() throws CloneNotSupportedException {
            return (WallyTxWitnessItem) super.clone();
        }
    }
}
