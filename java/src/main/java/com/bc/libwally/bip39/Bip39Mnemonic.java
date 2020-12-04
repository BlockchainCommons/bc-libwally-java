package com.bc.libwally.bip39;

import com.bc.libwally.ArrayUtils;
import com.bc.libwally.NativeWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.bc.libwally.ArrayUtils.joinToString;
import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.bip39.Bip39Constant.BIP39_SEED_LEN_512;
import static com.bc.libwally.bip39.Bip39Constant.BIP39_WORDLIST_LEN;
import static com.bc.libwally.bip39.Bip39Jni.bip39_get_word;
import static com.bc.libwally.bip39.Bip39Jni.bip39_get_wordlist;
import static com.bc.libwally.bip39.Bip39Jni.bip39_mnemonic_from_bytes;
import static com.bc.libwally.bip39.Bip39Jni.bip39_mnemonic_to_bytes;
import static com.bc.libwally.bip39.Bip39Jni.bip39_mnemonic_to_seed;
import static com.bc.libwally.bip39.Bip39Jni.bip39_mnemonic_validate;

public class Bip39Mnemonic {

    public static final int MAX_BYTES = 32;

    private final String[] words;

    public Bip39Mnemonic(Bip39Entropy entropy) {
        if (entropy.getLength() > MAX_BYTES)
            throw new Bip39Exception("entropy length is too large");

        String[] words = bip39_mnemonic_from_bytes(null, entropy.getData()).split(" ");
        if (!isValid(words))
            throw new Bip39Exception("invalid words");
        this.words = words;
    }

    public Bip39Mnemonic(String words) {
        this(words.split(" "));
    }

    public Bip39Mnemonic(String[] words) {
        if (!isValid(words))
            throw new Bip39Exception("invalid words");
        this.words = words;
    }

    public static String[] getBip39Words() {
        String[] words = new String[BIP39_WORDLIST_LEN];
        NativeWrapper.JniObject ptrObj = null;
        ptrObj = bip39_get_wordlist(null);
        for (int i = 0; i < BIP39_WORDLIST_LEN; i++) {
            String word = bip39_get_word(ptrObj, i);
            words[i] = word;
        }
        return words;
    }

    public static boolean isValid(String[] words) {
        if (words.length > MAX_BYTES)
            return false;

        List<String> bip39Words = Arrays.asList(getBip39Words());
        for (String word : words) {
            if (Collections.binarySearch(bip39Words, word) == -1)
                return false;
        }

        String mnemonic = joinToString(words, " ");
        return bip39_mnemonic_validate(null, mnemonic) == WALLY_OK;
    }

    public static boolean isValid(String words) {
        return isValid(words.split(" "));
    }

    public String[] getWords() {
        return this.words;
    }

    public Bip39Entropy getEntropy() {
        String mnemonic = getMnemonic();
        byte[] output = new byte[BIP39_SEED_LEN_512];
        int[] written = new int[1];

        if (bip39_mnemonic_to_bytes(null, mnemonic, output, written) != WALLY_OK) {
            throw new Bip39Exception("could not get entropy");
        }

        return new Bip39Entropy(output, written[0]);
    }

    public Bip39Seed getSeedHex(String passphrase) {
        String mnemonic = getMnemonic();
        byte[] output = new byte[BIP39_SEED_LEN_512];
        int[] written = new int[1];

        if (bip39_mnemonic_to_seed(mnemonic, passphrase, output, written) != WALLY_OK) {
            throw new Bip39Exception("could not get seed hex");
        }

        return new Bip39Seed(output, written[0]);
    }

    public Bip39Seed getSeedHex() {
        return getSeedHex("");
    }

    public String getMnemonic() {
        return ArrayUtils.joinToString(words, " ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bip39Mnemonic mnemonic = (Bip39Mnemonic) o;
        return Arrays.equals(words, mnemonic.words);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(words);
    }
}
