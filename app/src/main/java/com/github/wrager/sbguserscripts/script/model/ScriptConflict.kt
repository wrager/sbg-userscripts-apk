package com.github.wrager.sbguserscripts.script.model

data class ScriptConflict(
    val scriptIdentifier: ScriptIdentifier,
    val conflictsWith: ScriptIdentifier,
    val reason: String,
)
