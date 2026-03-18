package com.github.wrager.sbguserscripts.launcher

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.github.wrager.sbguserscripts.script.model.UserScript
import com.github.wrager.sbguserscripts.script.preset.ConflictDetector
import com.github.wrager.sbguserscripts.script.preset.PresetScripts
import com.github.wrager.sbguserscripts.script.storage.ScriptStorage
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloadResult
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val scriptStorage: ScriptStorage,
    private val conflictDetector: ConflictDetector,
    private val downloader: ScriptDownloader,
    private val appPreferences: SharedPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        loadScripts()
    }

    private fun loadScripts() {
        viewModelScope.launch {
            if (!appPreferences.getBoolean(KEY_PRESETS_DOWNLOADED, false)) {
                downloadPresets()
            }
            refreshScriptList()
        }
    }

    private suspend fun downloadPresets() {
        for (preset in PresetScripts.ALL) {
            val result = downloader.download(preset.downloadUrl, isPreset = true)
            when (result) {
                is ScriptDownloadResult.Success -> {
                    if (preset == PresetScripts.SVP) {
                        scriptStorage.setEnabled(result.script.identifier, true)
                    }
                    Log.i(LOG_TAG, "Загружен ${preset.displayName}: ${result.script.header.version}")
                }
                is ScriptDownloadResult.Failure -> {
                    Log.e(LOG_TAG, "Не удалось загрузить ${preset.displayName}: ${result.error}")
                }
            }
        }
        appPreferences.edit().putBoolean(KEY_PRESETS_DOWNLOADED, true).apply()
    }

    fun toggleScript(identifier: ScriptIdentifier, enabled: Boolean) {
        scriptStorage.setEnabled(identifier, enabled)
        refreshScriptList()
    }

    private fun refreshScriptList() {
        val scripts = scriptStorage.getAll()
        val enabledIdentifiers = scripts.filter { it.enabled }.map { it.identifier }.toSet()
        val nameByIdentifier = scripts.associate { it.identifier to it.header.name }

        val items = scripts.map { script ->
            buildScriptUiItem(script, enabledIdentifiers, nameByIdentifier)
        }
        _uiState.value = LauncherUiState(isLoading = false, scripts = items)
    }

    private fun buildScriptUiItem(
        script: UserScript,
        enabledIdentifiers: Set<ScriptIdentifier>,
        nameByIdentifier: Map<ScriptIdentifier, String>,
    ): ScriptUiItem {
        val conflicts = if (script.enabled) {
            conflictDetector.detectConflicts(
                script.identifier,
                enabledIdentifiers - script.identifier,
            )
        } else {
            emptyList()
        }

        val conflictNames = conflicts.map { conflict ->
            nameByIdentifier[conflict.conflictsWith] ?: conflict.conflictsWith.value
        }

        return ScriptUiItem(
            identifier = script.identifier,
            name = script.header.name,
            version = script.header.version,
            author = script.header.author,
            enabled = script.enabled,
            isPreset = script.isPreset,
            conflictNames = conflictNames,
        )
    }

    class Factory(
        private val scriptStorage: ScriptStorage,
        private val conflictDetector: ConflictDetector,
        private val downloader: ScriptDownloader,
        private val appPreferences: SharedPreferences,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LauncherViewModel(
                scriptStorage,
                conflictDetector,
                downloader,
                appPreferences,
            ) as T
        }
    }

    companion object {
        private const val KEY_PRESETS_DOWNLOADED = "presetsDownloaded"
        private const val LOG_TAG = "Launcher"
    }
}
