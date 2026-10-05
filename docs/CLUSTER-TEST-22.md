# DiPlay 0.2.12 — DiLink 4 cluster test 22

Opt in to keep wireless CarPlay running while an iPhone is plugged into USB for charging. Enable **Keep wireless CarPlay while charging via USB** under Settings → Connection setup. It is off by default, preserving the existing automatic switch to wired for other users. When enabled, USB attachment no longer overrides the selected connection mode, shuts down the wireless controller, or switches to wired CarPlay. This applies both to an existing projection screen and to a screen launched by USB attachment. Choose Connect using cable to explicitly use wired CarPlay; ordinary wired discovery and reconnect are unchanged.

Includes all test21 cluster picture controls and the tested native full-screen casting and safe-area preview fixes. Package: com.shihab.diplay.hudtest. Version: 0.2.12-dilink4-cluster-test22.

Vehicle test: enable the new setting, select wireless and establish CarPlay, then plug in the charging cable, unplug it and plug it in again. Check that audio, main video and cluster video stay connected and wireless remains selected. Also check USB attachment with wireless selected after reopening DiPlay. Finally disconnect and choose Connect using cable to check deliberate wired operation. Vehicle validation is required; unit tests cannot establish the iPhone's own charging/transport behavior.

The release workflow runs shared/common unit tests, mobile lint, standalone APK build, signature/version/runtime verification before publishing. Regression tests cover USB launch, repeated plug/unplug events preserving an existing wireless controller, and explicit wired selection.
