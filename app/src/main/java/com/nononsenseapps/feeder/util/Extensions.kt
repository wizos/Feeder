package com.nononsenseapps.feeder.util

inline fun <T> T.also(block: (T) -> Unit): T {
    block(this)
    return this
}
