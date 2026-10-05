package com.shilapi.xcertplay

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.shilapi.xcertplay.airplay.AirPlayIdentity
import com.shilapi.xcertplay.orchestration.CarPlayController
import com.shilapi.xcertplay.orchestration.CarPlayRuntimeConfig
import com.shilapi.xcertplay.orchestration.CarPlayTransport
import com.shilapi.xcertplay.orchestration.MfiTarget
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.util.concurrent.atomic.AtomicBoolean

/** USB charging must not change the user's selected transport or tear down its controller. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE, shadows = [HostUsbChargingTest.Bootstrap::class])
class HostUsbChargingTest {
    private val app get() = RuntimeEnvironment.getApplication()

    @Before fun reset() {
        app.getSharedPreferences("xcertplay_airplay", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun usbLaunchPreservesWirelessSelection() {
        AirPlayPersistence.saveWirelessEnabled(app, true)
        AirPlayPersistence.saveKeepWirelessOnUsb(app, true)
        val host = Robolectric.buildActivity(CarPlayHostActivity::class.java, usbIntent()).create()
        try {
            assertFalse(host.get().isFinishing)
            assertTrue(AirPlayPersistence.loadWirelessEnabled(app))
            assertEquals(CarPlayTransport.WIRELESS, runtimeConfig(host.get()).transport)
        } finally { host.destroy() }
    }

    @Test fun chargingPlugAndUnplugPreserveTheRunningWirelessController() {
        AirPlayPersistence.saveWirelessEnabled(app, true)
        AirPlayPersistence.saveKeepWirelessOnUsb(app, true)
        val host = Robolectric.buildActivity(CarPlayHostActivity::class.java).create()
        val controller = mock(CarPlayController::class.java)
        val field = CarPlayHostActivity::class.java.getDeclaredField("controller").apply { isAccessible = true }
        field.set(host.get(), controller)
        try {
            for (action in listOf(UsbManager.ACTION_USB_DEVICE_ATTACHED, UsbManager.ACTION_USB_DEVICE_DETACHED,
                UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                host.newIntent(usbIntent(action))
                assertSame(controller, field.get(host.get()))
                assertFalse(host.get().isFinishing)
                assertTrue(AirPlayPersistence.loadWirelessEnabled(app))
                assertEquals(CarPlayTransport.WIRELESS, runtimeConfig(host.get()).transport)
                val shuttingDown = CarPlayHostActivity::class.java.getDeclaredField("shuttingDown")
                    .apply { isAccessible = true }.get(host.get()) as AtomicBoolean
                assertFalse(shuttingDown.get())
            }
            verifyNoInteractions(controller)
        } finally {
            field.set(host.get(), null)
            host.destroy()
        }
    }

    @Test fun explicitCableSelectionStillUsesWiredTransport() {
        AirPlayPersistence.saveWirelessEnabled(app, false)
        AirPlayPersistence.saveKeepWirelessOnUsb(app, true)
        val host = Robolectric.buildActivity(CarPlayHostActivity::class.java, usbIntent()).create()
        try {
            host.newIntent(usbIntent())
            assertFalse(host.get().isFinishing)
            assertFalse(AirPlayPersistence.loadWirelessEnabled(app))
            assertEquals(CarPlayTransport.WIRED, runtimeConfig(host.get()).transport)
        } finally { host.destroy() }
    }

    @Test fun usbLaunchKeepsAutomaticWiredTakeoverByDefault() {
        assertFalse(AirPlayPersistence.loadKeepWirelessOnUsb(app))
        AirPlayPersistence.saveWirelessEnabled(app, true)
        val host = Robolectric.buildActivity(CarPlayHostActivity::class.java, usbIntent()).create()
        try {
            assertFalse(AirPlayPersistence.loadWirelessEnabled(app))
            assertEquals(CarPlayTransport.WIRED, runtimeConfig(host.get()).transport)
        } finally { host.destroy() }
    }

    @Test fun chargingPreferenceCanBeDisabledAgain() {
        AirPlayPersistence.saveKeepWirelessOnUsb(app, true)
        assertTrue(AirPlayPersistence.loadKeepWirelessOnUsb(app))
        AirPlayPersistence.saveKeepWirelessOnUsb(app, false)
        assertFalse(AirPlayPersistence.loadKeepWirelessOnUsb(app))
    }

    private fun usbIntent(action: String = UsbManager.ACTION_USB_DEVICE_ATTACHED): Intent {
        val device = mock(UsbDevice::class.java)
        `when`(device.vendorId).thenReturn(0x05ac)
        return Intent(app, CarPlayHostActivity::class.java).setAction(action)
            .putExtra(UsbManager.EXTRA_DEVICE, device)
    }

    private fun runtimeConfig(host: CarPlayHostActivity): CarPlayRuntimeConfig =
        CarPlayHostActivity::class.java.getDeclaredMethod("createRuntimeConfig")
            .apply { isAccessible = true }.invoke(host) as CarPlayRuntimeConfig

    @Implements(DiPlayBootstrap::class, isInAndroidSdk = false)
    internal class Bootstrap {
        @Implementation fun ensure(context: Context, mfiTarget: MfiTarget) = Unit
        @Implementation fun deviceId(identity: AirPlayIdentity): String = "02:00:00:00:00:01"
    }
}
