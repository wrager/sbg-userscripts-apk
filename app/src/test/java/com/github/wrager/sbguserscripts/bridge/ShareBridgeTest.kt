package com.github.wrager.sbguserscripts.bridge

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShareBridgeTest {

    private lateinit var context: Context
    private lateinit var bridge: ShareBridge

    @Before
    fun setUp() {
        context = mockk()
        bridge = ShareBridge(context)
    }

    @Test
    fun `open starts activity with correct URL`() {
        val intentSlot = slot<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        bridge.open("https://sbg-game.ru")

        verify { context.startActivity(any()) }
        assertEquals(Uri.parse("https://sbg-game.ru"), intentSlot.captured.data)
        assertEquals(Intent.ACTION_VIEW, intentSlot.captured.action)
    }

    @Test
    fun `open adds FLAG_ACTIVITY_NEW_TASK to intent`() {
        val intentSlot = slot<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        bridge.open("https://sbg-game.ru")

        assert(intentSlot.captured.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}
