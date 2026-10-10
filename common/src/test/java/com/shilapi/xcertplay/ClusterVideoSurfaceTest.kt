package com.shilapi.xcertplay

import android.view.Surface
import android.view.SurfaceHolder
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSurfaceView

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class ClusterVideoSurfaceTest {
    @Test fun destructionDetachesSynchronouslyAndNeverReleasesFrameworkSurface() {
        val events = mutableListOf<Surface?>()
        val view = ClusterVideoSurface(RuntimeEnvironment.getApplication()) { events += it }
        val callback = (view.holder as ShadowSurfaceView.FakeSurfaceHolder).callbacks.single()
        val surface = mock(Surface::class.java)
        val holder = mock(SurfaceHolder::class.java)
        `when`(holder.surface).thenReturn(surface)
        callback.surfaceCreated(holder)
        assertSame(surface, events.single())
        callback.surfaceDestroyed(holder)
        assertEquals(listOf(surface, null), events)
        verify(surface, never()).release()

        // Recreating the holder must publish it again, and close must detach exactly once.
        callback.surfaceCreated(holder)
        view.close()
        view.close()
        callback.surfaceDestroyed(holder)
        callback.surfaceCreated(holder) // A queued callback after close cannot reattach the decoder.
        assertEquals(listOf(surface, null, surface, null), events)
        verify(surface, never()).release()
        assertTrue((view.holder as ShadowSurfaceView.FakeSurfaceHolder).callbacks.isEmpty())
    }

    @Test fun surfaceReplacementDetachesOldOutputBeforePublishingNewOne() {
        val events = mutableListOf<Surface?>()
        val view = ClusterVideoSurface(RuntimeEnvironment.getApplication()) { events += it }
        val callback = (view.holder as ShadowSurfaceView.FakeSurfaceHolder).callbacks.single()
        val first = mock(Surface::class.java)
        val next = mock(Surface::class.java)
        val holder = mock(SurfaceHolder::class.java)
        `when`(holder.surface).thenReturn(first, next)
        try {
            callback.surfaceCreated(holder)
            callback.surfaceCreated(holder)
            assertEquals(listOf(first, null, next), events)
            verify(first, never()).release()
        } finally { view.close() }
    }
}
