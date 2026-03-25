package com.github.wrager.sbgscout.script.updater

import android.content.SharedPreferences

/**
 * Хранит информацию о выполненных обновлениях скриптов в SharedPreferences.
 *
 * Когда скрипты обновляются из GameActivity (автоматически или через drawer),
 * сюда сохраняется описание обновлений с release notes.
 * При следующем открытии LauncherActivity информация показывается и сбрасывается.
 */
class PendingScriptUpdateStorage(private val preferences: SharedPreferences) {

    /** Сохраняет текст с описанием обновлений для отложенного показа. */
    fun save(updateSummary: String) {
        preferences.edit().putString(KEY_PENDING_UPDATES, updateSummary).apply()
    }

    /** Читает и сбрасывает сохранённое описание обновлений. Возвращает null, если ничего не сохранено. */
    fun consumePending(): String? {
        val summary = preferences.getString(KEY_PENDING_UPDATES, null)
        if (summary != null) {
            preferences.edit().remove(KEY_PENDING_UPDATES).apply()
        }
        return summary
    }

    companion object {
        private const val KEY_PENDING_UPDATES = "pending_script_update_notes"
    }
}
