package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.address.PubKey
import com.bc.libwally.bip32.Network
import com.bc.libwally.core.Core
import com.bc.libwally.script.ScriptPubKey
import com.bc.libwally.script.ScriptSig
import com.bc.libwally.script.ScriptSigType
import com.bc.libwally.tx.Transaction
import com.bc.libwally.tx.TxException
import com.bc.libwally.tx.TxInput
import com.bc.libwally.tx.TxOutput
import com.bc.libwally.util.assertThrows
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionTest {

    private val scriptPubKey = ScriptPubKey("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac")
    private val pubKey = PubKey(
        "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
        Network.MAINNET
    )

    @Test
    fun testFromHash() {
        val hash = "0000000000000000000000000000000000000000000000000000000000000000"
        val tx = Transaction(hash)
        assertEquals(hash, Core.bytes2Hex(tx.hash))
        assertThrows<TxException>(
            "Test invalid hex length failed"
        ) { Transaction("00") }
    }

    @Test
    fun testOutput() {
        val output = TxOutput(scriptPubKey, 1000, Network.MAINNET)
        assertEquals(1000, output.amount)
        assertArrayEquals(scriptPubKey.data, output.scriptPubKey.data)
    }

    @Test
    fun testInput() {
        val prevTx = Transaction(
            "0000000000000000000000000000000000000000000000000000000000000000"
        )
        val vout = 0L
        val amount = 1000L
        val scriptSig = ScriptSig(ScriptSigType.payToPubKeyHash(pubKey))
        val input = TxInput(
            prevTx.hash,
            vout,
            0xffffffff,
            amount,
            scriptSig,
            scriptPubKey
        )
        assertArrayEquals(prevTx.hash, input.txHash)
        assertEquals(0, input.vout)
        assertEquals(0xffffffff, input.sequence)
        assertEquals(scriptSig.type, input.scriptSig.type)
        assertFalse(input.isSigned)
    }

    @Test
    fun testComposeTransaction() {
        val prevTx = Transaction(
            "0000000000000000000000000000000000000000000000000000000000000000"
        )
        val vout = 0L
        val amount = 1000L
        val scriptSig = ScriptSig(ScriptSigType.payToPubKeyHash(pubKey))
        val input = TxInput(
            prevTx.hash,
            vout,
            0xffffffff,
            amount,
            scriptSig,
            scriptPubKey
        )
        val output = TxOutput(scriptPubKey, 1000L, Network.MAINNET)
        val tx = Transaction(arrayOf(input), arrayOf(output))
        assertNull(tx.hash)
        assertEquals(1, tx.rawTx.version)
        assertEquals(1, tx.rawTx.inputs.size.toLong())
        assertEquals(1, tx.rawTx.outputs.size.toLong())
    }

    @Test
    fun testDeserialize() {
        val hex =
            "01000000010000000000000000000000000000000000000000000000000000000000000000000000006a47304402203d274300310c06582d0186fc197106120c4838fa5d686fe3aa0478033c35b97802205379758b11b869ede2f5ab13a738493a93571268d66b2a875ae148625bd20578012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711cffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac00000000"
        val tx = Transaction(hex)
        assertEquals(hex, tx.description)
    }
}