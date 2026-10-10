# Test26: DiLink 4 cluster SurfaceView (v0.2.15)

Based on upstream v0.2.15 (`b940efe`) with the read-only Test25 diagnostics retained
(now tagged Test26). Debug application ID: `com.shihab.diplay.hudtest`.

The car test found that main-screen colors became correct with Smooth video,
which selects SurfaceView, while the DiLink 4 cluster TextureView remained tinted.
Test26 adds an opt-in **Direct cluster video (experimental)** toggle inside
Advanced > CarPlay map on instrument cluster, visible for the ADB DiLink 4 route.
The existing TextureView remains the default.

With the toggle on, stream 111 decodes onto a framework-owned SurfaceView at
1920x720. Stream negotiation, safe-area insets, routing, and OEM map handling are
unchanged. The waiting black screen, turn card and safe-area preview remain
ordinary views above the video. Picture adjustments do not apply to this path;
their saved values remain available when returning to TextureView. Main-screen
Smooth video remains independent and should stay on for the observed workaround.

Changing the toggle while connected asks to apply and reconnect, matching existing
cluster-route settings. The old route is closed before reconnection. Surface
destruction waits for both current and retiring decoders before returning to the
framework; the holder Surface is never manually released. Recreation requests a
fresh frame, and host handoff detaches the old host before publishing to the next.

## Car check

1. Keep main-screen Smooth video on. Enable Direct cluster video, apply and reconnect.
2. Compare daytime and nighttime maps on the cluster, then verify turn guidance and
   the existing safe-area placement.
3. Disconnect: the old map should be covered in black. Reconnect and verify the map
   returns. Check the safe-area preview before connecting too.
4. Turn Direct cluster video off to compare with the previous TextureView path.

The exported report records the saved and active selection. Host logs include
`Test26 cluster renderer=SURFACE` (or `TEXTURE`) and synchronous detach results.
Unit tests cover surface ownership, recreation/close, decoder-detach ordering,
overlay/preview visibility, and cancel/apply behavior of the live-session toggle.
Actual color and OEM composition still require the car check.

CI runs the full AGENTS.md Gradle checks and builds an authentication-free APK.
The private car-test APK is provisioned with the user's previously supplied
runtime identity and signed separately; neither those bytes nor signing keys
belong in this source branch or its public CI artifacts. No Release is created.
