package com.bc.libwally;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ArrayUtils {

    public static String joinToString(String[] array, String delimiter) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    public static byte[] append(byte[] a, byte[] b) {
        byte[] ab = new byte[a.length + b.length];
        System.arraycopy(a, 0, ab, 0, a.length);
        System.arraycopy(b, 0, ab, a.length, b.length);
        return ab;
    }

    public static byte[] append(byte[] a, byte[] b, byte[] c) {
        return append(append(a, b), c);
    }

    public static byte[] append(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) {
        return append(append(a, b, c), append(d, e));
    }

    public static byte[] slice(byte[] bytes, int count) {
        return slice(bytes, 0, count);
    }

    public static byte[] slice(byte[] bytes, int start, int end) {
        return Arrays.copyOfRange(bytes, start, end);
    }

    public static int bytes2Int(byte[] bytes, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getInt();
    }

    public static long toUnsignedLong(int value) {
        return value & 4294967295L;
    }

    public static byte[] reversed(byte[] array) {
        if (array == null) {
            return null;
        }

        byte[] clone = new byte[array.length];
        System.arraycopy(array, 0, clone, 0, array.length);

        int i = 0;
        int j = clone.length - 1;
        byte tmp;
        while (j > i) {
            tmp = clone[j];
            clone[j] = clone[i];
            clone[i] = tmp;
            j--;
            i++;
        }
        return clone;
    }

    public static Object[] reversed(Object[] array) {
        if (array == null) {
            return null;
        }

        Object[] clone = new Object[array.length];
        System.arraycopy(array, 0, clone, 0, array.length);

        int i = 0;
        int j = clone.length - 1;
        Object tmp;
        while (j > i) {
            tmp = clone[j];
            clone[j] = clone[i];
            clone[i] = tmp;
            j--;
            i++;
        }
        return clone;
    }

}
