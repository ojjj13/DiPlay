package com.shilapi.xcertplay

import com.shilapi.xcertplay.airplay.CarPlayClusterDisplay
import org.junit.Assert.*
import org.junit.Test

class DiLink4ClusterDisplayTest {
    @Test fun nativeStreamKeepsSelectedContentWithoutDilink5Crop() {
        for (content in CarPlayClusterDisplay.Content.entries) {
            val config = DiLink4ClusterDisplay.streamConfig(content)
            assertEquals(1920, config.widthPixels)
            assertEquals(720, config.heightPixels)
            assertEquals(content.url, config.initialUrl)
            assertNull(config.safeArea)
        }
    }

    @Test fun similarDisplayNamesAreNotAccepted() {
        assertFalse(DiLink4ClusterDisplay.matches("shared_${DiLink4ClusterDisplay.NAME}_0", 1920, 720))
        assertFalse(DiLink4ClusterDisplay.matches("Passenger display", 1920, 720))
    }
}
