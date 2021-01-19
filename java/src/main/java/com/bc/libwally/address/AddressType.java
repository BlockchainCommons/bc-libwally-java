package com.bc.libwally.address;

public enum AddressType {
    PAY_TO_PUBKEY_HASH, // P2PKH
    PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH, // P2SH-P2WPKH
    PAY_TO_WITNESS_PUBKEY_HASH // P2WPKH
}
