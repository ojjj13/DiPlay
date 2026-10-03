package com.shilapi.xcertplay

import org.junit.Assert.*
import org.junit.Test

class AdbClusterRouterTest {
    private fun record(id: Int = 7, size: String = "1920 x 720", owner: String = "com.xdja.containerservice") =
        "mBaseDisplayInfo=DisplayInfo{\"fission_bg_xdjaVirtualSurface, displayId $id\", real $size, owner $owner (uid 1000)}"

    @Test fun usesCurrentLogicalDisplayIdRatherThanLayerStackOrAssumedOne() {
        val dump = "mCurrentLayerStack=1\nDisplay 7:\n" + record() + "\nmOverrideDisplayInfo=" + record()
        assertEquals(7, AdbClusterRouter.displayId(dump))
    }
    @Test fun rejectsMainPassengerWrongGeometryAndAmbiguousTargets() {
        assertNull(AdbClusterRouter.displayId(record(0)))
        assertNull(AdbClusterRouter.displayId(record(size = "1280 x 720")))
        assertNull(AdbClusterRouter.displayId(record(owner = "com.passenger")))
        assertNull(AdbClusterRouter.displayId(record() + "\n" + record(8)))
        assertNull(AdbClusterRouter.displayId(record().replace("mBaseDisplayInfo", "mOverrideDisplayInfo")))
    }
    @Test fun refusesSharedStackAndWrongTaskIdentity() {
        val component = "com.shihab.diplay.hudtest/com.shilapi.xcertplay.AdbClusterActivity"
        val own = AdbClusterRouteTool.Stack(9, 0, listOf(3), listOf(component))
        assertTrue(AdbClusterRouteTool.isolated(own, 3, component))
        assertFalse(AdbClusterRouteTool.isolated(own.copy(tasks = listOf(3, 4), names = listOf(component, "stock/map")), 3, component))
        assertFalse(AdbClusterRouteTool.isolated(own, 4, component))
        assertFalse(AdbClusterRouteTool.isolated(own.copy(names = listOf("stock/map")), 3, component))
    }
}
