package com.github.wrager.sbguserscripts.webview

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.wrager.sbguserscripts.script.injector.ScriptInjector

class SbgWebViewClient(
    private val scriptInjector: ScriptInjector,
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url?.contains("sbg-game.ru/app") == true && view != null) {
            scriptInjector.inject(view)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        if (url.contains("window.close")) {
            val context = view?.context
            if (context is Activity) context.finish()
            return true
        }
        // Все URL (включая Telegram OAuth) загружаются в WebView
        return false
    }
}
