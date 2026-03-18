package com.github.wrager.sbguserscripts.script.model

data class UserScript(
    val identifier: ScriptIdentifier,
    val header: ScriptHeader,
    val sourceUrl: String?,
    val updateUrl: String?,
    val content: String,
    val enabled: Boolean = false,
    val isPreset: Boolean = false,
)
