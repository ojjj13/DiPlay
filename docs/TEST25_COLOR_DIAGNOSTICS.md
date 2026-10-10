# Test25 color diagnostics (upstream v0.2.15)

Read-only logging; no color override, VPP toggle, codec selection, rendering, Surface opacity or picture defaults changed. Debug version: 0.2.15-test25. Existing debug application ID retained.

Capture a fresh connection, show CarPlay Settings and a daytime map, switch to a night map, and show both main screen and cluster. Export DiPlay diagnostics and, if possible, `adb logcat -d -v threadtime` immediately afterwards. Search for `Test25`. Codec events also use existing per-stream diagnostic callbacks; view/filter events are in logcat and main-view lifecycle events also enter the host log.

Each codec event includes stream, primary/mirror label and worker identity. Input/output format snapshots preserve absent and unknown values, including vendor values such as 86. Supported color formats describe capabilities, not the active Surface buffer format. No CSD bytes, decoded pixels, or MFi material are logged. Picture logs include active filter values and the computed matrix; original preview bypasses that matrix.

Display HDR/wide gamut fields describe capabilities only. Codec output format and first output release do not prove the dataspace or range used by SurfaceFlinger/HWC. If available via your own ADB connection, collect `adb shell dumpsys SurfaceFlinger` and `adb shell dumpsys display` while the affected frame is visible; these are read-only and may be denied by firmware. The app does not invoke privileged commands.

CI runs the full repository checks and produces an identity-free debug APK in an Actions artifact, without publishing a Release. The APK uses a disposable debug signature; adding your own MFi assets requires rebuilding/repacking and signing afterwards. This source-only APK cannot authenticate CarPlay until you provision those assets.
