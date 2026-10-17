<p align="center">
  <img src="docs/logo.png" alt="Photos for Proton" width="96" />
</p>

<h1 align="center">Photos for Proton</h1>

<p align="center">
  End-to-end encrypted photo backup and browsing for your Proton Drive Photos library.<br />
  <strong>Unofficial</strong> open-source Android client using Proton's incubating Drive SDK and documented API by
  <a href="https://akoos.eu">Akoos</a>. Not affiliated with Proton AG.
</p>

<p align="center">
  <a href="https://github.com/gitakoos/proton-photos/releases/latest"><img src="https://img.shields.io/github/v/release/gitakoos/proton-photos?label=release&color=blue" alt="Release" /></a>
  <a href="https://github.com/gitakoos/proton-photos/releases"><img src="https://img.shields.io/github/downloads/gitakoos/proton-photos/total?label=downloads&color=8B7CFF" alt="Downloads" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0-green" alt="License" /></a>
  <img src="https://img.shields.io/badge/minSdk-29-orange" alt="Min SDK" />
  <a href="https://www.photosforproton.eu"><img src="https://img.shields.io/badge/website-photosforproton.eu-8B7CFF" alt="Website" /></a>
</p>

<p align="center">
  <a href="https://www.buymeacoffee.com/akoos"><img src="https://img.shields.io/badge/Buy%20me%20a%20coffee-100f0f?logo=buymeacoffee&logoColor=FFDD00" alt="Buy me a coffee" /></a>
</p>

## Features

| Area | What you get |
| --- | --- |
| **End-to-end encrypted** | Photos, videos, albums and names encrypted with ProtonCore + GoOpenPGP. |
| **Backup** | Per-folder or back-up-everything (with a per-folder exclude list), Wi-Fi-only, continuous (uploads start seconds after capture and resume after a reboot), parallel uploads. Optional lighter uploads that compress photos and videos to a quality you pick, leaving the on-device originals untouched. Auto re-pairs your library after a reinstall; optional delete-after-backup that routes removed copies to the system trash so they stay recoverable, plus one-tap or automatic scheduled free-up of already-backed-up copies. An Activity screen shows uploads, downloads and offline saves in progress, the photos still waiting to upload, and a history of finished transfers. Import a Google Photos or Ente export straight into Proton Drive, keeping each photo's original date and place. |
| **Browse & find** | Pinch-zoom timeline that regroups by day, month or year, a year-jump scrubber, a calendar view (a hero photo and editable place per day, type-to-jump search), a photo map of your located shots with offline place names, per-place pages, search (type, date and sync filters, accent-insensitive, plus opt-in on-device search by describing a photo), a spinning globe and a places browser, and a memories page (On this day, seasons). |
| **Albums & sharing** | Cloud-native encrypted albums with custom covers; share by email with viewer or editor roles, manage members and invites, and save albums shared with you into your own library; mirror device folders to matching Drive albums. When adding photos, see which albums already hold each one and filter the picker to photos not yet in any album. |
| **Organize** | Multi-select by long-press or by dragging across the grid, in the timeline, albums and device folders, with bulk back-up, download, add-to-album, delete, hide and strip; a duplicate finder for exact and visually similar photos, on device and in the cloud, with a review step before removing extras; per-folder browsing; a display-only timeline filter; share to any other app. |
| **Edit** | Photo editor (thirteen adjustments, tone curves, per-colour HSL, filters, redact, rotate, free-form crop, undo and redo), a collage maker, and a tool to hide faces; video editor (trim, crop, rotate, combine clips, music overlay, grab a frame, make a GIF, strips location metadata); edit a photo's date, place and description on device, synced and cloud photos; RAW and DNG previews; open a photo from any app straight into the viewer or editor. |
| **People & on-device AI** | Find the people in your photos and videos and name them, all on the device, and read and copy the text inside a photo. The face and text models run on the device, are downloaded once, and work without a Proton account. |
| **No account needed** | Use the whole app without a Proton account: device photos, albums, folders, the map, search, face recognition and the duplicate finder all work signed out. Sign in any time to add end-to-end encrypted cloud backup and sync, with no data loss. |
| **Privacy & lock** | Hide any photo, or a whole cloud album, and restore it from the Hidden screen: photos only on this device move into a locked area behind biometric or PIN with a blur overlay, while backed-up and cloud-only photos are hidden from your timeline with the Drive copy never moved or deleted. Per-field metadata stripping (GPS, camera, timestamp, software) on upload or in bulk, including formats such as HEIC that can't be edited in place. Mirror-to-local so your on-device copy matches the cloud, in-app cloud trash, and a configurable app lock with timeout. |
| **Hardened** | TLS cert pinning on every Proton call, a strict host allowlist with cleartext blocked, no app state carried in cloud auto-backup or device transfer, and logs stripped from release builds. |
| **Comfort** | Make any cloud photo available offline, pinned at full quality with an offline badge, a Search filter and a storage readout; a full-screen viewer with slideshow, motion-photo playback and frame-by-frame video scrubbing with a filmstrip preview; an optional edge-to-edge grid for a denser, gapless timeline; an opt-in screenshot bar to edit, share, upload or link a new screenshot; a background thumbnail warm-up for smooth scrolling, a home-screen widget (including a Cloud Photos mode served from the encrypted cache), an in-app FAQ, an in-app updater, an adaptive app icon, an option to set the app as your default gallery, Ultra HDR photos shown in HDR, 11 languages, light, dark and system themes with a pure-black AMOLED option, and 11 colour palettes. |

## Install

Download the latest APK from the [releases page](https://github.com/gitakoos/proton-photos/releases/latest) and tap to install.

## Architecture

Clean architecture in a single module, with strict one-way layer dependencies:

| Layer | Holds | Depends on |
|---|---|---|
| **Presentation** | Jetpack Compose screens, ViewModels, navigation, theme | Domain |
| **Domain** | Entities, repository interfaces, use cases, pure Kotlin, no Android imports | none |
| **Data** | Proton Drive SDK client, documented-API compatibility layer, crypto wrapper, Room database, DataStore, repositories and WorkManager workers | Domain |

Hilt binds the Data implementations to the Domain interfaces; every Proton Drive feature lives under `data/repository/drive/` (detailed below).

## Project structure

```
app/src/main/kotlin/eu/akoos/photos/
├── presentation/     UI: Composables + ViewModels
│   ├── auth/         Sign-in
│   ├── onboarding/   First-launch wizard (about, backup mode, folder pick, privacy, metadata mirror, lock, appearance, permissions)
│   ├── gallery/      Photos tab + multi-select
│   ├── albums/       Albums tab + detail + sharing
│   ├── shared/       Shared-with-me albums tab
│   ├── folders/      Per-device-folder detail + multi-select
│   ├── memories/     On this day + seasons memories
│   ├── calendar/     Calendar view + per-day metadata editor
│   ├── map/          Photo map (osmdroid thumbnail pins, place drawer, city search)
│   ├── location/     Per-place detail page
│   ├── viewer/       Full-screen photo / video viewer
│   ├── editor/       Photo + video editor
│   ├── search/       Search with filename + content filters
│   ├── duplicates/   Duplicate finder (exact + visually similar)
│   ├── offline/      Make photos available offline
│   ├── hidden/       Hidden vault (PIN / biometric)
│   ├── settings/     Settings, About, Privacy, Permissions, Activity, Language, Theme, Palette, Trash, FAQ
│   ├── lock/         App lock
│   ├── updater/      In-app update orchestration
│   ├── whatsnew/     Post-update highlights
│   ├── common/       Shared composables (ErrorPopup, ConfirmDialog, EmptyState, ThemedSnackbar)
│   ├── util/         Presentation-layer helpers (formatters, focus helpers)
│   └── theme/        Colour tokens + palette factories
├── domain/           Pure domain layer
│   ├── entity/
│   ├── repository/   Interfaces
│   └── usecase/
├── data/             Implementations + IO
│   ├── api/          Retrofit Drive API + DTOs
│   ├── crypto/       DriveCryptoHelper (ProtonCore wrapper)
│   ├── db/           Room DAOs / entities / migrations
│   ├── repository/   incl. LocalExifBackfillScheduler + CloudGpsBackfillScheduler
│   │   │             (geotag + capture-date recovery for on-device
│   │   │             and cloud photos)
│   │   └── drive/    Drive backend split per concern (Upload, Download,
│   │                 Stream, Album, AlbumSharing, AlbumCryptoChain,
│   │                 CloudTrash, ThumbnailDecryptScheduler, …)
│   ├── preferences/  DataStore
│   ├── updater/      APK download + install
│   ├── hidden/       Hidden-vault storage
│   ├── offline/      Offline pinned-photo blobs
│   ├── transfer/     Live transfer state + history log
│   └── upload/       Upload-side photo + video compression, mirror + album-add helpers
├── di/               Hilt modules (Core, Network, Database, Repository,
│                     WorkManager, Updater, Stub)
├── navigation/       Single NavGraph
├── util/             Cross-cutting helpers (Exif, NetworkObserver, ErrorMessageSanitizer,
│                     OfflineGeocoder for offline reverse-geocoding from a bundled GeoNames dataset)
├── worker/           WorkManager workers
├── service/          Background-sync service + boot receiver + screenshot quick-action overlay
├── widget/           Home-screen widget (4 modes incl. Cloud Photos from encrypted cache)
└── App.kt + MainActivity.kt
```

## Tech stack

- Kotlin · Jetpack Compose · Material 3
- Hilt (DI) · Room (DB) · Coil (images) · OkHttp + Retrofit (network) · Media3/ExoPlayer (video) · [osmdroid](https://github.com/osmdroid/osmdroid) (map tiles)
- Offline place names from the [GeoNames](https://www.geonames.org/) cities15000 dataset (CC BY 4.0), bundled under `app/src/main/assets`
- [ProtonCore Android](https://github.com/ProtonMail/protoncore_android) (GPL-3.0) for auth, keys, and network
- [Proton Drive SDK Kotlin bindings](https://github.com/ProtonDriveApps/sdk/tree/main/incubating/client/kt) (`me.proton.drive:sdk:0.31.1-rust`, GPL-3.0; incubating) for the Photos operations exposed by the binding
- [GoOpenPGP](https://github.com/ProtonMail/gopenpgp) for app-specific album sharing and crypto operations not exposed by the current Kotlin SDK API
- Drive API reference: [ProtonDriveApps/android-drive](https://github.com/ProtonDriveApps/android-drive)
- Min SDK 29 (Android 10; required by the current Proton Drive SDK Android binding) · Target SDK 35 · ABIs: `armeabi-v7a`, `arm64-v8a`, `x86_64` (32-bit `x86` is not supported by the SDK)

## What's reused vs. what's built here

This project **uses the published `me.proton.core:*` libraries and Proton Drive SDK as-is** (no forks, no modifications). The Kotlin binding is incubating. The app uses it for Photos upload/download, tag updates, trash and permanent deletion. Restore remains on the documented API because Proton's restore endpoint has returned misleading per-link failures for successful restores, which the SDK currently maps as failures. Stream and album hydration, album CRUD/membership, link metadata needed by the app's encrypted thumbnail/cache pipeline, and sharing/invitation management also remain compatibility paths until the SDK binding exposes equivalent behavior and the app can validate parity. Authentication remains ProtonCore-based—the Drive SDK does not provide sign-in. Requests carry this app's own app-version / User-Agent; the client does not impersonate the official app.

**ProtonCore modules consumed** (all `me.proton.core:*` v36.7.0, GPL-3.0, via Maven Central; see `gradle/libs.versions.toml` for the exact list):

**Proton Drive SDK**: `me.proton.drive:sdk:0.31.1-rust` (Kotlin binding, incubating, Maven Central).

- Auth & account: `auth`, `account`, `account-manager`, `user`, `human-verification`
- Crypto & keys: `crypto`, `crypto-android`, `key`
- Network: `network`, `network-data`
- UI plumbing: `presentation`, `presentation-compose`, `account-manager-presentation-compose`, `human-verification-presentation`, `payment-presentation`
- Features & misc: `plan`, `feature-flag`, `data-room`, `util-*`, country/push/challenge/notification helpers, `*-dagger` bind modules for Hilt
- Recovery: `account-recovery`, `user-recovery`

Telemetry / observability modules are pulled in (transitively required by some dagger modules) but **the app does not emit any telemetry events**; there is no analytics call site anywhere in the codebase.

**What this app implements on top** (`app/src/main/kotlin/eu/akoos/photos/data/repository/drive/`):

| File | Responsibility |
|---|---|
| `PhotoUploadService` | Per-file streaming upload pipeline: 4 MB blocks → encrypt + sign → CDN PUT in parallel (bounded). Manifest = sorted block-hash concat + detached signature. Block spill files in `cacheDir`. |
| `PhotoDownloadService` | Cloud → device. Stream + decrypt + apply `DATE_TAKEN` from `captureTime + zone offset`. Optional album subfolder. |
| `PhotoStreamService` | Incremental cloud-state sync (mutation journal). Owns `createOrGetPhotosVolume` lazy bootstrap. |
| `AlbumService` | Album CRUD: create / rename / set-cover, add/remove photos (batched). Mirrors selected device folders into matching Drive albums. |
| `AlbumCryptoChain` | Single source of truth for album-share key selection: album NodeKey decrypt, photo parent-key resolution, and the share-context bundle that downstream code reuses. |
| `AlbumSharingService` | Public link mint, email invite (PKESK encrypt to invitee + sign) with viewer / editor roles, member + invitation management (revoke / remove), leave or stop sharing, shared-with-me (primary + v2 backup endpoints), accept / decline, and server-side "Save to my library" copy. The multi-step share popup (email chips, role picker, optional message) is wired through this service. |
| `PhotosShareService` | Per-user key cache (volumeId, shareId, rootLinkId, rootLinkKeyBytes, rootNodeHashKey) + shared API semaphore. Wiped on sign-out. |
| `PhotosVolumeBootstrap` | First-run Photos volume + share + root-link creation. Handles `ALREADY_EXISTS` race via `getVolumes()` fallback. |
| `CloudTrashService` | Cloud trash listing, restore (`moveTrashLinks`), permanent delete; surfaced as the in-app Trash screen. |
| `ThumbnailDecryptScheduler` | On-demand + background thumbnail decryption: resolves visible cells first (priority-ordered with prefetch), warms the rest of the library in the background, and keeps the decrypted-thumbnail cache within a size budget by evicting the least-recently-used. |
| `RecentUploadsTracker` | Short-TTL (90 s) recently-uploaded-linkIds map; stops stale rows from blocking cloud-delete propagation. |
| `LinkDetailHelpers`, `ThumbnailHelpers`, `PhotoEntityBuilder` | Shared helpers (batch link metadata, JPEG thumbnail ≤512 px, DTO → Room entity mapping). |

Plus the layers around them:

- `data/api/`: Retrofit interface + DTOs for `/drive/photos/*`, `/drive/v2/*`, block uploads, sharing endpoints.
- `data/crypto/DriveCryptoHelper.kt`: thin wrapper over `me.proton.core:crypto*` (no custom crypto primitives; PGP delegated entirely to upstream).
- `data/db/`: Room schema for the local mutation journal (`PhotoListingEntity`, `SyncStateEntity`) with exported schemas + migrations.
- `domain/usecase/`: `UploadPendingUseCase`, `ReconcileSyncStateUseCase`, `DownloadPhotosUseCase`, `FreeUpSpaceUseCase`, `DeletePhotoUseCase`, `GetGalleryItemsUseCase`, `CategorizeItem`.
- `worker/`: `SyncWorker`, `AlbumDownloadWorker`, `FreeUpSpaceWorker`, `CachePruneWorker` (WorkManager + foreground service notifications, battery-aware constraints).
- `presentation/`: Jetpack Compose UI (gallery, viewer, editor, albums, settings, …) and ViewModels.

No private endpoints, no undocumented APIs, no obfuscation (`-dontobfuscate` in `proguard-rules.pro` so the released APK can be byte-compared to a fresh build from this source).

## Disclaimer

Not affiliated with Proton AG. Proton, Proton Drive, and Proton Photos are trademarks of Proton AG. Built against the publicly documented Drive API with no proprietary code from Proton.

## Author

[**Akoos**](https://akoos.eu): creator and maintainer.

## License

[GPL-3.0](LICENSE): matches the upstream `me.proton.core:*` libraries this app links against.
