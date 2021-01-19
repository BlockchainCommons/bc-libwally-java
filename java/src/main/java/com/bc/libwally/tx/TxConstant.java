package com.bc.libwally.tx;

public class TxConstant {
    public static final int WALLY_TX_SEQUENCE_FINAL = 0xffffffff;

    public static final int WALLY_TX_VERSION_1 = 1;

    public static final int WALLY_TX_VERSION_2 = 2;

    public static final int WALLY_TX_IS_ELEMENTS = 1;

    public static final int WALLY_TX_IS_ISSUANCE = 2;

    public static final int WALLY_TX_IS_PEGIN = 4;

    public static final int WALLY_TX_IS_COINBASE = 8;

    public static final int WALLY_SATOSHI_PER_BTC = 100000000;

    public static final int WALLY_BTC_MAX = 21000000;

    public static final int WALLY_TXHASH_LEN = 32;

    public static final int WALLY_TX_FLAG_USE_WITNESS = 0x1;

    public static final int WALLY_TX_FLAG_USE_ELEMENTS = 0x2;

    public static final int WALLY_TX_FLAG_ALLOW_PARTIAL = 0x4;

    public static final int WALLY_TX_FLAG_BLINDED_INITIAL_ISSUANCE = 0x1;

    public static final int WALLY_TX_DUMMY_NULL = 0x1;

    public static final int WALLY_TX_DUMMY_SIG = 0x2;

    public static final int WALLY_TX_DUMMY_SIG_LOW_R = 0x4;

    public static final int WALLY_SIGHASH_ALL = 0x01;

    public static final int WALLY_SIGHASH_NONE = 0x02;

    public static final int WALLY_SIGHASH_SINGLE = 0x03;

    public static final int WALLY_SIGHASH_FORKID = 0x40;

    public static final int WALLY_SIGHASH_ANYONECANPAY = 0x80;

    public static final int WALLY_TX_ASSET_CT_VALUE_PREFIX_A = 8;

    public static final int WALLY_TX_ASSET_CT_VALUE_PREFIX_B = 9;

    public static final int WALLY_TX_ASSET_CT_ASSET_PREFIX_A = 10;

    public static final int WALLY_TX_ASSET_CT_ASSET_PREFIX_B = 11;

    public static final int WALLY_TX_ASSET_CT_NONCE_PREFIX_A = 2;

    public static final int WALLY_TX_ASSET_CT_NONCE_PREFIX_B = 3;

    public static final int WALLY_TX_ASSET_TAG_LEN = 32;

    public static final int WALLY_TX_ASSET_CT_VALUE_LEN = 33;

    public static final int WALLY_TX_ASSET_CT_VALUE_UNBLIND_LEN = 9;

    public static final int WALLY_TX_ASSET_CT_ASSET_LEN = 33;

    public static final int WALLY_TX_ASSET_CT_NONCE_LEN = 33;

    public static final int WALLY_TX_ASSET_CT_LEN = 33;

    public static final int WALLY_TX_ISSUANCE_FLAG = (1 << 31);

    public static final int WALLY_TX_PEGIN_FLAG = (1 << 30);

    public static final int WALLY_TX_INDEX_MASK = 0x3fffffff;
}
