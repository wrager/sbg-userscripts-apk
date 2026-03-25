package com.github.wrager.sbgscout.diagnostic

import android.webkit.ConsoleMessage
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Потокобезопасный кольцевой буфер для хранения последних [MAX_ENTRIES] записей
 * из WebView console (error, warn, uncaught exceptions).
 *
 * Используется для включения лога в баг-репорт.
 */
class ConsoleLogBuffer {

    data class Entry(
        val timestamp: Instant,
        val level: Level,
        val message: String,
        val source: String?,
        val lineNumber: Int,
    )

    enum class Level(val label: String) {
        ERROR("error"),
        WARNING("warn"),
    }

    private val entries = ArrayDeque<Entry>()

    @Synchronized
    fun add(consoleMessage: ConsoleMessage) {
        val level = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> Level.ERROR
            ConsoleMessage.MessageLevel.WARNING -> Level.WARNING
            else -> return // Сохраняем только error и warn
        }
        val entry = Entry(
            timestamp = Instant.now(),
            level = level,
            message = consoleMessage.message(),
            source = consoleMessage.sourceId(),
            lineNumber = consoleMessage.lineNumber(),
        )
        entries.addLast(entry)
        if (entries.size > MAX_ENTRIES) {
            entries.removeFirst()
        }
    }

    @Synchronized
    fun format(): String {
        if (entries.isEmpty()) return ""
        return entries.joinToString("\n") { entry ->
            val time = FORMATTER.format(entry.timestamp)
            val location = entry.source?.let { " [$it:${entry.lineNumber}]" }.orEmpty()
            "[$time] [${entry.level.label}] ${entry.message}$location"
        }
    }

    @Synchronized
    fun snapshot(): List<Entry> = entries.toList()

    @Synchronized
    fun clear() {
        entries.clear()
    }

    companion object {
        const val MAX_ENTRIES = 50
        private val FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)
    }
}
