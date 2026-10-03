package com.shilapi.xcertplay

import android.hardware.display.DisplayManager
import android.view.Display
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDisplayManager
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class ClusterMapPresentationTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val manager get() = context.getSystemService(DisplayManager::class.java)

    private fun display(name: String, spec: String = "w960dp-h360dp"): Int {
        // Display.TYPE_VIRTUAL (5) is hidden from the public Android SDK.
        val id = ShadowDisplayManager.addDisplay(spec, 5)
        shadowOf(manager.getDisplay(id)).apply {
            setName(name)
            setFlags(Display.FLAG_PRESENTATION)
        }
        return id
    }

    @Test fun legacyFirmwareKeepsOriginalBaseDisplayPreference() {
        val base = display("fission_bg_XDJAScreenProjection")
        val shared = display("shared_fission_bg_XDJAScreenProjection_0")
        try {
            assertEquals(base, ClusterMapPresentation.findDisplay(context)?.displayId)
        } finally {
            ShadowDisplayManager.removeDisplay(shared)
            ShadowDisplayManager.removeDisplay(base)
        }
    }

    @Test fun baseDisplayStillWorksWhenNoSharedLayerExists() {
        val base = display("fission_bg_XDJAScreenProjection")
        try {
            assertEquals(base, ClusterMapPresentation.findDisplay(context)?.displayId)
        } finally {
            ShadowDisplayManager.removeDisplay(base)
        }
    }

    @Test fun unrelatedPresentationDisplayIsNotUsedForTheCluster() {
        val other = display("Passenger display")
        try {
            assertNull(ClusterMapPresentation.findDisplay(context))
        } finally {
            ShadowDisplayManager.removeDisplay(other)
        }
    }
    @Test fun dilink4MeasuredProjectionIsSelected() {
        val id = display(DiLink4ClusterDisplay.NAME, "w1920dp-h720dp-mdpi")
        try {
            assertEquals(id, ClusterMapPresentation.findDisplay(context)?.displayId)
        } finally {
            ShadowDisplayManager.removeDisplay(id)
        }
    }

    @Test fun dilink4WrongGeometryIsRejected() {
        val id = display(DiLink4ClusterDisplay.NAME, "w1280dp-h720dp-mdpi")
        try {
            assertNull(ClusterMapPresentation.findDisplay(context))
        } finally {
            ShadowDisplayManager.removeDisplay(id)
        }
    }

    @Test fun dilink4RequiresPresentationFlag() {
        val id = display(DiLink4ClusterDisplay.NAME, "w1920dp-h720dp-mdpi")
        shadowOf(manager.getDisplay(id)).setFlags(0)
        try {
            assertNull(ClusterMapPresentation.findDisplay(context))
        } finally {
            ShadowDisplayManager.removeDisplay(id)
        }
    }

    @Test fun existingDilink5DisplayKeepsPriority() {
        val legacy = display(DiLink4ClusterDisplay.NAME, "w1920dp-h720dp-mdpi")
        val current = display(DiLink51ClusterLayout.BASE)
        try {
            assertEquals(current, ClusterMapPresentation.findDisplay(context)?.displayId)
        } finally {
            ShadowDisplayManager.removeDisplay(current)
            ShadowDisplayManager.removeDisplay(legacy)
        }
    }

}
