package com.shilapi.xcertplay.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface

/** Read-only snapshots. Never dump CSD, decoded pixels or authentication payloads. */
object VideoColorDiagnostics {
    fun surface(surface: Surface?): String = if (surface == null) "none" else
        "id=${System.identityHashCode(surface).toString(16)} valid=${surface.isValid}"

    fun format(format: MediaFormat): String = listOf(
        "mime", "width", "height", "crop-left", "crop-top", "crop-right", "crop-bottom",
        "stride", "slice-height", "color-format", "color-standard", "color-range", "color-transfer",
        "profile", "level", "bit-depth", "bit-depth-luma", "bit-depth-chroma", "priority",
        "operating-rate", "low-latency", "rotation-degrees",
    ).joinToString(" ") { key ->
        val value = if (!format.containsKey(key)) "absent" else runCatching {
            if (key == "mime") format.getString(key).toString() else format.getInteger(key).toString()
        }.getOrElse { "unreadable" }
        "$key=$value"
    } + " hdrStaticInfoPresent=${format.containsKey("hdr-static-info")} " +
        "[standard:1=BT709,2=BT601PAL,4=BT601NTSC,6=BT2020;range:1=full,2=limited;" +
        "transfer:1=linear,3=SDR,6=ST2084,7=HLG;other values remain raw]"

    fun codec(codec: MediaCodec, mime: String): String {
        val info = codec.codecInfo
        val flags = if (Build.VERSION.SDK_INT >= 29)
            "hardware=${info.isHardwareAccelerated} software=${info.isSoftwareOnly} vendor=${info.isVendor}"
        else "hardware/software/vendor=unavailable"
        val formats = info.getCapabilitiesForType(mime).colorFormats.joinToString(",")
        return "name=${codec.name} $flags supportedColorFormats=$formats api=${Build.VERSION.SDK_INT} " +
            "manufacturer=${Build.MANUFACTURER} model=${Build.MODEL} hardware=${Build.HARDWARE}"
    }
}
