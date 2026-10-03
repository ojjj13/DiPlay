# DiPlay 0.2.10 — DiLink 4 dedicated cluster activity (test 6)

This experimental build uses the two-activity arrangement that rendered concurrently on the 2022 Seal. No picture-filter changes are included.

## Parked car test
1. Install the APK. If Android reports a signing-key mismatch, uninstall the previous DiPlay test app first. The upstream app is a separate package.
2. In the cluster settings section, enable **DiLink 4 cluster video via ADB (experimental)** / **DiLink 4 仪表视频（ADB，实验性）**.
3. Tap **Authorize ADB / retry cluster routing**, approve debugging if asked, and wait for Ready. Approval does not carry over from the small probe APK or 甲壳虫.
4. Connect/reconnect the iPhone. Open Apple Maps and start a route.
5. The main CarPlay interface should stay on the head unit. The separate **DiPlay Cluster / DiPlay 仪表** activity requests and renders stream 111 at 1920×720 on the cluster.
6. If routing fails, use the already-working 仪表窗口管理 app to move the dedicated **DiPlay Cluster** task, then return to the main CarPlay task. Avoid moving the main DiPlay activity.
7. Export the diagnostic report if either the window move or the second video stream fails. It includes `routeTarget`, `routeIsolated`, `routeVerified`, `routeSuccess`, errors, and the session's stream 111 state.

## Implementation
- A dedicated, resizable `singleTask` activity has its own task affinity and SurfaceView in the CarPlay process.
- Stream 110 keeps its original renderer; stream 111 attaches to the dedicated activity's surface. ADB carries only window-control commands, not video frames.
- The phone receives the cluster configuration without needing app-level display enumeration.
- The shell discovers only the measured XDJA-owned 1920×720 logical display in `dumpsys display`, with no hard-coded numeric display ID.
- The helper verifies task ownership, isolates a shared fullscreen stack using Android 10's freeform task mode when needed, rejects mixed stacks, moves only that cluster stack, sizes the cluster task, verifies both displays, and restores main-stack input focus.
- Surface destruction detaches the decoder; loss of activity focus does not. Reconnection reattaches the current cluster surface to the new sink.
- Disabling the mode or destroying the host closes the cluster activity. The helper has a four-second watchdog and never changes stock projection modes, firmware, permissions, or other tasks.

The two-activity pattern test passed on the car. This integrated APK's automatic router and actual CarPlay second-stream output still require the car test; CI cannot establish them.
