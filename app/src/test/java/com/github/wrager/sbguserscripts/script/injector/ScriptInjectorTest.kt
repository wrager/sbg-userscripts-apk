package com.github.wrager.sbguserscripts.script.injector

import android.webkit.ValueCallback
import android.webkit.WebView
import com.github.wrager.sbguserscripts.script.model.ScriptHeader
import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.github.wrager.sbguserscripts.script.model.UserScript
import com.github.wrager.sbguserscripts.script.storage.ScriptStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScriptInjectorTest {

    private lateinit var scriptStorage: ScriptStorage
    private lateinit var webView: WebView
    private lateinit var injector: ScriptInjector

    @Before
    fun setUp() {
        scriptStorage = mockk()
        webView = mockk(relaxed = true)
        injector = ScriptInjector(
            scriptStorage = scriptStorage,
            applicationId = "com.github.wrager.sbguserscripts",
            versionName = "1.0",
        )
    }

    @Test
    fun `wrapInSafeIife wraps content in try-catch IIFE`() {
        val result = ScriptInjector.wrapInSafeIife("Test Script", "console.log('hello');")

        assertTrue(result.contains("(function() {"))
        assertTrue(result.contains("try {"))
        assertTrue(result.contains("console.log('hello');"))
        assertTrue(result.contains("} catch (error) {"))
        assertTrue(result.contains("console.error('[SBG Userscripts] \"Test Script\" failed:', error);"))
        assertTrue(result.contains("})();"))
    }

    @Test
    fun `wrapInSafeIife escapes single quotes in script name`() {
        val result = ScriptInjector.wrapInSafeIife("Script's Name", "code")

        assertTrue(result.contains("Script\\'s Name"))
    }

    @Test
    fun `wrapInSafeIife escapes backslashes in script name`() {
        val result = ScriptInjector.wrapInSafeIife("Script\\Path", "code")

        assertTrue(result.contains("Script\\\\Path"))
    }

    @Test
    fun `buildGlobalVariablesScript sets all four variables`() {
        val result = ScriptInjector.buildGlobalVariablesScript(
            "com.example.app",
            "2.0.1",
        )

        assertTrue(result.contains("window.__sbg_local = false;"))
        assertTrue(result.contains("window.__sbg_preset = '';"))
        assertTrue(result.contains("window.__sbg_package = 'com.example.app';"))
        assertTrue(result.contains("window.__sbg_package_version = '2.0.1';"))
    }

    @Test
    fun `buildGlobalVariablesScript escapes quotes in values`() {
        val result = ScriptInjector.buildGlobalVariablesScript(
            "com.app'test",
            "1.0'beta",
        )

        assertTrue(result.contains("com.app\\'test"))
        assertTrue(result.contains("1.0\\'beta"))
    }

    @Test
    fun `inject calls evaluateJavascript for globals and polyfill when no scripts`() {
        every { scriptStorage.getEnabled() } returns emptyList()

        injector.inject(webView)

        // globals + polyfill = 2 вызова evaluateJavascript
        verify(exactly = 2) {
            webView.evaluateJavascript(any(), any<ValueCallback<String>>())
        }
    }

    @Test
    fun `inject calls evaluateJavascript for each enabled script`() {
        every { scriptStorage.getEnabled() } returns listOf(
            createTestScript("script-a", "Script A", "code_a"),
            createTestScript("script-b", "Script B", "code_b"),
        )

        injector.inject(webView)

        // globals + polyfill + 2 скрипта = 4 вызова
        verify(exactly = 4) {
            webView.evaluateJavascript(any(), any<ValueCallback<String>>())
        }
    }

    @Test
    fun `inject uses getEnabled not getAll`() {
        every { scriptStorage.getEnabled() } returns emptyList()

        injector.inject(webView)

        verify { scriptStorage.getEnabled() }
        verify(exactly = 0) { scriptStorage.getAll() }
    }

    @Test
    fun `inject injects globals containing application id and version`() {
        every { scriptStorage.getEnabled() } returns emptyList()
        val capturedScripts = mutableListOf<String>()
        every {
            webView.evaluateJavascript(capture(capturedScripts), any<ValueCallback<String>>())
        } returns Unit

        injector.inject(webView)

        val globalsScript = capturedScripts.first()
        assertTrue(globalsScript.contains("window.__sbg_package = 'com.github.wrager.sbguserscripts'"))
        assertTrue(globalsScript.contains("window.__sbg_package_version = '1.0'"))
    }

    @Test
    fun `inject wraps each script in IIFE with try-catch`() {
        every { scriptStorage.getEnabled() } returns listOf(
            createTestScript("test/id", "My Script", "alert(1);"),
        )
        val capturedScripts = mutableListOf<String>()
        every {
            webView.evaluateJavascript(capture(capturedScripts), any<ValueCallback<String>>())
        } returns Unit

        injector.inject(webView)

        // Третий вызов — скрипт (после globals и polyfill)
        val scriptCall = capturedScripts[2]
        assertTrue(scriptCall.contains("(function() {"))
        assertTrue(scriptCall.contains("try {"))
        assertTrue(scriptCall.contains("alert(1);"))
        assertTrue(scriptCall.contains("} catch (error) {"))
    }

    @Test
    fun `inject injects clipboard polyfill with navigator check`() {
        every { scriptStorage.getEnabled() } returns emptyList()
        val capturedScripts = mutableListOf<String>()
        every {
            webView.evaluateJavascript(capture(capturedScripts), any<ValueCallback<String>>())
        } returns Unit

        injector.inject(webView)

        val polyfillScript = capturedScripts[1]
        assertTrue(polyfillScript.contains("navigator.clipboard"))
        assertTrue(polyfillScript.contains("Android.readText()"))
        assertTrue(polyfillScript.contains("Android.writeText(text)"))
    }

    @Test
    fun `inject does not include disabled scripts`() {
        val enabledScript = createTestScript("enabled", "Enabled", "enabled_code")
        every { scriptStorage.getEnabled() } returns listOf(enabledScript)
        val capturedScripts = mutableListOf<String>()
        every {
            webView.evaluateJavascript(capture(capturedScripts), any<ValueCallback<String>>())
        } returns Unit

        injector.inject(webView)

        val allInjected = capturedScripts.joinToString()
        assertTrue(allInjected.contains("enabled_code"))
        assertFalse(allInjected.contains("disabled_code"))
    }

    private fun createTestScript(
        identifier: String,
        name: String,
        content: String,
    ): UserScript = UserScript(
        identifier = ScriptIdentifier(identifier),
        header = ScriptHeader(name = name),
        sourceUrl = null,
        updateUrl = null,
        content = content,
        enabled = true,
    )
}
