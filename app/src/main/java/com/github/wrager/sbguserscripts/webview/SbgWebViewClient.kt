package com.github.wrager.sbguserscripts.webview

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.github.wrager.sbguserscripts.R
import com.github.wrager.sbguserscripts.script.injector.InjectionResult
import com.github.wrager.sbguserscripts.script.injector.ScriptInjector

class SbgWebViewClient(
    private val scriptInjector: ScriptInjector,
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url?.contains("sbg-game.ru/app") == true && view != null) {
            scriptInjector.inject(view) { results ->
                handleInjectionResults(view, results)
            }
        }
    }

    private fun handleInjectionResults(view: WebView, results: List<InjectionResult>) {
        val errors = results.filterIsInstance<InjectionResult.ScriptError>()
        if (errors.isEmpty()) return
        val scriptNames = errors.joinToString(", ") { it.identifier.value }
        val message = view.context.getString(R.string.script_injection_error, scriptNames)
        Toast.makeText(view.context, message, Toast.LENGTH_LONG).show()
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
