package com.bc.libwally;

import com.bc.libwally.address.Address;
import com.bc.libwally.address.Key;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.script.ScriptPubKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.bc.libwally.address.AddressType.PAY_TO_PUBKEY_HASH;
import static com.bc.libwally.address.AddressType.PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH;
import static com.bc.libwally.address.AddressType.PAY_TO_WITNESS_PUBKEY_HASH;
import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class AddressTest {

    private static final HDKey hdKeyMainnet = new HDKey(
            "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ");
    private static final HDKey hdKeyTestnet = new HDKey(
            "tpubDDgEAMpHn8tX5Bs19WWJLZBeFzbpE7BYuP3Qo71abZnQ7FmN3idRPg4oPWt2Q6Uf9huGv7AGMTu8M2BaCxAdThQArjLWLDLpxVX2gYfh2YJ");

    @Test
    public void testDeriveLegacyAddress() {
        Address address = new Address(hdKeyMainnet, PAY_TO_PUBKEY_HASH);
        assertEquals("1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj", address.getAddress());
    }

    @Test
    public void testDeriveLegacyAddressTestnet() {
        Address address = new Address(hdKeyTestnet, PAY_TO_PUBKEY_HASH);
        assertEquals("mnicNaAVzyGdFvDa9VkMrjgNdnr2wHBWxk", address.getAddress());
    }

    @Test
    public void testDeriveWrappedSegWitAddress() {
        Address address = new Address(hdKeyMainnet, PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH);
        assertEquals("3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu", address.getAddress());
    }

    @Test
    public void testDeriveWrappedSegWitAddressTestnet() {
        Address address = new Address(hdKeyTestnet, PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH);
        assertEquals("2N6M3ah9EoggimNz5pnAmQwnpE1Z3ya3V7A", address.getAddress());
    }

    @Test
    public void testDeriveNativeSegWitAddress() {
        Address address = new Address(hdKeyMainnet, PAY_TO_WITNESS_PUBKEY_HASH);
        assertEquals("bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt", address.getAddress());
    }

    @Test
    public void testDeriveNativeSegWitAddressTestnet() {
        Address address = new Address(hdKeyTestnet, PAY_TO_WITNESS_PUBKEY_HASH);
        assertEquals("tb1qfm7nmm28m9n7gy3fsfpze8vymds9qwtjwn4w7y", address.getAddress());
    }

    @Test
    public void testParseLegacyAddress() {
        Address address = new Address("1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj");
        assertEquals(new ScriptPubKey("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac"),
                     address.getScriptPubKey());
    }

    @Test
    public void testParseWrappedSegWitAddress() {
        Address address = new Address("3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu");
        assertEquals(new ScriptPubKey("a91486cc442a97817c245ce90ed0d31d6dbcde3841f987"),
                     address.getScriptPubKey());
    }

    @Test
    public void testParseNativeSegWitAddress() {
        Address address = new Address("bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt");
        assertEquals(new ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe"),
                     address.getScriptPubKey());
    }

    @Test
    public void testParseWIF() {
        String wif = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ";
        Key key = new Key(wif, Network.MAINNET, false);
        assertEquals("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d",
                     bytes2Hex(key.getData()));
        assertEquals(Network.MAINNET, key.getNetwork());
        assertFalse(key.isCompressed());
    }

    @Test
    public void testToWIF() {
        byte[] data = hex2Bytes("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d");
        Key key = new Key(data, Network.MAINNET, false);
        assertEquals("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ", key.getWif());
    }
}
