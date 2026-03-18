package com.github.wrager.sbguserscripts.script.injector

import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier

sealed class InjectionResult {
    data class Success(val identifier: ScriptIdentifier) : InjectionResult()
    data class ScriptError(
        val identifier: ScriptIdentifier,
        val errorMessage: String,
    ) : InjectionResult()
}
