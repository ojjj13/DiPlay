# DiPlay Test 23 — separate night picture controls

Based exactly on Test 21 (55e644d4588738423fa37dbe50673ec85e429fea).

Picture adjustments now save separate daytime and night brightness, contrast, saturation and warmth. Existing daytime settings and their rendering are unchanged. Night starts neutral, so the daytime correction is no longer applied to the dark map. Open Picture adjustments with CarPlay in night mode and increase Warmth to reduce a blue cast; saturation can be adjusted independently. The panel identifies the active profile and follows automatic mode changes. Reset affects only the active profile. Show original temporarily bypasses both screens without changing either profile.

Both main and cluster textures switch live using the same night-mode signal DiPlay sends to CarPlay (System, Ambient, Day or Night). No reconnect is required. This build excludes Test 22 wireless USB changes.

Validation: workflow runs unit tests, lint, APK build and signature/version/runtime checks before publishing. Vehicle testing is needed to tune the night warmth and verify day/night transitions with the phone. Package com.shihab.diplay.hudtest, version 0.2.12-dilink4-cluster-test23.
