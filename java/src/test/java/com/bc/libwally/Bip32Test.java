package com.bc.libwally;

import com.bc.libwally.bip32.Bip32Derivation;
import com.bc.libwally.bip32.Bip32Error;
import com.bc.libwally.bip32.Bip32Exception;
import com.bc.libwally.bip32.Bip32Path;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip39.Bip39Seed;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.util.TestUtils.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class Bip32Test {

    private static final Bip39Seed seed = new Bip39Seed(
            "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04");

    @Test
    public void testSeedToHDKey() {
        HDKey hdKey = new HDKey(seed);
        assertEquals(
                "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF",
                hdKey.getDescription());
    }

    @Test
    public void testBase58ToHDKey() {
        String xprv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        HDKey hdKey = new HDKey(xprv);
        assertEquals(xprv, hdKey.getDescription());
    }

    @Test
    public void testXpriv() {
        String xprv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        HDKey hdKey = new HDKey(xprv);
        assertEquals(xprv, hdKey.getXprv());
    }

    @Test
    public void testXpub() {
        String xpriv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        String xpub = "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy";
        HDKey hdKey = new HDKey(xpriv);
        assertEquals(xpub, hdKey.getXpub());
    }

    @Test
    public void testTpub() {
        String tpriv = "tprv8gzC1wn3dmCrBiqDFrqhw9XXgy5t4mzeL5SdWayHBHz1GmWbRKoqDBSwDLfunPAWxMqZ9bdGsdpTiYUfYiWypv4Wfj9g7AYX5K3H9gRYNCA";
        String tpub = "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ";
        HDKey hdKey = new HDKey(tpriv);
        assertEquals(tpub, hdKey.getXpub());
    }

    @Test
    public void testPubKey() {
        String xpub = "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy";
        HDKey hdKey = new HDKey(xpub);
        assertEquals("02f632717d78bf73e74aa8461e2e782532abae4eed5110241025afb59ebfd3d2fd",
                     bytes2Hex(hdKey.getPubKey().getData()));
    }

    @Test
    public void testParseXpub() {
        String xpub = "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy";
        HDKey hdKey = new HDKey(xpub);
        assertEquals(xpub, hdKey.getDescription());
        assertEquals(xpub, hdKey.getXpub());
        assertThrows("hdKey::getXprv does not throw Bip32Exception",
                     Bip32Exception.class,
                     hdKey::getXprv);
    }

    @Test
    public void testParseTpub() {
        String tpub = "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ";
        HDKey hdKey = new HDKey(tpub);
        assertEquals(tpub, hdKey.getDescription());
        assertEquals(tpub, hdKey.getXpub());
        assertThrows("hdKey::getXprv does not throw Bip32Exception",
                     Bip32Exception.class,
                     hdKey::getXprv);
    }

    @Test
    public void testFingerprint() {
        HDKey hdKey = new HDKey(seed);
        assertEquals("b4e3f5ed", bytes2Hex(hdKey.getFingerprint()));
    }

    @Test
    public void testMasterKeyFingerprint() {
        HDKey hdKey = new HDKey(seed);
        assertEquals("b4e3f5ed", bytes2Hex(hdKey.getMasterFingerprint()));

        HDKey childKey = new HDKey(seed).derive(new Bip32Path(0));
        assertEquals("b4e3f5ed", bytes2Hex(childKey.getMasterFingerprint()));

        String tpub = "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ";
        HDKey key = new HDKey(tpub, hex2Bytes("b4e3f5ed"));
        assertEquals("b4e3f5ed", bytes2Hex(key.getMasterFingerprint()));
    }

    @Test
    public void testInferFingerprintAtDepthZero() {
        String masterKeyXpriv = "tprv8ZgxMBicQKsPd9TeAdPADNnSyH9SSUUbTVeFszDE23Ki6TBB5nCefAdHkK8Fm3qMQR6sHwA56zqRmKmxnHk37JkiFzvncDqoKmPWubu7hDF";
        HDKey hdKey = new HDKey(masterKeyXpriv);
        assertEquals("d90c6a4f", bytes2Hex(hdKey.getMasterFingerprint()));
    }

    @Test
    public void testRelativePathFromString() {
        Bip32Path path = new Bip32Path("0'/0");
        Bip32Derivation[] expectedComponents = new Bip32Derivation[]{Bip32Derivation.newHardened(0),
                                                                     Bip32Derivation.newNormal(0)};
        assertTrue(Arrays.deepEquals(expectedComponents, path.getComponents()));
        assertEquals("0h/0", path.getPath());
    }

    @Test
    public void testAbsolutePathFromString() {
        Bip32Path path = new Bip32Path("m/0'/0");
        Bip32Derivation[] expectedComponents = new Bip32Derivation[]{Bip32Derivation.newHardened(0),
                                                                     Bip32Derivation.newNormal(0)};
        assertTrue(Arrays.deepEquals(expectedComponents, path.getComponents()));
        assertEquals("m/0h/0", path.getPath());
    }

    @Test
    public void testRelativePathFromInt() {
        Bip32Path path = new Bip32Path(0);
        Bip32Derivation[] expectedComponents = new Bip32Derivation[]{Bip32Derivation.newNormal(0)};
        assertTrue(Arrays.deepEquals(expectedComponents, path.getComponents()));
        assertEquals("0", path.getPath());
        Bip32Exception e = assertThrows(
                "new Bip32Path with too large value does not throw Bip32Exception",
                Bip32Exception.class,
                () -> new Bip32Path(Long.MAX_VALUE));
        assertEquals(Bip32Error.INVALID_INDEX.name(), e.getMessage());
    }

    @Test
    public void testAbsolutePathFromInt() {
        Bip32Path path = new Bip32Path(0, false);
        assertEquals("m/0", path.getPath());
        Bip32Exception e = assertThrows(
                "new Bip32Path with too large value does not throw Bip32Exception",
                Bip32Exception.class,
                () -> new Bip32Path(Long.MAX_VALUE, false));
        assertEquals(Bip32Error.INVALID_INDEX.name(), e.getMessage());
    }

    @Test
    public void testDerive() {
        String xpriv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        HDKey hdKey = new HDKey(xpriv);
        Bip32Path path = new Bip32Path(0);
        HDKey childKey = hdKey.derive(path);

        assertNotNull(childKey.getXprv());
        assertEquals(
                "xprv9vEG8CuCbvqnJXhr1ZTHZYJcYqGMZ8dkphAUT2CDZsfqewNpq42oSiFgBXXYwDWAHXVbHew4uBfiHNAahRGJ8kUWwqwTGSXUb4wrbWz9eqo",
                childKey.getXprv());
    }

    @Test
    public void testDeriveHardened() {
        String xpriv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        HDKey hdKey = new HDKey(xpriv);
        Bip32Path path = new Bip32Path(Bip32Derivation.newHardened(0));
        HDKey childKey = hdKey.derive(path);

        assertNotNull(childKey.getXprv());
        assertEquals(
                "xprv9vEG8CuLwbNkVNhb56dXckENNiU1SZEgwEAokv1yLodVwsHMRbAFyUMoMd5uyKEgPDgEPBwNfa42v5HYvCvT1ymQo1LQv9h5LtkBMvQD55b",
                childKey.getXprv());
    }

    @Test
    public void testDerivePath() {
        String xpriv = "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF";
        HDKey hdKey = new HDKey(xpriv);
        Bip32Path path = new Bip32Path("m/0'/0");
        HDKey childKey = hdKey.derive(path);

        assertNotNull(childKey.getXprv());
        assertEquals(
                "xprv9xcgxEx7PAbqP2YSijYjX38Vo6dV4i7g9ApmPRAkofDzQ6Hf4c3nBNRfW4EKSm2uhk4FBbjNFGjhZrATqLVKM2JjhsxSrUsDdJYK4UKhyQt",
                childKey.getXprv());
    }

    @Test
    public void testDeriveFromXpub() {
        String xpub = "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy";
        HDKey hdKey = new HDKey(xpub);
        Bip32Path path = new Bip32Path("m/0");
        HDKey childKey = hdKey.derive(path);

        assertNotNull(childKey.getXpub());
        assertEquals(
                "xpub69DcXiS6SJQ5X1nK7azHvgFM6s6qxbMcBv65FQbq8DCpXjhyNbM3zWaA2p4L7Na2siUqFvyuK9W11J6GjqQhtPeJkeadtSpFcf6XLdKsZLZ",
                childKey.getXpub());

        Bip32Path hardenedPath = new Bip32Path("m/0'");
        assertThrows("derive with hardened path must throw Bip32Exception",
                     Bip32Exception.class,
                     () -> hdKey.derive(hardenedPath));
    }

    @Test
    public void testDeriveWithAbsolutePath() {
        String xpub = "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi";
        HDKey hdKey = new HDKey(xpub);

        Bip32Path relativePath = new Bip32Path("0/0");
        HDKey expectedChildKey = hdKey.derive(relativePath);

        Bip32Path absolutePath = new Bip32Path("m/48h/0h/0h/2h/0/0");
        HDKey childKey = hdKey.derive(absolutePath);

        assertEquals(expectedChildKey.getXpub(), childKey.getXpub());
    }
}
