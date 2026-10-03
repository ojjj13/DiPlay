package com.shilapi.xcertplay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbClusterProbeTest {
    @Test fun apkPathIsOneShellArgumentEvenWithMetacharacters() {
        assertEquals("'/data/app/a b/\$literal.apk'", AdbClusterProbe.quote("/data/app/a b/\$literal.apk"))
        assertEquals("'a'\\''b'", AdbClusterProbe.quote("a'b"))
    }

    @Test fun helperCommandUsesOnlyThePackagedProbeEntryPoint() {
        val command = AdbClusterProbe.command("/data/app/test/base.apk")
        assertTrue(command.startsWith("CLASSPATH='/data/app/test/base.apk' app_process /system/bin "))
        assertTrue(command.endsWith("com.shilapi.xcertplay.AdbClusterProbeTool"))
    }
}
