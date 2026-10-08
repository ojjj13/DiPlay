package com.shilapi.xcertplay.media

import android.media.MediaFormat

/** Qualcomm's optional VPP control. Values request processing, not proof it ran. */
internal object DecoderColorOptions {
    const val VPP_MODE_KEY = "vendor.qti-ext-vpp.mode"

    fun isQualcommHardware(name: String): Boolean =
        name.startsWith("OMX.qcom.", ignoreCase = true) ||
            name.startsWith("OMX.qti.", ignoreCase = true) ||
            name.startsWith("c2.qti.", ignoreCase = true)

    fun vppMode(enabled: Boolean): String = if (enabled) "HQV_MODE_AUTO" else "HQV_MODE_OFF"

    fun apply(format: MediaFormat, codecName: String, forceBt709: Boolean,
              vppEnabled: Boolean, supportedVendorParameters: List<String>?): String? {
        if (forceBt709) format.setInteger(MediaFormat.KEY_COLOR_STANDARD, MediaFormat.COLOR_STANDARD_BT709)
        if (!isQualcommHardware(codecName)) return null
        if (supportedVendorParameters != null && VPP_MODE_KEY !in supportedVendorParameters) return null
        return vppMode(vppEnabled).also { format.setString(VPP_MODE_KEY, it) }
    }
}
