package com.shilapi.xcertplay

import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.View
import com.shilapi.xcertplay.media.VideoColorDiagnostics

/** Framework-visible composition state; this cannot inspect HWC's actual chosen dataspace. */
internal object VideoCompositionDiagnostics {
    fun log(event: String, view: View?, surface: Surface? = null, detail: String = ""): String {
        val line = runCatching {
            val display = view?.display
            val hdr = if (Build.VERSION.SDK_INT >= 24) display?.hdrCapabilities?.supportedHdrTypes?.joinToString(",") else null
            val wide = if (Build.VERSION.SDK_INT >= 26) display?.isWideColorGamut else null
            val texture = view as? android.view.TextureView
            "Test25 composition event=$event view=${view?.javaClass?.simpleName} " +
                "viewId=${view?.let { System.identityHashCode(it).toString(16) }} " +
                "size=${view?.width}x${view?.height} hwAccelerated=${view?.isHardwareAccelerated} " +
                "layerType=${view?.layerType} alpha=${view?.alpha} textureOpaque=${texture?.isOpaque} " +
                "display=${display?.displayId} flags=${display?.flags} refresh=${display?.refreshRate} " +
                "mode=${display?.mode} hdrTypes=$hdr wideGamut=$wide " +
                "surface=${VideoColorDiagnostics.surface(surface)} $detail " +
                "[framework capabilities only; actual SurfaceFlinger/HWC dataspace unavailable to app]"
        }.getOrElse { "Test25 composition event=$event snapshot unavailable=${it.javaClass.simpleName}" }
        Log.i("xcertplay-usb", line)
        return line
    }
}
