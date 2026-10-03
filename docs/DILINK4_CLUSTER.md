# DiPlay 0.2.10 — DiLink 4 direct cluster launch (test 8)

Test 8 replaces the unsuccessful task/stack move with a direct local-ADB Activity launch,
following the legacy platform-21 approach published by 寒叙 (@Hanxu4131):
https://github.com/Hanxu4131/BYD-CarPlay
Reference files: LegacyClusterTarget.kt, LegacyClusterMap.kt, ClusterMapActivity.kt.
Original project authors and license notices remain in place.

## Test
1. Update the existing test APK (same package). Main launcher: **DiPlay Test**; cluster launcher: **Cluster Map**, with a navigation icon.
2. Enable **DiLink 4 cluster video via ADB (experimental)** and tap **Authorize ADB / retry cluster routing**. Approve debugging if asked.
3. Reconnect the iPhone and open Apple Maps. The main CarPlay UI should remain on the head unit while stream 111 appears on the cluster.
4. If launch fails, export diagnostics. The report records the target, ADB state and shell acceptance. Shell acceptance alone does not prove visible output.

## Change
- Detects only the measured XDJA-owned 1920×720 logical cluster display; no assumed numeric ID.
- Uses `am start-activity --display <id> -f 0x18000000` to create an independent task directly on that display. Does not invoke the old stack-moving helper.
- Validates a per-launch token and the attached window display. On firmware reporting display 0 through the view context, checks the exact task in per-display ADB Activity history before attaching the decoder surface.
- Transparent, non-focusable cluster window. Stream 110 remains on the head unit; the existing stream-111 decoder attaches to the verified cluster SurfaceView.
- Failed/unconfirmed launches retry after five seconds. Confirmed Activity placement stops launch retries; this is not a first-video-frame recovery policy.
- Disable/host destruction invalidates launch tokens and cancels queued retries. Background retries never offer ADB authorization.
- Keeps test 7's 1920×624 stream geometry and provisional safe-area margins. Tang-specific map editors, OEM song display, L1 coordination and full decoder-mirroring changes are not included.
- No picture-filter changes.

Vehicle acceptance is pending. Automated checks cannot establish visible placement or firmware compatibility.
