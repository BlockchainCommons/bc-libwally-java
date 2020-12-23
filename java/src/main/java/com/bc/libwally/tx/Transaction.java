package com.bc.libwally.tx;

import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.script.ScriptSig;
import com.bc.libwally.script.Witness;

import java.util.Arrays;

import static com.bc.libwally.ArrayUtils.reversed;
import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.crypto.Crypto.ecPrvKeyVerify;
import static com.bc.libwally.crypto.Crypto.ecSig2Der;
import static com.bc.libwally.crypto.Crypto.ecSigFromBytes;
import static com.bc.libwally.crypto.Crypto.ecSigNormalize;
import static com.bc.libwally.crypto.Crypto.ecSigVerify;
import static com.bc.libwally.crypto.CryptoConstants.EC_FLAG_ECDSA;
import static com.bc.libwally.crypto.CryptoConstants.EC_FLAG_GRIND_R;
import static com.bc.libwally.crypto.CryptoConstants.SHA256_LEN;
import static com.bc.libwally.tx.TxConstant.WALLY_SIGHASH_ALL;
import static com.bc.libwally.tx.TxConstant.WALLY_TX_FLAG_USE_WITNESS;
import static com.bc.libwally.tx.TxJni.wally_tx_add_input;
import static com.bc.libwally.tx.TxJni.wally_tx_add_output;
import static com.bc.libwally.tx.TxJni.wally_tx_from_bytes;
import static com.bc.libwally.tx.TxJni.wally_tx_get_btc_signature_hash;
import static com.bc.libwally.tx.TxJni.wally_tx_get_total_output_satoshi;
import static com.bc.libwally.tx.TxJni.wally_tx_get_vsize;
import static com.bc.libwally.tx.TxJni.wally_tx_init_alloc;
import static com.bc.libwally.tx.TxJni.wally_tx_set_input_script;
import static com.bc.libwally.tx.TxJni.wally_tx_set_input_witness;
import static com.bc.libwally.tx.TxJni.wally_tx_to_hex;

public class Transaction {

    private final byte[] hash;

    private final TxInput[] inputs;

    private final TxOutput[] outputs;

    private final WallyTx tx;

    public Transaction(WallyTx tx) {
        this.tx = tx;
        this.hash = null;
        this.inputs = null;
        this.outputs = null;
    }

    public Transaction(String hex) {
        inputs = null;
        outputs = null;
        byte[] data = hex2Bytes(hex);
        if (data.length != SHA256_LEN) {
            tx = wally_tx_from_bytes(data, WALLY_TX_FLAG_USE_WITNESS);
            hash = null;
        } else {
            hash = reversed(data);
            tx = null;
        }
    }

    public Transaction(TxInput[] inputs, TxOutput[] outputs) {
        hash = null;
        this.inputs = inputs;
        this.outputs = outputs;
        int version = 1;
        int locktime = 0;

        WallyTx tx = wally_tx_init_alloc(version, locktime, inputs.length, outputs.length);
        for (TxInput input : inputs) {
            tx = wally_tx_add_input(tx, input.createWallyTxInput());
        }

        for (TxOutput output : outputs) {
            tx = wally_tx_add_output(tx, output.createWallyTxOutput());
        }

        this.tx = tx;
    }

    private Transaction(TxInput[] inputs, TxOutput[] outputs, WallyTx tx) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.tx = tx;
        this.hash = null;
    }

    public String getDescription() {
        if (tx == null) {
            return null;
        }

        if (inputs != null) {
            for (TxInput input : inputs) {
                if (!input.isSigned()) {
                    return null;
                }
            }
        }

        return wally_tx_to_hex(tx, WALLY_TX_FLAG_USE_WITNESS);
    }

    public Long getTotalIn() {
        if (inputs == null)
            return null;
        long total = 0;
        for (TxInput input : inputs) {
            total += input.getAmount();
        }
        return total;
    }

    public Long getTotalOut() {
        if (tx == null)
            return null;

        return wally_tx_get_total_output_satoshi(tx);
    }

    public Boolean isFunded() {
        Long totalIn = getTotalIn();
        Long totalOut = getTotalOut();
        if (totalIn == null || totalOut == null)
            return null;
        return totalOut <= totalIn;
    }

    public Integer getVBytes() {
        if (tx == null)
            return null;

        WallyTx tx = this.tx.nativeClone();

        // Set scriptSig for all unsigned inputs to FEE_WORST_CASE
        for (int i = 0; i < inputs.length; i++) {
            TxInput input = inputs[i];
            if (!input.isSigned()) {
                ScriptSig scriptSig = input.getScriptSig();
                if (scriptSig != null) {
                    byte[] scriptWorstCase = scriptSig.render(ScriptSig.Purpose.FEE_WORST_CASE);
                    tx = wally_tx_set_input_script(tx, i, scriptWorstCase);
                }
            }
        }

        return wally_tx_get_vsize(tx);
    }

    public Long getFee() {
        Long totalIn = getTotalIn();
        Long totalOut = getTotalOut();
        if (totalIn == null || totalOut == null)
            return null;
        return totalIn - totalOut;
    }

    public Float getFeeRate() {
        Long fee = getFee();
        Integer vbytes = getVBytes();
        if (fee == null || vbytes == null)
            return null;

        return (float) fee / (float) vbytes;
    }

    public Transaction signed(HDKey[] keys) {
        if (tx == null)
            throw new TxException("No tx to sign");

        if (inputs == null)
            throw new TxException("No input found");

        if (keys.length != inputs.length) {
            throw new TxException("Wrong number of keys to sign");
        }

        try {
            WallyTx clonedTx = tx.nativeClone();
            TxInput[] inputs = cloneInputs(this.inputs);

            for (int i = 0; i < inputs.length; i++) {
                TxInput input = inputs[i];
                boolean hasWitness = input.getWitness() != null;
                byte[] messageBytes = new byte[SHA256_LEN];

                if (hasWitness) {
                    switch (input.getWitness().getType().getType()) {
                        case PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH:
                            byte[] scriptSig = input.getScriptSig()
                                                    .render(ScriptSig.Purpose.SIGNED);
                            clonedTx = wally_tx_set_input_script(clonedTx, i, scriptSig);
                            // fallthrough here
                        case PAY_TO_WITNESS_PUBKEY_HASH:
                            byte[] pubKeyData = keys[i].getKey().getPubKey();
                            if (!Arrays.equals(pubKeyData,
                                               input.getWitness()
                                                    .getType()
                                                    .getPubKey()
                                                    .getData())) {
                                throw new TxException("Invalid pubkey");
                            }

                            byte[] scriptCode = input.getWitness().getScriptCode();

                            int ret = wally_tx_get_btc_signature_hash(clonedTx,
                                                                      i,
                                                                      scriptCode,
                                                                      input.getAmount(),
                                                                      WALLY_SIGHASH_ALL,
                                                                      WALLY_TX_FLAG_USE_WITNESS,
                                                                      messageBytes);
                            if (ret != WALLY_OK) {
                                throw new TxException("wally_tx_get_btc_signature_hash error");
                            }
                            break;
                    }
                } else {
                    byte[] scriptPubKey = input.getScriptPubKey().getData();
                    int ret = wally_tx_get_btc_signature_hash(clonedTx,
                                                              i,
                                                              scriptPubKey,
                                                              0,
                                                              WALLY_SIGHASH_ALL,
                                                              0,
                                                              messageBytes);
                    if (ret != WALLY_OK) {
                        throw new TxException("wally_tx_get_btc_signature_hash error");
                    }
                }


                byte[] privKey = keys[i].getKey().getPrivKey();
                // skip prefix byte 0
                privKey = slice(privKey, 1, privKey.length);

                // Ensure private key is valid
                if (!ecPrvKeyVerify(privKey)) {
                    throw new TxException("Invalid private key");
                }

                byte[] compactSigBytes = ecSigFromBytes(privKey,
                                                        messageBytes,
                                                        EC_FLAG_ECDSA | EC_FLAG_GRIND_R);

                // Check that signature is valid and for the correct public key
                if (!ecSigVerify(keys[i].getKey().getPubKey(),
                                 messageBytes,
                                 EC_FLAG_ECDSA,
                                 compactSigBytes)) {
                    throw new TxException("Could not verify signature");
                }

                // Convert to low s form
                byte[] sigNormBytes = ecSigNormalize(compactSigBytes);

                // Convert normalized signature to DER
                byte[] sigBytes = ecSig2Der(sigNormBytes);

                // Store signature in TxInput
                if (hasWitness) {
                    Witness witness = input.getWitness().signed(sigBytes);
                    input.setWitness(witness);
                    clonedTx = wally_tx_set_input_witness(clonedTx,
                                                          i,
                                                          witness.createWallyTxWitnessStack());
                } else {
                    input.getScriptSig().setSignature(sigBytes);

                    byte[] signedScriptSig = input.getScriptSig().render(ScriptSig.Purpose.SIGNED);
                    clonedTx = wally_tx_set_input_script(clonedTx, i, signedScriptSig);
                }
            }

            return new Transaction(inputs, outputs, clonedTx);

        } catch (CloneNotSupportedException e) {
            throw new TxException(e.getMessage());
        }
    }

    private TxInput[] cloneInputs(TxInput[] inputs) throws CloneNotSupportedException {
        TxInput[] cloned = new TxInput[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            cloned[i] = inputs[i].clone();
        }
        return cloned;
    }

    public byte[] getHash() {
        return hash;
    }

    public TxInput[] getInputs() {
        return inputs;
    }

    public TxOutput[] getOutputs() {
        return outputs;
    }

    public WallyTx getTx() {
        return tx;
    }
}
