package com.bc.libwally;

import com.bc.libwally.address.Address;
import com.bc.libwally.address.PubKey;
import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.script.ScriptSig;
import com.bc.libwally.script.ScriptSigType;
import com.bc.libwally.script.Witness;
import com.bc.libwally.script.WitnessType;
import com.bc.libwally.tx.raw.WallyTxWitnessStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.bc.libwally.ArrayUtils.append;
import static com.bc.libwally.Network.MAINNET;
import static com.bc.libwally.Network.TESTNET;
import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_DER_MAX_LOW_R_LEN;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.OP_RETURN;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_PUBKEY_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_SCRIPT_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_WITNESS_PUBKEY_HASH;
import static com.bc.libwally.script.ScriptPubKey.ScriptType.PAY_TO_WITNESS_SCRIPT_HASH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ScriptTest {

    @Test
    public void testDetectScriptPubKeyTypeP2PKH() {
        ScriptPubKey scriptPubKey = new ScriptPubKey(
                "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac");
        assertEquals(PAY_TO_PUBKEY_HASH, scriptPubKey.getType());
    }

    @Test
    public void testDetectScriptPubKeyTypeP2SH() {
        ScriptPubKey scriptPubKey = new ScriptPubKey(
                "a91486cc442a97817c245ce90ed0d31d6dbcde3841f987");
        assertEquals(PAY_TO_SCRIPT_HASH, scriptPubKey.getType());
    }

    @Test
    public void testDetectScriptPubKeyTypeNativeSegWit() {
        ScriptPubKey scriptPubKey = new ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe");
        assertEquals(PAY_TO_WITNESS_PUBKEY_HASH, scriptPubKey.getType());
    }

    @Test
    public void testDetectScriptPubKeyTypeOpReturn() {
        ScriptPubKey scriptPubKey = new ScriptPubKey("6a13636861726c6579206c6f766573206865696469");
        assertEquals(OP_RETURN, scriptPubKey.getType());
    }

    @Test
    public void testScriptSigP2PKH() {
        PubKey pubKey = new PubKey(
                "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
                MAINNET);
        ScriptSig scriptSig = new ScriptSig(ScriptSigType.payToPubKeyHash(pubKey));
        assertEquals(ScriptSigType.payToPubKeyHash(pubKey), scriptSig.getType());
        assertNull(scriptSig.render(ScriptSig.Purpose.SIGNED));
        assertNull(scriptSig.getSignature());

        int expectedRenderDataCount = 2 +
                                      EC_SIGNATURE_DER_MAX_LOW_R_LEN +
                                      1 +
                                      pubKey.getData().length;
        assertEquals(expectedRenderDataCount,
                     scriptSig.render(ScriptSig.Purpose.FEE_WORST_CASE).length);
        scriptSig.setSignature("01");
        byte[] sigHashBytes = hex2Bytes("01");
        byte[] signaturePush = append(hex2Bytes("02"), scriptSig.getSignature(), sigHashBytes);
        byte[] pubKeyPush = append(new byte[]{(byte) pubKey.getData().length}, pubKey.getData());
        assertArrayEquals(scriptSig.render(ScriptSig.Purpose.SIGNED),
                          append(signaturePush, pubKeyPush));
    }

    @Test
    public void testWitnessP2WPKH() {
        PubKey pubKey = new PubKey(
                "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
                Network.MAINNET);
        Witness witness = new Witness(WitnessType.payToWitnessPubKeyHash(pubKey));
        assertTrue(witness.isDummy());

        WallyTxWitnessStack stack = witness.createWallyTxWitnessStack();
        assertEquals(2, stack.getItems().length);

        assertEquals("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac",
                     bytes2Hex(witness.getScriptCode()));
        Witness signedWitness = new Witness(WitnessType.payToWitnessPubKeyHash(pubKey),
                                            hex2Bytes("01"));
        WallyTxWitnessStack signedStack = signedWitness.createWallyTxWitnessStack();
        assertEquals(2, signedStack.getItems().length);
    }

    @Test
    public void testMultisig() {
        PubKey pubKey1 = new PubKey(
                "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
                MAINNET); // [3442193e/0'/1]
        PubKey pubKey2 = new PubKey(
                "022e3d55c64908832291348d1faa74bff4ae1047e9777a28b26b064e410a554737",
                MAINNET); // [bd16bee5/0'/1]

        ScriptPubKey multiSig = new ScriptPubKey(new PubKey[]{pubKey1, pubKey2}, 2);
        assertEquals(ScriptPubKey.ScriptType.MULTI_SIG, multiSig.getType());
        assertArrayEquals(hex2Bytes(
                "5221022e3d55c64908832291348d1faa74bff4ae1047e9777a28b26b064e410a5547372103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c52ae"),
                          multiSig.getData());
        assertArrayEquals(hex2Bytes(
                "0020ce8c526b7a6c9491ed33861f4492299c86ffa8567a75286535f317ddede3062a"),
                          multiSig.getWitnessProgram());

        Address address = new Address(multiSig, MAINNET);
        assertEquals("bc1qe6x9y6m6dj2frmfnsc05fy3fnjr0l2zk0f6jsef47vtamm0rqc4qnfnxm0",
                     address.getAddress());
    }

    @Test
    public void testScriptPubKeyAddress() {
        ScriptPubKey scriptPubKeyPKH = new ScriptPubKey(
                "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac");
        assertEquals(PAY_TO_PUBKEY_HASH, scriptPubKeyPKH.getType());
        assertEquals("1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj",
                     new Address(scriptPubKeyPKH, MAINNET).getAddress());
        assertEquals("mxvewdhKCenLkYgNa8irv1UM2omEWPMdEE",
                     new Address(scriptPubKeyPKH, TESTNET).getAddress());

        ScriptPubKey scriptPubKeyP2SH = new ScriptPubKey(
                "a91486cc442a97817c245ce90ed0d31d6dbcde3841f987");
        assertEquals(PAY_TO_SCRIPT_HASH, scriptPubKeyP2SH.getType());
        assertEquals("3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu",
                     new Address(scriptPubKeyP2SH, MAINNET).getAddress());
        assertEquals("2N5XyEfAXtVde7mv6idZDXp5NFwajYEj9TD",
                     new Address(scriptPubKeyP2SH, TESTNET).getAddress());

        ScriptPubKey scriptP2WPKH = new ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe");
        assertEquals(PAY_TO_WITNESS_PUBKEY_HASH, scriptP2WPKH.getType());
        assertEquals("bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt",
                     new Address(scriptP2WPKH, MAINNET).getAddress());

        ScriptPubKey scriptP2WSH = new ScriptPubKey(
                "0020f8608e6e5b537f8fc8182eb113cf40f564b99cf99d87170c4f1ac259074ee8fd");
        assertEquals(PAY_TO_WITNESS_SCRIPT_HASH, scriptP2WSH.getType());
        assertEquals("bc1qlpsgumjm2dlcljqc96c38n6q74jtn88enkr3wrz0rtp9jp6war7s2h4lrs",
                     new Address(scriptP2WSH, MAINNET).getAddress());
    }

}
