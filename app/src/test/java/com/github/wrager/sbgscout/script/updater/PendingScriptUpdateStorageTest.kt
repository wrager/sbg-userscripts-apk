package com.github.wrager.sbgscout.script.updater

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PendingScriptUpdateStorageTest {

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var storage: PendingScriptUpdateStorage

    @Before
    fun setUp() {
        preferences = mockk()
        editor = mockk(relaxed = true)
        every { preferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        storage = PendingScriptUpdateStorage(preferences)
    }

    @Test
    fun `save stores summary in preferences`() {
        storage.save("SVP 0.5.0 → 0.6.0\nNew features")

        verify { editor.putString("pending_script_update_notes", "SVP 0.5.0 → 0.6.0\nNew features") }
        verify { editor.apply() }
    }

    @Test
    fun `consumePending returns null when nothing saved`() {
        every { preferences.getString("pending_script_update_notes", null) } returns null

        assertNull(storage.consumePending())
    }

    @Test
    fun `consumePending returns and clears saved summary`() {
        every { preferences.getString("pending_script_update_notes", null) } returns "Update notes"

        val result = storage.consumePending()

        assertEquals("Update notes", result)
        verify { editor.remove("pending_script_update_notes") }
        verify { editor.apply() }
    }

    @Test
    fun `consumePending does not clear when nothing to consume`() {
        every { preferences.getString("pending_script_update_notes", null) } returns null

        storage.consumePending()

        verify(exactly = 0) { editor.remove(any()) }
    }
}
