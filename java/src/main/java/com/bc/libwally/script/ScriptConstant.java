package com.bc.libwally.script;

public class ScriptConstant {

    /* Script types */
    public static final int WALLY_SCRIPT_TYPE_UNKNOWN = 0x0;

    public static final int WALLY_SCRIPT_TYPE_OP_RETURN = 0x1;

    public static final int WALLY_SCRIPT_TYPE_P2PKH = 0x2;

    public static final int WALLY_SCRIPT_TYPE_P2SH = 0x4;

    public static final int WALLY_SCRIPT_TYPE_P2WPKH = 0x8;

    public static final int WALLY_SCRIPT_TYPE_P2WSH = 0x10;

    public static final int WALLY_SCRIPT_TYPE_MULTISIG = 0x20;

    /* Standard script lengths */
    public static final int WALLY_SCRIPTPUBKEY_P2PKH_LEN = 25;

    public static final int WALLY_SCRIPTPUBKEY_P2SH_LEN = 23;

    public static final int WALLY_SCRIPTPUBKEY_P2WPKH_LEN = 22;

    public static final int WALLY_SCRIPTPUBKEY_P2WSH_LEN = 34;

    public static final int WALLY_SCRIPTPUBKEY_OP_RETURN_MAX_LEN = 83;

    public static final int WALLY_MAX_OP_RETURN_LEN = 80;

    public static final int WALLY_SCRIPTSIG_P2PKH_MAX_LEN = 140;

    public static final int WALLY_WITNESSSCRIPT_MAX_LEN = 35;

    public static final int WALLY_SCRIPT_VARINT_MAX_SIZE = 9;

    /* Script creation flags */
    public static final int WALLY_SCRIPT_HASH160 = 0x1;

    public static final int WALLY_SCRIPT_SHA256 = 0x2;

    public static final int WALLY_SCRIPT_AS_PUSH = 0x4;

    public static final int WALLY_SCRIPT_MULTISIG_SORTED = 0x8;
}
