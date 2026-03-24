package com.github.wrager.sbgscout.script.model

data class UserScript(
    val identifier: ScriptIdentifier,
    val header: ScriptHeader,
    val sourceUrl: String?,
    val updateUrl: String?,
    val content: String,
    val enabled: Boolean = false,
    val isPreset: Boolean = false,
    /** Тег GitHub-релиза, из которого был установлен скрипт (например "v6.14.0"). */
    val releaseTag: String? = null,
)
