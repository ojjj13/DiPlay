package com.shilapi.xcertplay

import com.shilapi.xcertplay.airplay.CarPlayClusterDisplay
import org.junit.Assert.*
import org.junit.Test

class DiLink4ClusterDisplayTest {
    @Test fun activitySizedStreamKeepsContentAndReusesDilink5SafeArea() {
        for (content in CarPlayClusterDisplay.Content.entries) {
            val config = DiLink4ClusterDisplay.streamConfig(content)
            assertEquals(1920, config.widthPixels)
            assertEquals(624, config.heightPixels)
            assertEquals(content.url, config.initialUrl)
            assertEquals(CarPlayClusterDisplay.config(1920, 624, scalePercent = 100, content = content).safeArea, config.safeArea)
            assertNotNull(config.safeArea)
            assertTrue(config.safeAreaDrawOutside)
        }
    }

    @Test fun similarDisplayNamesAreNotAccepted() {
        assertFalse(DiLink4ClusterDisplay.matches("shared_${DiLink4ClusterDisplay.NAME}_0", 1920, 720))
        assertFalse(DiLink4ClusterDisplay.matches("Passenger display", 1920, 720))
    }
}
