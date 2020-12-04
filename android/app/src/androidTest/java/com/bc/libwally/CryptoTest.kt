package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.core.Core
import com.bc.libwally.core.Core.bytes2Hex
import com.bc.libwally.core.Core.hex2Bytes
import com.bc.libwally.crypto.Crypto
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoTest {

    @Test
    fun testEcPubKeyFromPrvKey() {
        val prvKey = hex2Bytes("30ef3d794cd7f3439a8a1d97c6cfcdd66f5ebd014094bf95105e623c69576f2f")
        val pubKey = Crypto.ecPubKeyFromPrvKey(prvKey)
        assertEquals(
            "0359d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b",
            bytes2Hex(pubKey)
        )
    }

    @Test
    fun testEcPubKeyDecompress() {
        val pubKey =
            Core.hex2Bytes("0359d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b")
        val decompressedPubKey = Crypto.ecPubKeyDecompress(pubKey)
        assertEquals(
            "0459d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b262b62de697d6ea343cbd245e3ad67ae60804e423077828b6ffd98028acaf693",
            bytes2Hex(decompressedPubKey)
        )
    }
}