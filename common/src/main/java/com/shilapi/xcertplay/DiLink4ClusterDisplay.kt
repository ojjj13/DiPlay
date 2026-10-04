package com.shilapi.xcertplay

import com.shilapi.xcertplay.airplay.CarPlayClusterDisplay

/** 2022 Seal / DiLink 4.0: 1920x720 logical display, 1920x624 observed activity area. */
internal object DiLink4ClusterDisplay {
    const val NAME = "fission_bg_xdjaVirtualSurface"

    // Exact name and geometry only. Do not select arbitrary virtual or passenger displays.
    fun matches(name: String, width: Int, height: Int): Boolean =
        name == NAME && width == 1920 && height == 720

    const val STREAM_WIDTH = 1920
    const val STREAM_HEIGHT = 720

    // Reuse DiLink 5 marker-safe margins as a calibration starting point.
    // Draw outside remains enabled so the map background still fills the activity.
    fun streamConfig(content: CarPlayClusterDisplay.Content, horizontalStep: Int = 0, verticalStep: Int = 0) =
        CarPlayClusterDisplay.config(STREAM_WIDTH, STREAM_HEIGHT, scalePercent = 100,
            horizontalStep = horizontalStep, verticalStep = verticalStep, content = content)
}
