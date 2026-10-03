package com.shilapi.xcertplay

import android.view.Surface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class ClusterActivityOutputTest {
    @Test fun surfaceRecreationRejectsStaleDetachAndRebindsNewHost() {
        val host = Any()
        val oldActivity = Any()
        val newActivity = Any()
        val first = Surface()
        val second = Surface()
        val events = mutableListOf<Surface?>()
        try {
            ClusterActivityOutput.bind(host, 4) { events.add(it) }
            ClusterActivityOutput.attach(oldActivity, first)
            ClusterActivityOutput.attach(newActivity, second)
            ClusterActivityOutput.detach(oldActivity, first)
            assertSame(second, ClusterActivityOutput.surface)
            assertSame(second, events.last())
            val newHost = Any()
            var rebound: Surface? = null
            ClusterActivityOutput.bind(newHost, 5) { rebound = it }
            assertSame(second, rebound)
            ClusterActivityOutput.stop(host)
            assertSame(second, ClusterActivityOutput.surface)
            ClusterActivityOutput.detach(newActivity, second)
            assertNull(ClusterActivityOutput.surface)
            ClusterActivityOutput.stop(newHost)
        } finally {
            first.release(); second.release()
        }
    }
}
