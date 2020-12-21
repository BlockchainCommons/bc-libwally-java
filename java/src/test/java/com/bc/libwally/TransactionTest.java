package com.bc.libwally;

import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.script.ScriptPubKey;
import com.bc.libwally.script.ScriptSig;
import com.bc.libwally.script.ScriptSigType;
import com.bc.libwally.tx.Transaction;
import com.bc.libwally.tx.TxException;
import com.bc.libwally.tx.TxInput;
import com.bc.libwally.tx.TxOutput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.bc.libwally.core.Core.bytes2Hex;
import static com.bc.libwally.util.TestUtils.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class TransactionTest {

    private ScriptPubKey scriptPubKey = new ScriptPubKey(
            "76a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac");
    private PubKey pubKey = new PubKey(
            "03501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c",
            Network.MAINNET);

    @Test
    public void testFromHash() {
        String hash = "0000000000000000000000000000000000000000000000000000000000000000";
        Transaction tx = new Transaction(hash);
        assertEquals(hash, bytes2Hex(tx.getHash()));

        assertThrows("Test invalid hex length failed",
                     TxException.class,
                     () -> new Transaction("00"));
    }

    @Test
    public void testOutput() {
        TxOutput output = new TxOutput(scriptPubKey, 1000, Network.MAINNET);
        assertEquals(1000, output.getAmount());
        assertArrayEquals(scriptPubKey.getData(), output.getScriptPubKey().getData());
    }

    @Test
    public void testInput() {
        Transaction prevTx = new Transaction(
                "0000000000000000000000000000000000000000000000000000000000000000");
        int vout = 0;
        int amount = 1000;
        ScriptSig scriptSig = new ScriptSig(ScriptSigType.payToPubKeyHash(pubKey));

        TxInput input = new TxInput(prevTx.getHash(),
                                    vout,
                                    amount,
                                    scriptSig,
                                    scriptPubKey);

        assertArrayEquals(prevTx.getHash(), input.getTxHash());
        assertEquals(0, input.getVout());
        assertEquals(0xffffffff, input.getSequence());
        assertEquals(scriptSig.getType(), input.getScriptSig().getType());
        assertFalse(input.isSigned());
    }

    @Test
    public void testComposeTransaction() {
        Transaction prevTx = new Transaction(
                "0000000000000000000000000000000000000000000000000000000000000000");
        int vout = 0;
        int amount = 1000;
        ScriptSig scriptSig = new ScriptSig(ScriptSigType.payToPubKeyHash(pubKey));

        TxInput input = new TxInput(prevTx.getHash(),
                                    vout,
                                    amount,
                                    scriptSig,
                                    scriptPubKey);

        TxOutput output = new TxOutput(scriptPubKey, 1000, Network.MAINNET);

        Transaction tx = new Transaction(new TxInput[]{input}, new TxOutput[]{output});
        assertNull(tx.getHash());
        assertEquals(1, tx.getTx().getVersion());
        assertEquals(1, tx.getTx().getInputs().length);
        assertEquals(1, tx.getTx().getOutputs().length);
    }

    @Test
    public void testDeserialize() {
        String hex = "01000000010000000000000000000000000000000000000000000000000000000000000000000000006a47304402203d274300310c06582d0186fc197106120c4838fa5d686fe3aa0478033c35b97802205379758b11b869ede2f5ab13a738493a93571268d66b2a875ae148625bd20578012103501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711cffffffff01e8030000000000001976a914bef5a2f9a56a94aab12459f72ad9cf8cf19c7bbe88ac00000000";
        Transaction tx = new Transaction(hex);
        assertEquals(hex, tx.getDescription());
    }
}
