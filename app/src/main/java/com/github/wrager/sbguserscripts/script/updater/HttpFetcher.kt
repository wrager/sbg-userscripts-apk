package com.github.wrager.sbguserscripts.script.updater

interface HttpFetcher {
    suspend fun fetch(url: String): String
}
