# DiPlay Test 24 — decoder color experiments

Based exactly on Test 23 (`634d91c1dc01153b3c555a5a08b1781e551b4cef`). Keeps separate daytime/night picture profiles, cluster routing, and excludes Test 22 USB transport changes.

Settings → Display and performance adds two independent switches:

- **Force BT.709 (experimental)**: supplies `color-standard=1` when configuring each decoder. Does not override color range or transfer, rewrite SPS, or guarantee the decoder adopts it.
- **Qualcomm VPP (experimental)**: requests `vendor.qti-ext-vpp.mode=HQV_MODE_AUTO` when enabled and `HQV_MODE_OFF` when disabled, only on Qualcomm hardware decoder names. Other decoders skip the vendor parameter. API 31+ skips a parameter not advertised by the decoder; on older DiLink firmware support is unknown and this is a best-effort request.

Both switches default off, persist on Save, and apply after reconnect to main, cluster and any mirror decoders. Cancel restores saved values. VPP off explicitly requests OFF on Qualcomm, so it may differ from the firmware-selected default in Test 23. A rejected configuration falls back to an ordinary decoder format and logs the bypass; it does not silently claim the requested feature is active.

Logs identify requested parameters and reported output standard/range/transfer by name as well as the existing `color=standard/range/transfer` tuple. Accepted configuration or an advertised key is not evidence that VPP actually processed frames.

For comparison, set picture sliders neutral and keep codec, map scene and day/night mode fixed. Try both off, BT.709 only, VPP only, then both on. Save/reconnect between configurations. Export diagnostics with each observation. Vendor processing and visual benefit need vehicle validation.

The Android checks workflow runs unit tests, lint and source-only APK builds. The existing Test24 release gate rejects APKs without standalone authentication. Recovery preserves that gate and does not provision authentication or publish an APK. Keep MFi private keys outside Git and public build artifacts. Package `com.shihab.diplay.hudtest`, version `0.2.12-dilink4-cluster-test24`. Temporary debug signing keys may require uninstalling the previous test APK first.

Recovery uses remote `dea66cec3ba2f61cf79c89a279e7140a577935d7` plus the source, tests and translations preserved in local `cb61036`. The previous worktree and remote Test24 branch are retained. The earlier source-only release is not a standalone CarPlay test package.
