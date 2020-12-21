package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.address.PubKey
import com.bc.libwally.bip32.HDKey
import com.bc.libwally.bip32.Network
import com.bc.libwally.script.*
import com.bc.libwally.tx.Transaction
import com.bc.libwally.tx.TxInput
import com.bc.libwally.tx.TxOutput
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionInstanceTest {

    companion object {
        private const val LEGACY_INPUT_BYTES = 192
        private const val NATIVE_SEGWIT_INPUT_BYTES = 113
        private const val WRAPPED_SEGWIT_INPUT_BYTES = 136

        private val SCRIPT_PUB_KEY1 =
            ScriptPubKey("76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac")
        private val SCRIPT_PUB_KEY2 = ScriptPubKey("0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe")
        private val SCRIPT_PUB_KEY3 = ScriptPubKey("a91486cc442a97817c245ce90ed0d31d6dbcde3841f987")

        private val PUB_KEY = PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET
        )

        private val PREV_TX =
            Transaction("0000000000000000000000000000000000000000000000000000000000000000")
        private val TX_INPUT1 = TxInput(
            PREV_TX.hash,
            0,
            1000L + LEGACY_INPUT_BYTES,
            ScriptSig(ScriptSigType.payToPubKeyHash(PUB_KEY)),
            SCRIPT_PUB_KEY1
        )
        private val TX_INPUT2 = TxInput(
            PREV_TX.hash,
            0,
            1000L + NATIVE_SEGWIT_INPUT_BYTES,
            Witness(WitnessType.payToWitnessPubKeyHash(PUB_KEY)),
            SCRIPT_PUB_KEY2
        )
        private val TX_INPUT3 = TxInput(
            PREV_TX.hash,
            0,
            1000L + WRAPPED_SEGWIT_INPUT_BYTES,
            Witness(WitnessType.payToScriptHashPayToWitnessPubKeyHash(PUB_KEY)),
            SCRIPT_PUB_KEY3
        )

        private val TX_OUTPUT = TxOutput(SCRIPT_PUB_KEY1, 1000L, Network.MAINNET)

        private val TX1 = Transaction(arrayOf(TX_INPUT1), arrayOf(TX_OUTPUT))
        private val TX2 = Transaction(arrayOf(TX_INPUT2), arrayOf(TX_OUTPUT))
        private val TX3 = Transaction(arrayOf(TX_INPUT3), arrayOf(TX_OUTPUT))
        private val HD_KEY =
            HDKey("xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs")
    }


    @Test
    fun testTotalIn() {
        assertEquals(1000L + LEGACY_INPUT_BYTES, TX1.totalIn.toLong())
        assertEquals(1000L + NATIVE_SEGWIT_INPUT_BYTES, TX2.totalIn.toLong())
        assertEquals(1000L + WRAPPED_SEGWIT_INPUT_BYTES, TX3.totalIn.toLong())
        val tx = Transaction("0000000000000000000000000000000000000000000000000000000000000000")
        assertNull(tx.totalIn)
    }

    @Test
    fun testTotalOut() {
        assertEquals(1000L, TX1.totalOut.toLong())
        val tx = Transaction("0000000000000000000000000000000000000000000000000000000000000000")
        assertNull(tx.totalOut)
    }

    @Test
    fun testFunded() {
        assertTrue(TX1.isFunded)
    }

    @Test
    fun testSize() {
        assertEquals(LEGACY_INPUT_BYTES, TX1.vBytes.toInt())
        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, TX2.vBytes.toInt())
        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, TX3.vBytes.toInt())
        val tx = Transaction("0000000000000000000000000000000000000000000000000000000000000000")
        assertNull(tx.vBytes)
    }

    @Test
    fun testFee() {
        assertEquals(LEGACY_INPUT_BYTES, TX1.fee.toInt())
        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, TX2.fee.toInt())
        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, TX3.fee.toInt())
    }

    @Test
    fun testFeeRate() {
        assertEquals(1.0f, TX1.feeRate, 0.0f)
        assertEquals(1.0f, TX2.feeRate, 0.0f)
        assertEquals(1.0f, TX3.feeRate, 0.0f)
    }


    @Test
    fun testSign() {
        val signedTx = TX1.signed(arrayOf(HD_KEY))
        assertTrue(signedTx.inputs[0].isSigned)
        assertEquals(
            "01000000010000000000000000000000000000000000000000000000000000000000000000000000006a47304402203d274300310c06582d0186fc197106120c4838fa5d686fe3aa0478033c35b97802205379758b11b869ede2f5ab13a738493a93571268d66b2a875ae148625bd20578012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711cffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac00000000",
            signedTx.description
        )

        assertEquals(
            LEGACY_INPUT_BYTES - 1.toLong(),
            signedTx.vBytes.toInt().toLong()
        )
    }

    @Test
    fun testSignNativeSegWit() {
        val signedTx = TX2.signed(arrayOf(HD_KEY))
        assertTrue(signedTx.inputs[0].isSigned)
        assertEquals(
            "0100000000010100000000000000000000000000000000000000000000000000000000000000000000000000ffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac0247304402204094361e267c39fb942b3d30c6efb96de32ea0f81e87fc36c53e00de2c24555c022069f368ac9cacea21be7b5e7a7c1dad01aa244e437161d000408343a4d6f5da0e012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c00000000",
            signedTx.description
        )
        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, signedTx.vBytes)
    }

    @Test
    fun testSignWrappedSegWit() {
        val signedTx = TX3.signed(arrayOf(HD_KEY))
        assertTrue(signedTx.inputs[0].isSigned)
        assertEquals(
            "0100000000010100000000000000000000000000000000000000000000000000000000000000000000000017160014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbeffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac024730440220514e02e6d4aff5e1bfcf72a98eab3a415176c757e2bf6feb7ccb893f8ffcf09b022048fe33e6a1dc80585f30aac20f58442d711739ac07d192a3a7867a1dbef6b38d012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c00000000",
            signedTx.description
        )
        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, signedTx.vBytes)
    }
}