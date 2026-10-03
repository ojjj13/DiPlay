# DiPlay 0.2.10 — experimental DiLink 4 cluster output

Based on upstream v0.2.10 (`3e43e25`), with only the experimental DiLink 4 cluster changes. No picture/color filters or picture-adjustment controls are included.

## Test 5: stock container projection getter

Tap **Test cluster access via ADB**, then export the report. This build also queries `AutoContainerNative` using transaction 5 and the `android.os.IAutoContainer` interface token. Both are verified from `BpAutoContainer::getProjectionDisplayInfo` in the device-supplied JNI library. This is the getter used by the stock container's active virtual-display path.

The helper decodes the Binder status, return code and size-prefixed projection-info parcelable, reporting the name, dimensions and producer presence. It records permission and format errors. Returned producers are not connected to or used for rendering. No system installation or projection-mode changes are performed.

## Test 4: exact native surface metadata query

Tap **Test cluster access via ADB**, then export the diagnostic report. The helper now also queries `FissionHostSvc` transaction 101 using an empty request Parcel, matching `getQtProjectionDispInfoArrayNative` in the device-supplied `libxdjacontainerservice_jni.so`. Android's `service call` command adds an interface token and therefore does not reproduce that request exactly.

The report records whether the transaction was handled, reply size, surface count, names, dimensions and whether a producer Binder was returned. It does not construct or connect to those surfaces, draw into them, or change projection modes. A positive result would identify a route for further investigation; it does not implement video transport. Zero, malformed replies and permission errors are recorded without claiming that the physical cluster is unavailable.

## Test 3: ADB-assisted access probe

In the cluster settings section, tap **Test cluster access via ADB** while parked. Approve DiPlay's own debugging key if prompted. After the finished message, save the diagnostic report and share it.

The helper lists displays under the ADB shell identity and attempts an invisible, short-lived Presentation surface on the exact measured DiLink 4 display only. It exits automatically and records any access error. It also collects the display-service dump and display-related service names. No root, permission modifications, guessed Binder transactions or projection-mode changes are used.

This build tests ADB rendering access; it does not yet transport the CarPlay video stream through ADB. A valid surface is not proof of physical cluster routing. The normal app-level cluster path remains available.

## Test 2: settings visibility and diagnostics

- Cluster settings now have their own section, independent of the BYD HUD/navigation service check.
- If no compatible display is visible, the section explains this instead of disappearing.
- Exported reports always include the saved cluster toggle, receiver availability, all visible display names/geometry/flags, presentation display IDs and selected display, even without a CarPlay connection.
- Open Settings → CarPlay map on instrument cluster (experimental). Enable the toggle if available, then reconnect the iPhone. If the section reports no compatible display, export a diagnostic report directly.

## Changes

- Recognises the exact public presentation display `fission_bg_xdjaVirtualSurface` at 1920×720, observed on a 2022 BYD Seal / DiLink 4.0. No display ID is hard-coded.
- Sends the iPhone instrument-cluster stream (111) through the existing separate decoder and Android Presentation surface.
- Requests native 1920×720 video without importing DiLink 5's unverified crop/safe-area coordinates. DiLink 5-only size/marker/pause controls are hidden for this candidate.
- Preserves existing DiLink 5/5.1 selection priority.
- Logs display names, geometry, flags, requested stream and presentation failures.

## Test later, while parked

1. Install the test APK. It uses `com.shihab.diplay.hudtest`, alongside the official app. A previous test build may have a different debug signing key and require uninstalling that test build first.
2. Enable the car's native cluster map/projection mode, then open DiPlay settings and enable **CarPlay map on instrument cluster (experimental)**.
3. Connect/reconnect the iPhone and open Apple Maps. Upstream v0.2.10 includes the fix for USB startup with incomplete manual-hotspot settings.
4. Check for the map on the instrument cluster. If blank, export DiPlay's log; look for `DiLink 4 candidate`, `presentation shown`, and `cluster stream active=true`.
5. Turn off the experimental cluster toggle to stop DiPlay's projection.

## What remains unverified

Display enumeration and the host compositor's `fission_cluster` layer are observed facts; forwarding this application's Presentation to the physical cluster has **not** been tested. The stock map may reclaim the projection, and the car may require an additional proprietary routing command. This build does not issue guessed Binder calls or switch BYD projection modes. If the display appears only after connecting, enable native projection first and reconnect.

HUD support is unchanged: this does not fix the missing SOME/IP HUD service. No code from AmapService.apk is included.

The test build uses the same checksum-pinned upstream v0.2.8 preview authentication assets as the previous picture test, outside the source tree. Application source is v0.2.10.
