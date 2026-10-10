package com.shilapi.xcertplay

import android.os.SystemClock
import android.util.Log

internal object ClusterRgbDiagnostics {
    private val events = ArrayDeque<String>()
    private var framesReceived = 0L
    private var framesDrawn = 0L
    @Synchronized fun event(message: String) {
        val line = "${SystemClock.elapsedRealtime()}ms $message"
        if (events.size >= 24) events.removeFirst()
        events.addLast(line)
        Log.i("xcertplay-usb", "Test27.1 RGB $line")
    }
    @Synchronized fun reset() { framesReceived = 0; framesDrawn = 0; event("selected") }
    @Synchronized fun received() { framesReceived++ }
    @Synchronized fun drawn() {
        framesDrawn++
        if (framesDrawn == 1L) event("first RGB draw submitted")
    }
    @Synchronized fun report(): String =
        "RGB bridge framesReceived=$framesReceived framesDrawn=$framesDrawn\n" + events.joinToString("\n")
}
