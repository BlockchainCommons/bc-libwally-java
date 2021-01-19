package com.bc.libwally.bip32;

public class Bip32Exception extends IllegalStateException {
    Bip32Exception(String message) {
        super(message);
    }

    Bip32Exception(Bip32Error error) {
        this(error.name());
    }
}
