package com.nononsenseapps.feeder

import android.app.Application
import android.webkit.WebView
import org.conscrypt.Conscrypt
import java.security.Security

@Suppress("unused")
class FeederApplication: Application() {
    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        // Instantiate a webview to avoid issue with webview resetting night mode
        // See: https://stackoverflow.com/a/54191884/535073
        try {
            WebView(applicationContext)
        } catch (_: Throwable) {
            // Ignored
        }
    }
}
