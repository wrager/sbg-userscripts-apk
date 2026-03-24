package com.github.wrager.sbgscout.script.updater

interface HttpFetcher {
    suspend fun fetch(
        url: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Int) -> Unit)? = null,
    ): String
}
