package com.github.wrager.sbguserscripts.launcher

import android.content.SharedPreferences
import com.github.wrager.sbguserscripts.script.model.ScriptHeader
import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.github.wrager.sbguserscripts.script.model.UserScript
import com.github.wrager.sbguserscripts.script.preset.ConflictDetector
import com.github.wrager.sbguserscripts.script.preset.PresetScripts
import com.github.wrager.sbguserscripts.script.preset.StaticConflictRules
import com.github.wrager.sbguserscripts.script.storage.ScriptStorage
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloadResult
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloader
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var scriptStorage: ScriptStorage
    private val conflictDetector = ConflictDetector(StaticConflictRules())
    private lateinit var downloader: ScriptDownloader
    private lateinit var appPreferences: SharedPreferences
    private lateinit var preferencesEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        scriptStorage = mockk()
        downloader = mockk()
        appPreferences = mockk()
        preferencesEditor = mockk()

        every { appPreferences.edit() } returns preferencesEditor
        every { preferencesEditor.putBoolean(any(), any()) } returns preferencesEditor
        every { preferencesEditor.apply() } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads existing scripts from storage`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns true
        every { scriptStorage.getAll() } returns listOf(testScript())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.scripts.size)
        assertEquals("Test Script", state.scripts[0].name)
    }

    @Test
    fun `downloads presets on first launch`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns false
        every { scriptStorage.getAll() } returns emptyList()
        coEvery { downloader.download(any(), isPreset = true) } returns
            ScriptDownloadResult.Success(testScript())
        every { scriptStorage.setEnabled(any(), any()) } just Runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        coVerify(exactly = PresetScripts.ALL.size) { downloader.download(any(), isPreset = true) }
    }

    @Test
    fun `enables SVP by default on first launch`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns false
        every { scriptStorage.getAll() } returns emptyList()
        val svpScript = testScript(identifier = PresetScripts.SVP.identifier, name = "SVP")
        coEvery { downloader.download(PresetScripts.SVP.downloadUrl, isPreset = true) } returns
            ScriptDownloadResult.Success(svpScript)
        coEvery { downloader.download(neq(PresetScripts.SVP.downloadUrl), isPreset = true) } returns
            ScriptDownloadResult.Success(testScript())
        every { scriptStorage.setEnabled(any(), any()) } just Runs

        createViewModel()
        advanceUntilIdle()

        verify { scriptStorage.setEnabled(PresetScripts.SVP.identifier, true) }
    }

    @Test
    fun `toggles script enabled state`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns true
        val script = testScript()
        every { scriptStorage.getAll() } returns listOf(script)
        every { scriptStorage.setEnabled(any(), any()) } just Runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleScript(script.identifier, true)

        verify { scriptStorage.setEnabled(script.identifier, true) }
    }

    @Test
    fun `detects conflicts for enabled scripts`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns true
        val svp = testScript(
            identifier = PresetScripts.SVP.identifier,
            name = "SVP",
            enabled = true,
        )
        val eui = testScript(
            identifier = PresetScripts.EUI.identifier,
            name = "EUI",
            enabled = true,
        )
        every { scriptStorage.getAll() } returns listOf(svp, eui)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val svpItem = state.scripts.first { it.identifier == PresetScripts.SVP.identifier }
        assertTrue(svpItem.conflictNames.contains("EUI"))

        val euiItem = state.scripts.first { it.identifier == PresetScripts.EUI.identifier }
        assertTrue(euiItem.conflictNames.contains("SVP"))
    }

    @Test
    fun `no conflicts for disabled scripts`() = runTest {
        every { appPreferences.getBoolean("presetsDownloaded", false) } returns true
        val svp = testScript(
            identifier = PresetScripts.SVP.identifier,
            name = "SVP",
            enabled = false,
        )
        val eui = testScript(
            identifier = PresetScripts.EUI.identifier,
            name = "EUI",
            enabled = true,
        )
        every { scriptStorage.getAll() } returns listOf(svp, eui)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val svpItem = state.scripts.first { it.identifier == PresetScripts.SVP.identifier }
        assertTrue(svpItem.conflictNames.isEmpty())
    }

    private fun createViewModel() = LauncherViewModel(
        scriptStorage,
        conflictDetector,
        downloader,
        appPreferences,
    )

    private fun testScript(
        identifier: ScriptIdentifier = ScriptIdentifier("test/script"),
        name: String = "Test Script",
        version: String = "1.0.0",
        enabled: Boolean = false,
    ) = UserScript(
        identifier = identifier,
        header = ScriptHeader(name = name, version = version),
        sourceUrl = "https://example.com/script.user.js",
        updateUrl = "https://example.com/script.meta.js",
        content = "console.log('test')",
        enabled = enabled,
        isPreset = false,
    )
}
