# CarPlay picture controls

Based on DiPlay v0.2.7 (`11dc9581df5323170e8033efb6f6b318857a5c81`).

Open Settings → Picture adjustments. Changes save immediately and apply to the
main CarPlay TextureView, including when returning from Settings, without a
stream reconnect. Reset picture restores the unfiltered image.

| Setting | Range | Default |
| --- | --- | --- |
| Brightness | −50 to +50 (RGB offset, percent of full scale) | 0 |
| Contrast | 0–200% | 100% |
| Saturation | 0–200% | 100% |
| Warmth | −100 (cool) to +100 (warm) | 0 |

Brightness changes pixels rather than screen backlight. Warmth adjusts red/blue
gains by up to 20%. Extreme combinations can clip highlights or shadows.
The decoder selection, video transport, touch overlay, app UI and cluster
presentation are unchanged. Filtering uses Android's existing hardware-layer
Paint/ColorMatrixColorFilter support; there is no per-frame CPU bitmap copy.

## Car validation

1. Start with defaults and confirm the image matches the upstream release.
2. Set saturation to zero and confirm the whole main CarPlay image is grayscale.
3. Adjust each control separately; return to CarPlay and verify the change.
4. Restart the app and confirm saved settings remain.
5. Reset picture and verify the original image returns.
6. Check map scrolling, touch, day/night appearance and reconnect behavior at
   the usual resolution/frame rate. If using the cluster, verify it is unaffected.

## Building

Follow BUILD.md for source-only and standalone builds. Ordinary debug builds
contain no accessory identity and can be used to inspect the settings UI, but
cannot connect as standalone CarPlay receivers. A car-test build requires the
explicit external `DIPLAY_AUTH_ASSETS_DIR` inputs documented upstream. No identity
or signing material is added to this branch.

## Validation for this patch

- Shared and common Android unit tests passed, including five new picture tests.
- Mobile debug lint and source-only debug APK build passed.
- Independent Kotlin/Android colour-matrix checks passed.
- Public source credential check and git whitespace checks passed.
- Physical DiLink testing remains outstanding.
