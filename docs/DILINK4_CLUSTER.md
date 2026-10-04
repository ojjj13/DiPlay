# DiPlay 0.2.11 — DiLink 4 direct cluster launch (test 13)

Test 13 keeps all editor edges within the available window and mirrors the calibration outline live onto the cluster while editing. Test 12 adds a draggable cluster safe-area box editor and defaults main-screen fullscreen off. Test 11 exposes the dashboard content and placement controls in ADB mode. Test 10 extends the navigation overlay and safe-area controls. The branch ports test 8’s direct local-ADB Activity launch onto the exact upstream v0.2.11 tag,
following the legacy platform-21 approach published by 寒叙 (@Hanxu4131):
https://github.com/Hanxu4131/BYD-CarPlay
Reference files: LegacyClusterTarget.kt, LegacyClusterMap.kt, ClusterMapActivity.kt.
Original project authors and license notices remain in place.

App version and release title both use `clusterTestVersion=13` from gradle.properties.

## Safe-area editor
Under **CarPlay map on instrument cluster**, tap **Cluster safe area · edit box**. Drag the four green boundaries in the fitted 1920×720 preview; a matching outline updates on the cluster. The outline clears on save, cancel, dialog dismissal or leaving settings. It is a calibration preview, not a live change to Apple Maps layout. Then save/apply. It takes effect on the next CarPlay connection; a manual replug may still be necessary because the settings-reconnect issue is unresolved. Cancel keeps the existing mapping. **Reset cluster safe area** restores marker-offset-based placement. The main-screen safe-area mapping is independent.

Fullscreen now defaults off for unset preferences. Existing saved top/bottom bar choices are preserved; disable those switches once if they were already saved on.

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

Test 9 direct cluster placement and remembered USB permissions were confirmed by the user. Test 14 uses a standalone safe-area dialog so the main-screen editor has allocated drawing space, while retaining the live cluster outline. Map with the built-in turn card is now the default for an unset dashboard choice. Saved choices remain unchanged. Editor placement and 1920×720 geometry still require vehicle testing. Automated checks cannot establish visible placement or firmware compatibility.

## USB reconnect

The upstream USB attach filter listed only the CH341 authentication adapter. This build also registers Apple vendor 1452, matching the existing `IphoneUsbMatcher.appleVendor()` discovery policy. Product ID is intentionally unrestricted because the iPhone can change its USB identity when entering CarPlay mode. The CH341 entry remains.

When Android offers DiPlay for the connected iPhone, select it and tick **Always use / Use by default**. The initial USB mode and the CarPlay mode may each need their own first confirmation. Android owns the remembered default and grants permission on a matching future attachment; DiPlay still checks `hasPermission()` before requesting access. No permission dialog is automatically clicked or privileged permission granted.

The ordinary permission grant expires on disconnect. This change enables Android’s standard default-handler path; it is not a verified guarantee that DiLink’s custom USB dialog will retain the default. Test unplug/replug and a head-unit restart. If no default checkbox is offered or the firmware forgets the choice, further device investigation is needed.

## Test 15 live picture controls

Open Settings → Display and performance → Picture adjustments while CarPlay is connected.
A compact panel overlays the live main stream; sliders immediately save brightness, contrast,
saturation and warmth without negotiating or reconnecting the phone. Show original temporarily
bypasses the filters on both streams; Done, Back or leaving the host restores the saved filter.
Reset returns all sliders to neutral. Previous picture-control preferences are reused.

The measured DiLink 4 cluster now renders stream 111 through a transparent TextureView with
a fixed 1920×720 buffer so the same saved filter can apply live. Factory instruments, custom
turn overlays and safe-area guides are not filtered. Other firmware's renderer is unchanged.
This renderer change requires vehicle validation; test 14 remains the known-good layout build.
Controls provide manual compensation, not a verified fix for intermittent colour-range faults.

## Validation status

Test 16 also adds descriptor polling after a successful USB mode-transition request. Previously
that state waited exclusively for an attach broadcast; the initial discovery state already polled.
The fallback rechecks every two seconds and proceeds only when a matching device exposes a
complete CarPlay configuration. It still requests/checks Android USB permission normally.
Phase/generation checks stop stale polls after attachment, opening data paths, restart or close.
A regression test refreshes descriptors without any attach event and verifies one data-path open.
This fixes a confirmed discovery gap, not a proven diagnosis of every vehicle reconnect failure.

- Upstream baseline: `v0.2.11`, commit `6014025c653c4dae88d319ce446e0bf1ddb658ea`.
- Local public-tree scan and whitespace checks pass. Changed resource/manifest XML parses; one launcher remains and the cluster Activity keeps its exported, independent task configuration.
- All changed Kotlin source/test files parse without errors. Changed resource XML parses and has unique resource names.
- APK compilation, unit tests and lint have **not run for this build**. Local Gradle bootstrap failed to download through the execution environment’s network connection.
- The fork workflow targets the new branch, runs unit tests/lint, verifies the signed test APK and runtime assets, and publishes only after these checks succeed. Its upstream input is the SHA-256-pinned official 0.2.11 APK.
- Test 16 is authorized for publication to ojjj13/DiPlay. APK validation is pending the branch workflow; vehicle acceptance remains separate from automated checks.
