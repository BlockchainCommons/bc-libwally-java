package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.bip32.*
import com.bc.libwally.bip39.Bip39Seed
import com.bc.libwally.core.Core
import com.bc.libwally.util.assertThrows
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class Bip32Test {
    private val seed = Bip39Seed(
        "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04"
    )

    @Test
    fun testSeedToHDKey() {
        val hdKey = HDKey(seed)
        assertEquals(
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF",
            hdKey.description
        )
    }

    @Test
    fun testBase58ToHDKey() {
        val xprv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val hdKey = HDKey(xprv)
        assertEquals(xprv, hdKey.description)
    }

    @Test
    fun testXpriv() {
        val xprv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val hdKey = HDKey(xprv)
        assertEquals(xprv, hdKey.xprv)
    }

    @Test
    fun testXpub() {
        val xpriv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val xpub =
            "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy"
        val hdKey = HDKey(xpriv)
        assertEquals(xpub, hdKey.xpub)
    }

    @Test
    fun testTpub() {
        val tpriv =
            "tprv8gzC1wn3dmCrBiqDFrqhw9XXgy5t4mzeL5SdWayHBHz1GmWbRKoqDBSwDLfunPAWxMqZ9bdGsdpTiYUfYiWypv4Wfj9g7AYX5K3H9gRYNCA"
        val tpub =
            "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ"
        val hdKey = HDKey(tpriv)
        assertEquals(tpub, hdKey.xpub)
    }

    @Test
    fun testPubKey() {
        val xpub =
            "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy"
        val hdKey = HDKey(xpub)
        assertEquals(
            "02f632717d78bf73e74aa8461e2e782532abae4eed5110241025afb59ebfd3d2fd",
            Core.bytes2Hex(hdKey.pubKey.data)
        )
    }

    @Test
    fun testParseXpub() {
        val xpub =
            "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy"
        val hdKey = HDKey(xpub)
        assertEquals(xpub, hdKey.description)
        assertEquals(xpub, hdKey.xpub)
        assertThrows<Bip32Exception>(
            "hdKey::getXprv does not throw Bip32Exception"
        ) { hdKey.xprv }
    }

    @Test
    fun testParseTpub() {
        val tpub =
            "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ"
        val hdKey = HDKey(tpub)
        assertEquals(tpub, hdKey.description)
        assertEquals(tpub, hdKey.xpub)
        assertThrows<Bip32Exception>(
            "hdKey::getXprv does not throw Bip32Exception"
        ) { hdKey.xprv }
    }

    @Test
    fun testFingerprint() {
        val hdKey = HDKey(seed)
        assertEquals("b4e3f5ed", Core.bytes2Hex(hdKey.fingerprint))
    }

    @Test
    fun testMasterKeyFingerprint() {
        val hdKey = HDKey(seed)
        assertEquals("b4e3f5ed", Core.bytes2Hex(hdKey.masterFingerprint))
        val childKey = HDKey(seed).derive(Bip32Path(0))
        assertEquals("b4e3f5ed", Core.bytes2Hex(childKey.masterFingerprint))
        val tpub =
            "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ"
        val key = HDKey(tpub, Core.hex2Bytes("b4e3f5ed"))
        assertEquals("b4e3f5ed", Core.bytes2Hex(key.masterFingerprint))
    }

    @Test
    fun testInferFingerprintAtDepthZero() {
        val masterKeyXpriv =
            "tprv8ZgxMBicQKsPd9TeAdPADNnSyH9SSUUbTVeFszDE23Ki6TBB5nCefAdHkK8Fm3qMQR6sHwA56zqRmKmxnHk37JkiFzvncDqoKmPWubu7hDF"
        val hdKey = HDKey(masterKeyXpriv)
        assertEquals("d90c6a4f", Core.bytes2Hex(hdKey.masterFingerprint))
    }

    @Test
    fun testRelativePathFromString() {
        val path = Bip32Path("0'/0")
        val expectedComponents = arrayOf(
            Bip32Derivation.newHardened(0),
            Bip32Derivation.newNormal(0)
        )
        Assert.assertTrue(
            Arrays.deepEquals(
                expectedComponents,
                path.components
            )
        )
        assertEquals("0h/0", path.path)
    }

    @Test
    fun testAbsolutePathFromString() {
        val path = Bip32Path("m/0'/0")
        val expectedComponents = arrayOf(
            Bip32Derivation.newHardened(0),
            Bip32Derivation.newNormal(0)
        )
        Assert.assertTrue(
            Arrays.deepEquals(
                expectedComponents,
                path.components
            )
        )
        assertEquals("m/0h/0", path.path)
    }

    @Test
    fun testRelativePathFromInt() {
        val path = Bip32Path(0)
        val expectedComponents =
            arrayOf(Bip32Derivation.newNormal(0))
        Assert.assertTrue(
            Arrays.deepEquals(
                expectedComponents,
                path.components
            )
        )
        assertEquals("0", path.path)
        val e = assertThrows<Bip32Exception>(
            "new Bip32Path with too large value does not throw Bip32Exception"
        ) { Bip32Path(Long.MAX_VALUE) }
        assertEquals(Bip32Error.INVALID_INDEX.name, e.message)
    }

    @Test
    fun testAbsolutePathFromInt() {
        val path = Bip32Path(0, false)
        assertEquals("m/0", path.path)
        val e = assertThrows<Bip32Exception>(
            "new Bip32Path with too large value does not throw Bip32Exception"
        ) { Bip32Path(Long.MAX_VALUE, false) }
        assertEquals(Bip32Error.INVALID_INDEX.name, e.message)
    }

    @Test
    fun testDerive() {
        val xpriv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val hdKey = HDKey(xpriv)
        val path = Bip32Path(0)
        val childKey = hdKey.derive(path)
        Assert.assertNotNull(childKey.xprv)
        assertEquals(
            "xprv9vEG8CuCbvqnJXhr1ZTHZYJcYqGMZ8dkphAUT2CDZsfqewNpq42oSiFgBXXYwDWAHXVbHew4uBfiHNAahRGJ8kUWwqwTGSXUb4wrbWz9eqo",
            childKey.xprv
        )
    }

    @Test
    fun testDeriveHardened() {
        val xpriv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val hdKey = HDKey(xpriv)
        val path = Bip32Path(Bip32Derivation.newHardened(0))
        val childKey = hdKey.derive(path)
        Assert.assertNotNull(childKey.xprv)
        assertEquals(
            "xprv9vEG8CuLwbNkVNhb56dXckENNiU1SZEgwEAokv1yLodVwsHMRbAFyUMoMd5uyKEgPDgEPBwNfa42v5HYvCvT1ymQo1LQv9h5LtkBMvQD55b",
            childKey.xprv
        )
    }

    @Test
    fun testDerivePath() {
        val xpriv =
            "xprv9s21ZrQH143K3h3fDYiay8mocZ3afhfULfb5GX8kCBdno77K4HiA15Tg23wpbeF1pLfs1c5SPmYHrEpTuuRhxMwvKDwqdKiGJS9XFKzUsAF"
        val hdKey = HDKey(xpriv)
        val path = Bip32Path("m/0'/0")
        val childKey = hdKey.derive(path)
        Assert.assertNotNull(childKey.xprv)
        assertEquals(
            "xprv9xcgxEx7PAbqP2YSijYjX38Vo6dV4i7g9ApmPRAkofDzQ6Hf4c3nBNRfW4EKSm2uhk4FBbjNFGjhZrATqLVKM2JjhsxSrUsDdJYK4UKhyQt",
            childKey.xprv
        )
    }

    @Test
    fun testDeriveFromXpub() {
        val xpub =
            "xpub661MyMwAqRbcGB88KaFbLGiYAat55APKhtWg4uYMkXAmfuSTbq2QYsn9sKJCj1YqZPafsboef4h4YbXXhNhPwMbkHTpkf3zLhx7HvFw1NDy"
        val hdKey = HDKey(xpub)
        val path = Bip32Path("m/0")
        val childKey = hdKey.derive(path)
        Assert.assertNotNull(childKey.xpub)
        assertEquals(
            "xpub69DcXiS6SJQ5X1nK7azHvgFM6s6qxbMcBv65FQbq8DCpXjhyNbM3zWaA2p4L7Na2siUqFvyuK9W11J6GjqQhtPeJkeadtSpFcf6XLdKsZLZ",
            childKey.xpub
        )
        val hardenedPath = Bip32Path("m/0'")
        assertThrows<Bip32Exception>(
            "derive with hardened path must throw Bip32Exception"
        ) { hdKey.derive(hardenedPath) }
    }

    @Test
    fun testDeriveWithAbsolutePath() {
        val xpub =
            "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi"
        val hdKey = HDKey(xpub)
        val relativePath = Bip32Path("0/0")
        val expectedChildKey = hdKey.derive(relativePath)
        val absolutePath = Bip32Path("m/48h/0h/0h/2h/0/0")
        val childKey = hdKey.derive(absolutePath)
        assertEquals(expectedChildKey.xpub, childKey.xpub)
    }
}