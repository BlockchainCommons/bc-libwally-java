package com.bc.libwally.bip32;

public class WallyHDKey {

    private final byte[] chainCode;

    private final byte[] parent160;

    private final short depth;

    private final byte[] pad1;

    private final byte[] privKey;

    private final long childNum;

    private final byte[] hash160;

    private final long version;

    private final byte[] pad2;

    private final byte[] pubKey;

    WallyHDKey(byte[] chainCode,
               byte[] parent160,
               short depth,
               byte[] pad1,
               byte[] privKey,
               long childNum,
               byte[] hash160,
               long version,
               byte[] pad2,
               byte[] pubKey) {
        this.chainCode = chainCode;
        this.parent160 = parent160;
        this.depth = depth;
        this.pad1 = pad1;
        this.privKey = privKey;
        this.childNum = childNum;
        this.hash160 = hash160;
        this.version = version;
        this.pad2 = pad2;
        this.pubKey = pubKey;
    }

    public byte[] getChainCode() {
        return chainCode;
    }

    public byte[] getParent160() {
        return parent160;
    }

    public short getDepth() {
        return depth;
    }

    public byte[] getPad1() {
        return pad1;
    }

    public byte[] getPrivKey() {
        return privKey;
    }

    public long getChildNum() {
        return childNum;
    }

    public byte[] getHash160() {
        return hash160;
    }

    public long getVersion() {
        return version;
    }

    public byte[] getPad2() {
        return pad2;
    }

    public byte[] getPubKey() {
        return pubKey;
    }
}
