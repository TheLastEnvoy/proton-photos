# Fix: "This version of the app is no longer supported" lockout

## Symptom
Users get stuck on a non-dismissible "Update required" screen (server text: *"This version of the app is no longer supported, please update to continue using the app"*). Installing the latest APK does not clear it.

## Root cause
1. The text is Proton's API error `Code 5003` (`APP_VERSION_BAD`). Verified live against `account-api.proton.me`: it is returned only for **first-party** app IDs below the server's minimum version (`android-drive@1.0.0`, `web-drive@1.0.0`). Third-party IDs (`external-drive-*`) are exempt.
2. ProtonCore `36.6.0` (`network-domain`) routes codes `5003` and `5005` to `ApiClient.forceUpdate()` through `ProtonForceUpdateHandler`.
3. `PhotosApiClient.forceUpdate()` set `FORCE_UPDATE_REQUIRED = true` in DataStore and **no code ever cleared it**. One 5003/5005 — e.g. from a build that sent a first-party `android-drive@…` header before commit `691074f` switched to `external-drive-akoos_proton_photos@<semver>-stable` — locked the app permanently, because app data survives APK updates.
4. The dialog was non-dismissible and its only action opened `market://details?id=eu.akoos.photos`. The app is distributed from GitHub releases, so that link leads nowhere.

## Fix

### `data/api/PhotosApiClient.kt`
- Record the build that tripped the latch in a new key `force_update_version_code`.
- `clearStaleForceUpdate(context)`: clears a latch left by an **older** build (or a legacy latch with no recorded build). Called once per launch.
- `clearForceUpdate(context)`: explicit user-initiated retry.

### `MainActivity.kt`
- `ForceUpdateDialog` is now dismissible. **Retry** calls `clearForceUpdate` and returns to the app; **Update** runs the in-app updater (`runManualUpdateCheck()`) instead of the dead Play Store link.
- `UpdaterHost()` moved out of the NavGraph branch so the updater's download/install prompt can render over the force-update screen.

### `App.kt`
- `onCreate` (main process) calls `PhotosApiClient.clearStaleForceUpdate(this)`.

### `res/values/strings.xml`
- Added `force_update_retry` ("Retry").

## Result
- A blocked user can always recover instead of being trapped.
- After an update, the latch is dropped; Proton re-evaluates the version gate on the next request and only re-locks if it still rejects the version.
- "Update" points at the in-app updater, which actually delivers the APK.

## Verification
API probe (`POST https://account-api.proton.me/auth/v4/info`, `x-pm-appversion` as shown):

| Header | Result |
|---|---|
| `android-drive@1.0.0` | `422 / 5003` "This version of the app is no longer supported…" |
| `android-drive@2.38.0` … `@6.0.0` | `401` (passes the version gate) |
| `external-drive-akoos_proton_photos@2.5.0-stable` | `200 / 1000` |
| `external-drive-*@0.0.0` … `@10.0.0` | `200 / 1000` (exempt) |
| `external-drive-akoos-proton-photos@…` (hyphenated) | `400 / 2064` "Invalid section name" |

Conclusion: the current `external-drive-…` header is correct and is not subject to the version gate. The permanent lockout, not the header, was the defect.
