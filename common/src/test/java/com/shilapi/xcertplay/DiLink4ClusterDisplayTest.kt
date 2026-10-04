package com.shilapi.xcertplay

import com.shilapi.xcertplay.airplay.CarPlayClusterDisplay
import org.junit.Assert.*
import org.junit.Test

class DiLink4ClusterDisplayTest {
    @Test fun fullPanelStreamKeepsContentAndReusesDilink5SafeArea() {
        for (content in CarPlayClusterDisplay.Content.entries) {
            val config = DiLink4ClusterDisplay.streamConfig(content)
            assertEquals(1920, config.widthPixels)
            assertEquals(720, config.heightPixels)
            assertEquals(content.url, config.initialUrl)
            assertEquals(CarPlayClusterDisplay.config(1920, 720, scalePercent = 100, content = content).safeArea, config.safeArea)
            assertNotNull(config.safeArea)
            assertEquals(true, config.safeAreaDrawOutside)
        }
    }

    @Test fun savedMarkerOffsetsMoveSafeAreaWithoutChangingResolution() {
        val centered = DiLink4ClusterDisplay.streamConfig(CarPlayClusterDisplay.Content.MAP)
        val shifted = DiLink4ClusterDisplay.streamConfig(CarPlayClusterDisplay.Content.MAP, 1, -1)
        assertEquals(1920, shifted.widthPixels)
        assertEquals(720, shifted.heightPixels)
        assertNotEquals(centered.safeArea, shifted.safeArea)
        assertEquals(CarPlayClusterDisplay.config(1920, 720, 100, 1, -1).safeArea, shifted.safeArea)
    }

    @Test fun similarDisplayNamesAreNotAccepted() {
        assertFalse(DiLink4ClusterDisplay.matches("shared_${DiLink4ClusterDisplay.NAME}_0", 1920, 720))
        assertFalse(DiLink4ClusterDisplay.matches("Passenger display", 1920, 720))
    }
}
