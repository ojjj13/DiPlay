package com.shilapi.xcertplay

/** Initialization must be possible before the decoder can deliver its first frame. */
internal class ClusterRgbFrameState {
    @Volatile var isClosed = false
        private set
    @Volatile private var frameAvailable = false
    val canDraw: Boolean get() = !isClosed && frameAvailable

    fun beginInitialization(): Boolean {
        if (isClosed) return false
        frameAvailable = false
        return true
    }
    fun frameArrived(): Boolean {
        if (isClosed) return false
        frameAvailable = true
        return true
    }
    fun resetFrames() { frameAvailable = false }
    fun close() { isClosed = true; frameAvailable = false }
}
