package com.shilapi.xcertplay

import com.shilapi.xcertplay.airplay.CarPlayClusterDisplay

/** Candidate measured on the 2022 Seal / DiLink 4.0; physical routing still needs a car test. */
internal object DiLink4ClusterDisplay {
    const val NAME = "fission_bg_xdjaVirtualSurface"

    // Exact name and geometry only. Do not select arbitrary virtual or passenger displays.
    fun matches(name: String, width: Int, height: Int): Boolean =
        name == NAME && width == 1920 && height == 720

    fun streamConfig(content: CarPlayClusterDisplay.Content) =
        CarPlayClusterDisplay.config(1920, 720, scalePercent = 100, content = content)
            .copy(safeArea = null)
}

