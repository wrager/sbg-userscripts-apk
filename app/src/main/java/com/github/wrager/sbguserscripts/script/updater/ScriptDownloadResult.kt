package com.github.wrager.sbguserscripts.script.updater

import com.github.wrager.sbguserscripts.script.model.UserScript

sealed class ScriptDownloadResult {
    data class Success(val script: UserScript) : ScriptDownloadResult()
    data class Failure(val url: String, val error: Throwable) : ScriptDownloadResult()
}
