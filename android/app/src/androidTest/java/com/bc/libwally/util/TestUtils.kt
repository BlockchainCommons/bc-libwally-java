package com.bc.libwally.util

inline fun <reified T : Throwable> assertThrows(
    msg: String?,
    callable: () -> Unit
): T {
    try {
        callable()
        throw RuntimeException(msg)
    } catch (e: Throwable) {
        if (e is T) return e
        throw e
    }
}