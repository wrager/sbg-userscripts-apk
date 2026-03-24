package com.github.wrager.sbgscout.script.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptVersionTest {

    @Test
    fun `equal versions return zero`() {
        assertEquals(0, ScriptVersion("1.0.0").compareTo(ScriptVersion("1.0.0")))
    }

    @Test
    fun `newer version is greater`() {
        assertTrue(ScriptVersion("1.1.0") > ScriptVersion("1.0.0"))
    }

    @Test
    fun `older version is less`() {
        assertTrue(ScriptVersion("1.0.0") < ScriptVersion("1.1.0"))
    }

    @Test
    fun `major version difference takes precedence`() {
        assertTrue(ScriptVersion("2.0.0") > ScriptVersion("1.99.99"))
    }

    @Test
    fun `version with fewer segments treated as zero-padded`() {
        assertEquals(0, ScriptVersion("1.0").compareTo(ScriptVersion("1.0.0")))
    }

    @Test
    fun `version with more segments is greater when prefix matches`() {
        assertTrue(ScriptVersion("1.0.0.1") > ScriptVersion("1.0.0"))
    }

    @Test
    fun `handles real SVP version comparison`() {
        assertTrue(ScriptVersion("0.5.0") > ScriptVersion("0.4.1"))
    }

    @Test
    fun `handles real CUI version with large numbers`() {
        assertTrue(ScriptVersion("26.1.7") > ScriptVersion("1.14.82"))
    }

    @Test
    fun `handles real EUI version`() {
        assertTrue(ScriptVersion("8.1.0") > ScriptVersion("8.0.9"))
    }

    @Test
    fun `handles non-numeric segments as zero`() {
        assertEquals(0, ScriptVersion("1.0.0-beta").compareTo(ScriptVersion("1.0.0")))
    }

    @Test
    fun `single segment versions compare correctly`() {
        assertTrue(ScriptVersion("2") > ScriptVersion("1"))
    }
}
