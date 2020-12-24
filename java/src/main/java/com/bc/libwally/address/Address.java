package com.bc.libwally.address;

import com.bc.libwally.bip32.HDKey;
import com.bc.libwally.bip32.Network;
import com.bc.libwally.script.ScriptPubKey;

import java.util.Objects;

import static com.bc.libwally.WallyConstant.WALLY_OK;
import static com.bc.libwally.address.AddressConstant.WALLY_ADDRESS_TYPE_P2PKH;
import static com.bc.libwally.address.AddressConstant.WALLY_ADDRESS_TYPE_P2SH_P2WPKH;
import static com.bc.libwally.address.AddressConstant.WALLY_ADDRESS_TYPE_P2WPKH;
import static com.bc.libwally.address.AddressConstant.WALLY_NETWORK_BITCOIN_MAINNET;
import static com.bc.libwally.address.AddressConstant.WALLY_NETWORK_BITCOIN_TESTNET;
import static com.bc.libwally.address.AddressJni.wally_addr_segwit_from_bytes;
import static com.bc.libwally.address.AddressJni.wally_addr_segwit_to_bytes;
import static com.bc.libwally.address.AddressJni.wally_address_to_scriptpubkey;
import static com.bc.libwally.address.AddressJni.wally_bip32_key_to_addr_segwit;
import static com.bc.libwally.address.AddressJni.wally_bip32_key_to_address;
import static com.bc.libwally.address.AddressJni.wally_scriptpubkey_to_address;


public class Address {

    private final ScriptPubKey scriptPubKey;

    private final String address;

    private Network network;

    public Address(String addr) {
        this.address = addr;

        byte[] bytes = new byte[addr.length()];
        int[] written = new int[1];

        // Try if this is a bench32 Bitcoin mainnet address:
        this.network = Network.MAINNET;
        int ret = wally_addr_segwit_to_bytes(this.address,
                                             getAddrFamily(this.network),
                                             bytes,
                                             written);

        if (ret != WALLY_OK) {
            // Try if this is a bench32 Bitcoin testnet address:
            this.network = Network.TESTNET;
            ret = wally_addr_segwit_to_bytes(this.address,
                                             getAddrFamily(this.network),
                                             bytes,
                                             written);
        }

        if (ret != WALLY_OK) {
            // Try if this is a mainnet base58 addresses (P2PKH or P2SH)
            ret = wally_address_to_scriptpubkey(addr,
                                                WALLY_NETWORK_BITCOIN_MAINNET,
                                                bytes,
                                                written);
            this.network = Network.MAINNET;
        }

        if (ret != WALLY_OK) {
            // Try if this is a testnet base58 addresses (P2PKH or P2SH)
            ret = wally_address_to_scriptpubkey(addr,
                                                WALLY_NETWORK_BITCOIN_TESTNET,
                                                bytes,
                                                written);
        }


        if (ret != WALLY_OK) {
            throw new AddressException("Could not construct address");
        }

        this.scriptPubKey = new ScriptPubKey(bytes, written[0]);
    }

    public Address(HDKey key, AddressType type) {
        long addrType;
        String addr;

        switch (type) {
            case PAY_TO_PUBKEY_HASH:
                addrType = WALLY_ADDRESS_TYPE_P2PKH;
                break;
            case PAY_TO_SCRIPT_HASH_PAY_TO_WITNESS_PUBKEY_HASH:
                addrType = WALLY_ADDRESS_TYPE_P2SH_P2WPKH;
                break;
            case PAY_TO_WITNESS_PUBKEY_HASH:
                addrType = WALLY_ADDRESS_TYPE_P2WPKH;
                break;
            default:
                throw new AddressException("Unknown address type");
        }

        if (addrType == WALLY_ADDRESS_TYPE_P2PKH || addrType == WALLY_ADDRESS_TYPE_P2SH_P2WPKH) {
            long version;
            if (key.getNetwork() == Network.MAINNET) {
                version = addrType == WALLY_ADDRESS_TYPE_P2PKH ? 0x00 : 0x05;
            } else {
                version = addrType == WALLY_ADDRESS_TYPE_P2PKH ? 0x6F : 0xC4;
            }

            addr = wally_bip32_key_to_address(key.getKey(), addrType, version);

        } else {
            addr = wally_bip32_key_to_addr_segwit(key.getKey(), getAddrFamily(key.getNetwork()));
        }

        Address address = new Address(addr);
        this.address = address.address;
        this.scriptPubKey = address.scriptPubKey;
        this.network = address.network;
    }

    public Address(ScriptPubKey scriptPubKey, Network network) {
        this.network = network;
        this.scriptPubKey = scriptPubKey;
        switch (this.scriptPubKey.getType()) {
            case PAY_TO_PUBKEY_HASH:
            case PAY_TO_SCRIPT_HASH:
                long networkFlag = this.network == Network.MAINNET
                                   ? WALLY_NETWORK_BITCOIN_MAINNET
                                   : WALLY_NETWORK_BITCOIN_TESTNET;
                this.address = wally_scriptpubkey_to_address(this.scriptPubKey.getData(),
                                                             networkFlag);
                break;
            case PAY_TO_WITNESS_PUBKEY_HASH:
            case PAY_TO_WITNESS_SCRIPT_HASH:
                this.address = wally_addr_segwit_from_bytes(scriptPubKey.getData(),
                                                            getAddrFamily(this.network));
                break;
            case MULTI_SIG:
                this.address = wally_addr_segwit_from_bytes(scriptPubKey.getWitnessProgram(),
                                                            getAddrFamily(this.network));
                break;
            default:
                throw new AddressException("Could not construct address");
        }
    }

    public Network getNetwork() {
        return network;
    }

    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public String getAddress() {
        return address;
    }

    private String getAddrFamily(Network network) {
        return network == Network.MAINNET ? "bc" : "tb";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Address address1 = (Address) o;
        return address.equals(address1.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
