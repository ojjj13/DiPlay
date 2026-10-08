package com.shilapi.xcertplay.media

import android.media.MediaFormat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class DecoderColorOptionsTest {
    @Test fun bt709OverridePreservesRangeAndTransferAndSkipsNonQualcommVpp() {
        val format = MediaFormat.createVideoFormat("video/avc", 1920, 720).apply {
            setInteger(MediaFormat.KEY_COLOR_STANDARD, 86)
            setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_FULL)
            setInteger(MediaFormat.KEY_COLOR_TRANSFER, MediaFormat.COLOR_TRANSFER_SDR_VIDEO)
        }
        assertNull(DecoderColorOptions.apply(format, "OMX.MTK.VIDEO.DECODER.AVC", true, true, null))
        assertEquals(MediaFormat.COLOR_STANDARD_BT709, format.getInteger(MediaFormat.KEY_COLOR_STANDARD))
        assertEquals(MediaFormat.COLOR_RANGE_FULL, format.getInteger(MediaFormat.KEY_COLOR_RANGE))
        assertEquals(MediaFormat.COLOR_TRANSFER_SDR_VIDEO, format.getInteger(MediaFormat.KEY_COLOR_TRANSFER))
        assertFalse(format.containsKey(DecoderColorOptions.VPP_MODE_KEY))
    }

    @Test fun legacyQualcommRequestsAutoAndOffWithoutInventingColorMetadata() {
        for (name in listOf("OMX.qcom.video.decoder.avc", "OMX.qti.video.decoder.hevc", "c2.qti.avc.decoder")) {
            for (enabled in listOf(false, true)) {
                val format = MediaFormat.createVideoFormat("video/avc", 1920, 720)
                val expected = if (enabled) "HQV_MODE_AUTO" else "HQV_MODE_OFF"
                assertEquals(expected, DecoderColorOptions.apply(format, name, false, enabled, null))
                assertEquals(expected, format.getString(DecoderColorOptions.VPP_MODE_KEY))
                assertFalse(format.containsKey(MediaFormat.KEY_COLOR_STANDARD))
                assertFalse(format.containsKey(MediaFormat.KEY_COLOR_RANGE))
                assertFalse(format.containsKey(MediaFormat.KEY_COLOR_TRANSFER))
            }
        }
    }

    @Test fun advertisedUnsupportedVppIsSkippedButBt709StillApplies() {
        val format = MediaFormat.createVideoFormat("video/avc", 1920, 720)
        assertNull(DecoderColorOptions.apply(format, "c2.qti.avc.decoder", true, true, emptyList()))
        assertFalse(format.containsKey(DecoderColorOptions.VPP_MODE_KEY))
        assertEquals(MediaFormat.COLOR_STANDARD_BT709, format.getInteger(MediaFormat.KEY_COLOR_STANDARD))
    }
}
