package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.bip39.Bip39Entropy
import com.bc.libwally.bip39.Bip39Exception
import com.bc.libwally.bip39.Bip39Mnemonic
import com.bc.libwally.bip39.Bip39Seed
import com.bc.libwally.util.assertThrows
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class Bip39Test {

    private val validMnemonic =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
    private val validMnemonic24 =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon art"

    @Test
    fun testGetWordList() {
        val bip39Words = Bip39Mnemonic.getBip39Words()
        assertEquals(2048, bip39Words.size.toLong())
        assertEquals("abandon", bip39Words[0])
    }

    @Test
    fun testMnemonicIsValid() {
        assertTrue(Bip39Mnemonic.isValid(validMnemonic))
        assertFalse(Bip39Mnemonic.isValid("notavalidword"))
        assertFalse(Bip39Mnemonic.isValid("abandon"))
        assertFalse(Bip39Mnemonic.isValid(arrayOf("abandon", "abandon")))
    }

    @Test
    fun testInitializeMnemonic() {
        val mnemonic = Bip39Mnemonic(validMnemonic)
        assertTrue(Arrays.deepEquals(validMnemonic.split(" ").toTypedArray(), mnemonic.words))
    }

    @Test
    fun testInitializeMnemonicFromBytes() {
        val bytes = ByteArray(32)
        val entropy = Bip39Entropy(bytes)
        val mnemonic = Bip39Mnemonic(entropy)
        assertTrue(Arrays.deepEquals(validMnemonic24.split(" ").toTypedArray(), mnemonic.words))
    }

    @Test
    fun testInitializeInvalidMnemonic() {
        assertThrows<Bip39Exception>("") { Bip39Mnemonic(arrayOf("notavalidword")) }
    }

    @Test
    fun testMnemonicLosslessStringConvertible() {
        val mnemonic = Bip39Mnemonic(validMnemonic)
        assertEquals(validMnemonic, mnemonic.mnemonic)
    }

    @Test
    fun testMnemonicToEntropy() {
        val mnemonic = Bip39Mnemonic(validMnemonic)
        assertEquals("00000000000000000000000000000000", mnemonic.entropy.hexData)
        val mnemonic2 =
            Bip39Mnemonic("legal winner thank year wave sausage worth useful legal winner thank yellow")
        assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", mnemonic2.entropy.hexData)
    }

    @Test
    fun testEntropyToMnemonic() {
        val expectedMnemonic =
            Bip39Mnemonic("legal winner thank year wave sausage worth useful legal winner thank yellow")
        val entropy = Bip39Entropy("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f")
        val mnemonic = Bip39Mnemonic(entropy)
        assertEquals(expectedMnemonic, mnemonic)
    }

    @Test
    fun testEntropyLosslessStringConvertible() {
        val entropy = Bip39Entropy("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f")
        assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", entropy.hexData)
    }

    @Test
    fun testMnemonicToSeedHexString() {
        val mnemonic = Bip39Mnemonic(validMnemonic)
        assertEquals(
            "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04",
            mnemonic.getSeedHex("TREZOR").hexData
        )
        assertEquals(
            "5eb00bbddcf069084889a8ab9155568165f5c453ccb85e70811aaed6f6da5fc19a5ac40b389cd370d086206dec8aa6c43daea6690f20ad3d8d48b2d2ce9e38e4",
            mnemonic.seedHex.hexData
        )
        assertEquals(
            "5eb00bbddcf069084889a8ab9155568165f5c453ccb85e70811aaed6f6da5fc19a5ac40b389cd370d086206dec8aa6c43daea6690f20ad3d8d48b2d2ce9e38e4",
            mnemonic.getSeedHex("").hexData
        )
    }

    @Test
    fun testSeedLosslessStringConvertible() {
        val mnemonic = Bip39Mnemonic(validMnemonic)
        val expectedSeed: Bip39Seed = mnemonic.getSeedHex("TREZOR")
        val actualSeed = Bip39Seed(
            "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04"
        )
        assertEquals(expectedSeed, actualSeed)
    }
}