package com.github.wrager.sbguserscripts.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClipboardBridgeTest {

    private lateinit var context: Context
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var bridge: ClipboardBridge

    @Before
    fun setUp() {
        context = mockk()
        clipboardManager = mockk()
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager
        bridge = ClipboardBridge(context)
    }

    @Test
    fun `readText returns text from clipboard`() {
        val clipItem = mockk<ClipData.Item>()
        val clipData = mockk<ClipData>()
        every { clipItem.text } returns "hello"
        every { clipData.getItemAt(0) } returns clipItem
        every { clipboardManager.primaryClip } returns clipData

        assertEquals("hello", bridge.readText())
    }

    @Test
    fun `readText returns empty string when clipboard is empty`() {
        every { clipboardManager.primaryClip } returns null

        assertEquals("", bridge.readText())
    }

    @Test
    fun `writeText puts text into clipboard`() {
        val clipDataSlot = slot<ClipData>()
        every { clipboardManager.setPrimaryClip(capture(clipDataSlot)) } returns Unit

        bridge.writeText("world")

        verify { clipboardManager.setPrimaryClip(any()) }
        assertEquals("world", clipDataSlot.captured.getItemAt(0).text)
    }
}
