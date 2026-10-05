# DiPlay 0.2.12 — DiLink 4 cluster test 19

Based on upstream v0.2.12 with the fork test package com.shihab.diplay.hudtest.

When DiLink 4 cluster video via ADB is selected, suppress DiLink 3 automatic cluster-mode commands during display preparation, map/guidance changes, retries and recovery. Preserve the driver-selected native casting mode. A pending DiLink 3 recovery journal remains available for recovery after the ADB option is deselected. Stock-map holding is unchanged and should remain Off for this vehicle test.

Adds regression coverage for mode suppression and deferred recovery, plus routing diagnostics for the selected stock-map hold mode and mode suppression.

Test while parked: keep stock-map holding Off, open native full-screen cluster casting, enable the ADB cluster route, authorize if needed, then connect the iPhone and open Apple Maps. Check whether casting stays full screen and the CarPlay cluster map appears. Export diagnostics after a failure. This is an unverified vehicle fix, not proof that the AMap adapter is the cause.

Shared/common unit tests, mobile lint and the source-only APK build passed on d8f7ce4. The authorized release workflow builds the same source as a standalone APK and verifies its signature, test19 version and runtime assets before publishing. Runtime inputs remain outside the source tree.
