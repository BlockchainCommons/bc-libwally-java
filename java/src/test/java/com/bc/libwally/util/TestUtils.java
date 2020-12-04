package com.bc.libwally.util;

public class TestUtils {

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertThrows(String msg,
                                                       Class<T> expectedError,
                                                       Callable callable) {
        try {
            callable.call();
            throw new RuntimeException(msg);
        } catch (Throwable e) {
            if (expectedError.isAssignableFrom(e.getClass()))
                return (T) e;
            throw e;
        }
    }
}
