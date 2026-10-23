# Oneshot Build Prompt — Veyra: Super-Optimized Android Minecraft Launcher
### (Complete feature + mechanics specification — hand this entire document to a coding agent as a single prompt)

This document assumes the agent has **zero prior context**. Every term, system, screen, and mechanic is defined inline. Paste the whole thing as one message/prompt.

---

## PART 0 — Who you are building this for, and why

I am an Android developer with three existing assets to integrate rather than redesign:

1. **Veyra** (`dev.whaltermc.veyra`) — my in-progress Kotlin + Jetpack Compose Android app, already containing account management (local/Microsoft/Ely.by) and a `MinecraftRuntime` stub that builds JVM/game launch arguments. It is compiled **on-device via Termux**, not Android Studio: Termux's `aapt2` binary is substituted in, and the Termux SDK path is set in `local.properties`. Do not re-derive or replace this toolchain — build inside it.
2. **FristOneRR PanVK** — a Mesa-based Vulkan driver I maintain for Mali GPUs using the "kbase" kernel interface (e.g. Mali-G57 MC2). It is a real, working Vulkan implementation, currently packaged for a Winlator/DXVK wrapper use case. I am porting it to load like a Turnip driver blob (Turnip = the open-source Vulkan driver for Adreno GPUs, loaded as a swappable `.so` by Android GPU-driver-switching frameworks) so it can serve as a native Vulkan backend for Minecraft, and as the GPU driver underneath Zink.
3. **RustGL** — a Rust plugin I wrote that translates desktop OpenGL calls (what Minecraft's LWJGL3 renderer emits) into OpenGL ES calls (what Android supports), built originally for ZalithLauncher 2.

### Background (explained, not assumed)
- **Minecraft: Java Edition has no native Android support.** It's a desktop Java app using LWJGL3 (a Java graphics/windowing/input library) for rendering via desktop OpenGL. Existing launchers (PojavLauncher, ZalithLauncher 2, Boardwalk, Amethyst) work by bundling an Android-compatible JVM, translating LWJGL's OpenGL calls into something Android's GPU driver can run, and emulating a desktop window/input surface on a touchscreen. This app does the same, better.
- **Zink**: a Mesa component implementing OpenGL on top of Vulkan, so OpenGL-only code can run on a Vulkan-only driver. It needs a real Vulkan driver underneath.
- **GL4ES**: an older OpenGL→OpenGL ES translation library, used as the universal fallback by existing launchers. Correctness-first, slow.
- **Why a custom render chain matters**: existing launchers converge on GL4ES or generic Zink with a generic Vulkan driver, leaving Mali performance on the table. Chaining **PanVK → Zink → RustGL → GL4ES**, auto-selected per device, should beat them on Mali hardware while still working everywhere else.

### Definition of done
A finished-feeling (not developer-tool-feeling) Kotlin + Compose Android app where a user logs in, installs any version/modloader combo, configures mods/resources/controls, and launches the game using whichever render backend is fastest on their device — automatically, with manual override available. No visible debug output unless explicitly enabled.

### Hard constraints
- Package `dev.whaltermc.veyra`, built via Termux as described.
- Min SDK 24+; hot-path performance tuning targets Android 10+ on Mali-G57/G68/G72-class GPUs.
- Fully offline-capable except auth, downloads, and mod-browser calls.
- No telemetry, no ads, no bundled copyrighted Minecraft assets — user supplies their own game ownership via Microsoft account or existing local files.

---

## PART 1 — Full screen inventory (every screen, every function on it)

### 1.1 Splash / cold-start
- Shows app icon + name while: (a) checking for an existing valid session token, (b) running the one-time device GPU-backend benchmark if it has never run on this install, (c) loading the instance list from disk.
- Function `warmBoot()`: if a previous launch's resolved asset/library graph is cached for the most-recently-used instance, pre-warm it on a background thread while the splash is showing, so the first real launch after opening the app is fast.

### 1.2 Login / account screen
- Three auth paths, each its own function and UI state machine:
  - **Microsoft OAuth device-code flow**: call Microsoft's device-code endpoint → display a short code + URL (and a "copy code" button + "open browser" button) → poll the token endpoint on an interval with exponential backoff → on success, exchange the Microsoft token for an Xbox Live token → exchange for an XSTS token → exchange for a Minecraft Services token → fetch the Minecraft profile (UUID, username, skin URL, cape list) → persist encrypted tokens (access + refresh) to local secure storage.
  - **Ely.by login**: username/password form → OAuth2 flow against Ely.by's endpoints → persist session the same way.
  - **Offline/local profile**: username-only form, client-side UUID generated deterministically from the username (so the same offline name always maps to the same UUID across sessions), no network call, explicitly labeled "offline — multiplayer on most servers will not work."
- **Account switcher**: list of all logged-in accounts (can hold more than one simultaneously), each showing skin head render, username, auth type icon, and "last used" timestamp. Functions: `switchActiveAccount(id)`, `removeAccount(id)` (wipes tokens + optionally cached skin), `refreshAccount(id)` (forces a token refresh / re-fetch of profile+skin now).
- **Token refresh**: background function `ensureValidToken(accountId)` called before every launch; if the Microsoft/Ely.by token is expired, silently refreshes using the stored refresh token with no UI interruption; only falls back to showing the login screen if the refresh token itself is invalid/expired.

### 1.3 Instance list (home screen)
- Grid/list (toggleable, persisted preference) of instances. Each card shows: instance icon (custom or auto-generated from modloader+version), name, version string, modloader badge, last-played timestamp, play-time total, and a small render-backend indicator icon.
- Functions per card: `launch(instanceId)`, `openInstanceSettings(instanceId)`, `duplicateInstance(instanceId)`, `deleteInstance(instanceId)` (confirmation dialog, option to keep or wipe the save directory), `exportInstance(instanceId)` (zips the instance folder + a manifest into a shareable file), `pinInstance(instanceId)` (pins to top of list).
- Top bar: search/filter box (by name, version, modloader), sort dropdown (name, last played, play time, date created), "+ New Instance" button, account switcher avatar, global settings gear icon.
- Empty state: first-run illustration + "Create your first instance" call to action.

### 1.4 New/edit instance screen
- **Name field** (free text, defaults to a generated name like "Fabric 1.21.1" but editable).
- **Version picker**: tabs for Release / Snapshot / Old Beta / Old Alpha, pulled from Mojang's public version manifest; search box; each entry shows release date.
- **Modloader picker**: None (vanilla), Fabric, Quilt, Forge, NeoForge — each shows its own loader-version sub-list once the base game version is picked (loader versions are fetched from that loader's own metadata API and filtered to ones compatible with the chosen game version).
- **Memory allocation slider**: min/max RAM in MB, bounded to a safe range computed from `ActivityManager.getMemoryInfo()` (never allow a setting that would reliably trigger an OOM-kill of the whole app).
- **Java version indicator**: auto-computed read-only field showing which bundled JRE major version this combination requires (8/11/17/21), with a manual override dropdown for advanced users.
- **Render backend override**: dropdown — Auto (default), PanVK (native Vulkan), Zink+PanVK, RustGL, GL4ES — with a short description of each shown on selection.
- **Advanced JVM args field**: free text, pre-filled with the auto-generated args (see Part 3), editable, with a "reset to default" button.
- **Icon picker**: choose from a built-in set or import an image, cropped to a square.
- Save button: validates all required fields, writes the instance manifest (see Part 5 data model), creates the isolated instance directory.

### 1.5 Instance detail / pre-launch screen
Reached by tapping an instance without launching immediately (long-press, or an "i" button on the card). Tabs:
- **Overview**: version/modloader/last-played/play-time summary, big "Play" button, quick-access to the four tabs below.
- **Mods tab**: see 1.6.
- **Resource Packs tab**: list of installed packs with drag-to-reorder (pack priority order matters in Minecraft — top of list = highest priority), enable/disable toggle per pack, "+ Add" (browse built-in catalog or import a `.zip`), thumbnail preview generated from the pack's `pack.png`.
- **Shader Packs tab**: same pattern as resource packs but for shader `.zip` files; only one active at a time (radio-button style, not multi-enable); shows a compatibility warning if the selected render backend doesn't support the shader's required OpenGL/Vulkan feature set.
- **Worlds/Saves tab**: list of local singleplayer worlds inside this instance, with thumbnail (from the world's icon.png), last-played date, gamemode/difficulty badges, and `deleteWorld()`, `duplicateWorld()`, `renameWorld()`, `exportWorld()` (zips the world folder), `importWorld()` (unzips a shared world folder into this instance).
- **Logs tab**: live-tailed `latest.log` while running, or the last session's full log when not running, with search/filter by log level, and a "share log" button that exports it as text.
- **Screenshots tab**: gallery of screenshots taken in this instance (Minecraft's F2 screenshot key mapped to an on-screen button — see 1.8), with share/delete functions.

### 1.6 Mod manager
- **Installed tab**: list of mods currently in this instance, each with name, version, icon, enable/disable toggle (renames the jar to `.jar.disabled` without deleting it), `uninstall()`, and a dependency-conflict warning badge if two installed mods declare incompatible version ranges of a shared dependency.
- **Browse tab**: search box hitting both Modrinth and CurseForge public APIs in parallel, merged results tagged by source, filterable by category (performance, tech, adventure, etc.) and filtered automatically to only show mods compatible with this instance's game version + modloader.
- **Mod detail sheet**: description (rendered from the API's markdown/HTML), screenshots, version list (filtered to compatible versions), dependency list shown before install with an "also install dependencies" checkbox pre-checked.
- **Install function** `installMod(modId, versionId)`: downloads the jar, verifies its hash against the API-provided hash, resolves and recursively offers/installs declared dependencies, writes to this instance's isolated mods folder only.
- **Manual import**: file picker for `.jar` files not available on either platform; same dependency-conflict check applied, with a "mod could not be verified, install anyway?" warning since there's no API metadata to cross-check.
- **Update checker**: background function `checkModUpdates(instanceId)` compares installed mod versions against the latest compatible version on their source platform; surfaces an "Updates available" badge with a one-tap "update all" action.

### 1.7 Live gameplay session (the actual running game)
- Boot sequence screen (replaces the UI once Play is tapped): progress stages shown as text + progress bar — "Verifying files" → "Downloading missing assets" (only if needed) → "Preparing Java runtime" → "Initializing render backend: {name}" → "Starting Minecraft". Each stage is a real function (`verifyInstanceFiles()`, `downloadMissingAssets()`, `prepareJre()`, `initRenderBackend()`, `launchJvm()`), not a fake timer.
- Once the JVM's render surface attaches, the Compose UI **fully unmounts** (not just hidden behind the game view) so it costs zero CPU/GPU overhead during gameplay — this is a named requirement, not incidental.
- **On-screen control overlay** rendered as a separate lightweight native layer (not Compose — see 1.9), touch events routed directly to it with no recomposition in the hot path.
- **In-game quick menu**: a single persistent small button (position configurable) that opens a slide-out panel without pausing/exiting the game, containing: FPS/backend debug overlay toggle, screenshot button, mute/volume, "edit controls" shortcut, and "exit to launcher" (with confirmation if a singleplayer world is open, to avoid accidental progress loss — actually just triggers Minecraft's normal save-and-quit, doesn't force-kill the process).
- **Crash handling in-session**: if the render backend throws an unrecoverable error mid-session, function `handleBackendFailure()` captures the crash log, attempts the next backend in the fallback chain, and relaunches the same instance automatically with a brief "Switching render backend and recovering…" toast — never a silent hard-crash to the home screen. If all backends in the chain fail, show a clear error screen with the captured log and a "copy diagnostic bundle" button, not a generic Android ANR/crash dialog.

### 1.8 Control layout editor
- Visual canvas overlaying a static background image of a Minecraft world, with draggable/resizable elements: movable joystick (for WASD-equivalent movement), action buttons (jump, sneak, sprint toggle, inventory, chat, drop item, swap hands, attack/mine, use/place — each individually placeable and resizable), a full on-screen hotbar (9 slots, tap to select), and an optional on-screen D-pad for look/camera as an alternative to touch-drag-to-look.
- Functions: `addButton(type)`, `removeButton(id)`, `resizeButton(id, scale)`, `moveButton(id, x, y)`, `setButtonOpacity(id, alpha)`, `saveLayout(name)`, `loadLayout(name)`, `setDefaultLayout(instanceId, layoutName)` (per-instance default, since a mod-heavy instance might need extra buttons a vanilla one doesn't).
- **Preset layouts** shipped: Vanilla Survival, Creative/Building, PvP, Minimal (just movement+look, for keyboard/mouse users who only need touch as backup).
- **Touch-to-look sensitivity slider** and **dead-zone slider** for the joystick, both live-previewed on the editor canvas.
- **Physical input detection**: function `detectPhysicalInput()` listens for a connected Bluetooth/USB-OTG keyboard or mouse via Android's `InputDevice` APIs; when detected, auto-switches the active control scheme to keyboard+mouse pass-through and dims (not removes — user can still touch) the on-screen overlay, with a setting to disable this auto-switch.

### 1.9 Settings screen
Organized into sections, every toggle/value is a real persisted setting (not cosmetic):
- **Render**: global default backend (Auto/PanVK/Zink/RustGL/GL4ES), "re-run device benchmark" button, debug overlay default-on toggle, shader-pipeline-cache management (`clearPipelineCache()`, shows current cache size).
- **Performance**: default render-distance/graphics-preset mapping applied to new instances, background-download throttling (don't saturate the connection while the game is running), "pause downloads on mobile data" toggle.
- **Storage**: per-instance disk usage breakdown, `clearUnusedAssets()` (removes cached asset/library files not referenced by any current instance), `clearLogs()`, default install location if external storage is available.
- **Accounts**: same list as 1.2's switcher, reachable here too.
- **Controls**: default control layout, global sensitivity defaults, physical-input auto-switch toggle.
- **Diagnostics**: `exportDiagnosticBundle()` — bundles device info (model, Android version, GPU vendor/renderer string, RAM), chosen/available backends, last crash log, and recent app logs into a single shareable file for bug reports. No bundle is ever sent anywhere automatically — it's generated locally and shared via the normal Android share sheet only when the user taps share.
- **About**: version info, licenses for PanVK/RustGL/GL4ES/Zink and other bundled open-source components, link to source if applicable.

---

## PART 2 — The render backend system (core mechanic, specified in full)

### 2.1 Backend definitions
| Backend | What it is | When it's chosen |
|---|---|---|
| Native Vulkan (PanVK) | FristOneRR PanVK loaded directly as the Vulkan ICD (Installable Client Driver) for LWJGL's Vulkan path, when a given LWJGL/mod combo renders via Vulkan directly | First choice on Mali-kbase devices when the instance's rendering path supports Vulkan directly |
| Zink + PanVK | Zink (GL-over-Vulkan) using PanVK as its underlying Vulkan driver | Second choice: when the instance needs an OpenGL surface (most vanilla/modded Minecraft) but we still want Mali-tuned Vulkan underneath |
| RustGL | Custom Rust GL-to-GLES translation plugin | Third choice: devices/configs where Vulkan (native or via Zink) isn't viable, e.g. non-Mali GPUs without a working PanVK target, or specific mod incompatibilities with Zink |
| GL4ES | Generic OpenGL-to-GLES translation | Last resort, maximum compatibility, used when nothing above initializes successfully |

### 2.2 Device benchmark (`runBackendBenchmark()`)
Runs once per install (cached result keyed by a hash of GPU vendor/renderer string + driver version, re-run automatically if that hash changes, e.g. after a system update):
1. Read GPU vendor/renderer string via `GLES20.glGetString`.
2. Attempt to initialize each backend in preference order inside a short-lived throwaway surface.
3. For each that initializes successfully, run an identical short synthetic workload (a fixed sequence of draw calls representative of Minecraft's chunk-mesh rendering) and record average frametime.
4. Rank by frametime among successfully-initialized backends; persist the winner as the device default, and persist the full ranked list (so the fallback chain in 1.7's crash handling has a real order to walk, not just a hardcoded guess).
5. Store results in a local benchmark-results table, timestamped, viewable in Settings → Render for transparency.

### 2.3 Runtime backend switching
- `initRenderBackend(instanceId)`: reads instance override if set, else device default; attempts init; on failure, logs the failure reason and calls itself recursively with the next backend in the persisted ranked list; only shows an error to the user if every backend in the list fails.
- `getLiveBackendStats()`: while running, exposes current backend name, instantaneous FPS, 1%-low FPS over a rolling window, and frametime, consumed by the in-game debug overlay (1.7) and nowhere else (never shown unprompted outside the overlay toggle).

### 2.4 Shader/pipeline cache
- `persistPipelineCache(backend, instanceId)`: after a session, if the backend is Vulkan-based (native or Zink), serialize its compiled pipeline cache to disk keyed by backend+driver-version+instance's mod/shader set, so a cold start doesn't recompile shaders it already compiled last time.
- `invalidatePipelineCache()` called automatically when the mod list, shader pack, or PanVK driver version changes for that instance (stale caches referencing changed shaders must never be reused).

---

## PART 3 — JVM & runtime management (full mechanics)

### 3.1 JRE management
- `resolveRequiredJreMajor(gameVersion, modloader)`: deterministic lookup table (Java 8 for ≤1.16, Java 16 for 1.17, Java 17 for 1.18–1.20.4, Java 21 for 1.20.5+, with modloader-specific overrides where a loader pins a different requirement).
- `ensureJreInstalled(major)`: checks a local per-major-version JRE cache directory; if missing, downloads a Termux-compatible prebuilt JRE for that major from a trusted distribution (e.g. Adoptium builds packaged for Android/Termux), verifies checksum, extracts.
- JREs are shared across instances that need the same major (not duplicated per-instance) to save storage, but each instance's launch command points at the correct shared JRE explicitly.

### 3.2 Argument generation (`buildLaunchArgs(instance)`)
Auto-generates, in order:
1. JVM memory flags from the instance's RAM slider (`-Xms`, `-Xmx`).
2. GC flags: G1GC with a pause-time target tuned for mobile (`-XX:+UseG1GC -XX:MaxGCPauseMillis=…`), sized to avoid excessive heap-region overhead on constrained RAM.
3. Native library path flags pointing at the resolved LWJGL3 natives for `arm64-v8a` and the render backend's native libraries.
4. System properties needed by the render backend (e.g. forcing the Vulkan ICD path to PanVK's `.so`, or forcing LWJGL to prefer a specific GL/Vulkan path depending on the chosen backend).
5. Standard Minecraft game arguments: username/UUID/access-token (from the active account), game directory (this instance's isolated folder), assets directory + index, version name, user type.
6. Modloader-specific main-class and tweaker/launch-wrapper arguments when the instance isn't vanilla.
- The generated string is what populates the "Advanced JVM args" field in 1.4 — it's a real generator, not a static template, and user edits to that field are preserved per-instance and merged with (not silently overwritten by) future regenerations.

### 3.3 Fast warm-boot
- `cacheLaunchGraph(instanceId)`: after a successful launch, persist the fully-resolved library/asset dependency graph (which files were needed, where they resolved from) so the next launch of the same instance can skip re-walking Mojang's version JSON and re-verifying every file hash, falling back to a quick existence+size check instead, with a full re-verify only triggered if the instance's version/mods changed since the cache was written.

### 3.4 Asset/library verification
- `verifyInstanceFiles(instanceId)`: checks required libraries, natives, and asset objects against expected hashes from the version manifest; `downloadMissingAssets(instanceId)`: downloads only what's missing/mismatched, on a background thread pool, never blocking the render thread once gameplay starts, with a visible progress bar during the pre-launch boot sequence (1.7).

---

## PART 4 — Account/auth mechanics (full detail, expanding 1.2)

- Microsoft flow implemented as a real multi-step token exchange (not a WebView login scrape): device code → OAuth token → Xbox Live `user.auth.xboxlive.com` → XSTS `xsts.auth.xboxlive.com` → Minecraft Services login-with-xbox → profile fetch from `api.minecraftservices.com`.
- All tokens stored using Android's EncryptedSharedPreferences (or equivalent Keystore-backed storage), never in plaintext.
- `ensureValidToken()` runs before every file-verification step in the boot sequence (3.4), not just at app start, so a token that expired while the app was backgrounded doesn't cause a launch failure.
- Skin/cape rendering: fetch and cache the account's current skin texture for use in the account switcher avatar and instance cards; refresh on manual pull-to-refresh in the account switcher, not polled continuously.

---

## PART 5 — Data model (what gets persisted, and where)

- **App-level settings**: single key-value store (render defaults, control defaults, storage path, diagnostics prefs).
- **Accounts table**: id, display name, UUID, auth type, encrypted token blob, skin cache path, last-used timestamp.
- **Instances table**: id, name, icon path, game version, modloader + loader version, java major override, RAM min/max, render backend override, advanced JVM args (user-edited delta from generated default), created/last-played timestamps, total play-time seconds, pinned flag.
- **Per-instance directory layout**: `instances/{id}/` containing `mods/`, `resourcepacks/`, `shaderpacks/`, `saves/`, `screenshots/`, `config/`, `logs/`, and an `instance.json` manifest mirroring the instances-table row (so an exported/imported instance is self-describing).
- **Shared caches** (not per-instance): `jre/{major}/`, `libraries/`, `assets/{hash-prefixed objects + indexes}`, `natives/{backend}/`, `pipeline-cache/{backend}-{driver-version}-{instance-content-hash}`.
- **Benchmark results table**: device GPU hash → ranked backend list + frametimes + timestamp.

---

## PART 6 — Build order (do not skip stages; report status at each)

1. **Boot plumbing**: splash → account screen → instance list, all with real (not mocked) persistence, restyled to a coherent Minecraft-launcher visual identity built on the existing Veyra Compose UI.
2. **One vanilla instance, GL4ES only**: proves JVM resolution, argument generation, asset verification/download, and launch end-to-end using only the universal fallback backend.
3. **Native Vulkan (PanVK) backend**: wire in the benchmark (2.2), get PanVK selected automatically on Mali test hardware, verify the debug overlay shows backend name + live FPS.
4. **Zink+PanVK and RustGL backends**: verified on devices/configs where native Vulkan direct-path isn't used, confirming the fallback chain (2.3) actually walks correctly on induced failures.
5. **Full instance management**: mod manager (1.6) including Modrinth+CurseForge browse/install/dependency-resolution, resource/shader pack manager, worlds tab, logs tab, screenshots tab.
6. **Modloader support**: Fabric, Quilt, Forge, NeoForge instances launching with mods installed, each correctly resolving its own JRE major and launch args.
7. **Control system**: editor (1.8), presets, physical-input auto-detect/switch, verified both touch-only and with a Bluetooth keyboard+mouse connected.
8. **Crash recovery + diagnostics**: deliberately break the preferred backend mid-session and confirm automatic fallback + relaunch works; verify the diagnostic bundle export (1.9) contains everything listed.
9. **Settings completeness pass**: confirm every toggle/value listed in Part 1.9 is wired to a real, functioning effect — nothing cosmetic/no-op.

At each numbered stage, stop and report: what works, what doesn't, and the single next concrete blocker. Do not silently continue past a broken stage to work on polish.
