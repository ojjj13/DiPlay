# DiPlay 0.2.12 — DiLink 4 cluster test 21

Applies the saved and live picture controls to the DiLink 4 ADB cluster video texture. Brightness, contrast, saturation and warmth now use the same settings on the main CarPlay screen and cluster. Show original temporarily bypasses the filter on both; Reset restores neutral settings live. The cluster binding is released when its video view closes.

Includes the test19 native full-screen casting fix and test20 safe-area preview before connecting the iPhone, both reported working on a 2022 BYD Seal / DiLink 4.0. Keep stock-map holding Off and activate native full-screen casting as before.

To test: connect CarPlay, open the live picture panel on the main screen, and change contrast or saturation enough to see the effect on the cluster immediately. Compare with Show original and Reset. Reconnect to check saved adjustments still apply. Vehicle validation is required for the new filter binding.

Package: com.shihab.diplay.hudtest. Version: 0.2.12-dilink4-cluster-test21. The workflow runs shared/common unit tests (including the actual cluster texture's filter lifecycle), mobile lint, standalone APK build and APK verification before publishing.
