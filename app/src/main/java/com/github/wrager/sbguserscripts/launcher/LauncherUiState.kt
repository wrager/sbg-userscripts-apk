package com.github.wrager.sbguserscripts.launcher

import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier

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
)
