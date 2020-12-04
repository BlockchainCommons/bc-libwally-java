package com.bc.libwally;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.Crypto.ecPubKeyDecompress;
import static com.bc.libwally.crypto.Crypto.ecPubKeyFromPrvKey;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CryptoTest {

    @Test
    public void testEcPubKeyFromPrvKey() {
        byte[] prvKey = hex2Bytes("30ef3d794cd7f3439a8a1d97c6cfcdd66f5ebd014094bf95105e623c69576f2f");
        byte[] pubKey = ecPubKeyFromPrvKey(prvKey);
        assertEquals("0359d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b",
                     bytes2Hex(pubKey));
    }

    @Test
    public void testEcPubKeyDecompress() {
        byte[] pubKey = hex2Bytes(
                "0359d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b");
        byte[] decompressedPubKey = ecPubKeyDecompress(pubKey);
        assertEquals(
                "0459d56f9de85d8541b5e205b2241c21e7bbb7cb0bf66427c9323dd3289e34d47b262b62de697d6ea343cbd245e3ad67ae60804e423077828b6ffd98028acaf693",
                bytes2Hex(decompressedPubKey));
    }
}
