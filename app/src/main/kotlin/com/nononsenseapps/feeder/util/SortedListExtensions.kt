package com.nononsenseapps.feeder.util

import android.support.v7.util.SortedList

fun <T : Any?> SortedList<T>.withBatchedUpdates(init: () -> Unit) {
    beginBatchedUpdates()
    init()
    endBatchedUpdates()
}
