package com.bc.libwally.psbt.raw;

public class WallyMap {

    private final WallyMapItem[] items;

    private final int itemsAllocLength;

    WallyMap(WallyMapItem[] items, int itemsAllocLen) {
        this.items = items;
        this.itemsAllocLength = itemsAllocLen;
    }

    public WallyMapItem[] getItems() {
        return items;
    }

    public int getItemsAllocLength() {
        return itemsAllocLength;
    }

    public int size() {
        return items.length;
    }

    public boolean hasValue() {
        return size() > 0;
    }

    public static class WallyMapItem {

        private final byte[] key;

        private final byte[] value;

        WallyMapItem(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }
    }
}
