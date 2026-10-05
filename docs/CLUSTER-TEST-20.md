# DiPlay 0.2.12 — DiLink 4 cluster test 20

Includes the test19 casting-mode fix reported working on a 2022 BYD Seal / DiLink 4.0.

Opening Cluster safe area · edit box now launches the cluster window even before connecting the iPhone. The calibration-only launch uses authorized local ADB, validates the target display and launch token, and does not hold/disable the stock map or start CarPlay. Closing, cancelling or saving the editor clears the outline and closes a preview-only window. An existing CarPlay window stays alive when the editor closes.

Keep stock-map holding Off and activate native full-screen casting. Before tapping Connect using cable, open the cluster safe-area editor: the green outline should appear on the cluster and follow edits. Cancel/dismissal should remove it; Save should retain the new rectangle for the next connection. Then check normal cluster CarPlay and calibration during a connected session. This preview change needs vehicle validation.

Package: com.shihab.diplay.hudtest. Version: 0.2.12-dilink4-cluster-test20. The workflow runs shared/common unit tests, mobile lint, builds a standalone APK with the approved upstream runtime inputs outside the source tree, and verifies the APK before publishing.
