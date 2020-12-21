package com.bc.libwally;

import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.script.ScriptSig;
import com.bc.libwally.script.ScriptSigType;
import com.bc.libwally.script.Witness;
import com.bc.libwally.script.WitnessType;
import com.bc.libwally.tx.Transaction;
import com.bc.libwally.tx.TxInput;
import com.bc.libwally.tx.TxOutput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class TransactionInstanceTest {

    private static final int LEGACY_INPUT_BYTES = 192;
    private static final int NATIVE_SEGWIT_INPUT_BYTES = 113;
    private static final int WRAPPED_SEGWIT_INPUT_BYTES = 136;

    private static final ScriptPubKey SCRIPT_PUB_KEY1 = new ScriptPubKey(
            "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac");
    private static final ScriptPubKey SCRIPT_PUB_KEY2 = new ScriptPubKey(
            "0014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe");
    private static final ScriptPubKey SCRIPT_PUB_KEY3 = new ScriptPubKey(
            "a91486cc442a97817c245ce90ed0d31d6dbcde3841f987");

    private static final PubKey PUB_KEY = new PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET);

    private static final Transaction PREV_TX = new Transaction(
            "0000000000000000000000000000000000000000000000000000000000000000");
    private static final TxInput TX_INPUT1 = new TxInput(PREV_TX.getHash(),
                                                         0,
                                                         1000L + LEGACY_INPUT_BYTES,
                                                         new ScriptSig(ScriptSigType.payToPubKeyHash(
                                                                 PUB_KEY)),
                                                         SCRIPT_PUB_KEY1);
    private static final TxInput TX_INPUT2 = new TxInput(PREV_TX.getHash(),
                                                         0,
                                                         1000L + NATIVE_SEGWIT_INPUT_BYTES,
                                                         new Witness(WitnessType.payToWitnessPubKeyHash(
                                                                 PUB_KEY)),
                                                         SCRIPT_PUB_KEY2);
    private static final TxInput TX_INPUT3 = new TxInput(PREV_TX.getHash(),
                                                         0,
                                                         1000L + WRAPPED_SEGWIT_INPUT_BYTES,
                                                         new Witness(WitnessType.payToScriptHashPayToWitnessPubKeyHash(
                                                                 PUB_KEY)),
                                                         SCRIPT_PUB_KEY3);

    private static final TxOutput TX_OUTPUT = new TxOutput(SCRIPT_PUB_KEY1, 1000L, Network.MAINNET);

    private static final Transaction TX1 = new Transaction(new TxInput[]{TX_INPUT1},
                                                           new TxOutput[]{TX_OUTPUT});
    private static final Transaction TX2 = new Transaction(new TxInput[]{TX_INPUT2},
                                                           new TxOutput[]{TX_OUTPUT});
    private static final Transaction TX3 = new Transaction(new TxInput[]{TX_INPUT3},
                                                           new TxOutput[]{TX_OUTPUT});
    private static final HDKey HD_KEY = new HDKey(
            "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs");

    @Test
    public void testTotalIn() {
        assertEquals(1000L + LEGACY_INPUT_BYTES, TX1.getTotalIn().longValue());
        assertEquals(1000L + NATIVE_SEGWIT_INPUT_BYTES, TX2.getTotalIn().longValue());
        assertEquals(1000L + WRAPPED_SEGWIT_INPUT_BYTES, TX3.getTotalIn().longValue());

        Transaction tx = new Transaction(
                "0000000000000000000000000000000000000000000000000000000000000000");
        assertNull(tx.getTotalIn());
    }

    @Test
    public void testTotalOut() {
        assertEquals(1000L, TX1.getTotalOut().longValue());
        Transaction tx = new Transaction(
                "0000000000000000000000000000000000000000000000000000000000000000");
        assertNull(tx.getTotalOut());
    }

    @Test
    public void testFunded() {
        assertTrue(TX1.isFunded());
    }

    @Test
    public void testSize() {
        assertEquals(LEGACY_INPUT_BYTES, TX1.getVBytes().intValue());
        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, TX2.getVBytes().intValue());
        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, TX3.getVBytes().intValue());

        Transaction tx = new Transaction(
                "0000000000000000000000000000000000000000000000000000000000000000");
        assertNull(tx.getVBytes());
    }

    @Test
    public void testFee() {
        assertEquals(LEGACY_INPUT_BYTES, TX1.getFee().longValue());
        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, TX2.getFee().longValue());
        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, TX3.getFee().longValue());
    }

    @Test
    public void testFeeRate() {
        assertEquals(1.0, TX1.getFeeRate(), 0);
        assertEquals(1.0, TX2.getFeeRate(), 0);
        assertEquals(1.0, TX3.getFeeRate(), 0);
    }


    @Test
    public void testSign() {
        Transaction signedTx = TX1.signed(new HDKey[]{HD_KEY});
        assertTrue(signedTx.getInputs()[0].isSigned());
        assertEquals(
                "01000000010000000000000000000000000000000000000000000000000000000000000000000000006a47304402203d274300310c06582d0186fc197106120c4838fa5d686fe3aa0478033c35b97802205379758b11b869ede2f5ab13a738493a93571268d66b2a875ae148625bd20578012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711cffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac00000000",
                signedTx.getDescription());

        assertEquals(LEGACY_INPUT_BYTES - 1, signedTx.getVBytes().intValue());
    }

    @Test
    public void testSignNativeSegWit() {
        Transaction signedTx = TX2.signed(new HDKey[]{HD_KEY});
        assertTrue(signedTx.getInputs()[0].isSigned());
        assertEquals(
                "0100000000010100000000000000000000000000000000000000000000000000000000000000000000000000ffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac0247304402204094361e267c39fb942b3d30c6efb96de32ea0f81e87fc36c53e00de2c24555c022069f368ac9cacea21be7b5e7a7c1dad01aa244e437161d000408343a4d6f5da0e012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c00000000",
                signedTx.getDescription());

        assertEquals(NATIVE_SEGWIT_INPUT_BYTES, signedTx.getVBytes().intValue());
    }

    @Test
    public void testSignWrappedSegWit() {
        Transaction signedTx = TX3.signed(new HDKey[]{HD_KEY});
        assertTrue(signedTx.getInputs()[0].isSigned());
        assertEquals(
                "0100000000010100000000000000000000000000000000000000000000000000000000000000000000000017160014bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbeffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac024730440220514e02e6d4aff5e1bfcf72a98eab3a415176c757e2bf6feb7ccb893f8ffcf09b022048fe33e6a1dc80585f30aac20f58442d711739ac07d192a3a7867a1dbef6b38d012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c00000000",
                signedTx.getDescription());

        assertEquals(WRAPPED_SEGWIT_INPUT_BYTES, signedTx.getVBytes().intValue());
    }
}
