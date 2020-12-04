package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.address.Address
import com.bc.libwally.address.AddressType
import com.bc.libwally.address.Key
import com.bc.libwally.bip32.HDKey
import com.bc.libwally.bip32.Network
import com.bc.libwally.core.Core
import com.bc.libwally.script.ScriptPubKey
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddressTest {

    private val hdKeyMainnet = HDKey(
        "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ"
    )
    private val hdKeyTestnet = HDKey(
        "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ"
    )

    @Test
    fun testDeriveLegacyAddress() {
        val address = Address(hdKeyMainnet, AddressType.PAY_TO_PUBKEY_HASH)
        Assert.assertEquals(
            "1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj",
            address.address
        )
    }

    @Test
    fun testDeriveLegacyAddressTestnet() {
        val address = Address(hdKeyTestnet, AddressType.PAY_TO_PUBKEY_HASH)
        Assert.assertEquals(
            "mnicNaAVzyGdFvDa9VkMrjgNdnr2wHBWxk",
            address.address
        )
    }

    @Test
    fun testDeriveWrappedSegWitAddress() {
        val address = Address(
            hdKeyMainnet,
            AddressType.PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH
        )
        Assert.assertEquals(
            "3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu",
            address.address
        )
    }

    @Test
    fun testDeriveWrappedSegWitAddressTestnet() {
        val address = Address(
            hdKeyTestnet,
            AddressType.PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH
        )
        Assert.assertEquals(
            "2N6M3ah9EoggimNz5pnAmQwnpE1Z3ya3V7A",
            address.address
        )
    }

    @Test
    fun testDeriveNativeSegWitAddress() {
        val address = Address(hdKeyMainnet, AddressType.PAY_TO_WITNESS_PUBKEY_HASH)
        Assert.assertEquals(
            "bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt",
            address.address
        )
    }

    @Test
    fun testDeriveNativeSegWitAddressTestnet() {
        val address = Address(hdKeyTestnet, AddressType.PAY_TO_WITNESS_PUBKEY_HASH)
        Assert.assertEquals(
            "tb1qfm7nmm28m9n7gy3fsfpze8vymds9qwtjwn4w7y",
            address.address
        )
    }

    @Test
    fun testParseLegacyAddress() {
        val address = Address("1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj")
        Assert.assertEquals(
            ScriptPubKey("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac"),
            address.scriptPubKey
        )
    }

    @Test
    fun testParseWrappedSegWitAddress() {
        val address = Address("3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu")
        Assert.assertEquals(
            ScriptPubKey("a91486cc442a97817c245ce90ed0d31d6dbcde3841f987"),
            address.scriptPubKey
        )
    }

    @Test
    fun testParseNativeSegWitAddress() {
        val address = Address("bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt")
        Assert.assertEquals(
            ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe"),
            address.scriptPubKey
        )
    }

    @Test
    fun testParseWIF() {
        val wif = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ"
        val key = Key(wif, Network.MAINNET, false)
        Assert.assertEquals(
            "0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d",
            Core.bytes2Hex(key.data)
        )
        Assert.assertEquals(Network.MAINNET, key.network)
        Assert.assertFalse(key.isCompressed)
    }

    @Test
    fun testToWIF() {
        val data =
            Core.hex2Bytes("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d")
        val key = Key(data, Network.MAINNET, false)
        Assert.assertEquals(
            "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ",
            key.wif
        )
    }
}