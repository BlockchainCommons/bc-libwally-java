package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.core.Core.*
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreTest {

    @Test
    fun testBytes2Hex() {
        val bytes = byteArrayOf(
            0x01,
            0xAF.toByte(),
            0x5F,
            0xF2.toByte(),
            0x9A.toByte(),
            0x12,
            0x89.toByte(),
            0xAA.toByte(),
            0xBA.toByte(),
            0x45,
            0x00
        )
        val expectedHex = "01af5ff29a1289aaba4500"
        assertEquals(expectedHex, bytes2Hex(bytes))
    }

    @Test
    fun testHex2Bytes() {
        val hex = "01af5ff29a1289aaba4500"
        val expectedBytes = byteArrayOf(
            0x01,
            0xAF.toByte(),
            0x5F,
            0xF2.toByte(),
            0x9A.toByte(),
            0x12,
            0x89.toByte(),
            0xAA.toByte(),
            0xBA.toByte(),
            0x45,
            0x00
        )
        assertArrayEquals(expectedBytes, hex2Bytes(hex))
    }

    @Test
    fun testBytes2Base58() {
        val bytes = byteArrayOf(
            0x01,
            0xAF.toByte(),
            0x5F,
            0xF2.toByte(),
            0x9A.toByte(),
            0x12,
            0x89.toByte(),
            0xAA.toByte(),
            0xBA.toByte(),
            0x45,
            0x00
        )
        val expectedBase58 = "3jb7AeBHgv74mmN2YbA2"
        assertEquals(expectedBase58, bytes2Base58(bytes))
    }

    @Test
    fun testBase582Bytes() {
        val base58 = "3jb7AeBHgv74mmN2YbA2"
        val expectedBytes = byteArrayOf(
            0x01,
            0xAF.toByte(),
            0x5F,
            0xF2.toByte(),
            0x9A.toByte(),
            0x12,
            0x89.toByte(),
            0xAA.toByte(),
            0xBA.toByte(),
            0x45,
            0x00
        )
        assertArrayEquals(expectedBytes, base582Bytes(base58))
    }

    @Test
    fun testBytes2Base64() {
        val bytes = hex2Bytes("013af3d1a34bac2aa113ad2aabb2a2598013")
        val expectedBase64 = "ATrz0aNLrCqhE60qq7KiWYAT"
        assertEquals(expectedBase64, bytes2Base64(bytes))
    }

    @Test
    fun testBase642Bytes() {
        val base64 = "ATrz0aNLrCqhE60qq7KiWYAT"
        val expectedBytes = hex2Bytes("013af3d1a34bac2aa113ad2aabb2a2598013")
        assertArrayEquals(expectedBytes, base642Bytes(base64))
    }
}