package com.bc.libwally.tx.raw;

public class WallyTxWitnessStack implements Cloneable {

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
