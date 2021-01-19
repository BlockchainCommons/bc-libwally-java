package com.bc.libwally;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.bc.libwally.core.Core.base582Bytes;
import static com.bc.libwally.core.Core.base642Bytes;
import static com.bc.libwally.core.Core.bytes2Base58;
import static com.bc.libwally.core.Core.bytes2Base64;
import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CoreTest {

    @Test
    public void testBytes2Hex() {
        byte[] bytes = new byte[]{0x01,
                                  (byte) 0xAF,
                                  0x5F,
                                  (byte) 0xF2,
                                  (byte) 0x9A,
                                  0x12,
                                  (byte) 0x89,
                                  (byte) 0xAA,
                                  (byte) 0xBA,
                                  0x45,
                                  0x00};

        String expectedHex = "01af5ff29a1289aaba4500";
        assertEquals(expectedHex, bytes2Hex(bytes));
    }

    @Test
    public void testHex2Bytes() {
        String hex = "01af5ff29a1289aaba4500";
        byte[] expectedBytes = new byte[]{0x01,
                                          (byte) 0xAF,
                                          0x5F,
                                          (byte) 0xF2,
                                          (byte) 0x9A,
                                          0x12,
                                          (byte) 0x89,
                                          (byte) 0xAA,
                                          (byte) 0xBA,
                                          0x45,
                                          0x00};
        assertArrayEquals(expectedBytes, hex2Bytes(hex));
    }

    @Test
    public void testBytes2Base58() {
        byte[] bytes = new byte[]{0x01,
                                  (byte) 0xAF,
                                  0x5F,
                                  (byte) 0xF2,
                                  (byte) 0x9A,
                                  0x12,
                                  (byte) 0x89,
                                  (byte) 0xAA,
                                  (byte) 0xBA,
                                  0x45,
                                  0x00};
        String expectedBase58 = "3jb7AeBHgv74mmN2YbA2";
        assertEquals(expectedBase58, bytes2Base58(bytes));
    }

    @Test
    public void testBase582Bytes() {
        String base58 = "3jb7AeBHgv74mmN2YbA2";
        byte[] expectedBytes = new byte[]{0x01,
                                          (byte) 0xAF,
                                          0x5F,
                                          (byte) 0xF2,
                                          (byte) 0x9A,
                                          0x12,
                                          (byte) 0x89,
                                          (byte) 0xAA,
                                          (byte) 0xBA,
                                          0x45,
                                          0x00};
        assertArrayEquals(expectedBytes, base582Bytes(base58));
    }

    @Test
    public void testBytes2Base64() {
        byte[] bytes = hex2Bytes("013af3d1a34bac2aa113ad2aabb2a2598013");
        String expectedBase64 = "ATrz0aNLrCqhE60qq7KiWYAT";
        assertEquals(expectedBase64, bytes2Base64(bytes));
    }

    @Test
    public void testBase642Bytes() {
        String base64 = "ATrz0aNLrCqhE60qq7KiWYAT";
        byte[] expectedBytes = hex2Bytes("013af3d1a34bac2aa113ad2aabb2a2598013");
        assertArrayEquals(expectedBytes, base642Bytes(base64));
    }

}
