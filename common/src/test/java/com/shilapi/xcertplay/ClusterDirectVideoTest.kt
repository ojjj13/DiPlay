package com.shilapi.xcertplay

import android.content.Intent
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.shilapi.xcertplay.airplay.SafeAreaRect
import com.shilapi.xcertplay.media.AndroidMediaSink
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.util.ReflectionHelpers
import java.util.concurrent.CopyOnWriteArraySet

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class ClusterDirectVideoTest {
    @After fun cleanup() {
        ClusterActivityOutput.stopForSettings()
        AirPlayPersistence.saveAdbClusterSurfaceView(RuntimeEnvironment.getApplication(), false)
    }

    @Test fun directVideoKeepsWaitingCoverTurnCardAndSafeAreaPreviewAboveVideo() {
        val app = RuntimeEnvironment.getApplication()
        AirPlayPersistence.saveAdbClusterEnabled(app, true)
        AirPlayPersistence.saveAdbClusterSurfaceView(app, true)
        val host = Any()
        ClusterActivityOutput.bind(host, 42) { }
        val token = "01234567-89ab-cdef-0123-456789abcdef"
        ReflectionHelpers.setField(ClusterActivityOutput, "launchToken", token)
        ReflectionHelpers.setField(ClusterActivityOutput, "expectedDisplay", 7)
        val controller = Robolectric.buildActivity(AdbClusterActivity::class.java,
            Intent(app, AdbClusterActivity::class.java).putExtra("cluster_launch_token", token)).create()
        val activity = controller.get()
        try {
            assertFalse(activity.isFinishing)
            assertTrue(ClusterActivityOutput.confirm(activity, token, 7))
            val root = activity.findViewById<FrameLayout>(android.R.id.content).getChildAt(0) as FrameLayout
            assertTrue(root.getChildAt(0) is ClusterVideoSurface)
            assertTrue(root.getChildAt(1) is TextView)
            assertTrue(root.getChildAt(2) is ClusterTurnCardView)
            assertTrue(root.getChildAt(3) is SafeAreaEditorView)
            assertEquals(View.VISIBLE, root.getChildAt(1).visibility)
            ClusterActivityOutput.setStreamActive(true)
            assertEquals(View.GONE, root.getChildAt(1).visibility)
            ClusterActivityOutput.setStreamActive(false)
            assertEquals(View.VISIBLE, root.getChildAt(1).visibility)
            val editor = Any()
            ClusterActivityOutput.beginSafeAreaPreview(editor, SafeAreaRect(300, 100, 1500, 620))
            assertEquals(View.VISIBLE, root.getChildAt(3).visibility)
            ClusterActivityOutput.endSafeAreaPreview(editor)
            assertEquals(View.GONE, root.getChildAt(3).visibility)
        } finally {
            ClusterActivityOutput.stop(host)
            controller.destroy()
        }
    }

    @Test fun holderDestructionWaitsForLiveAndRetiringDecodersBeforeReturning() {
        val activity = Robolectric.buildActivity(CarPlayHostActivity::class.java).get()
        val live = mock(AndroidMediaSink::class.java)
        val retiring = mock(AndroidMediaSink::class.java)
        val liveDetach = mock(AndroidMediaSink.SurfaceDetach::class.java)
        val retiringDetach = mock(AndroidMediaSink.SurfaceDetach::class.java)
        val surface = mock(Surface::class.java)
        `when`(live.beginSurfaceDetach(surface, false)).thenReturn(liveDetach)
        `when`(retiring.beginSurfaceDetach(surface, false)).thenReturn(retiringDetach)
        `when`(liveDetach.await()).thenReturn(true)
        `when`(retiringDetach.await()).thenReturn(true)
        ReflectionHelpers.setField(activity, "sink", live)
        ReflectionHelpers.setField(activity, "clusterSurface", surface)
        ReflectionHelpers.setField(activity, "clusterSurfaceUsesHolder", true)
        val sinks = ReflectionHelpers.getField<CopyOnWriteArraySet<AndroidMediaSink>>(activity, "retiringSinks")
        sinks += retiring
        activity.javaClass.getDeclaredMethod("onClusterSurface", Surface::class.java)
            .apply { isAccessible = true }.invoke(activity, null)
        val ordered = inOrder(live, retiring, liveDetach, retiringDetach)
        ordered.verify(live).beginSurfaceDetach(surface, false)
        ordered.verify(retiring).beginSurfaceDetach(surface, false)
        ordered.verify(liveDetach).await()
        ordered.verify(retiringDetach).await()
        ordered.verify(live).clearSurface(111, surface)
        assertNull(ReflectionHelpers.getField<Surface?>(activity, "clusterSurface"))
        verify(surface, never()).release()
    }
}
