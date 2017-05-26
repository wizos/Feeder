package com.nononsenseapps.feeder.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nononsenseapps.feeder.ui.ReaderFragment.RssItemFromBundle
import com.nononsenseapps.feeder.util.markItemAsRead

class ReaderWebViewFragment : Fragment() {
    companion object factory {
        fun getInstance(uri: String) {
            val fragment = ReaderWebViewFragment()
            fragment.url = uri
        }
    }
    var url: String = ""

    private var webView: WebView? = null
    private var isWebViewAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rssItem = RssItemFromBundle(arguments ?: Bundle.EMPTY)
        url = rssItem.link

        if (rssItem.id > 0) {
            activity.contentResolver.markItemAsRead(rssItem.id, true)
        }
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        webView?.destroy()
        webView = WebView(context)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.builtInZoomControls = true
        webView?.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // prevent links from loading in external web browser
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // prevent links from loading in external web browser
                return false
            }
        })

        isWebViewAvailable = true
        webView?.loadUrl(url)
        return webView
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    override fun onResume() {
        webView?.onResume()
        super.onResume()
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    override fun onDestroyView() {
        isWebViewAvailable = false
        super.onDestroyView()
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    /**
     * Gets the WebView.
     */
    fun getWebView(): WebView? {
        return if (isWebViewAvailable) webView else null
    }
}
