package com.github.wrager.sbguserscripts.launcher

import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.github.wrager.sbguserscripts.script.updater.GithubReleaseProvider

data class LauncherUiState(
    val isLoading: Boolean = true,
    val scripts: List<ScriptUiItem> = emptyList(),
)

data class ScriptUiItem(
    val identifier: ScriptIdentifier,
    val name: String,
    val version: String?,
    val author: String?,
    val enabled: Boolean,
    val isPreset: Boolean,
    val conflictNames: List<String>,
    val sourceUrl: String?,
) {
    val isGithubHosted: Boolean
        get() = sourceUrl != null &&
            GithubReleaseProvider.extractOwnerAndRepository(sourceUrl) != null
}

sealed class LauncherEvent {
    data class ScriptAdded(val scriptName: String) : LauncherEvent()
    data class ScriptAddFailed(val errorMessage: String) : LauncherEvent()
    data class ScriptDeleted(val scriptName: String) : LauncherEvent()
    data class UpdatesCompleted(val updatedCount: Int) : LauncherEvent()
}
