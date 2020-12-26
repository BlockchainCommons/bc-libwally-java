package com.bc.libwally;

import com.bc.libwally.address.Key;
import com.bc.libwally.address.PubKey;
import com.bc.libwally.bip32.Bip32Path;
import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.psbt.KeyOrigin;
import com.bc.libwally.psbt.Psbt;
import com.bc.libwally.psbt.PsbtException;
import com.bc.libwally.psbt.PsbtInput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import static com.bc.libwally.core.Core.base642Bytes;
import static com.bc.libwally.core.Core.hex2Bytes;
import static com.bc.libwally.util.TestUtils.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class PsbtTest {

    private static final byte[] FINGERPRINT = hex2Bytes("d90c6a4f");

    private static final String VALID_PSBT = "cHNidP8BAHUCAAAAASaBcTce3/KF6Tet7qSze3gADAVmy7OtZGQ" +
                                             "XE8pCFxv2AAAAAAD+////AtPf9QUAAAAAGXapFNDFmQPFusKGh2" +
                                             "DpD9UhpGZap2UgiKwA4fUFAAAAABepFDVF5uM7gyxHBQ8k0+65P" +
                                             "JwDlIvHh7MuEwAAAQD9pQEBAAAAAAECiaPHHqtNIOA3G7ukzGmP" +
                                             "opXJRjr6Ljl/hTPMti+VZ+UBAAAAFxYAFL4Y0VKpsBIDna89p95" +
                                             "PUzSe7LmF/////4b4qkOnHf8USIk6UwpyN+9rRgi7st0tAXHmOu" +
                                             "xqSJC0AQAAABcWABT+Pp7xp0XpdNkCxDVZQ6vLNL1TU/////8CA" +
                                             "MLrCwAAAAAZdqkUhc/xCX/Z4Ai7NK9wnGIZeziXikiIrHL++E4s" +
                                             "AAAAF6kUM5cluiHv1irHU6m80GfWx6ajnQWHAkcwRAIgJxK+IuA" +
                                             "nDzlPVoMR3HyppolwuAJf3TskAinwf4pfOiQCIAGLONfc0xTnNM" +
                                             "kna9b7QPZzMlvEuqFEyADS8vAtsnZcASED0uFWdJQbrUqZY3LLh" +
                                             "+GFbTZSYG2YVi/jnF6efkE/IQUCSDBFAiEA0SuFLYXc2WHS9fSr" +
                                             "ZgZU327tzHlMDDPOXMMJ/7X85Y0CIGczio4OFyXBl/saiK9Z9R5" +
                                             "E5CVbIBZ8hoQDHAXR8lkqASECI7cr7vCWXRC+B3jv7NYfysb3mk" +
                                             "6haTkzgHNEZPhPKrMAAAAAAAAA";

    private static final String FINALIZED_PSBT = "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV" +
                                                 "5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpoqka7CwmK6k" +
                                                 "QiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWA" +
                                                 "BTYXCtx0AYLCcmIauuBXlCZHdoSTQDh9QUAAAAAFgAUAK6p" +
                                                 "ouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQG" +
                                                 "L0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRA" +
                                                 "IgWPb8fGoz4bMVSNSByCbAFb0wE1qtQs1neQ2rZtKtJDsCI" +
                                                 "Eoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7/" +
                                                 "//8CgPD6AgAAAAAXqRQPuUY0IWlrgsgzryQceMF9295JNIf" +
                                                 "Q8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9hh2UAAA" +
                                                 "ABB9oARzBEAiB0AYrUGACXuHMyPAAVcgs2hMyBI4kQSOfbz" +
                                                 "ZtVrWecmQIgc9Npt0Dj61Pc76M4I8gHBRTKVafdlUTxV8Fn" +
                                                 "kTJhEYwBSDBFAiEA9hA4swjcHahlo0hSdG8BV3KTQgjG0kR" +
                                                 "UOTzZm98iF3cCIAVuZ1pnWm0KArhbFOXikHTYolqbV2C+oo" +
                                                 "FvZhkQoAbqAUdSIQKVg785rgpgl0etGZrd1jT6YQhVnWxc0" +
                                                 "5tMIYPxq5bgfyEC2rYf9JoU22p9ArDNH7t4/EsYMStbTlTa" +
                                                 "5Nui+/71NtdSrgABASAAwusLAAAAABepFLf1+vQOPUClpFm" +
                                                 "x2zU18rcvqSHohwEHIyIAIIwjUxc3Q7WV37Sge3K6jkLjeX" +
                                                 "2nTof+fZ10l+OyAokDAQjaBABHMEQCIGLrelVhB6fHP0WsS" +
                                                 "rWh3d9vcHX7EnWWmn84Pv/3hLyyAiAMBdu3Rw2/LwhVfdNW" +
                                                 "xzJcHtMJE+mWzThAlF2xIijaXwFHMEQCIGX0W6WZi1mif/4" +
                                                 "ae+0BavHx+Q1Us6qPdFCqX1aiUQO9AiB/ckcDrR7blmgLKE" +
                                                 "tW1P/LiPf7dZ6rvgiqMPKbhROD0gFHUiEDCJ3BDHrG21T5E" +
                                                 "ymvYXMz2ziM6tDCMfcjN50bmQMLAtwhAjrdkE89bc9Z3bkG" +
                                                 "sN7iNSm3/7ntUOXoYVGSaGAiHw5zUq4AIgIDqaTDf1mW06o" +
                                                 "l26xrVwrwZQOUSSlCRgs1R1Ptnuylh3EQ2QxqTwAAAIAAAA" +
                                                 "CABAAAgAAiAgJ/Y5l1fS7/VaE2rQLGhLGDi2VW5fG2s0KCq" +
                                                 "UtrUAUQlhDZDGpPAAAAgAAAAIAFAACAAA==";

    private static final String UNSIGNED_PSBT = "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5" +
                                                "JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpoqka7CwmK6kQi" +
                                                "wHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTY" +
                                                "XCtx0AYLCcmIauuBXlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw" +
                                                "+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL0l+E" +
                                                "rkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8" +
                                                "fGoz4bMVSNSByCbAFb0wE1qtQs1neQ2rZtKtJDsCIEoc7SYE" +
                                                "xnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6" +
                                                "AgAAAAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAA" +
                                                "ABepFCnKdPigj4GZlCgYXJe12FLkBj9hh2UAAAABAwQBAAAA" +
                                                "AQRHUiEClYO/Oa4KYJdHrRma3dY0+mEIVZ1sXNObTCGD8auW" +
                                                "4H8hAtq2H/SaFNtqfQKwzR+7ePxLGDErW05U2uTbovv+9TbX" +
                                                "Uq4iBgKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bg" +
                                                "fxDZDGpPAAAAgAAAAIAAAACAIgYC2rYf9JoU22p9ArDNH7t4" +
                                                "/EsYMStbTlTa5Nui+/71NtcQ2QxqTwAAAIAAAACAAQAAgAAB" +
                                                "ASAAwusLAAAAABepFLf1+vQOPUClpFmx2zU18rcvqSHohwED" +
                                                "BAEAAAABBCIAIIwjUxc3Q7WV37Sge3K6jkLjeX2nTof+fZ10" +
                                                "l+OyAokDAQVHUiEDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcj" +
                                                "N50bmQMLAtwhAjrdkE89bc9Z3bkGsN7iNSm3/7ntUOXoYVGS" +
                                                "aGAiHw5zUq4iBgI63ZBPPW3PWd25BrDe4jUpt/+57VDl6GFR" +
                                                "kmhgIh8OcxDZDGpPAAAAgAAAAIADAACAIgYDCJ3BDHrG21T5" +
                                                "EymvYXMz2ziM6tDCMfcjN50bmQMLAtwQ2QxqTwAAAIAAAACA" +
                                                "AgAAgAAiAgOppMN/WZbTqiXbrGtXCvBlA5RJKUJGCzVHU+2e" +
                                                "7KWHcRDZDGpPAAAAgAAAAIAEAACAACICAn9jmXV9Lv9VoTat" +
                                                "AsaEsYOLZVbl8bazQoKpS2tQBRCWENkMak8AAACAAAAAgAUA" +
                                                "AIAA";

    private static final String SIGNED_PSBT = "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JP" +
                                              "VFiHuyq911AAAAAAD/////g40EJ9DsZQpoqka7CwmK6kQiwHGy" +
                                              "yng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0A" +
                                              "YLCcmIauuBXlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9V" +
                                              "Ruh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL0l+ErkALaISL4J" +
                                              "23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSB" +
                                              "yCbAFb0wE1qtQs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3X" +
                                              "iwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAAAAAXqRQPuUY0" +
                                              "IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlC" +
                                              "gYXJe12FLkBj9hh2UAAAAiAgKVg785rgpgl0etGZrd1jT6YQhV" +
                                              "nWxc05tMIYPxq5bgf0cwRAIgdAGK1BgAl7hzMjwAFXILNoTMgS" +
                                              "OJEEjn282bVa1nnJkCIHPTabdA4+tT3O+jOCPIBwUUylWn3ZVE" +
                                              "8VfBZ5EyYRGMASICAtq2H/SaFNtqfQKwzR+7ePxLGDErW05U2u" +
                                              "Tbovv+9TbXSDBFAiEA9hA4swjcHahlo0hSdG8BV3KTQgjG0kRU" +
                                              "OTzZm98iF3cCIAVuZ1pnWm0KArhbFOXikHTYolqbV2C+ooFvZh" +
                                              "kQoAbqAQEDBAEAAAABBEdSIQKVg785rgpgl0etGZrd1jT6YQhV" +
                                              "nWxc05tMIYPxq5bgfyEC2rYf9JoU22p9ArDNH7t4/EsYMStbTl" +
                                              "Ta5Nui+/71NtdSriIGApWDvzmuCmCXR60Zmt3WNPphCFWdbFzT" +
                                              "m0whg/GrluB/ENkMak8AAACAAAAAgAAAAIAiBgLath/0mhTban" +
                                              "0CsM0fu3j8SxgxK1tOVNrk26L7/vU21xDZDGpPAAAAgAAAAIAB" +
                                              "AACAAAEBIADC6wsAAAAAF6kUt/X69A49QKWkWbHbNTXyty+pIe" +
                                              "iHIgIDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtxH" +
                                              "MEQCIGLrelVhB6fHP0WsSrWh3d9vcHX7EnWWmn84Pv/3hLyyAi" +
                                              "AMBdu3Rw2/LwhVfdNWxzJcHtMJE+mWzThAlF2xIijaXwEiAgI6" +
                                              "3ZBPPW3PWd25BrDe4jUpt/+57VDl6GFRkmhgIh8Oc0cwRAIgZf" +
                                              "RbpZmLWaJ//hp77QFq8fH5DVSzqo90UKpfVqJRA70CIH9yRwOt" +
                                              "HtuWaAsoS1bU/8uI9/t1nqu+CKow8puFE4PSAQEDBAEAAAABBC" +
                                              "IAIIwjUxc3Q7WV37Sge3K6jkLjeX2nTof+fZ10l+OyAokDAQVH" +
                                              "UiEDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtwhAj" +
                                              "rdkE89bc9Z3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5zUq4iBgI6" +
                                              "3ZBPPW3PWd25BrDe4jUpt/+57VDl6GFRkmhgIh8OcxDZDGpPAA" +
                                              "AAgAAAAIADAACAIgYDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcj" +
                                              "N50bmQMLAtwQ2QxqTwAAAIAAAACAAgAAgAAiAgOppMN/WZbTqi" +
                                              "XbrGtXCvBlA5RJKUJGCzVHU+2e7KWHcRDZDGpPAAAAgAAAAIAE" +
                                              "AACAACICAn9jmXV9Lv9VoTatAsaEsYOLZVbl8bazQoKpS2tQBR" +
                                              "CWENkMak8AAACAAAAAgAUAAIAA";

    private static final String MASTER_KEY_XPRIV = "tprv8ZgxMBicQKsPd9TeAdPADNnSyH9SSUUbTVe" +
                                                   "FszDE23Ki6TBB5nCefAdHkK8Fm3qMQR6sHwA56z" +
                                                   "qRmKmxnHk37JkiFzvncDqoKmPWubu7hDF";

    // Paths
    private static final Bip32Path PATH_0 = new Bip32Path("m/0'/0'/0'");
    private static final Bip32Path PATH_1 = new Bip32Path("m/0'/0'/1'");
    private static final Bip32Path PATH_2 = new Bip32Path("m/0'/0'/2'");
    private static final Bip32Path PATH_3 = new Bip32Path("m/0'/0'/3'");
    private static final Bip32Path PATH_4 = new Bip32Path("m/0'/0'/4'");
    private static final Bip32Path PATH_5 = new Bip32Path("m/0'/0'/5'");

    // Private keys (testnet)
    private static final String WIF_0 = "cP53pDbR5WtAD8dYAW9hhTjuvvTVaEiQBdrz9XPrgLBeRFiyCbQr"; // m/0'/0'/0'
    private static final String WIF_1 = "cT7J9YpCwY3AVRFSjN6ukeEeWY6mhpbJPxRaDaP5QTdygQRxP9Au"; // m/0'/0'/1'
    private static final String WIF_2 = "cR6SXDoyfQrcp4piaiHE97Rsgta9mNhGTen9XeonVgwsh4iSgw6d"; // m/0'/0'/2'
    private static final String WIF_3 = "cNBc3SWUip9PPm1GjRoLEJT6T41iNzCYtD7qro84FMnM5zEqeJsE"; // m/0'/0'/3'

    // Public keys
    private static final PubKey PUB_KEY_0 = new PubKey(
            "029583bf39ae0a609747ad199addd634fa6108559d6c5cd39b4c2183f1ab96e07f",
            Network.TESTNET);
    private static final PubKey PUB_KEY_1 = new PubKey(
            "02dab61ff49a14db6a7d02b0cd1fbb78fc4b18312b5b4e54dae4dba2fbfef536d7",
            Network.TESTNET);
    private static final PubKey PUB_KEY_2 = new PubKey(
            "03089dc10c7ac6db54f91329af617333db388cead0c231f723379d1b99030b02dc",
            Network.TESTNET);
    private static final PubKey PUB_KEY_3 = new PubKey(
            "023add904f3d6dcf59ddb906b0dee23529b7ffb9ed50e5e86151926860221f0e73",
            Network.TESTNET);
    private static final PubKey PUB_KEY_4 = new PubKey(
            "03a9a4c37f5996d3aa25dbac6b570af0650394492942460b354753ed9eeca58771",
            Network.TESTNET);
    private static final PubKey PUB_KEY_5 = new PubKey(
            "027f6399757d2eff55a136ad02c684b1838b6556e5f1b6b34282a94b6b50051096",
            Network.TESTNET);

    // Singed with keys m/0'/0'/0' and m/0'/0'/2'
    private static final String SIGNED_PSBT_0_2 = "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyH" +
                                                  "V5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpoqka7CwmK" +
                                                  "6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAA" +
                                                  "AWABTYXCtx0AYLCcmIauuBXlCZHdoSTQDh9QUAAAAAFgAU" +
                                                  "AK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqt" +
                                                  "c5MQGL0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAA" +
                                                  "SEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qtQs1neQ2rZt" +
                                                  "KtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4" +
                                                  "FX26Af7///8CgPD6AgAAAAAXqRQPuUY0IWlrgsgzryQceM" +
                                                  "F9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLk" +
                                                  "Bj9hh2UAAAAiAgKVg785rgpgl0etGZrd1jT6YQhVnWxc05" +
                                                  "tMIYPxq5bgf0cwRAIgdAGK1BgAl7hzMjwAFXILNoTMgSOJ" +
                                                  "EEjn282bVa1nnJkCIHPTabdA4+tT3O+jOCPIBwUUylWn3Z" +
                                                  "VE8VfBZ5EyYRGMAQEDBAEAAAABBEdSIQKVg785rgpgl0et" +
                                                  "GZrd1jT6YQhVnWxc05tMIYPxq5bgfyEC2rYf9JoU22p9Ar" +
                                                  "DNH7t4/EsYMStbTlTa5Nui+/71NtdSriIGApWDvzmuCmCX" +
                                                  "R60Zmt3WNPphCFWdbFzTm0whg/GrluB/ENkMak8AAACAAA" +
                                                  "AAgAAAAIAiBgLath/0mhTban0CsM0fu3j8SxgxK1tOVNrk" +
                                                  "26L7/vU21xDZDGpPAAAAgAAAAIABAACAAAEBIADC6wsAAA" +
                                                  "AAF6kUt/X69A49QKWkWbHbNTXyty+pIeiHIgIDCJ3BDHrG" +
                                                  "21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtxHMEQCIGLrel" +
                                                  "VhB6fHP0WsSrWh3d9vcHX7EnWWmn84Pv/3hLyyAiAMBdu3" +
                                                  "Rw2/LwhVfdNWxzJcHtMJE+mWzThAlF2xIijaXwEBAwQBAA" +
                                                  "AAAQQiACCMI1MXN0O1ld+0oHtyuo5C43l9p06H/n2ddJfj" +
                                                  "sgKJAwEFR1IhAwidwQx6xttU+RMpr2FzM9s4jOrQwjH3Iz" +
                                                  "edG5kDCwLcIQI63ZBPPW3PWd25BrDe4jUpt/+57VDl6GFR" +
                                                  "kmhgIh8Oc1KuIgYCOt2QTz1tz1nduQaw3uI1Kbf/ue1Q5e" +
                                                  "hhUZJoYCIfDnMQ2QxqTwAAAIAAAACAAwAAgCIGAwidwQx6" +
                                                  "xttU+RMpr2FzM9s4jOrQwjH3IzedG5kDCwLcENkMak8AAA" +
                                                  "CAAAAAgAIAAIAAIgIDqaTDf1mW06ol26xrVwrwZQOUSSlC" +
                                                  "Rgs1R1Ptnuylh3EQ2QxqTwAAAIAAAACABAAAgAAiAgJ/Y5" +
                                                  "l1fS7/VaE2rQLGhLGDi2VW5fG2s0KCqUtrUAUQlhDZDGpP" +
                                                  "AAAAgAAAAIAFAACAAA==";

    // Singed with keys m/0'/0'/1' (test vector modified for EC_FLAG_GRIND_R) and m/0'/0'/3'
    private static final String SIGNED_PSBT_1_3 = "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyH" +
                                                  "V5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpoqka7CwmK" +
                                                  "6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAA" +
                                                  "AWABTYXCtx0AYLCcmIauuBXlCZHdoSTQDh9QUAAAAAFgAU" +
                                                  "AK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqt" +
                                                  "c5MQGL0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAA" +
                                                  "SEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qtQs1neQ2rZt" +
                                                  "KtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4" +
                                                  "FX26Af7///8CgPD6AgAAAAAXqRQPuUY0IWlrgsgzryQceM" +
                                                  "F9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLk" +
                                                  "Bj9hh2UAAAAiAgLath/0mhTban0CsM0fu3j8SxgxK1tOVN" +
                                                  "rk26L7/vU210gwRQIhAPYQOLMI3B2oZaNIUnRvAVdyk0II" +
                                                  "xtJEVDk82ZvfIhd3AiAFbmdaZ1ptCgK4WxTl4pB02KJam1" +
                                                  "dgvqKBb2YZEKAG6gEBAwQBAAAAAQRHUiEClYO/Oa4KYJdH" +
                                                  "rRma3dY0+mEIVZ1sXNObTCGD8auW4H8hAtq2H/SaFNtqfQ" +
                                                  "KwzR+7ePxLGDErW05U2uTbovv+9TbXUq4iBgKVg785rgpg" +
                                                  "l0etGZrd1jT6YQhVnWxc05tMIYPxq5bgfxDZDGpPAAAAgA" +
                                                  "AAAIAAAACAIgYC2rYf9JoU22p9ArDNH7t4/EsYMStbTlTa" +
                                                  "5Nui+/71NtcQ2QxqTwAAAIAAAACAAQAAgAABASAAwusLAA" +
                                                  "AAABepFLf1+vQOPUClpFmx2zU18rcvqSHohyICAjrdkE89" +
                                                  "bc9Z3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5zRzBEAiBl9F" +
                                                  "ulmYtZon/+GnvtAWrx8fkNVLOqj3RQql9WolEDvQIgf3JH" +
                                                  "A60e25ZoCyhLVtT/y4j3+3Weq74IqjDym4UTg9IBAQMEAQ" +
                                                  "AAAAEEIgAgjCNTFzdDtZXftKB7crqOQuN5fadOh/59nXSX" +
                                                  "47ICiQMBBUdSIQMIncEMesbbVPkTKa9hczPbOIzq0MIx9y" +
                                                  "M3nRuZAwsC3CECOt2QTz1tz1nduQaw3uI1Kbf/ue1Q5ehh" +
                                                  "UZJoYCIfDnNSriIGAjrdkE89bc9Z3bkGsN7iNSm3/7ntUO" +
                                                  "XoYVGSaGAiHw5zENkMak8AAACAAAAAgAMAAIAiBgMIncEM" +
                                                  "esbbVPkTKa9hczPbOIzq0MIx9yM3nRuZAwsC3BDZDGpPAA" +
                                                  "AAgAAAAIACAACAACICA6mkw39ZltOqJdusa1cK8GUDlEkp" +
                                                  "QkYLNUdT7Z7spYdxENkMak8AAACAAAAAgAQAAIAAIgICf2" +
                                                  "OZdX0u/1WhNq0CxoSxg4tlVuXxtrNCgqlLa1AFEJYQ2Qxq" +
                                                  "TwAAAIAAAACABQAAgAA=";

    // Mainnet multisig wallet based on BIP32 test vectors.
    // To import into Bitcoin Core (experimental descriptor wallet branch) use:
    // importdescriptors '[{"range":1000,"timestamp":"now","watchonly":true,"internal":false,
    // "desc":"wsh(sortedmulti(2,[3442193e\/48h\/0h\/0h\/2h]xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi\/0\/*,
    // [bd16bee5\/48h\/0h\/0h\/2h]xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a\/0\/*))#75z63vc9","active":true},
    // {"range":1000,"timestamp":"now","watchonly":true,"internal":true,"desc":"wsh(sortedmulti(2,[3442193e\/48h\/0h\/0h\/2h]xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi\/1\/*,
    // [bd16bee5\/48h\/0h\/0h\/2h]xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a\/1\/*))#8837llds","active":true}]'

    private static final byte[] FINGERPRINT_1 = hex2Bytes("3442193e");
    private static final byte[] FINGERPRINT_2 = hex2Bytes("bd16bee5");
    private static final String MASTER_1 = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3" +
                                           "jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBx" +
                                           "rMPHi";
    private static final String MASTER_2 = "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2D" +
                                           "tALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYs" +
                                           "CzC2U";
    private static final String MULTI_UNSIGNED_PSBT_WITHOUT_CHANGE = "cHNidP8BAFICAAAAAV/0Rj8kmS/" +
                                                                     "ZB5NjsQvCKM1LTtovmhuQu2GITt" +
                                                                     "z/XUFnAAAAAAD9////Af4SAAAAA" +
                                                                     "AAAFgAUgPiTflaS1yPZmZleFfTq" +
                                                                     "7fUwdIYAAAAAAAEBK4gTAAAAAAA" +
                                                                     "AIgAg+GCObltTf4/IGC6xE89A9W" +
                                                                     "S5nPmdhxcMTxrCWQdO6P0BBUdSI" +
                                                                     "QIRWymltMLmSLuvwQBG3wDoMRcQ" +
                                                                     "lj79Fah1NMZw3Q6w+iEDkxPICph" +
                                                                     "GAQSk6avIbx9z0fqYLssxciadkX" +
                                                                     "QV5q7uJnVSriIGAhFbKaW0wuZIu" +
                                                                     "6/BAEbfAOgxFxCWPv0VqHU0xnDd" +
                                                                     "DrD6HL0WvuUwAACAAAAAgAAAAIA" +
                                                                     "CAACAAAAAAAAAAAAiBgOTE8gKmE" +
                                                                     "YBBKTpq8hvH3PR+pguyzFyJp2Rd" +
                                                                     "BXmru4mdRw0Qhk+MAAAgAAAAIAA" +
                                                                     "AACAAgAAgAAAAAAAAAAAAAA=";

    private static final String MULTI_PSBT_WITHOUT_CHANGE_HEX = "020000000001015ff4463f24992fd907" +
                                                                "9363b10bc228cd4b4eda2f9a1b90bb61" +
                                                                "884edcff5d41670000000000fdffffff" +
                                                                "01fe1200000000000016001480f8937e" +
                                                                "5692d723d999995e15f4eaedf5307486" +
                                                                "04004830450221009222d670173b1231" +
                                                                "512e96056597ab3a509e7d0919581a7e" +
                                                                "95aa7b272b69b6de022062a6b500367b" +
                                                                "0e0bd39557f5fa7e4539dc65c1c0fb44" +
                                                                "57559aea9d7efb1fba87014830450221" +
                                                                "00e02212a6eb7c6b3feb411aec6a0a8b" +
                                                                "4bce6bdca8379e03b9c5d8a809027891" +
                                                                "59022041eec69689e7eae62f5c120edf" +
                                                                "a77fe5d3a4a631f2a2e7b763603e1bb4" +
                                                                "2a72560147522102115b29a5b4c2e648" +
                                                                "bbafc10046df00e8311710963efd15a8" +
                                                                "7534c670dd0eb0fa21039313c80a9846" +
                                                                "0104a4e9abc86f1f73d1fa982ecb3172" +
                                                                "269d917415e6aeee267552ae00000000";

    private static final String MULTI_UNSIGNED_PSBT_WITH_CHANGE = "cHNidP8BAH0CAAAAAV/0Rj8kmS/ZB5" +
                                                                  "NjsQvCKM1LTtovmhuQu2GITtz/XUFn" +
                                                                  "AAAAAAD9////AqAPAAAAAAAAIgAg2S" +
                                                                  "AanVpF/Lx6c7mjRV2xL95PrYeO1kq+" +
                                                                  "yERNnuQ5oBYzAwAAAAAAABYAFID4k3" +
                                                                  "5Wktcj2ZmZXhX06u31MHSGAAAAAAAB" +
                                                                  "ASuIEwAAAAAAACIAIPhgjm5bU3+PyB" +
                                                                  "gusRPPQPVkuZz5nYcXDE8awlkHTuj9" +
                                                                  "AQVHUiECEVsppbTC5ki7r8EARt8A6D" +
                                                                  "EXEJY+/RWodTTGcN0OsPohA5MTyAqY" +
                                                                  "RgEEpOmryG8fc9H6mC7LMXImnZF0Fe" +
                                                                  "au7iZ1Uq4iBgIRWymltMLmSLuvwQBG" +
                                                                  "3wDoMRcQlj79Fah1NMZw3Q6w+hy9Fr" +
                                                                  "7lMAAAgAAAAIAAAACAAgAAgAAAAAAA" +
                                                                  "AAAAIgYDkxPICphGAQSk6avIbx9z0f" +
                                                                  "qYLssxciadkXQV5q7uJnUcNEIZPjAA" +
                                                                  "AIAAAACAAAAAgAIAAIAAAAAAAAAAAA" +
                                                                  "ABAUdSIQMROfTTVvMRvdrTpGn+pMYv" +
                                                                  "CLB/78Bc/PK8qqIYwgg1diEDUb/gzE" +
                                                                  "HWzqIxfhWictWQ+Osk5XiRlQCzWIzI" +
                                                                  "+0xHd11SriICAxE59NNW8xG92tOkaf" +
                                                                  "6kxi8IsH/vwFz88ryqohjCCDV2HL0W" +
                                                                  "vuUwAACAAAAAgAAAAIACAACAAQAAAA" +
                                                                  "IAAAAiAgNRv+DMQdbOojF+FaJy1ZD4" +
                                                                  "6yTleJGVALNYjMj7TEd3XRw0Qhk+MA" +
                                                                  "AAgAAAAIAAAACAAgAAgAEAAAACAAAA" +
                                                                  "AAA=";

    private static final String MULTI_SIGNED_PSBT_WITH_CHANGE = "cHNidP8BAH0CAAAAAV/0Rj8kmS/ZB5Nj" +
                                                                "sQvCKM1LTtovmhuQu2GITtz/XUFnAAAA" +
                                                                "AAD9////AqAPAAAAAAAAIgAg2SAanVpF" +
                                                                "/Lx6c7mjRV2xL95PrYeO1kq+yERNnuQ5" +
                                                                "oBYzAwAAAAAAABYAFID4k35Wktcj2ZmZ" +
                                                                "XhX06u31MHSGAAAAAAABASuIEwAAAAAA" +
                                                                "ACIAIPhgjm5bU3+PyBgusRPPQPVkuZz5" +
                                                                "nYcXDE8awlkHTuj9IgIDkxPICphGAQSk" +
                                                                "6avIbx9z0fqYLssxciadkXQV5q7uJnVI" +
                                                                "MEUCIQCcKOgwlnCDCaYRYQQWzGu9tcZu" +
                                                                "J9JPX3UcU0/8fBSBAgIgUBUbWh7fxytG" +
                                                                "/Fm0rQE6f08wLu3GwXbNkykAHzBR8f4B" +
                                                                "IgICEVsppbTC5ki7r8EARt8A6DEXEJY+" +
                                                                "/RWodTTGcN0OsPpHMEQCIHxzEBZRBpJ7" +
                                                                "B3lHTe6kAgDJq7d2O47710Sz4kglToOO" +
                                                                "AiA5bGwOgJXYc/y19RZ60wZWdJN/DlE8" +
                                                                "4mGtoJFE0NT5bQEBBUdSIQIRWymltMLm" +
                                                                "SLuvwQBG3wDoMRcQlj79Fah1NMZw3Q6w" +
                                                                "+iEDkxPICphGAQSk6avIbx9z0fqYLssx" +
                                                                "ciadkXQV5q7uJnVSriIGAhFbKaW0wuZI" +
                                                                "u6/BAEbfAOgxFxCWPv0VqHU0xnDdDrD6" +
                                                                "HL0WvuUwAACAAAAAgAAAAIACAACAAAAA" +
                                                                "AAAAAAAiBgOTE8gKmEYBBKTpq8hvH3PR" +
                                                                "+pguyzFyJp2RdBXmru4mdRw0Qhk+MAAA" +
                                                                "gAAAAIAAAACAAgAAgAAAAAAAAAAAAAEB" +
                                                                "R1IhAxE59NNW8xG92tOkaf6kxi8IsH/v" +
                                                                "wFz88ryqohjCCDV2IQNRv+DMQdbOojF+" +
                                                                "FaJy1ZD46yTleJGVALNYjMj7TEd3XVKu" +
                                                                "IgIDETn001bzEb3a06Rp/qTGLwiwf+/A" +
                                                                "XPzyvKqiGMIINXYcvRa+5TAAAIAAAACA" +
                                                                "AAAAgAIAAIABAAAAAgAAACICA1G/4MxB" +
                                                                "1s6iMX4VonLVkPjrJOV4kZUAs1iMyPtM" +
                                                                "R3ddHDRCGT4wAACAAAAAgAAAAIACAACA" +
                                                                "AQAAAAIAAAAAAA==";

    private static final String MULTI_PSBT_WITH_CHANGE_HEX = "020000000001015ff4463f24992fd907936" +
                                                             "3b10bc228cd4b4eda2f9a1b90bb61884edc" +
                                                             "ff5d41670000000000fdffffff02a00f000" +
                                                             "000000000220020d9201a9d5a45fcbc7a73" +
                                                             "b9a3455db12fde4fad878ed64abec8444d9" +
                                                             "ee439a016330300000000000016001480f8" +
                                                             "937e5692d723d999995e15f4eaedf530748" +
                                                             "6040047304402207c7310165106927b0779" +
                                                             "474deea40200c9abb7763b8efbd744b3e24" +
                                                             "8254e838e0220396c6c0e8095d873fcb5f5" +
                                                             "167ad3065674937f0e513ce261ada09144d" +
                                                             "0d4f96d014830450221009c28e830967083" +
                                                             "09a611610416cc6bbdb5c66e27d24f5f751" +
                                                             "c534ffc7c148102022050151b5a1edfc72b" +
                                                             "46fc59b4ad013a7f4f302eedc6c176cd932" +
                                                             "9001f3051f1fe0147522102115b29a5b4c2" +
                                                             "e648bbafc10046df00e8311710963efd15a" +
                                                             "87534c670dd0eb0fa21039313c80a984601" +
                                                             "04a4e9abc86f1f73d1fa982ecb3172269d9" +
                                                             "17415e6aeee267552ae00000000";

    private static final String CHANGE_INDEX_999999 = "cHNidP8BAH0CAAAAAUJTCRglAyBzBJKy8g6IQZOs6m" +
                                                      "W/TAcZQBAwZ1+0nIM2AAAAAAD9////AgMLAAAAAAAA" +
                                                      "IgAgCrk8USQ4V1PTbvmbC1d4XF6tE0FHxg4DYjSyZ+" +
                                                      "v36CboAwAAAAAAABYAFMQKYgtvMZZKBJaRRzu2ymKm" +
                                                      "ITLSIkwJAAABASugDwAAAAAAACIAINkgGp1aRfy8en" +
                                                      "O5o0VdsS/eT62HjtZKvshETZ7kOaAWAQVHUiEDETn0" +
                                                      "01bzEb3a06Rp/qTGLwiwf+/AXPzyvKqiGMIINXYhA1" +
                                                      "G/4MxB1s6iMX4VonLVkPjrJOV4kZUAs1iMyPtMR3dd" +
                                                      "Uq4iBgMROfTTVvMRvdrTpGn+pMYvCLB/78Bc/PK8qq" +
                                                      "IYwgg1dhy9Fr7lMAAAgAAAAIAAAACAAgAAgAEAAAAC" +
                                                      "AAAAIgYDUb/gzEHWzqIxfhWictWQ+Osk5XiRlQCzWI" +
                                                      "zI+0xHd10cNEIZPjAAAIAAAACAAAAAgAIAAIABAAAA" +
                                                      "AgAAAAABAUdSIQJVEmEwhGKa0JX96JPOEz0ksJ7/7o" +
                                                      "gUteBmZsuzy8uRRiEC1V/QblpSYPxOd6UP4ufuL2dI" +
                                                      "y7LAn3MbVmE7q5+FXj5SriICAlUSYTCEYprQlf3ok8" +
                                                      "4TPSSwnv/uiBS14GZmy7PLy5FGHDRCGT4wAACAAAAA" +
                                                      "gAAAAIACAACAAQAAAD9CDwAiAgLVX9BuWlJg/E53pQ" +
                                                      "/i5+4vZ0jLssCfcxtWYTurn4VePhy9Fr7lMAAAgAAA" +
                                                      "AIAAAACAAgAAgAEAAAA/Qg8AAAA=";

    private static final String CHANGE_INDEX_1000000 = "cHNidP8BAH0CAAAAAUJTCRglAyBzBJKy8g6IQZOs6" +
                                                       "mW/TAcZQBAwZ1+0nIM2AAAAAAD9////AugDAAAAAA" +
                                                       "AAFgAUxApiC28xlkoElpFHO7bKYqYhMtIDCwAAAAA" +
                                                       "AACIAIJdT/Bk+sg3L4UXNnCMQ+76c531xAF4pGWkh" +
                                                       "ztn4evpsIkwJAAABASugDwAAAAAAACIAINkgGp1aR" +
                                                       "fy8enO5o0VdsS/eT62HjtZKvshETZ7kOaAWAQVHUi" +
                                                       "EDETn001bzEb3a06Rp/qTGLwiwf+/AXPzyvKqiGMI" +
                                                       "INXYhA1G/4MxB1s6iMX4VonLVkPjrJOV4kZUAs1iM" +
                                                       "yPtMR3ddUq4iBgMROfTTVvMRvdrTpGn+pMYvCLB/7" +
                                                       "8Bc/PK8qqIYwgg1dhy9Fr7lMAAAgAAAAIAAAACAAg" +
                                                       "AAgAEAAAACAAAAIgYDUb/gzEHWzqIxfhWictWQ+Os" +
                                                       "k5XiRlQCzWIzI+0xHd10cNEIZPjAAAIAAAACAAAAA" +
                                                       "gAIAAIABAAAAAgAAAAAAAQFHUiEC1/v7nPnBRo1jl" +
                                                       "hIyjJPwMaBdjZhiYYVxQu52lLXNDeAhA4NzKqUnt/" +
                                                       "XjzyTC7BzuKiGV96QPVF151rJuX4ZV59vNUq4iAgL" +
                                                       "X+/uc+cFGjWOWEjKMk/AxoF2NmGJhhXFC7naUtc0N" +
                                                       "4Bw0Qhk+MAAAgAAAAIAAAACAAgAAgAEAAABAQg8AI" +
                                                       "gIDg3MqpSe39ePPJMLsHO4qIZX3pA9UXXnWsm5fhl" +
                                                       "Xn280cvRa+5TAAAIAAAACAAAAAgAIAAIABAAAAQEI" +
                                                       "PAAA=";

    private void testInvalidPsbt(String psbt) {
        assertThrows("", PsbtException.class, () -> new Psbt(psbt, Network.TESTNET));
    }

    @Test
    public void testParseTooShortPSBT() {
        testInvalidPsbt("");
    }

    @Test
    public void testInvalidCharacters() {
        testInvalidPsbt("$|-||-|-");
    }

    @Test
    public void testParseBase64() {
        Psbt psbt = new Psbt(VALID_PSBT, Network.TESTNET);
        assertEquals(VALID_PSBT, psbt.getDescription());
    }

    @Test
    public void testParseBinary() {
        byte[] data = base642Bytes(VALID_PSBT);
        Psbt psbt = new Psbt(data, Network.TESTNET);
        assertEquals(VALID_PSBT, psbt.getDescription());
        assertArrayEquals(data, psbt.getData());
    }

    @Test
    public void testInvalidPSBT() {
        testInvalidPsbt(
                "AgAAAAEmgXE3Ht/yhek3re6ks3t4AAwFZsuzrWRkFxPKQhcb9gAAAABqRzBEAiBwsiRRI+a/R01gxbUMBD1MaRpdJDXwmjSnZiqdwlF5CgIgATKcqdrPKAvfMHQOwDkEIkIsgctFg5RXrrdvwS7dlbMBIQJlfRGNM1e44PTCzUbbezn22cONmnCry5st5dyNv+TOMf7///8C09/1BQAAAAAZdqkU0MWZA8W6woaHYOkP1SGkZlqnZSCIrADh9QUAAAAAF6kUNUXm4zuDLEcFDyTT7rk8nAOUi8eHsy4TAA==");
    }

    @Test
    public void testComplete() {
        Psbt incompletePsbt = new Psbt(VALID_PSBT, Network.TESTNET);
        Psbt completePsbt = new Psbt(FINALIZED_PSBT, Network.TESTNET);
        assertFalse(incompletePsbt.isComplete());
        assertFalse(new Psbt(UNSIGNED_PSBT, Network.TESTNET).isComplete());
        assertFalse(new Psbt(SIGNED_PSBT_0_2, Network.TESTNET).isComplete());
        assertTrue(completePsbt.isComplete());
    }

    @Test
    public void testExtractTransaction() {
        Psbt incompletePsbt = new Psbt(VALID_PSBT, Network.TESTNET);
        assertNull(incompletePsbt.getTransactionFinal());

        Psbt completePsbt = new Psbt(FINALIZED_PSBT, Network.TESTNET);
        assertNotNull(completePsbt.getTransactionFinal());
        assertEquals("0200000000010258e87a21b56daf0c23be8e7070456c336f7cbaa5c8757924f545887bb2ab" +
                     "dd7500000000da00473044022074018ad4180097b873323c0015720b3684cc8123891048e7db" +
                     "cd9b55ad679c99022073d369b740e3eb53dcefa33823c8070514ca55a7dd9544f157c1679132" +
                     "61118c01483045022100f61038b308dc1da865a34852746f015772934208c6d24454393cd99b" +
                     "df2217770220056e675a675a6d0a02b85b14e5e29074d8a25a9b5760bea2816f661910a006ea" +
                     "01475221029583bf39ae0a609747ad199addd634fa6108559d6c5cd39b4c2183f1ab96e07f21" +
                     "02dab61ff49a14db6a7d02b0cd1fbb78fc4b18312b5b4e54dae4dba2fbfef536d752aeffffff" +
                     "ff838d0427d0ec650a68aa46bb0b098aea4422c071b2ca78352a077959d07cea1d0100000023" +
                     "2200208c2353173743b595dfb4a07b72ba8e42e3797da74e87fe7d9d7497e3b2028903ffffff" +
                     "ff0270aaf00800000000160014d85c2b71d0060b09c9886aeb815e50991dda124d00e1f50500" +
                     "00000016001400aea9a2e5f0f876a588df5546e8742d1d87008f000400473044022062eb7a55" +
                     "6107a7c73f45ac4ab5a1dddf6f7075fb1275969a7f383efff784bcb202200c05dbb7470dbf2f" +
                     "08557dd356c7325c1ed30913e996cd3840945db12228da5f01473044022065f45ba5998b59a2" +
                     "7ffe1a7bed016af1f1f90d54b3aa8f7450aa5f56a25103bd02207f724703ad1edb96680b284b" +
                     "56d4ffcb88f7fb759eabbe08aa30f29b851383d20147522103089dc10c7ac6db54f91329af61" +
                     "7333db388cead0c231f723379d1b99030b02dc21023add904f3d6dcf59ddb906b0dee23529b7" +
                     "ffb9ed50e5e86151926860221f0e7352ae00000000",
                     completePsbt.getTransactionFinal().getDescription());
    }

    @Test
    public void testSignWithKey() {
        Key privKey0 = new Key(WIF_0, Network.TESTNET);
        Key privKey1 = new Key(WIF_1, Network.TESTNET);
        Key privKey2 = new Key(WIF_2, Network.TESTNET);
        Key privKey3 = new Key(WIF_3, Network.TESTNET);

        Psbt psbt1 = new Psbt(UNSIGNED_PSBT, Network.TESTNET);
        Psbt psbt2 = new Psbt(UNSIGNED_PSBT, Network.TESTNET);

        Psbt expectedPsbt02 = new Psbt(SIGNED_PSBT_0_2, Network.TESTNET);
        Psbt expectedPsbt13 = new Psbt(SIGNED_PSBT_1_3, Network.TESTNET);

        Psbt p102 = psbt1.signed(privKey0).signed(privKey2);
        assertEquals(expectedPsbt02.getDescription(), p102.getDescription());

        Psbt p213 = psbt2.signed(privKey1).signed(privKey3);
        assertEquals(expectedPsbt13.getDescription(), p213.getDescription());
    }

    @Test
    public void testInputs() {
        Psbt psbt = new Psbt(UNSIGNED_PSBT, Network.TESTNET);
        assertEquals(2, psbt.getInputs().length);
    }

    @Test
    public void testOutput() {
        Psbt psbt = new Psbt(UNSIGNED_PSBT, Network.TESTNET);
        assertEquals(2, psbt.getOutputs().length);
    }

    @Test
    public void testKeyPaths() {
        KeyOrigin expectedOrigin0 = new KeyOrigin(FINGERPRINT, PATH_0);
        KeyOrigin expectedOrigin1 = new KeyOrigin(FINGERPRINT, PATH_1);
        KeyOrigin expectedOrigin2 = new KeyOrigin(FINGERPRINT, PATH_2);
        KeyOrigin expectedOrigin3 = new KeyOrigin(FINGERPRINT, PATH_3);
        KeyOrigin expectedOrigin4 = new KeyOrigin(FINGERPRINT, PATH_4);
        KeyOrigin expectedOrigin5 = new KeyOrigin(FINGERPRINT, PATH_5);
        Psbt psbt = new Psbt(UNSIGNED_PSBT, Network.TESTNET);

        assertEquals(2, psbt.getInputs().length);
        Map<PubKey, KeyOrigin> inOrigin0 = psbt.getInputs()[0].getOriginMap();
        assertEquals(2, inOrigin0.size());
        assertEquals(expectedOrigin0, inOrigin0.get(PUB_KEY_0));
        assertEquals(expectedOrigin1, inOrigin0.get(PUB_KEY_1));
        Map<PubKey, KeyOrigin> inOrigin1 = psbt.getInputs()[1].getOriginMap();
        assertEquals(2, inOrigin1.size());
        assertEquals(expectedOrigin3, inOrigin1.get(PUB_KEY_3));
        assertEquals(expectedOrigin2, inOrigin1.get(PUB_KEY_2));

        assertEquals(2, psbt.getOutputs().length);
        Map<PubKey, KeyOrigin> outOrigin0 = psbt.getOutputs()[0].getOriginMap();
        assertEquals(1, outOrigin0.size());
        assertEquals(expectedOrigin4, outOrigin0.get(PUB_KEY_4));
        Map<PubKey, KeyOrigin> outOrigin1 = psbt.getOutputs()[1].getOriginMap();
        assertEquals(1, outOrigin1.size());
        assertEquals(expectedOrigin5, outOrigin1.get(PUB_KEY_5));
    }

    @Test
    public void testCanSign() {
        HDKey hdKey = new HDKey(MASTER_KEY_XPRIV);
        Psbt psbt = new Psbt(UNSIGNED_PSBT, Network.TESTNET);
        for (PsbtInput input : psbt.getInputs()) {
            assertTrue(input.canSign(hdKey));
        }
    }

    @Test
    public void testFinalize() {
        Psbt psbt = new Psbt(SIGNED_PSBT, Network.TESTNET);
        Psbt expected = new Psbt(FINALIZED_PSBT, Network.TESTNET);
        Psbt finalized = psbt.finalized();
        assertEquals(expected, finalized);
    }

    @Test
    public void testSignWithHDKey() {
        Psbt psbt = new Psbt(UNSIGNED_PSBT, Network.TESTNET);
        HDKey masterKey = new HDKey(MASTER_KEY_XPRIV);
        Psbt signed = psbt.signed(masterKey);
        Psbt finalized = signed.finalized();
        assertTrue(finalized.isComplete());
    }

    @Test
    public void testCanSignNeutered() {
        HDKey us = new HDKey(
                "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzv" +
                "LQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi",
                hex2Bytes("3442193e"));
        Psbt psbt = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);
        for (PsbtInput input : psbt.getInputs()) {
            assertTrue(input.canSign(us));
        }
    }

    @Test
    public void testSignRealMultisigWithHDKey() {
        HDKey keySigner1 = new HDKey(MASTER_1);
        HDKey keySigner2 = new HDKey(MASTER_2);
        Psbt psbtWithoutChange = new Psbt(MULTI_UNSIGNED_PSBT_WITHOUT_CHANGE,
                                                  Network.MAINNET);
        Psbt psbtWithChange = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);

        Psbt psbtWithoutChangeSigned = psbtWithoutChange.signed(keySigner1).signed(keySigner2);
        Psbt psbtWithoutChangeFinalized = psbtWithoutChangeSigned.finalized();
        assertTrue(psbtWithoutChangeFinalized.isComplete());
        assertEquals(MULTI_PSBT_WITHOUT_CHANGE_HEX,
                     psbtWithoutChangeFinalized.getTransactionFinal().getDescription());

        Psbt psbtWithChangeSigned = psbtWithChange.signed(keySigner1).signed(keySigner2);
        Psbt psbtWithChangeFinalized = psbtWithChangeSigned.finalized();
        assertTrue(psbtWithChangeFinalized.isComplete());
        assertEquals(MULTI_PSBT_WITH_CHANGE_HEX,
                     psbtWithChangeFinalized.getTransactionFinal().getDescription());

        assertEquals(4000, psbtWithChangeFinalized.getOutputs()[0].getTxOutput().getAmount());
        assertEquals("bc1qmysp4826gh7tc7nnhx352hd39l0yltv83mty40kgg3xeaepe5qtq4c50qe",
                     psbtWithChangeFinalized.getOutputs()[0].getTxOutput().getAddress());

        assertEquals(819, psbtWithChangeFinalized.getOutputs()[1].getTxOutput().getAmount());
        assertEquals("bc1qsrufxljkjttj8kven90pta82ah6nqayxfr8p9h",
                     psbtWithChangeFinalized.getOutputs()[1].getTxOutput().getAddress());
    }

    @Test
    public void testIsChange() {
        HDKey us = new HDKey(MASTER_1);
        HDKey cosigner = new HDKey(MASTER_2);
        Psbt psbt = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);
        assertTrue(psbt.getOutputs()[0].isChange(us, psbt.getInputs(), cosigner, 2));
        assertFalse(psbt.getOutputs()[1].isChange(us, psbt.getInputs(), cosigner, 2));

        // Test maximum permitted change index
        psbt = new Psbt(CHANGE_INDEX_999999, Network.MAINNET);
        assertTrue(psbt.getOutputs()[0].isChange(us, psbt.getInputs(), cosigner, 2));
        assertFalse(psbt.getOutputs()[1].isChange(us, psbt.getInputs(), cosigner, 2));

        // Test out of bounds change index
        psbt = new Psbt(CHANGE_INDEX_1000000, Network.MAINNET);
        assertFalse(psbt.getOutputs()[0].isChange(us, psbt.getInputs(), cosigner, 2));
        assertFalse(psbt.getOutputs()[1].isChange(us, psbt.getInputs(), cosigner, 2));
    }

    @Test
    public void testIsChangeWithNeuteredCosignerKey() {
        HDKey us = new HDKey(MASTER_1);
        HDKey cosigner = new HDKey(
                "xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGK" +
                "G1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a",
                hex2Bytes("bd16bee5"));

        Psbt psbt = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);
        assertTrue(psbt.getOutputs()[0].isChange(us, psbt.getInputs(), cosigner, 2));
        assertFalse(psbt.getOutputs()[1].isChange(us, psbt.getInputs(), cosigner, 2));
    }

    @Test
    public void testIsChangeWithNeuteredAllKeys() {
        HDKey us = new HDKey(
                "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzv" +
                "LQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi",
                hex2Bytes("3442193e"));
        HDKey cosigner = new HDKey(
                "xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGK" +
                "G1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a",
                hex2Bytes("bd16bee5"));
        Psbt psbt = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);
        assertTrue(psbt.getOutputs()[0].isChange(us, psbt.getInputs(), cosigner, 2));
        assertFalse(psbt.getOutputs()[1].isChange(us, psbt.getInputs(), cosigner, 2));
    }

    @Test
    public void testGetTransactionFee() {
        Psbt psbt = new Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET);
        assertEquals(181L, (long) psbt.getFee());
    }


}
