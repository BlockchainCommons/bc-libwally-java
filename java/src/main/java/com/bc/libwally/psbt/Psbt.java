package com.bc.libwally.psbt;

import com.bc.libwally.address.Key;
import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Exception;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.core.CoreException;
import com.bc.libwally.psbt.raw.WallyPsbt;
import com.bc.libwally.tx.Transaction;
import com.bc.libwally.tx.WallyTx;

import java.util.Arrays;
import java.util.Map;

import static com.bc.libwally.ArrayUtils.slice;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.core.Core.base642Bytes;
import static com.bc.libwally.core.Core.bytes2Base64;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_clone_alloc;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_extract;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_finalize;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_from_bytes;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_get_length;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_sign;
import static com.bc.libwally.psbt.PsbtJni.wally_psbt_to_bytes;

public class Psbt {

    private final Network network;

    private final PsbtInput[] inputs;

    private final PsbtOutput[] outputs;

    private final WallyPsbt psbt;

    private WallyPsbt nativeClone() {
        return wally_psbt_clone_alloc(psbt, 0);
    }

    public static Psbt newInstance(byte[] data, Network network) {
        WallyPsbt psbt = wally_psbt_from_bytes(data);
        if (psbt.getTx() == null) {
            throw new PsbtException("psbt tx is NULL");
        }
        return new Psbt(psbt, network);
    }

    public static Psbt newInstance(String base64, Network network) {
        if (base64 == null || base64.isEmpty()) {
            throw new PsbtException("base64 string is invalid");
        }

        byte[] data;
        try {
            data = base642Bytes(base64);
        } catch (CoreException ignore) {
            throw new PsbtException("base64 string is invalid");
        }
        return newInstance(data, network);
    }

    private Psbt(WallyPsbt psbt, Network network) {
        this.network = network;
        this.psbt = psbt;

        PsbtInput[] inputs = new PsbtInput[psbt.getInputsAllocLength()];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new PsbtInput(psbt.getInputs()[i], network);
        }
        this.inputs = inputs;

        PsbtOutput[] outputs = new PsbtOutput[psbt.getOutputsAllocLength()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = new PsbtOutput(psbt.getOutputs()[i],
                                        psbt.getTx().getOutputs()[i],
                                        network);
        }
        this.outputs = outputs;
    }

    public byte[] getData() {
        int len = wally_psbt_get_length(psbt, 0);
        byte[] data = new byte[len];
        int[] written = new int[1];
        if (wally_psbt_to_bytes(psbt, 0, data, written) != WALLY_OK) {
            throw new PsbtException("wally_psbt_to_bytes error");
        }

        return slice(data, written[0]);
    }

    public String getDescription() {
        return bytes2Base64(getData());
    }

    public boolean isComplete() {
        // TODO: add function to libwally-core to check this directly
        return getTransactionFinal() != null;
    }

    public Transaction getTransaction() {
        if (psbt.getTx() == null) {
            throw new PsbtException("psbt tx is NULL");
        }
        return new Transaction(psbt.getTx());
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
            WallyTx tx = wally_psbt_extract(psbt);
            return new Transaction(tx);
        } catch (PsbtException ignore) {
            return null;
        }
    }

    public Psbt signed(Key privKey) {
        // TODO: sanity key for network
        WallyPsbt clonedPsbt = nativeClone();
        return new Psbt(wally_psbt_sign(clonedPsbt, privKey.getData(), 0), network);
    }

    public Psbt signed(HDKey hdKey) {
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
        WallyPsbt clonedPsbt = nativeClone();
        return new Psbt(wally_psbt_finalize(clonedPsbt), network);
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

    public WallyPsbt getPsbt() {
        return psbt;
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
