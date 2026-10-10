package com.shilapi.xcertplay

import org.junit.Assert.*
import org.junit.Test

class ClusterRgbFrameStateTest {
    @Test fun initializationDoesNotWaitForTheFrameThatRequiresItsInputSurface() {
        val state = ClusterRgbFrameState()
        assertFalse(state.canDraw)
        assertTrue(state.beginInitialization())
        assertFalse(state.canDraw)
        assertTrue(state.frameArrived())
        assertTrue(state.canDraw)
    }
    @Test fun surfaceRecreationWaitsForANewFrameAndCloseRejectsLateCallbacks() {
        val state = ClusterRgbFrameState()
        assertTrue(state.beginInitialization())
        state.frameArrived()
        state.resetFrames()
        assertFalse(state.canDraw)
        assertTrue(state.beginInitialization())
        assertFalse(state.canDraw)
        state.frameArrived()
        assertTrue(state.canDraw)
        state.close()
        assertFalse(state.canDraw)
        assertFalse(state.beginInitialization())
        assertFalse(state.frameArrived())
    }
}
