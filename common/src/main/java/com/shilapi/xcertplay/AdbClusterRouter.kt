package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.adb.AdbKeys
import com.shilapi.xcertplay.adb.LocalAdb
import java.io.File

/** Strictly measured DiLink 4 target, discovered afresh through the authorized shell. */
internal object AdbClusterRouter {
    private const val REPORT = "adb-cluster-route.txt"
    data class Result(val success: Boolean, val report: String)

    // Match only the base logical display, not a device's layer-stack number or override record.
    internal fun displayId(dump: String): Int? {
        val candidates = dump.lineSequence().mapNotNull { line ->
            if (!line.contains("mBaseDisplayInfo=DisplayInfo{\"${DiLink4ClusterDisplay.NAME}, displayId ") ||
                !Regex("\\breal 1920 x 720\\b").containsMatchIn(line) ||
                !line.contains("owner com.xdja.containerservice (uid 1000)")) return@mapNotNull null
            Regex("displayId (\\d+)\"").find(line)?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it > 0 }
        }.distinct().toList()
        return candidates.singleOrNull()
    }

    internal fun command(apk: String, pkg: String, clusterTask: Int, hostTask: Int, display: Int): String =
        "CLASSPATH=${AdbClusterProbe.quote(apk)} app_process /system/bin ${AdbClusterRouteTool::class.java.name} " +
            "${AdbClusterProbe.quote(pkg)} $clusterTask $hostTask $display"

    fun route(context: Context, clusterTask: Int, hostTask: Int): Result {
        var success = false
        val text = buildString {
            appendLine("ADB cluster routing capturedAt=${java.util.Date()}")
            appendLine("clusterTask=$clusterTask mainTask=$hostTask")
            try {
                require(clusterTask >= 0 && hostTask >= 0 && clusterTask != hostTask) { "Separate live tasks required" }
                LocalAdb(AdbKeys.load(context)).use { adb ->
                    // Approval is offered only by an explicit settings action, never on connection.
                    val access = adb.connect(mayAsk = false)
                    appendLine("adbAccess=$access")
                    if (access != LocalAdb.Access.READY) return@use
                    val dump = adb.shell("dumpsys display") ?: error("Display dump unavailable")
                    val display = displayId(dump)
                    appendLine("routeTarget=${display ?: "none"}")
                    if (display == null) return@use
                    check(AirPlayPersistence.loadAdbClusterEnabled(context)) { "ADB cluster mode was disabled" }
                    val output = adb.shell(command(context.applicationInfo.sourceDir, context.packageName,
                        clusterTask, hostTask, display)) ?: error("Routing helper transport failed")
                    appendLine(output)
                    success = output.lineSequence().any { it == "routeSuccess=true" }
                }
            } catch (error: Exception) {
                appendLine("routeError=${error.javaClass.simpleName}: ${error.message}")
            }
        }
        File(context.filesDir, REPORT).writeText(text)
        return Result(success, text)
    }

    fun report(context: Context): String = File(context.filesDir, REPORT).let {
        if (it.isFile) it.readText() else "ADB cluster routing has not been run."
    }
}
