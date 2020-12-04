package com.bc.libwally;

import com.bc.libwally.bip39.Bip39Entropy;
import com.bc.libwally.bip39.Bip39Exception;
import com.bc.libwally.bip39.Bip39Mnemonic;
import com.bc.libwally.bip39.Bip39Seed;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static com.bc.libwally.util.TestUtils.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class Bip39Test {

    private static final String validMnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    private static final String validMnemonic24 = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon art";

    @Test
    public void testGetWordList() {
        String[] bip39Words = Bip39Mnemonic.getBip39Words();
        assertEquals(2048, bip39Words.length);
        assertEquals("abandon", bip39Words[0]);
    }

    @Test
    public void testMnemonicIsValid() {
        assertTrue(Bip39Mnemonic.isValid(validMnemonic));
        assertFalse(Bip39Mnemonic.isValid("notavalidword"));
        assertFalse(Bip39Mnemonic.isValid("abandon"));
        assertFalse(Bip39Mnemonic.isValid(new String[]{"abandon", "abandon"}));
    }

    @Test
    public void testInitializeMnemonic() {
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(validMnemonic);
        assertTrue(Arrays.deepEquals(validMnemonic.split(" "), mnemonic.getWords()));
    }

    @Test
    public void testInitializeMnemonicFromBytes() {
        byte[] bytes = new byte[32];
        Bip39Entropy entropy = new Bip39Entropy(bytes);
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(entropy);
        assertTrue(Arrays.deepEquals(validMnemonic24.split(" "), mnemonic.getWords()));
    }

    @Test
    public void testInitializeInvalidMnemonic() {
        assertThrows("",
                     Bip39Exception.class,
                     () -> new Bip39Mnemonic(new String[]{"notavalidword"}));
    }

    @Test
    public void testMnemonicLosslessStringConvertible() {
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(validMnemonic);
        assertEquals(validMnemonic, mnemonic.getMnemonic());
    }

    @Test
    public void testMnemonicToEntropy() {
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(validMnemonic);
        assertEquals("00000000000000000000000000000000", mnemonic.getEntropy().getHexData());
        Bip39Mnemonic mnemonic2 = new Bip39Mnemonic(
                "legal winner thank year wave sausage worth useful legal winner thank yellow");
        assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", mnemonic2.getEntropy().getHexData());
    }

    @Test
    public void testEntropyToMnemonic() {
        Bip39Mnemonic expectedMnemonic = new Bip39Mnemonic(
                "legal winner thank year wave sausage worth useful legal winner thank yellow");
        Bip39Entropy entropy = new Bip39Entropy("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f");
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(entropy);
        assertEquals(expectedMnemonic, mnemonic);
    }

    @Test
    public void testEntropyLosslessStringConvertible() {
        Bip39Entropy entropy = new Bip39Entropy("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f");
        assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", entropy.getHexData());
    }

    @Test
    public void testMnemonicToSeedHexString() {
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(validMnemonic);
        assertEquals(
                "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04",
                mnemonic.getSeedHex("TREZOR").getHexData());
        assertEquals(
                "5eb00bbddcf069084889a8ab9155568165f5c453ccb85e70811aaed6f6da5fc19a5ac40b389cd370d086206dec8aa6c43daea6690f20ad3d8d48b2d2ce9e38e4",
                mnemonic.getSeedHex().getHexData());
        assertEquals(
                "5eb00bbddcf069084889a8ab9155568165f5c453ccb85e70811aaed6f6da5fc19a5ac40b389cd370d086206dec8aa6c43daea6690f20ad3d8d48b2d2ce9e38e4",
                mnemonic.getSeedHex("").getHexData());
    }

    @Test
    public void testSeedLosslessStringConvertible() {
        Bip39Mnemonic mnemonic = new Bip39Mnemonic(validMnemonic);
        Bip39Seed expectedSeed = mnemonic.getSeedHex("TREZOR");
        Bip39Seed actualSeed = new Bip39Seed(
                "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04");
        assertEquals(expectedSeed, actualSeed);
    }

}
