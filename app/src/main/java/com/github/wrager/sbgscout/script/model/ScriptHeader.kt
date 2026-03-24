package com.github.wrager.sbgscout.script.model

data class ScriptHeader(
    val name: String,
    val version: String? = null,
    val description: String? = null,
    val author: String? = null,
    val namespace: String? = null,
    val match: List<String> = emptyList(),
    val grant: List<String> = emptyList(),
    val downloadUrl: String? = null,
    val updateUrl: String? = null,
    val runAt: String? = null,
    val iconUrl: String? = null,
    val rawEntries: Map<String, List<String>> = emptyMap(),
)
