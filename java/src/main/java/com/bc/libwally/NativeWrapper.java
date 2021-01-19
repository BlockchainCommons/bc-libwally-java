package com.bc.libwally;

public abstract class NativeWrapper implements AutoCloseable {

    protected JniObject ptrObj;

    public NativeWrapper(JniObject ptrObj) {
        this.ptrObj = ptrObj;
    }

    public boolean isClosed() {
        return ptrObj == null;
    }

    public static class JniObject {

        private final transient long ptr;

        JniObject(final long ptr) {
            this.ptr = ptr;
        }

        long getPtr() {
            return ptr;
        }
    }
}