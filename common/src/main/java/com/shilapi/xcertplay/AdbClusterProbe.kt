package com.shilapi.xcertplay

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.shilapi.xcertplay.adb.AdbKeys
import com.shilapi.xcertplay.adb.LocalAdb
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** User-triggered, bounded probe; never runs automatically or changes BYD projection modes. */
internal object AdbClusterProbe {
    val running = AtomicBoolean(false)
    private const val REPORT = "adb-cluster-probe.txt"
    internal fun quote(value: String) = "'" + value.replace("'", "'\\''") + "'"
    internal fun command(apk: String) =
        "CLASSPATH=${quote(apk)} app_process /system/bin ${AdbClusterProbeTool::class.java.name}"

    fun run(context: Context): String {
        val result = buildString {
            appendLine("ADB cluster probe capturedAt=${java.util.Date()}")
            try {
                LocalAdb(AdbKeys.load(context)).use { adb ->
                    val access = adb.connect(mayAsk = true)
                    appendLine("adbAccess=$access")
                    if (access == LocalAdb.Access.READY) {
                        appendLine("--- ADB helper display/surface access ---")
                        appendLine(adb.shell(command(context.applicationInfo.sourceDir)) ?: "helperTransportFailed=true")
                        appendLine("--- ADB display service dump ---")
                        val dump = adb.shell("dumpsys display")
                        appendLine(dump?.take(96_000) ?: "displayDumpFailed=true")
                        if (dump != null && dump.length > 96_000) appendLine("displayDumpTruncated=true")
                        appendLine("--- Display-related service names ---")
                        appendLine(adb.shell("service list")?.lineSequence()?.filter {
                            it.contains("display", true) || it.contains("SurfaceFlinger", true)
                        }?.joinToString("\n") ?: "serviceListFailed=true")
                    }
                }
            } catch (error: Exception) {
                appendLine("probeError=${error.javaClass.simpleName}: ${error.message}")
            }
        }
        File(context.filesDir, REPORT).writeText(result)
        return result
    }

    fun report(context: Context): String = File(context.filesDir, REPORT).let {
        if (it.isFile) it.readText() else "ADB cluster probe has not been run."
    }
}

/** Runs as the already-authorized ADB shell user, with ordinary Android access checks intact. */
object AdbClusterProbeTool {
    @JvmStatic
    @SuppressLint("PrivateApi")
    fun main(args: Array<String>) {
        // A crashed or unresponsive window service must not leave a helper behind.
        Thread({ Thread.sleep(8_000); println("helperTimeout=true"); System.exit(2) }, "cluster-probe-watchdog")
            .apply { isDaemon = true; start() }
        var presentation: Presentation? = null
        try {
            if (Looper.myLooper() == null) Looper.prepareMainLooper()
            val thread = Class.forName("android.app.ActivityThread")
            val main = thread.getMethod("systemMain").invoke(null)
            val system = thread.getMethod("getSystemContext").invoke(main) as Context
            val context = system.createPackageContext("com.android.shell", 0)
            println("helperUid=${Process.myUid()} package=${context.packageName}")
            val manager = context.getSystemService(DisplayManager::class.java)
            val displays = manager.displays
            for (display in displays) {
                val size = ClusterMapPresentation.sizeOf(display)
                println("helperDisplay=${display.displayId}:${display.name} ${size.x}x${size.y} flags=${display.flags} valid=${display.isValid}")
            }
            // Do not touch the main display, passenger displays, or guessed numeric IDs.
            val candidate = displays.firstOrNull {
                val size = ClusterMapPresentation.sizeOf(it)
                DiLink4ClusterDisplay.matches(it.name, size.x, size.y)
            }
            if (candidate == null) {
                println("helperCandidate=none")
                return
            }
            println("helperCandidate=${candidate.displayId}:${candidate.name}")
            presentation = Presentation(context, candidate)
            // Invisible probe: obtain a valid Surface without covering the stock cluster map.
            presentation.window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                attributes = attributes.apply { alpha = 0f }
            }
            val view = SurfaceView(presentation.context)
            view.holder.setFormat(PixelFormat.TRANSLUCENT)
            view.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    println("helperSurfaceValid=${holder.surface.isValid}")
                    Handler(Looper.getMainLooper()).post {
                        runCatching { presentation?.dismiss() }
                        println("helperFinished=true")
                        System.exit(0)
                    }
                }
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
                override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
            })
            presentation.setContentView(view)
            presentation.show()
            println("helperPresentationShown=true")
            Handler(Looper.getMainLooper()).postDelayed({
                println("helperSurfaceWaitExpired=true")
                runCatching { presentation?.dismiss() }
                println("helperFinished=true")
                System.exit(0)
            }, 3_000)
            Looper.loop()
        } catch (error: Throwable) {
            println("helperError=${error.javaClass.simpleName}: ${error.message}")
            error.cause?.let { println("helperCause=${it.javaClass.simpleName}: ${it.message}") }
        } finally {
            runCatching { presentation?.dismiss() }
            println("helperFinished=true")
            System.exit(0)
        }
    }
}
