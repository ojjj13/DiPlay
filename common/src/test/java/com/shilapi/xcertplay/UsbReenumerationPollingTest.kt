package com.shilapi.xcertplay

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbConfiguration
import android.hardware.usb.UsbInterface
import android.os.Looper
import com.shilapi.xcertplay.airplay.*
import com.shilapi.xcertplay.orchestration.*
import com.shilapi.xcertplay.transport.*
import java.time.Duration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class UsbReenumerationPollingTest {
    private fun controller(statuses: MutableList<CarPlayStatus>) = CarPlayController(
        RuntimeEnvironment.getApplication(),
        CarPlayRuntimeConfig(mfiTarget = MfiTarget.LOCAL, transport = CarPlayTransport.WIRED,
            identification = Iap2IdentificationConfig(name = "test", modelIdentifier = "test",
                manufacturer = "test", serialNumber = "test", firmwareVersion = "1",
                hardwareVersion = "1", carPlayUsbInterfaceNumber = 3)),
        AirPlayConfig(deviceName = "test", deviceId = "02:00:00:00:00:02", btMac = "02:00:00:00:00:01",
            sourceVersion = "1", main = AirPlayDisplayConfig(widthPixels = 1920, heightPixels = 720)),
        AirPlayIdentity.generate(), PairingStore(), object : AirPlaySessionListener {},
        object : AirPlayMediaHandler {}, { statuses.add(it) })

    private fun invoke(controller: CarPlayController, method: String, device: UsbDevice) {
        controller.javaClass.getDeclaredMethod(method, UsbDevice::class.java).apply { isAccessible = true }
            .invoke(controller, device)
    }
    private fun host(controller: CarPlayController, device: UsbDevice): IphoneUsbHost {
        val host = mock(IphoneUsbHost::class.java)
        `when`(host.discover()).thenReturn(listOf(device))
        `when`(host.requestPermission(device)).thenReturn(IphoneUsbHost.PermissionRequest.AlreadyGranted(device))
        controller.javaClass.getDeclaredField("iphoneHost").apply { isAccessible = true }.set(controller, host)
        return host
    }
    private fun makeReady(device: UsbDevice) {
        val mux = mock(UsbInterface::class.java)
        `when`(mux.interfaceClass).thenReturn(0xff)
        `when`(mux.interfaceSubclass).thenReturn(0xfe)
        `when`(mux.interfaceProtocol).thenReturn(2)
        val ncm = mock(UsbInterface::class.java)
        `when`(ncm.interfaceClass).thenReturn(2)
        `when`(ncm.interfaceSubclass).thenReturn(0x0d)
        val configuration = mock(UsbConfiguration::class.java)
        `when`(configuration.interfaceCount).thenReturn(2)
        `when`(configuration.getInterface(0)).thenReturn(mux)
        `when`(configuration.getInterface(1)).thenReturn(ncm)
        `when`(device.configurationCount).thenReturn(1)
        `when`(device.getConfiguration(0)).thenReturn(configuration)
    }
    @Suppress("UNCHECKED_CAST")
    private fun completeTransition(host: IphoneUsbHost) {
        val invocation = mockingDetails(host).invocations.single { it.method.name == "requestCarPlayReenumerationAsync" }
        (invocation.arguments[2] as (IphoneUsbHost.TransitionResult) -> Unit)
            .invoke(IphoneUsbHost.TransitionResult.ReenumerationRequested)
    }

    @Test fun refreshedDescriptorsReconnectWithoutAttachBroadcastAndOnlyOpenOnce() {
        val statuses = mutableListOf<CarPlayStatus>()
        controller(statuses).use { controller ->
            val device = mock(UsbDevice::class.java)
            val host = host(controller, device)
            invoke(controller, "beginReenumeration", device)
            completeTransition(host)
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
            assertTrue(statuses.contains(CarPlayStatus.WaitingForReenumeration))
            assertFalse(mockingDetails(host).invocations.any { it.method.name == "openIap2UsbSessionAsync" })
            makeReady(device)
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
            assertTrue(statuses.contains(CarPlayStatus.OpeningDataPaths))
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6))
            assertEquals(1, mockingDetails(host).invocations.count { it.method.name == "openIap2UsbSessionAsync" })
        }
    }
    @Test fun transitionCallbackAndPollingCannotRestartClosedController() {
        val controller = controller(mutableListOf())
        val device = mock(UsbDevice::class.java)
        val host = host(controller, device)
        invoke(controller, "beginReenumeration", device)
        controller.close()
        completeTransition(host)
        makeReady(device)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6))
        assertFalse(mockingDetails(host).invocations.any { it.method.name == "requestPermission" })
    }
}
