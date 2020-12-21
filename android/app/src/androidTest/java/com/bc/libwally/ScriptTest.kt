package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.address.Address
import com.bc.libwally.address.PubKey
import com.bc.libwally.bip32.Network
import com.bc.libwally.core.Core
import com.bc.libwally.core.Core.bytes2Hex
import com.bc.libwally.core.Core.hex2Bytes
import com.bc.libwally.crypto.CryptoConstants.EC_SIGNATURE_DER_MAX_LOW_R_LEN
import com.bc.libwally.script.*
import com.bc.libwally.script.ScriptPubKey.ScriptType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScriptTest {

    @Test
    fun testDetectScriptPubKeyTypeP2PKH() {
        val scriptPubKey = ScriptPubKey("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac")
        assertEquals(ScriptPubKey.ScriptType.PAY_TO_PUBKEY_HASH, scriptPubKey.type)
    }

    @Test
    fun testDetectScriptPubKeyTypeP2SH() {
        val scriptPubKey = ScriptPubKey("a91486cc442a97817c245ce90ed0d31d6dbcde3841f987")
        assertEquals(ScriptPubKey.ScriptType.PAY_TO_SCRIPT_HASH, scriptPubKey.type)
    }

    @Test
    fun testDetectScriptPubKeyTypeNativeSegWit() {
        val scriptPubKey = ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe")
        assertEquals(ScriptPubKey.ScriptType.PAY_TO_WITNESS_PUBKEY_HASH, scriptPubKey.type)
    }

    @Test
    fun testDetectScriptPubKeyTypeOpReturn() {
        val scriptPubKey = ScriptPubKey("6a13636861726c6579206c6f766573206865696469")
        assertEquals(ScriptPubKey.ScriptType.OP_RETURN, scriptPubKey.type)
    }

    @Test
    fun testScriptSigP2PKH() {
        val pubKey = PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET
        )
        val scriptSig = ScriptSig(ScriptSigType.payToPubKeyHash(pubKey))
        assertEquals(ScriptSigType.payToPubKeyHash(pubKey), scriptSig.type)
        assertNull(scriptSig.render(ScriptSig.Purpose.SIGNED))
        assertNull(scriptSig.signature)

        val expectedRenderDataCount = 2 + EC_SIGNATURE_DER_MAX_LOW_R_LEN + 1 + pubKey.data.size
        assertEquals(
            expectedRenderDataCount,
            scriptSig.render(ScriptSig.Purpose.FEE_WORST_CASE).size
        )
        scriptSig.setSignature("01")
        val sigHashBytes = hex2Bytes("01")
        val signaturePush = ArrayUtils.append(hex2Bytes("02"), scriptSig.signature, sigHashBytes)
        val pubKeyPush = ArrayUtils.append(byteArrayOf(pubKey.data.size.toByte()), pubKey.data)
        assertArrayEquals(
            scriptSig.render(ScriptSig.Purpose.SIGNED),
            ArrayUtils.append(signaturePush, pubKeyPush)
        )
    }

    @Test
    fun testWitnessP2WPKH() {
        val pubKey = PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET
        )
        val witness = Witness(WitnessType.payToWitnessPubKeyHash(pubKey))
        assertTrue(witness.isDummy)

        val stack = witness.createWallyTxWitnessStack()
        assertEquals(2, stack.items.size.toLong())

        assertEquals(
            "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac",
            bytes2Hex(witness.scriptCode)
        )
        val signedWitness = Witness(
            WitnessType.payToWitnessPubKeyHash(pubKey),
            hex2Bytes("01")
        )
        val signedStack = signedWitness.createWallyTxWitnessStack()
        assertEquals(2, signedStack.items.size.toLong())
    }

    @Test
    fun testMultisig() {
        val pubKey1 = PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET
        ) // [3442193e/0'/1]
        val pubKey2 = PubKey(
            "022e3d55c64908832291348d1faa74bff4ae1047e9777a28b26b064e410a554737",
            Network.MAINNET
        ) // [bd16bee5/0'/1]
        val multiSig = ScriptPubKey(arrayOf(pubKey1, pubKey2), 2)
        assertEquals(ScriptType.MULTI_SIG, multiSig.type)
        assertArrayEquals(
            hex2Bytes("5221022e3d55c64908832291348d1faa74bff4ae1047e9777a28b26b064e410a5547372103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c52ae"),
            multiSig.data
        )
        assertArrayEquals(
            hex2Bytes("0020ce8c526b7a6c9491ed33861f4492299c86ffa8567a75286535f317ddede3062a"),
            multiSig.witnessProgram
        )
        val address = Address(multiSig, Network.MAINNET)
        assertEquals(
            "bc1qe6x9y6m6dj2frmfnsc05fy3fnjr0l2zk0f6jsef47vtamm0rqc4qnfnxm0",
            address.address
        )
    }

    @Test
    fun testScriptPubKeyAddress() {
        val scriptPubKeyPKH = ScriptPubKey(
            "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac"
        )
        assertEquals(ScriptType.PAY_TO_PUBKEY_HASH, scriptPubKeyPKH.type)
        assertEquals(
            "1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj",
            Address(scriptPubKeyPKH, Network.MAINNET).address
        )
        assertEquals(
            "mxvewdhKCenLkYgNa8irv1UM2omEWPMdEE",
            Address(scriptPubKeyPKH, Network.TESTNET).address
        )

        val scriptPubKeyP2SH = ScriptPubKey("a91486cc442a97817c245ce90ed0d31d6dbcde3841f987")
        assertEquals(ScriptType.PAY_TO_SCRIPT_HASH, scriptPubKeyP2SH.type)
        assertEquals(
            "3DymAvEWH38HuzHZ3VwLus673bNZnYwNXu",
            Address(scriptPubKeyP2SH, Network.MAINNET).address
        )
        assertEquals(
            "2N5XyEfAXtVde7mv6idZDXp5NFwajYEj9TD",
            Address(scriptPubKeyP2SH, Network.TESTNET).address
        )

        val scriptP2WPKH =
            ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe")
        assertEquals(ScriptType.PAY_TO_WITNESS_PUBKEY_HASH, scriptP2WPKH.type)
        assertEquals(
            "bc1qhm6697d9d2224vfyt8mj4kw03ncec7a7fdafvt",
            Address(scriptP2WPKH, Network.MAINNET).address
        )

        val scriptP2WSH = ScriptPubKey(
            "0020f8608e6e5b537f8fc8182eb113cf40f564b99cf99d87170c4f1ac259074ee8fd"
        )
        assertEquals(ScriptType.PAY_TO_WITNESS_SCRIPT_HASH, scriptP2WSH.type)
        assertEquals(
            "bc1qlpsgumjm2dlcljqc96c38n6q74jtn88enkr3wrz0rtp9jp6war7s2h4lrs",
            Address(scriptP2WSH, Network.MAINNET).address
        )
    }
}