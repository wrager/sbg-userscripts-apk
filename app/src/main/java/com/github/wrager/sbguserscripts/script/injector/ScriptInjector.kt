package com.github.wrager.sbguserscripts.script.injector

import android.webkit.WebView
import com.github.wrager.sbguserscripts.script.model.UserScript
import com.github.wrager.sbguserscripts.script.storage.ScriptStorage

class ScriptInjector(
    private val scriptStorage: ScriptStorage,
    private val applicationId: String,
    private val versionName: String,
) {

    fun inject(webView: WebView) {
        injectGlobalVariables(webView)
        injectClipboardPolyfill(webView)
        val enabledScripts = scriptStorage.getEnabled()
        for (script in enabledScripts) {
            injectScript(webView, script)
        }
    }

    private fun injectGlobalVariables(webView: WebView) {
        val script = buildGlobalVariablesScript(applicationId, versionName)
        webView.evaluateJavascript(script) {}
    }

    private fun injectClipboardPolyfill(webView: WebView) {
        webView.evaluateJavascript(CLIPBOARD_POLYFILL) {}
    }

    private fun injectScript(webView: WebView, script: UserScript) {
        val wrapped = wrapInSafeIife(script.header.name, script.content)
        webView.evaluateJavascript(wrapped) {}
    }

    companion object {

        internal fun wrapInSafeIife(scriptName: String, content: String): String {
            val escapedName = scriptName.replace("\\", "\\\\").replace("'", "\\'")
            return """
                (function() {
                    try {
                        $content
                    } catch (error) {
                        console.error('[SBG Userscripts] "$escapedName" failed:', error);
                    }
                })();
            """.trimIndent()
        }

        internal fun buildGlobalVariablesScript(
            applicationId: String,
            versionName: String,
        ): String {
            val escapedApplicationId = applicationId.replace("'", "\\'")
            val escapedVersionName = versionName.replace("'", "\\'")
            return """
                window.__sbg_local = false;
                window.__sbg_preset = '';
                window.__sbg_package = '$escapedApplicationId';
                window.__sbg_package_version = '$escapedVersionName';
            """.trimIndent()
        }

        private val CLIPBOARD_POLYFILL = """
            (function() {
                if (navigator.clipboard) return;
                navigator.clipboard = {
                    readText: function() {
                        return new Promise(function(resolve) {
                            resolve(Android.readText());
                        });
                    },
                    writeText: function(text) {
                        return new Promise(function(resolve) {
                            Android.writeText(text);
                            resolve();
                        });
                    }
                };
            })();
        """.trimIndent()
    }
}
