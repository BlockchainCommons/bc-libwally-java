package com.bc.libwally;

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
        byte[] ab = append(a, b);
        return append(ab, c);
    }

    public static byte[] append(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) {
        byte[] abc = append(a, b, c);
        byte[] de = append(d, e);
        return append(abc, de);
    }

}
