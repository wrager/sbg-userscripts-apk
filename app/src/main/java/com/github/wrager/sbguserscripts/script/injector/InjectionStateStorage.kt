package com.github.wrager.sbguserscripts.script.injector

import android.content.SharedPreferences
import com.github.wrager.sbguserscripts.script.model.UserScript

class InjectionStateStorage(private val preferences: SharedPreferences) {

    fun saveSnapshot(scripts: List<UserScript>) {
        val entries = scripts.map { "${it.identifier.value}::${it.header.version ?: ""}" }.toSet()
        preferences.edit().putStringSet(KEY_LAST_INJECTED, entries).apply()
    }

    fun getSnapshot(): Set<String> {
        return preferences.getStringSet(KEY_LAST_INJECTED, null) ?: emptySet()
    }

    companion object {
        private const val KEY_LAST_INJECTED = "last_injected_state"
    }
}
