# DiPlay 0.2.11 — DiLink 4 direct cluster launch (test 11)

Test 11 exposes the dashboard content and placement controls in ADB mode. Test 10 extends the navigation overlay and safe-area controls. The branch ports test 8’s direct local-ADB Activity launch onto the exact upstream v0.2.11 tag,
following the legacy platform-21 approach published by 寒叙 (@Hanxu4131):
https://github.com/Hanxu4131/BYD-CarPlay
Reference files: LegacyClusterTarget.kt, LegacyClusterMap.kt, ClusterMapActivity.kt.
Original project authors and license notices remain in place.

## Test
1. Update the existing test APK (same `com.shihab.diplay.hudtest` package). There is now one launcher: **DiPlay Test**. The cluster Activity remains available to the app’s direct ADB launch.
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
- Restores the stream to 1920×720 at 100% scale. Existing car-marker horizontal/vertical controls now adjust its safe area.
- Select **Dashboard shows → Map with custom turn card** to show the upstream maneuver card in the ADB-launched cluster Activity. Existing card size and horizontal/vertical controls apply live.
- The card shows arrival time (in the head unit's time zone/time format) and remaining route distance when the phone supplies them. If arrival time is absent, remaining minutes are shown when available. Missing totals stay blank.
- Guidance clears on route expiry/end/disconnect, hides when the stream stops, and survives cluster Activity recreation through the host-owned state.
- OEM song display, L1 coordination and full decoder-mirroring changes are not included.
- No picture-filter changes.

Test 9 direct cluster placement and remembered USB permissions were confirmed by the user. Test 11 overlay placement and 1920×720 geometry still require vehicle testing. Automated checks cannot establish visible placement or firmware compatibility.

## USB reconnect

The upstream USB attach filter listed only the CH341 authentication adapter. This build also registers Apple vendor 1452, matching the existing `IphoneUsbMatcher.appleVendor()` discovery policy. Product ID is intentionally unrestricted because the iPhone can change its USB identity when entering CarPlay mode. The CH341 entry remains.

When Android offers DiPlay for the connected iPhone, select it and tick **Always use / Use by default**. The initial USB mode and the CarPlay mode may each need their own first confirmation. Android owns the remembered default and grants permission on a matching future attachment; DiPlay still checks `hasPermission()` before requesting access. No permission dialog is automatically clicked or privileged permission granted.

The ordinary permission grant expires on disconnect. This change enables Android’s standard default-handler path; it is not a verified guarantee that DiLink’s custom USB dialog will retain the default. Test unplug/replug and a head-unit restart. If no default checkbox is offered or the firmware forgets the choice, further device investigation is needed.

## Validation status

- Upstream baseline: `v0.2.11`, commit `6014025c653c4dae88d319ce446e0bf1ddb658ea`.
- Local public-tree scan and whitespace checks pass. Changed resource/manifest XML parses; one launcher remains and the cluster Activity keeps its exported, independent task configuration.
- All changed Kotlin source/test files parse without errors. Changed resource XML parses and has unique resource names.
- APK compilation, unit tests and lint have **not run for this build**. Local Gradle bootstrap failed to download through the execution environment’s network connection.
- The fork workflow targets the new branch, runs unit tests/lint, verifies the signed test APK and runtime assets, and publishes only after these checks succeed. Its upstream input is the SHA-256-pinned official 0.2.11 APK.
- Test 11 is authorized for publication to ojjj13/DiPlay. APK validation is pending the branch workflow; vehicle acceptance remains separate from automated checks.
