package com.shilapi.xcertplay

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

/** An independent video layer; ordinary sibling views still draw the waiting screen and overlays. */
internal class ClusterVideoSurface(context: Context, private val onSurface: (Surface?) -> Unit) :
    SurfaceView(context), java.io.Closeable {
    private var output: Surface? = null
    private var closed = false
    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            if (closed) return
            detachOutput()
            output = holder.surface
            onSurface(output)
            VideoCompositionDiagnostics.log("cluster-surface-created", this@ClusterVideoSurface, output,
                "buffer=1920x720 pictureAdjustments=false")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            if (!closed) VideoCompositionDiagnostics.log("cluster-surface-changed", this@ClusterVideoSurface,
                output, "buffer=${width}x$height format=$format")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) = detachOutput()
    }

    init {
        // Leave the surface below the app window so the black placeholder, turn card and calibration
        // editor remain visible. Do not use setZOrderOnTop or fractional surface alpha on Android 10.
        holder.setFixedSize(DiLink4ClusterDisplay.STREAM_WIDTH, DiLink4ClusterDisplay.STREAM_HEIGHT)
        holder.addCallback(callback)
    }

    private fun detachOutput() {
        if (output == null) return
        // The host synchronously waits for all codec users before this callback returns. The framework
        // owns this Surface; unlike a TextureView wrapper, it must never be released by this view.
        onSurface(null)
        output = null
    }

    override fun close() {
        if (closed) return
        closed = true
        holder.removeCallback(callback)
        detachOutput()
    }
}
