package com.shilapi.xcertplay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.TextureView

/** Main-screen pixel adjustments; decoder, backlight and cluster output are unaffected. */
internal object CarPlayPicture {
    const val BRIGHTNESS = "brightness"
    const val CONTRAST = "contrast"
    const val SATURATION = "saturation"
    const val WARMTH = "warmth"

    fun preferences(context: Context): SharedPreferences =
        context.getSharedPreferences("carplay_picture", Context.MODE_PRIVATE)

    fun defaultValue(key: String): Int = if (key == CONTRAST || key == SATURATION) 100 else 0
    fun range(key: String): IntRange = when (key) {
        BRIGHTNESS -> -50..50
        WARMTH -> -100..100
        else -> 0..200
    }
    fun value(prefs: SharedPreferences, key: String): Int =
        prefs.getInt(key, defaultValue(key)).coerceIn(range(key))

    fun matrix(prefs: SharedPreferences): ColorMatrix {
        val contrast = value(prefs, CONTRAST) / 100f
        val brightness = value(prefs, BRIGHTNESS) * 255f / 100f
        val offset = 127.5f * (1f - contrast) + brightness
        val warmth = value(prefs, WARMTH) / 100f * 0.2f
        return ColorMatrix().apply {
            setSaturation(value(prefs, SATURATION) / 100f)
            postConcat(ColorMatrix(floatArrayOf(
                contrast, 0f, 0f, 0f, offset,
                0f, contrast, 0f, 0f, offset,
                0f, 0f, contrast, 0f, offset,
                0f, 0f, 0f, 1f, 0f,
            )))
            postConcat(ColorMatrix().apply { setScale(1f + warmth, 1f, 1f - warmth, 1f) })
        }
    }

    fun apply(view: TextureView, prefs: SharedPreferences) {
        val neutral = listOf(BRIGHTNESS, CONTRAST, SATURATION, WARMTH)
            .all { value(prefs, it) == defaultValue(it) }
        view.setLayerPaint(if (neutral) null else Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix(prefs))
        })
    }
}
