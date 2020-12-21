package com.bc.libwally.tx;

public class TxException extends IllegalStateException {
    TxException(String message) {
        super(message);
    }
}
