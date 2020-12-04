package com.bc.libwally.address;

public class AddressConstant {

    public static int WALLY_WIF_FLAG_COMPRESSED = 0x0;

    public static int WALLY_WIF_FLAG_UNCOMPRESSED = 0x1;

    public static int WALLY_CA_PREFIX_LIQUID = 0x0c;

    public static int WALLY_CA_PREFIX_LIQUID_REGTEST = 0x04;

    public static int WALLY_NETWORK_BITCOIN_MAINNET = 0x01;

    public static int WALLY_NETWORK_BITCOIN_TESTNET = 0x02;

    public static int WALLY_NETWORK_LIQUID = 0x03;

    public static int WALLY_NETWORK_LIQUID_REGTEST = 0x04;

    public static int WALLY_ADDRESS_TYPE_P2PKH = 0x01;

    public static int WALLY_ADDRESS_TYPE_P2SH_P2WPKH = 0x02;

    public static int WALLY_ADDRESS_TYPE_P2WPKH = 0x04;

    public static int WALLY_ADDRESS_VERSION_P2PKH_MAINNET = 0x00;

    public static int WALLY_ADDRESS_VERSION_P2PKH_TESTNET = 0x6F;

    public static int WALLY_ADDRESS_VERSION_P2PKH_LIQUID = 0x39;

    public static int WALLY_ADDRESS_VERSION_P2PKH_LIQUID_REGTEST = 0xEB;

    public static int WALLY_ADDRESS_VERSION_P2SH_MAINNET = 0x05;

    public static int WALLY_ADDRESS_VERSION_P2SH_TESTNET = 0xC4;

    public static int WALLY_ADDRESS_VERSION_P2SH_LIQUID = 0x27;

    public static int WALLY_ADDRESS_VERSION_P2SH_LIQUID_REGTEST = 0x4B;

    public static int WALLY_ADDRESS_VERSION_WIF_MAINNET = 0x80;

    public static int WALLY_ADDRESS_VERSION_WIF_TESTNET = 0xEF;
}
