package com.bc.libwally

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.libwally.address.Key
import com.bc.libwally.address.PubKey
import com.bc.libwally.bip32.Bip32Path
import com.bc.libwally.bip32.HDKey
import com.bc.libwally.bip32.Network
import com.bc.libwally.core.Core
import com.bc.libwally.core.Core.base642Bytes
import com.bc.libwally.core.Core.hex2Bytes
import com.bc.libwally.psbt.KeyOrigin
import com.bc.libwally.psbt.Psbt
import com.bc.libwally.psbt.PsbtException
import com.bc.libwally.util.assertThrows
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PsbtTest {

    companion object {

        private val FINGERPRINT = Core.hex2Bytes("d90c6a4f")

        private const val VALID_PSBT =
            "cHNidP8BAHUCAAAAASaBcTce3/KF6Tet7qSze3gADAVmy7OtZGQXE8pCFxv2AAAAAAD+////AtPf9QUAAAAA" +
                    "GXapFNDFmQPFusKGh2DpD9UhpGZap2UgiKwA4fUFAAAAABepFDVF5uM7gyxHBQ8k0+65PJwDlIvH" +
                    "h7MuEwAAAQD9pQEBAAAAAAECiaPHHqtNIOA3G7ukzGmPopXJRjr6Ljl/hTPMti+VZ+UBAAAAFxYA" +
                    "FL4Y0VKpsBIDna89p95PUzSe7LmF/////4b4qkOnHf8USIk6UwpyN+9rRgi7st0tAXHmOuxqSJC0" +
                    "AQAAABcWABT+Pp7xp0XpdNkCxDVZQ6vLNL1TU/////8CAMLrCwAAAAAZdqkUhc/xCX/Z4Ai7NK9w" +
                    "nGIZeziXikiIrHL++E4sAAAAF6kUM5cluiHv1irHU6m80GfWx6ajnQWHAkcwRAIgJxK+IuAnDzlP" +
                    "VoMR3HyppolwuAJf3TskAinwf4pfOiQCIAGLONfc0xTnNMkna9b7QPZzMlvEuqFEyADS8vAtsnZc" +
                    "ASED0uFWdJQbrUqZY3LLh+GFbTZSYG2YVi/jnF6efkE/IQUCSDBFAiEA0SuFLYXc2WHS9fSrZgZU" +
                    "327tzHlMDDPOXMMJ/7X85Y0CIGczio4OFyXBl/saiK9Z9R5E5CVbIBZ8hoQDHAXR8lkqASECI7cr" +
                    "7vCWXRC+B3jv7NYfysb3mk6haTkzgHNEZPhPKrMAAAAAAAAA"

        private const val FINALIZED_PSBT =
            "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpo" +
                    "qka7CwmK6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0AYLCcmIauuB" +
                    "XlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL" +
                    "0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qt" +
                    "Qs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAA" +
                    "AAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9h" +
                    "h2UAAAABB9oARzBEAiB0AYrUGACXuHMyPAAVcgs2hMyBI4kQSOfbzZtVrWecmQIgc9Npt0Dj61Pc" +
                    "76M4I8gHBRTKVafdlUTxV8FnkTJhEYwBSDBFAiEA9hA4swjcHahlo0hSdG8BV3KTQgjG0kRUOTzZ" +
                    "m98iF3cCIAVuZ1pnWm0KArhbFOXikHTYolqbV2C+ooFvZhkQoAbqAUdSIQKVg785rgpgl0etGZrd" +
                    "1jT6YQhVnWxc05tMIYPxq5bgfyEC2rYf9JoU22p9ArDNH7t4/EsYMStbTlTa5Nui+/71NtdSrgAB" +
                    "ASAAwusLAAAAABepFLf1+vQOPUClpFmx2zU18rcvqSHohwEHIyIAIIwjUxc3Q7WV37Sge3K6jkLj" +
                    "eX2nTof+fZ10l+OyAokDAQjaBABHMEQCIGLrelVhB6fHP0WsSrWh3d9vcHX7EnWWmn84Pv/3hLyy" +
                    "AiAMBdu3Rw2/LwhVfdNWxzJcHtMJE+mWzThAlF2xIijaXwFHMEQCIGX0W6WZi1mif/4ae+0BavHx" +
                    "+Q1Us6qPdFCqX1aiUQO9AiB/ckcDrR7blmgLKEtW1P/LiPf7dZ6rvgiqMPKbhROD0gFHUiEDCJ3B" +
                    "DHrG21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtwhAjrdkE89bc9Z3bkGsN7iNSm3/7ntUOXoYVGS" +
                    "aGAiHw5zUq4AIgIDqaTDf1mW06ol26xrVwrwZQOUSSlCRgs1R1Ptnuylh3EQ2QxqTwAAAIAAAACA" +
                    "BAAAgAAiAgJ/Y5l1fS7/VaE2rQLGhLGDi2VW5fG2s0KCqUtrUAUQlhDZDGpPAAAAgAAAAIAFAACA" +
                    "AA=="

        private const val UNSIGNED_PSBT =
            "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpo" +
                    "qka7CwmK6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0AYLCcmIauuB" +
                    "XlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL" +
                    "0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qt" +
                    "Qs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAA" +
                    "AAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9h" +
                    "h2UAAAABAwQBAAAAAQRHUiEClYO/Oa4KYJdHrRma3dY0+mEIVZ1sXNObTCGD8auW4H8hAtq2H/Sa" +
                    "FNtqfQKwzR+7ePxLGDErW05U2uTbovv+9TbXUq4iBgKVg785rgpgl0etGZrd1jT6YQhVnWxc05tM" +
                    "IYPxq5bgfxDZDGpPAAAAgAAAAIAAAACAIgYC2rYf9JoU22p9ArDNH7t4/EsYMStbTlTa5Nui+/71" +
                    "NtcQ2QxqTwAAAIAAAACAAQAAgAABASAAwusLAAAAABepFLf1+vQOPUClpFmx2zU18rcvqSHohwED" +
                    "BAEAAAABBCIAIIwjUxc3Q7WV37Sge3K6jkLjeX2nTof+fZ10l+OyAokDAQVHUiEDCJ3BDHrG21T5" +
                    "EymvYXMz2ziM6tDCMfcjN50bmQMLAtwhAjrdkE89bc9Z3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5z" +
                    "Uq4iBgI63ZBPPW3PWd25BrDe4jUpt/+57VDl6GFRkmhgIh8OcxDZDGpPAAAAgAAAAIADAACAIgYD" +
                    "CJ3BDHrG21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtwQ2QxqTwAAAIAAAACAAgAAgAAiAgOppMN/" +
                    "WZbTqiXbrGtXCvBlA5RJKUJGCzVHU+2e7KWHcRDZDGpPAAAAgAAAAIAEAACAACICAn9jmXV9Lv9V" +
                    "oTatAsaEsYOLZVbl8bazQoKpS2tQBRCWENkMak8AAACAAAAAgAUAAIAA"

        private const val SIGNED_PSBT =
            "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpo" +
                    "qka7CwmK6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0AYLCcmIauuB" +
                    "XlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL" +
                    "0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qt" +
                    "Qs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAA" +
                    "AAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9h" +
                    "h2UAAAAiAgKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bgf0cwRAIgdAGK1BgAl7hzMjwA" +
                    "FXILNoTMgSOJEEjn282bVa1nnJkCIHPTabdA4+tT3O+jOCPIBwUUylWn3ZVE8VfBZ5EyYRGMASIC" +
                    "Atq2H/SaFNtqfQKwzR+7ePxLGDErW05U2uTbovv+9TbXSDBFAiEA9hA4swjcHahlo0hSdG8BV3KT" +
                    "QgjG0kRUOTzZm98iF3cCIAVuZ1pnWm0KArhbFOXikHTYolqbV2C+ooFvZhkQoAbqAQEDBAEAAAAB" +
                    "BEdSIQKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bgfyEC2rYf9JoU22p9ArDNH7t4/EsY" +
                    "MStbTlTa5Nui+/71NtdSriIGApWDvzmuCmCXR60Zmt3WNPphCFWdbFzTm0whg/GrluB/ENkMak8A" +
                    "AACAAAAAgAAAAIAiBgLath/0mhTban0CsM0fu3j8SxgxK1tOVNrk26L7/vU21xDZDGpPAAAAgAAA" +
                    "AIABAACAAAEBIADC6wsAAAAAF6kUt/X69A49QKWkWbHbNTXyty+pIeiHIgIDCJ3BDHrG21T5Eymv" +
                    "YXMz2ziM6tDCMfcjN50bmQMLAtxHMEQCIGLrelVhB6fHP0WsSrWh3d9vcHX7EnWWmn84Pv/3hLyy" +
                    "AiAMBdu3Rw2/LwhVfdNWxzJcHtMJE+mWzThAlF2xIijaXwEiAgI63ZBPPW3PWd25BrDe4jUpt/+5" +
                    "7VDl6GFRkmhgIh8Oc0cwRAIgZfRbpZmLWaJ//hp77QFq8fH5DVSzqo90UKpfVqJRA70CIH9yRwOt" +
                    "HtuWaAsoS1bU/8uI9/t1nqu+CKow8puFE4PSAQEDBAEAAAABBCIAIIwjUxc3Q7WV37Sge3K6jkLj" +
                    "eX2nTof+fZ10l+OyAokDAQVHUiEDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtwhAjrd" +
                    "kE89bc9Z3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5zUq4iBgI63ZBPPW3PWd25BrDe4jUpt/+57VDl" +
                    "6GFRkmhgIh8OcxDZDGpPAAAAgAAAAIADAACAIgYDCJ3BDHrG21T5EymvYXMz2ziM6tDCMfcjN50b" +
                    "mQMLAtwQ2QxqTwAAAIAAAACAAgAAgAAiAgOppMN/WZbTqiXbrGtXCvBlA5RJKUJGCzVHU+2e7KWH" +
                    "cRDZDGpPAAAAgAAAAIAEAACAACICAn9jmXV9Lv9VoTatAsaEsYOLZVbl8bazQoKpS2tQBRCWENkM" +
                    "ak8AAACAAAAAgAUAAIAA"

        private const val MASTER_KEY_XPRIV = "tprv8ZgxMBicQKsPd9TeAdPADNnSyH9SSUUbTVe" +
                "FszDE23Ki6TBB5nCefAdHkK8Fm3qMQR6sHwA56z" +
                "qRmKmxnHk37JkiFzvncDqoKmPWubu7hDF"

        // Paths
        private val PATH_0 = Bip32Path("m/0'/0'/0'")
        private val PATH_1 = Bip32Path("m/0'/0'/1'")
        private val PATH_2 = Bip32Path("m/0'/0'/2'")
        private val PATH_3 = Bip32Path("m/0'/0'/3'")
        private val PATH_4 = Bip32Path("m/0'/0'/4'")
        private val PATH_5 = Bip32Path("m/0'/0'/5'")

        // Private keys (testnet)
        private const val WIF_0 =
            "cP53pDbR5WtAD8dYAW9hhTjuvvTVaEiQBdrz9XPrgLBeRFiyCbQr" // m/0'/0'/0'

        private const val WIF_1 =
            "cT7J9YpCwY3AVRFSjN6ukeEeWY6mhpbJPxRaDaP5QTdygQRxP9Au" // m/0'/0'/1'

        private const val WIF_2 =
            "cR6SXDoyfQrcp4piaiHE97Rsgta9mNhGTen9XeonVgwsh4iSgw6d" // m/0'/0'/2'

        private const val WIF_3 =
            "cNBc3SWUip9PPm1GjRoLEJT6T41iNzCYtD7qro84FMnM5zEqeJsE" // m/0'/0'/3'


        // Public keys
        private val PUB_KEY_0 = PubKey(
            "029583bf39ae0a609747ad199addd634fa6108559d6c5cd39b4c2183f1ab96e07f",
            Network.TESTNET
        )
        private val PUB_KEY_1 = PubKey(
            "02dab61ff49a14db6a7d02b0cd1fbb78fc4b18312b5b4e54dae4dba2fbfef536d7",
            Network.TESTNET
        )
        private val PUB_KEY_2 = PubKey(
            "03089dc10c7ac6db54f91329af617333db388cead0c231f723379d1b99030b02dc",
            Network.TESTNET
        )
        private val PUB_KEY_3 = PubKey(
            "023add904f3d6dcf59ddb906b0dee23529b7ffb9ed50e5e86151926860221f0e73",
            Network.TESTNET
        )
        private val PUB_KEY_4 = PubKey(
            "03a9a4c37f5996d3aa25dbac6b570af0650394492942460b354753ed9eeca58771",
            Network.TESTNET
        )
        private val PUB_KEY_5 = PubKey(
            "027f6399757d2eff55a136ad02c684b1838b6556e5f1b6b34282a94b6b50051096",
            Network.TESTNET
        )

        // Singed with keys m/0'/0'/0' and m/0'/0'/2'
        private const val SIGNED_PSBT_0_2 =
            "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpo" +
                    "qka7CwmK6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0AYLCcmIauuB" +
                    "XlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL" +
                    "0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qt" +
                    "Qs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAA" +
                    "AAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9h" +
                    "h2UAAAAiAgKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bgf0cwRAIgdAGK1BgAl7hzMjwA" +
                    "FXILNoTMgSOJEEjn282bVa1nnJkCIHPTabdA4+tT3O+jOCPIBwUUylWn3ZVE8VfBZ5EyYRGMAQED" +
                    "BAEAAAABBEdSIQKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bgfyEC2rYf9JoU22p9ArDN" +
                    "H7t4/EsYMStbTlTa5Nui+/71NtdSriIGApWDvzmuCmCXR60Zmt3WNPphCFWdbFzTm0whg/GrluB/" +
                    "ENkMak8AAACAAAAAgAAAAIAiBgLath/0mhTban0CsM0fu3j8SxgxK1tOVNrk26L7/vU21xDZDGpP" +
                    "AAAAgAAAAIABAACAAAEBIADC6wsAAAAAF6kUt/X69A49QKWkWbHbNTXyty+pIeiHIgIDCJ3BDHrG" +
                    "21T5EymvYXMz2ziM6tDCMfcjN50bmQMLAtxHMEQCIGLrelVhB6fHP0WsSrWh3d9vcHX7EnWWmn84" +
                    "Pv/3hLyyAiAMBdu3Rw2/LwhVfdNWxzJcHtMJE+mWzThAlF2xIijaXwEBAwQBAAAAAQQiACCMI1MX" +
                    "N0O1ld+0oHtyuo5C43l9p06H/n2ddJfjsgKJAwEFR1IhAwidwQx6xttU+RMpr2FzM9s4jOrQwjH3" +
                    "IzedG5kDCwLcIQI63ZBPPW3PWd25BrDe4jUpt/+57VDl6GFRkmhgIh8Oc1KuIgYCOt2QTz1tz1nd" +
                    "uQaw3uI1Kbf/ue1Q5ehhUZJoYCIfDnMQ2QxqTwAAAIAAAACAAwAAgCIGAwidwQx6xttU+RMpr2Fz" +
                    "M9s4jOrQwjH3IzedG5kDCwLcENkMak8AAACAAAAAgAIAAIAAIgIDqaTDf1mW06ol26xrVwrwZQOU" +
                    "SSlCRgs1R1Ptnuylh3EQ2QxqTwAAAIAAAACABAAAgAAiAgJ/Y5l1fS7/VaE2rQLGhLGDi2VW5fG2" +
                    "s0KCqUtrUAUQlhDZDGpPAAAAgAAAAIAFAACAAA=="

        // Singed with keys m/0'/0'/1' (test vector modified for EC_FLAG_GRIND_R) and m/0'/0'/3'
        private const val SIGNED_PSBT_1_3 =
            "cHNidP8BAJoCAAAAAljoeiG1ba8MI76OcHBFbDNvfLqlyHV5JPVFiHuyq911AAAAAAD/////g40EJ9DsZQpo" +
                    "qka7CwmK6kQiwHGyyng1Kgd5WdB86h0BAAAAAP////8CcKrwCAAAAAAWABTYXCtx0AYLCcmIauuB" +
                    "XlCZHdoSTQDh9QUAAAAAFgAUAK6pouXw+HaliN9VRuh0LR2HAI8AAAAAAAEAuwIAAAABqtc5MQGL" +
                    "0l+ErkALaISL4J23BurCrBgpi6vucatlb4sAAAAASEcwRAIgWPb8fGoz4bMVSNSByCbAFb0wE1qt" +
                    "Qs1neQ2rZtKtJDsCIEoc7SYExnNbY5PltBaR3XiwDwxZQvufdRhW+qk4FX26Af7///8CgPD6AgAA" +
                    "AAAXqRQPuUY0IWlrgsgzryQceMF9295JNIfQ8gonAQAAABepFCnKdPigj4GZlCgYXJe12FLkBj9h" +
                    "h2UAAAAiAgLath/0mhTban0CsM0fu3j8SxgxK1tOVNrk26L7/vU210gwRQIhAPYQOLMI3B2oZaNI" +
                    "UnRvAVdyk0IIxtJEVDk82ZvfIhd3AiAFbmdaZ1ptCgK4WxTl4pB02KJam1dgvqKBb2YZEKAG6gEB" +
                    "AwQBAAAAAQRHUiEClYO/Oa4KYJdHrRma3dY0+mEIVZ1sXNObTCGD8auW4H8hAtq2H/SaFNtqfQKw" +
                    "zR+7ePxLGDErW05U2uTbovv+9TbXUq4iBgKVg785rgpgl0etGZrd1jT6YQhVnWxc05tMIYPxq5bg" +
                    "fxDZDGpPAAAAgAAAAIAAAACAIgYC2rYf9JoU22p9ArDNH7t4/EsYMStbTlTa5Nui+/71NtcQ2Qxq" +
                    "TwAAAIAAAACAAQAAgAABASAAwusLAAAAABepFLf1+vQOPUClpFmx2zU18rcvqSHohyICAjrdkE89" +
                    "bc9Z3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5zRzBEAiBl9FulmYtZon/+GnvtAWrx8fkNVLOqj3RQ" +
                    "ql9WolEDvQIgf3JHA60e25ZoCyhLVtT/y4j3+3Weq74IqjDym4UTg9IBAQMEAQAAAAEEIgAgjCNT" +
                    "FzdDtZXftKB7crqOQuN5fadOh/59nXSX47ICiQMBBUdSIQMIncEMesbbVPkTKa9hczPbOIzq0MIx" +
                    "9yM3nRuZAwsC3CECOt2QTz1tz1nduQaw3uI1Kbf/ue1Q5ehhUZJoYCIfDnNSriIGAjrdkE89bc9Z" +
                    "3bkGsN7iNSm3/7ntUOXoYVGSaGAiHw5zENkMak8AAACAAAAAgAMAAIAiBgMIncEMesbbVPkTKa9h" +
                    "czPbOIzq0MIx9yM3nRuZAwsC3BDZDGpPAAAAgAAAAIACAACAACICA6mkw39ZltOqJdusa1cK8GUD" +
                    "lEkpQkYLNUdT7Z7spYdxENkMak8AAACAAAAAgAQAAIAAIgICf2OZdX0u/1WhNq0CxoSxg4tlVuXx" +
                    "trNCgqlLa1AFEJYQ2QxqTwAAAIAAAACABQAAgAA="

        // Mainnet multisig wallet based on BIP32 test vectors.
        // To import into Bitcoin Core (experimental descriptor wallet branch) use:
        // importdescriptors '[{"range":1000,"timestamp":"now","watchonly":true,"internal":false,
        // "desc":"wsh(sortedmulti(2,[3442193e\/48h\/0h\/0h\/2h]xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi\/0\/*,
        // [bd16bee5\/48h\/0h\/0h\/2h]xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a\/0\/*))#75z63vc9","active":true},
        // {"range":1000,"timestamp":"now","watchonly":true,"internal":true,"desc":"wsh(sortedmulti(2,[3442193e\/48h\/0h\/0h\/2h]xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi\/1\/*,
        // [bd16bee5\/48h\/0h\/0h\/2h]xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a\/1\/*))#8837llds","active":true}]'
        private val FINGERPRINT_1 = Core.hex2Bytes("3442193e")
        private val FINGERPRINT_2 = Core.hex2Bytes("bd16bee5")
        private const val MASTER_1 = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqji" +
                "ChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi"
        private const val MASTER_2 = "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGds" +
                "o3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U"
        private const val MULTI_UNSIGNED_PSBT_WITHOUT_CHANGE =
            "cHNidP8BAFICAAAAAV/0Rj8kmS/ZB5NjsQvCKM1LTtovmhuQu2GITtz/XUFnAAAAAAD9////Af4SAAAAAAAA" +
                    "FgAUgPiTflaS1yPZmZleFfTq7fUwdIYAAAAAAAEBK4gTAAAAAAAAIgAg+GCObltTf4/IGC6xE89A" +
                    "9WS5nPmdhxcMTxrCWQdO6P0BBUdSIQIRWymltMLmSLuvwQBG3wDoMRcQlj79Fah1NMZw3Q6w+iED" +
                    "kxPICphGAQSk6avIbx9z0fqYLssxciadkXQV5q7uJnVSriIGAhFbKaW0wuZIu6/BAEbfAOgxFxCW" +
                    "Pv0VqHU0xnDdDrD6HL0WvuUwAACAAAAAgAAAAIACAACAAAAAAAAAAAAiBgOTE8gKmEYBBKTpq8hv" +
                    "H3PR+pguyzFyJp2RdBXmru4mdRw0Qhk+MAAAgAAAAIAAAACAAgAAgAAAAAAAAAAAAAA="

        private const val MULTI_PSBT_WITHOUT_CHANGE_HEX =
            "020000000001015ff4463f24992fd9079363b10bc228cd4b4eda2f9a1b90bb61884edcff5d4167000000" +
                    "0000fdffffff01fe1200000000000016001480f8937e5692d723d999995e15f4eaedf5307486" +
                    "04004830450221009222d670173b1231512e96056597ab3a509e7d0919581a7e95aa7b272b69" +
                    "b6de022062a6b500367b0e0bd39557f5fa7e4539dc65c1c0fb4457559aea9d7efb1fba870148" +
                    "3045022100e02212a6eb7c6b3feb411aec6a0a8b4bce6bdca8379e03b9c5d8a8090278915902" +
                    "2041eec69689e7eae62f5c120edfa77fe5d3a4a631f2a2e7b763603e1bb42a72560147522102" +
                    "115b29a5b4c2e648bbafc10046df00e8311710963efd15a87534c670dd0eb0fa21039313c80a" +
                    "98460104a4e9abc86f1f73d1fa982ecb3172269d917415e6aeee267552ae00000000"

        private const val MULTI_UNSIGNED_PSBT_WITH_CHANGE =
            "cHNidP8BAH0CAAAAAV/0Rj8kmS/ZB5NjsQvCKM1LTtovmhuQu2GITtz/XUFnAAAAAAD9////AqAPAAAAAAAA" +
                    "IgAg2SAanVpF/Lx6c7mjRV2xL95PrYeO1kq+yERNnuQ5oBYzAwAAAAAAABYAFID4k35Wktcj2ZmZ" +
                    "XhX06u31MHSGAAAAAAABASuIEwAAAAAAACIAIPhgjm5bU3+PyBgusRPPQPVkuZz5nYcXDE8awlkH" +
                    "Tuj9AQVHUiECEVsppbTC5ki7r8EARt8A6DEXEJY+/RWodTTGcN0OsPohA5MTyAqYRgEEpOmryG8f" +
                    "c9H6mC7LMXImnZF0Feau7iZ1Uq4iBgIRWymltMLmSLuvwQBG3wDoMRcQlj79Fah1NMZw3Q6w+hy9" +
                    "Fr7lMAAAgAAAAIAAAACAAgAAgAAAAAAAAAAAIgYDkxPICphGAQSk6avIbx9z0fqYLssxciadkXQV" +
                    "5q7uJnUcNEIZPjAAAIAAAACAAAAAgAIAAIAAAAAAAAAAAAABAUdSIQMROfTTVvMRvdrTpGn+pMYv" +
                    "CLB/78Bc/PK8qqIYwgg1diEDUb/gzEHWzqIxfhWictWQ+Osk5XiRlQCzWIzI+0xHd11SriICAxE5" +
                    "9NNW8xG92tOkaf6kxi8IsH/vwFz88ryqohjCCDV2HL0WvuUwAACAAAAAgAAAAIACAACAAQAAAAIA" +
                    "AAAiAgNRv+DMQdbOojF+FaJy1ZD46yTleJGVALNYjMj7TEd3XRw0Qhk+MAAAgAAAAIAAAACAAgAA" +
                    "gAEAAAACAAAAAAA="

        private const val MULTI_SIGNED_PSBT_WITH_CHANGE =
            "cHNidP8BAH0CAAAAAV/0Rj8kmS/ZB5NjsQvCKM1LTtovmhuQu2GITtz/XUFnAAAAAAD9////AqAPAAAAAAAA" +
                    "IgAg2SAanVpF/Lx6c7mjRV2xL95PrYeO1kq+yERNnuQ5oBYzAwAAAAAAABYAFID4k35Wktcj2ZmZ" +
                    "XhX06u31MHSGAAAAAAABASuIEwAAAAAAACIAIPhgjm5bU3+PyBgusRPPQPVkuZz5nYcXDE8awlkH" +
                    "Tuj9IgIDkxPICphGAQSk6avIbx9z0fqYLssxciadkXQV5q7uJnVIMEUCIQCcKOgwlnCDCaYRYQQW" +
                    "zGu9tcZuJ9JPX3UcU0/8fBSBAgIgUBUbWh7fxytG/Fm0rQE6f08wLu3GwXbNkykAHzBR8f4BIgIC" +
                    "EVsppbTC5ki7r8EARt8A6DEXEJY+/RWodTTGcN0OsPpHMEQCIHxzEBZRBpJ7B3lHTe6kAgDJq7d2" +
                    "O47710Sz4kglToOOAiA5bGwOgJXYc/y19RZ60wZWdJN/DlE84mGtoJFE0NT5bQEBBUdSIQIRWyml" +
                    "tMLmSLuvwQBG3wDoMRcQlj79Fah1NMZw3Q6w+iEDkxPICphGAQSk6avIbx9z0fqYLssxciadkXQV" +
                    "5q7uJnVSriIGAhFbKaW0wuZIu6/BAEbfAOgxFxCWPv0VqHU0xnDdDrD6HL0WvuUwAACAAAAAgAAA" +
                    "AIACAACAAAAAAAAAAAAiBgOTE8gKmEYBBKTpq8hvH3PR+pguyzFyJp2RdBXmru4mdRw0Qhk+MAAA" +
                    "gAAAAIAAAACAAgAAgAAAAAAAAAAAAAEBR1IhAxE59NNW8xG92tOkaf6kxi8IsH/vwFz88ryqohjC" +
                    "CDV2IQNRv+DMQdbOojF+FaJy1ZD46yTleJGVALNYjMj7TEd3XVKuIgIDETn001bzEb3a06Rp/qTG" +
                    "Lwiwf+/AXPzyvKqiGMIINXYcvRa+5TAAAIAAAACAAAAAgAIAAIABAAAAAgAAACICA1G/4MxB1s6i" +
                    "MX4VonLVkPjrJOV4kZUAs1iMyPtMR3ddHDRCGT4wAACAAAAAgAAAAIACAACAAQAAAAIAAAAAAA=="

        private const val MULTI_PSBT_WITH_CHANGE_HEX =
            "020000000001015ff4463f24992fd9079363b10bc228cd4b4eda2f9a1b90bb61884edcff5d4167000000" +
                    "0000fdffffff02a00f000000000000220020d9201a9d5a45fcbc7a73b9a3455db12fde4fad87" +
                    "8ed64abec8444d9ee439a016330300000000000016001480f8937e5692d723d999995e15f4ea" +
                    "edf5307486040047304402207c7310165106927b0779474deea40200c9abb7763b8efbd744b3" +
                    "e248254e838e0220396c6c0e8095d873fcb5f5167ad3065674937f0e513ce261ada09144d0d4" +
                    "f96d014830450221009c28e83096708309a611610416cc6bbdb5c66e27d24f5f751c534ffc7c" +
                    "148102022050151b5a1edfc72b46fc59b4ad013a7f4f302eedc6c176cd9329001f3051f1fe01" +
                    "47522102115b29a5b4c2e648bbafc10046df00e8311710963efd15a87534c670dd0eb0fa2103" +
                    "9313c80a98460104a4e9abc86f1f73d1fa982ecb3172269d917415e6aeee267552ae00000000"

        private const val CHANGE_INDEX_999999 =
            "cHNidP8BAH0CAAAAAUJTCRglAyBzBJKy8g6IQZOs6mW/TAcZQBAwZ1+0nIM2AAAAAAD9////AgMLAAAAAAAA" +
                    "IgAgCrk8USQ4V1PTbvmbC1d4XF6tE0FHxg4DYjSyZ+v36CboAwAAAAAAABYAFMQKYgtvMZZKBJaR" +
                    "Rzu2ymKmITLSIkwJAAABASugDwAAAAAAACIAINkgGp1aRfy8enO5o0VdsS/eT62HjtZKvshETZ7k" +
                    "OaAWAQVHUiEDETn001bzEb3a06Rp/qTGLwiwf+/AXPzyvKqiGMIINXYhA1G/4MxB1s6iMX4VonLV" +
                    "kPjrJOV4kZUAs1iMyPtMR3ddUq4iBgMROfTTVvMRvdrTpGn+pMYvCLB/78Bc/PK8qqIYwgg1dhy9" +
                    "Fr7lMAAAgAAAAIAAAACAAgAAgAEAAAACAAAAIgYDUb/gzEHWzqIxfhWictWQ+Osk5XiRlQCzWIzI" +
                    "+0xHd10cNEIZPjAAAIAAAACAAAAAgAIAAIABAAAAAgAAAAABAUdSIQJVEmEwhGKa0JX96JPOEz0k" +
                    "sJ7/7ogUteBmZsuzy8uRRiEC1V/QblpSYPxOd6UP4ufuL2dIy7LAn3MbVmE7q5+FXj5SriICAlUS" +
                    "YTCEYprQlf3ok84TPSSwnv/uiBS14GZmy7PLy5FGHDRCGT4wAACAAAAAgAAAAIACAACAAQAAAD9C" +
                    "DwAiAgLVX9BuWlJg/E53pQ/i5+4vZ0jLssCfcxtWYTurn4VePhy9Fr7lMAAAgAAAAIAAAACAAgAA" +
                    "gAEAAAA/Qg8AAAA="

        private const val CHANGE_INDEX_1000000 =
            "cHNidP8BAH0CAAAAAUJTCRglAyBzBJKy8g6IQZOs6mW/TAcZQBAwZ1+0nIM2AAAAAAD9////AugDAAAAAAAA" +
                    "FgAUxApiC28xlkoElpFHO7bKYqYhMtIDCwAAAAAAACIAIJdT/Bk+sg3L4UXNnCMQ+76c531xAF4p" +
                    "GWkhztn4evpsIkwJAAABASugDwAAAAAAACIAINkgGp1aRfy8enO5o0VdsS/eT62HjtZKvshETZ7k" +
                    "OaAWAQVHUiEDETn001bzEb3a06Rp/qTGLwiwf+/AXPzyvKqiGMIINXYhA1G/4MxB1s6iMX4VonLV" +
                    "kPjrJOV4kZUAs1iMyPtMR3ddUq4iBgMROfTTVvMRvdrTpGn+pMYvCLB/78Bc/PK8qqIYwgg1dhy9" +
                    "Fr7lMAAAgAAAAIAAAACAAgAAgAEAAAACAAAAIgYDUb/gzEHWzqIxfhWictWQ+Osk5XiRlQCzWIzI" +
                    "+0xHd10cNEIZPjAAAIAAAACAAAAAgAIAAIABAAAAAgAAAAAAAQFHUiEC1/v7nPnBRo1jlhIyjJPw" +
                    "MaBdjZhiYYVxQu52lLXNDeAhA4NzKqUnt/XjzyTC7BzuKiGV96QPVF151rJuX4ZV59vNUq4iAgLX" +
                    "+/uc+cFGjWOWEjKMk/AxoF2NmGJhhXFC7naUtc0N4Bw0Qhk+MAAAgAAAAIAAAACAAgAAgAEAAABA" +
                    "Qg8AIgIDg3MqpSe39ePPJMLsHO4qIZX3pA9UXXnWsm5fhlXn280cvRa+5TAAAIAAAACAAAAAgAIA" +
                    "AIABAAAAQEIPAAA="
    }

    private fun testInvalidPsbt(psbt: String) {
        assertThrows<PsbtException>() {
            Psbt(
                psbt,
                Network.TESTNET
            )
        }
    }

    @Test
    fun testParseTooShortPSBT() {
        testInvalidPsbt("")
    }

    @Test
    fun testInvalidCharacters() {
        testInvalidPsbt("$|-||-|-")
    }

    @Test
    fun testParseBase64() {
        val psbt = Psbt(VALID_PSBT, Network.TESTNET)
        assertEquals(VALID_PSBT, psbt.description)
    }

    @Test
    fun testParseBinary() {
        val data = base642Bytes(VALID_PSBT);
        val psbt = Psbt(data, Network.TESTNET)
        assertEquals(VALID_PSBT, psbt.description)
        assertArrayEquals(data, psbt.data)
    }

    @Test
    fun testInvalidPSBT() {
        testInvalidPsbt("AgAAAAEmgXE3Ht/yhek3re6ks3t4AAwFZsuzrWRkFxPKQhcb9gAAAABqRzBEAiBwsiRRI+a/R01gxbUMBD1MaRpdJDXwmjSnZiqdwlF5CgIgATKcqdrPKAvfMHQOwDkEIkIsgctFg5RXrrdvwS7dlbMBIQJlfRGNM1e44PTCzUbbezn22cONmnCry5st5dyNv+TOMf7///8C09/1BQAAAAAZdqkU0MWZA8W6woaHYOkP1SGkZlqnZSCIrADh9QUAAAAAF6kUNUXm4zuDLEcFDyTT7rk8nAOUi8eHsy4TAA==");
    }

    @Test
    fun testComplete() {
        val incompletePsbt = Psbt(VALID_PSBT, Network.TESTNET)
        val completePsbt = Psbt(FINALIZED_PSBT, Network.TESTNET)
        assertFalse(incompletePsbt.isComplete)
        assertFalse(Psbt(UNSIGNED_PSBT, Network.TESTNET).isComplete)
        assertFalse(Psbt(SIGNED_PSBT_0_2, Network.TESTNET).isComplete)
        assertTrue(completePsbt.isComplete)
    }

    @Test
    fun testExtractTransaction() {
        val incompletePsbt = Psbt(VALID_PSBT, Network.TESTNET)
        assertNull(incompletePsbt.transactionFinal)

        val completePsbt = Psbt(FINALIZED_PSBT, Network.TESTNET)
        assertNotNull(completePsbt.transactionFinal)
        assertEquals(
            "0200000000010258e87a21b56daf0c23be8e7070456c336f7cbaa5c8757924f545887bb2ab" +
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
            completePsbt.transactionFinal.description
        )
    }

    @Test
    fun testSignWithKey() {
        val privKey0 = Key(WIF_0, Network.TESTNET)
        val privKey1 = Key(WIF_1, Network.TESTNET)
        val privKey2 = Key(WIF_2, Network.TESTNET)
        val privKey3 = Key(WIF_3, Network.TESTNET)

        val psbt1 = Psbt(UNSIGNED_PSBT, Network.TESTNET)
        val psbt2 = Psbt(UNSIGNED_PSBT, Network.TESTNET)

        val expectedPsbt02 = Psbt(SIGNED_PSBT_0_2, Network.TESTNET)
        val expectedPsbt13 = Psbt(SIGNED_PSBT_1_3, Network.TESTNET)

        val p102 = psbt1.signed(privKey0).signed(privKey2)
        assertEquals(expectedPsbt02.description, p102.description)

        val p213 = psbt2.signed(privKey1).signed(privKey3)
        assertEquals(expectedPsbt13.description, p213.description)
    }

    @Test
    fun testInputs() {
        val psbt = Psbt(UNSIGNED_PSBT, Network.TESTNET)
        assertEquals(2, psbt.inputs.size)
    }

    @Test
    fun testOutput() {
        val psbt = Psbt(UNSIGNED_PSBT, Network.TESTNET)
        assertEquals(2, psbt.outputs.size)
    }

    @Test
    fun testKeyPaths() {
        val expectedOrigin0 = KeyOrigin(FINGERPRINT, PATH_0)
        val expectedOrigin1 = KeyOrigin(FINGERPRINT, PATH_1)
        val expectedOrigin2 = KeyOrigin(FINGERPRINT, PATH_2)
        val expectedOrigin3 = KeyOrigin(FINGERPRINT, PATH_3)
        val expectedOrigin4 = KeyOrigin(FINGERPRINT, PATH_4)
        val expectedOrigin5 = KeyOrigin(FINGERPRINT, PATH_5)
        val psbt = Psbt(UNSIGNED_PSBT, Network.TESTNET)

        assertEquals(2, psbt.inputs.size)
        val inOrigin0 = psbt.inputs[0].originMap
        assertEquals(2, inOrigin0.size)
        assertEquals(expectedOrigin0, inOrigin0[PUB_KEY_0])
        assertEquals(expectedOrigin1, inOrigin0[PUB_KEY_1])
        val inOrigin1 = psbt.inputs[1].originMap
        assertEquals(2, inOrigin1.size)
        assertEquals(expectedOrigin3, inOrigin1[PUB_KEY_3])
        assertEquals(expectedOrigin2, inOrigin1[PUB_KEY_2])

        assertEquals(2, psbt.outputs.size)
        val outOrigin0 = psbt.outputs[0].originMap
        assertEquals(1, outOrigin0.size)
        assertEquals(expectedOrigin4, outOrigin0[PUB_KEY_4])
        val outOrigin1 = psbt.outputs[1].originMap
        assertEquals(1, outOrigin1.size)
        assertEquals(expectedOrigin5, outOrigin1[PUB_KEY_5])
    }

    @Test
    fun testCanSign() {
        val hdKey = HDKey(MASTER_KEY_XPRIV)
        val psbt = Psbt(UNSIGNED_PSBT, Network.TESTNET)
        for (input in psbt.inputs) {
            assertTrue(input.canSign(hdKey))
        }
    }

    @Test
    fun testFinalize() {
        val psbt = Psbt(SIGNED_PSBT, Network.TESTNET)
        val expected = Psbt(FINALIZED_PSBT, Network.TESTNET)
        val finalized = psbt.finalized()
        assertEquals(expected, finalized)
    }

    @Test
    fun testSignWithHDKey() {
        val psbt = Psbt(UNSIGNED_PSBT, Network.TESTNET)
        val masterKey = HDKey(MASTER_KEY_XPRIV)
        val signed = psbt.signed(masterKey)
        val finalized = signed.finalized()
        assertTrue(finalized.isComplete)
    }

    @Test
    fun testCanSignNeutered() {
        val us = HDKey(
            "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzv" +
                    "LQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi",
            hex2Bytes("3442193e")
        )
        val psbt = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)
        for (input in psbt.inputs) {
            assertTrue(input.canSign(us))
        }
    }

    @Test
    fun testSignRealMultisigWithHDKey() {
        val keySigner1 = HDKey(MASTER_1)
        val keySigner2 = HDKey(MASTER_2)
        val psbtWithoutChange =
            Psbt(MULTI_UNSIGNED_PSBT_WITHOUT_CHANGE, Network.MAINNET)
        val psbtWithChange = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)

        val psbtWithoutChangeSigned = psbtWithoutChange.signed(keySigner1).signed(keySigner2)
        val psbtWithoutChangeFinalized = psbtWithoutChangeSigned.finalized()
        assertTrue(psbtWithoutChangeFinalized.isComplete)
        assertEquals(
            MULTI_PSBT_WITHOUT_CHANGE_HEX,
            psbtWithoutChangeFinalized.transactionFinal.description
        )

        val psbtWithChangeSigned = psbtWithChange.signed(keySigner1).signed(keySigner2)
        val psbtWithChangeFinalized = psbtWithChangeSigned.finalized()
        assertTrue(psbtWithChangeFinalized.isComplete)
        assertEquals(
            MULTI_PSBT_WITH_CHANGE_HEX,
            psbtWithChangeFinalized.transactionFinal.description
        )

        assertEquals(
            4000,
            psbtWithChangeFinalized.outputs[0].txOutput.amount
        )
        assertEquals(
            "bc1qmysp4826gh7tc7nnhx352hd39l0yltv83mty40kgg3xeaepe5qtq4c50qe",
            psbtWithChangeFinalized.outputs[0].txOutput.address
        )

        assertEquals(
            819,
            psbtWithChangeFinalized.outputs[1].txOutput.amount
        )
        assertEquals(
            "bc1qsrufxljkjttj8kven90pta82ah6nqayxfr8p9h",
            psbtWithChangeFinalized.outputs[1].txOutput.address
        )
    }

    @Test
    fun testIsChange() {
        val us = HDKey(MASTER_1)
        val cosigner = HDKey(MASTER_2)
        var psbt = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)
        assertTrue(psbt.outputs[0].isChange(us, psbt.inputs, cosigner, 2))
        assertFalse(psbt.outputs[1].isChange(us, psbt.inputs, cosigner, 2))

        // Test maximum permitted change index
        psbt = Psbt(CHANGE_INDEX_999999, Network.MAINNET)
        assertTrue(psbt.outputs[0].isChange(us, psbt.inputs, cosigner, 2))
        assertFalse(psbt.outputs[1].isChange(us, psbt.inputs, cosigner, 2))

        // Test out of bounds change index
        psbt = Psbt(CHANGE_INDEX_1000000, Network.MAINNET)
        assertFalse(psbt.outputs[0].isChange(us, psbt.inputs, cosigner, 2))
        assertFalse(psbt.outputs[1].isChange(us, psbt.inputs, cosigner, 2))
    }

    @Test
    fun testIsChangeWithNeuteredCosignerKey() {
        val us = HDKey(MASTER_1)
        val cosigner = HDKey(
            "xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oB" +
                    "PMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a",
            hex2Bytes("bd16bee5")
        )

        val psbt = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)
        assertTrue(psbt.outputs[0].isChange(us, psbt.inputs, cosigner, 2))
        assertFalse(psbt.outputs[1].isChange(us, psbt.inputs, cosigner, 2))
    }

    @Test
    fun testIsChangeWithNeuteredAllKeys() {
        val us = HDKey(
            "xpub6E64WfdQwBGz85XhbZryr9gUGUPBgoSu5WV6tJWpzAvgAmpVpdPHkT3XYm9R5J6MeWzvLQoz4q845taC9Q28XutbptxAmg7q8QPkjvTL4oi",
            hex2Bytes("3442193e")
        )
        val cosigner = HDKey(
            "xpub6DwQ4gBCmJZM3TaKogP41tpjuEwnMH2nWEi3PFev37LfsWPvjZrh1GfAG8xvoDYMPWGKG1oBPMCfKpkVbJtUHRaqRdCb6X6o1e9PQTVK88a",
            hex2Bytes("bd16bee5")
        )
        val psbt = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)
        assertTrue(psbt.outputs[0].isChange(us, psbt.inputs, cosigner, 2))
        assertFalse(psbt.outputs[1].isChange(us, psbt.inputs, cosigner, 2))
    }

    @Test
    fun testGetTransactionFee() {
        val psbt = Psbt(MULTI_UNSIGNED_PSBT_WITH_CHANGE, Network.MAINNET)
        assertEquals(181L, psbt.fee as Long)
    }
}