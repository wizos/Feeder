package com.nononsenseapps.feeder.util

fun getAbsoluteUrl(url: String?): String? {
    if (url != null && url.startsWith("//")) {
        // Missing protocol, actually legal according to the spec but it's not handled well
        return "http:" + url
    }

    return url
}
