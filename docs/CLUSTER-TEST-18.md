# DiPlay 0.2.11 — DiLink 4 cluster test 18

Built from upstream main at 36302e05f4bb4f06592ea5f910e4a41c3702d0fb after PR #213 (merge 1d298f3a53079a955a668083312020572827b0f8) and PR #187 merged on 2026-10-04.

This replaces the test-17 implementation with the consolidated upstream route. It retains exact XDJA display selection, per-launch admission, stream-111 ownership, a fitted live safe-area editor, black cover on disconnect, and upstream live picture/USB improvements. In ADB cluster mode, an unset dashboard choice defaults to Map with turn card (the phone's built-in card). Existing saved dashboard and calibration choices are preserved. Upstream's display-specific defaults supersede the old fork's global defaults.

The package remains com.shihab.diplay.hudtest with version 0.2.11-dilink4-cluster-test18. Install on the Android head unit, enable DiLink 4 cluster video via ADB, authorize ADB if needed, then reconnect the iPhone and open Apple Maps. A manual replug may still be needed after changing connection settings.

PR #187 adds optional stock-map holding and HUD text. Both default off. Stock-map holding restores journaled OEM state when mirroring stops; after force-stop or a crash, recovery can require reopening the app. DiLink 4 HUD compatibility remains unverified.

The existing fork workflow runs shared/common unit tests and mobile lint, builds assembleStandaloneDebug using runtime assets from the SHA-256-pinned official 0.2.11 APK, verifies the APK signature, version and bundled runtime inputs, and publishes only after those checks succeed. This build has not been vehicle-tested.

See [upstream cluster setup](DILINK4_CLUSTER.md) for routing and calibration details.
