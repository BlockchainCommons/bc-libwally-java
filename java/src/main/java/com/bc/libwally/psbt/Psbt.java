package com.bc.libwally.psbt;

import com.bc.libwally.address.Key;
import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Exception;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.psbt.raw.WallyPsbt;
import com.bc.libwally.tx.Transaction;
import com.bc.libwally.tx.raw.WallyTx;

import java.util.Arrays;
import java.util.Map;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_clone_alloc;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_extract;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_finalize;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_from_base64;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_from_bytes;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_get_length;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_is_finalized;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_sign;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_to_base64;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_to_bytes;

public class Psbt {

    private final Network network;

    private final PsbtInput[] inputs;

    private final PsbtOutput[] outputs;

    private final WallyPsbt rawPsbt;

    public Psbt(String base64, Network network) {
        this(wally_psbt_from_base64(base64), network);
    }

    public Psbt(byte[] data, Network network) {
        this(wally_psbt_from_bytes(data), network);
    }

    private Psbt(WallyPsbt rawPsbt, Network network) {
        if (rawPsbt.getTx() == null) {
            throw new PsbtException("psbt tx is NULL");
        }

        this.network = network;
        this.rawPsbt = rawPsbt;

        PsbtInput[] inputs = new PsbtInput[rawPsbt.getInputsAllocLength()];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new PsbtInput(rawPsbt.getInputs()[i], network);
        }
        this.inputs = inputs;

        PsbtOutput[] outputs = new PsbtOutput[rawPsbt.getOutputsAllocLength()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = new PsbtOutput(rawPsbt.getOutputs()[i],
                                        rawPsbt.getTx().getOutputs()[i],
                                        network);
        }
        this.outputs = outputs;
    }

    public byte[] getData() {
        int len = wally_psbt_get_length(rawPsbt, 0);
        byte[] data = new byte[len];
        int[] written = new int[1];
        if (wally_psbt_to_bytes(rawPsbt, 0, data, written) != WALLY_OK) {
            throw new PsbtException("wally_psbt_to_bytes error");
        }

        return slice(data, written[0]);
    }

    public String getDescription() {
        return wally_psbt_to_base64(rawPsbt, 0);
    }

    public boolean isComplete() {
        return wally_psbt_is_finalized(rawPsbt);
    }

    public Transaction getTransaction() {
        if (rawPsbt.getTx() == null) {
            throw new PsbtException("psbt tx is NULL");
        }
        return new Transaction(rawPsbt.getTx());
    }

    public Long getFee() {
        Long valueOut = getTransaction().getTotalOut();
        if (valueOut == null) {
            throw new PsbtException("psbt tx total out is NULL");
        }

        Long tally = 0L;
        for (PsbtInput input : inputs) {
            if (input.isSegwit() && input.getAmount() == null) {
                return null;
            }

            tally += input.getAmount();
        }

        if (tally < valueOut) {
            throw new PsbtException("invalid total in");
        }

        return tally - valueOut;
    }

    public Transaction getTransactionFinal() {
        try {
            WallyTx tx = wally_psbt_extract(rawPsbt);
            return new Transaction(tx);
        } catch (PsbtException ignore) {
            return null;
        }
    }

    public Psbt signed(Key privKey) {
        if (privKey.getNetwork() != network) {
            throw new PsbtException("Invalid key network");
        }
        WallyPsbt clonedPsbt = psbtNativeClone();
        return new Psbt(wally_psbt_sign(clonedPsbt, privKey.getData(), 0), network);
    }

    public Psbt signed(HDKey hdKey) {
        if (hdKey.getNetwork() != network) {
            throw new PsbtException("Invalid key network");
        }
        Psbt psbt = this;
        for (PsbtInput input : inputs) {
            Map<PubKey, KeyOrigin> originMap = input.getCanSignOriginMap(hdKey);
            if (originMap != null) {
                for (Map.Entry<PubKey, KeyOrigin> e : originMap.entrySet()) {
                    try {
                        HDKey childKey = hdKey.derive(e.getValue().getPath());
                        if (childKey.getPrivKey().getPubKey().equals(e.getKey())) {
                            psbt = psbt.signed(childKey.getPrivKey());
                        }
                    } catch (Bip32Exception ignore) {
                    }
                }
            }
        }

        return psbt;
    }

    public Psbt finalized() {
        WallyPsbt clonedPsbt = psbtNativeClone();
        return new Psbt(wally_psbt_finalize(clonedPsbt), network);
    }

    private WallyPsbt psbtNativeClone() {
        return wally_psbt_clone_alloc(rawPsbt, 0);
    }

    public Network getNetwork() {
        return network;
    }

    public PsbtInput[] getInputs() {
        return inputs;
    }

    public PsbtOutput[] getOutputs() {
        return outputs;
    }

    public WallyPsbt getRawPsbt() {
        return rawPsbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Psbt psbt1 = (Psbt) o;
        return network == psbt1.network && Arrays.equals(getData(), psbt1.getData());
    }
}
