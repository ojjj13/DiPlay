# Dual Display Probe 0.1

Standalone Android 10+ APK with exactly two activities and no DiPlay dependencies.
Separate task affinities, independently animated SurfaceViews, no permissions or network access.

## Parked car test
1. Install the APK; two launcher entries appear: **Probe MAIN** and **Probe CLUSTER**.
2. Open **Probe CLUSTER**. Use the existing **仪表窗口管理** app to move its foreground task onto the instrument cluster.
3. From the head-unit launcher, open **Probe MAIN**. Do not move the whole package back.
4. Check the blue MAIN view on the head unit and green CLUSTER view on the instrument cluster at the same time. Both frame counters and dots should advance.
5. Tap/focus MAIN and check that CLUSTER keeps animating even without focus.
6. Report each view's task ID, display ID, and whether either freezes, disappears, or moves with the other.

This app deliberately delegates routing to the existing window manager that already worked on this car.
If that app only selects a package and moves MAIN instead, report that limitation; do not interpret it as evidence that two activities cannot work.
Different task IDs are required. The display ID shown is app-level diagnostic data and might still be filtered by vendor firmware.
Successful animation proves concurrent surface rendering, not CarPlay second-stream decoding or automatic ADB routing.
Rendering follows surface lifetime rather than onPause, and recreates the surface renderer after migration.

## Build
JDK 17, Gradle 8.9, Android SDK platform 35:
`gradle -p cluster-probe :app:lintDebug :app:assembleDebug`

Source is independent of the main DiPlay build. Package: `com.ojjj13.dualdisplayprobe`.
