# NoCloudChat — Decision Log

All significant architectural and product decisions are recorded here.

---

## DEC-001: Technology Stack

**Date**: 2026-03-02 (initial) → revised 2026-03-03
**Participants**: Tech Lead, PM, CEO
**Status**: Decided (revised)

**Decision**: Use **JetBrains Compose Multiplatform** (Kotlin 2.0.21, Compose 1.7.3, JVM 21).

**Rationale**:
- This is a real production app, not just a hackathon demo — Electron was rejected by CEO for performance reasons
- Compose Multiplatform uses Skia for native GPU rendering (no Chromium/webview overhead)
- JVM networking (`DatagramSocket`, `ServerSocket`) is mature and cross-platform — identical networking capability to Node.js, without browser sandbox constraints
- Kotlin coroutines + StateFlow provide clean reactive state management with zero boilerplate
- Compose desktop supports packaging to native installers (MSI, DMG, DEB) via `packageDistributionForCurrentOS`
- Future mobile support (Android/iOS) possible with the same UI codebase
- Memory baseline ~80–120 MB vs ~200 MB for Electron; startup < 1s after JVM warms up

**Initial stack (rejected)**: Electron + Node.js (performance concerns, large binary, Chromium overhead)
**Alternatives evaluated**: Tauri+Rust, Python+PyQt5, Flutter (see `tech-investigation.md`)

---

## DEC-006: Compose Multiplatform vs Flutter Desktop

**Date**: 2026-03-03
**Participants**: Tech Lead
**Status**: Decided

**Decision**: Use **Compose Multiplatform** over Flutter Desktop.

**Rationale**:
- Flutter was available on the machine but requires Dart — a separate language with a smaller ecosystem
- Compose uses Kotlin which has a larger professional library ecosystem and Android compatibility
- Compose integrates better with JVM networking libraries used for the LAN stack
- CEO specifically requested Compose

---

## DEC-002: Peer Discovery Mechanism

**Date**: 2026-03-02
**Participants**: Tech Lead
**Status**: Decided

**Decision**: Use **UDP broadcast** (not mDNS/Bonjour).

**Rationale**:
- `node-mdns` and similar packages require native compilation (node-gyp), creating platform-specific build issues
- UDP broadcast via `dgram` is pure JavaScript — zero native dependencies
- Works reliably on flat LAN networks (same subnet), which is the target environment
- Simple `ANNOUNCE` format is easy to implement, debug, and extend

**Trade-off**: Does not work across router boundaries (by design — subnet-scoped is a feature, not a bug).

---

## DEC-003: Messaging Transport

**Date**: 2026-03-02
**Participants**: Tech Lead
**Status**: Decided

**Decision**: Use **raw TCP with 4-byte length-prefix framing** (not WebSockets, not HTTP).

**Rationale**:
- Pure Node.js `net` module — zero dependencies
- Length-prefix framing is simpler than WebSocket handshake for LAN messaging
- Fire-and-forget connection model (new TCP per message) eliminates connection state management
- WebSocket (`ws` npm package) would add one dependency without meaningful benefit at this scale

---

## DEC-004: No External Runtime Dependencies

**Date**: 2026-03-02
**Participants**: Tech Lead
**Status**: Decided

**Decision**: The app's only npm dependency is `electron` (devDependency). No runtime npm packages.

**Rationale**:
- Reduces install surface and potential breakage
- Proves the architecture is sound (no duct-tape libraries needed)
- Faster `npm install` in demo environments

---

## DEC-005: In-Memory Message History

**Date**: 2026-03-02
**Participants**: PM, Tech Lead
**Status**: Decided (MVP scope)

**Decision**: Messages are stored in-memory (Map in main process), not persisted to disk.

**Rationale**:
- Simplifies MVP implementation significantly
- Deferred to Phase 2 per PRD scope decisions
- No file I/O means no path/permissions issues across platforms

**Phase 2 plan**: Write history to `~/.nocloudchat/history.json` (append-only, per-peer files).

---

## DEC-007: Migrate to Kotlin Multiplatform for Android Support

**Date**: 2026-03-08
**Participants**: Tech Lead, PM
**Status**: Decided

**Decision**: Migrate the project from a single `kotlin("jvm")` module to **Kotlin Multiplatform** with `desktop` (JVM) and `androidTarget` targets.

**Rationale**:
- Android support is listed as a stretch goal in `CLAUDE.md` and the project constitution
- KMP allows all networking, state management, and UI code to live in `commonMain` and be compiled for both targets with zero duplication
- The `expect`/`actual` mechanism cleanly isolates the handful of platform-specific calls (`openFileInExplorer`, `getDownloadDirectory`, `detectSsid`) without polluting shared code
- JetBrains Compose Multiplatform already supports Android — no additional UI framework needed
- The desktop distribution pipeline is unchanged; `compose.desktop {}` config is preserved as-is
- Sharing ~95% of code between desktop and Android ensures feature parity is maintained as the app evolves

**Trade-offs**:
- Build complexity increases slightly (Android SDK required for Android builds)
- `local.properties` with `sdk.dir` must be set for Android builds (already present)
- `javax.swing.*` calls in `ChatPanel` (file picker) remain desktop-only; Android needs a separate file picker implementation (Phase 2)

---

## DEC-008: Network Passphrase Feature

**Date**: 2026-03-08
**Participants**: Tech Lead, Netsec, PM
**Status**: Decided

**Decision**: Add an opt-in **network passphrase** system where peers filter each other via a SHA-256 hash of a shared passphrase included in UDP ANNOUNCE packets.

**Rationale**:
- Users on shared networks (apartment buildings, offices, hotels) need a way to isolate their NoCloud Chat group from strangers on the same subnet
- SHA-256 hash avoids transmitting the plaintext passphrase over the network
- Enforced at the discovery layer (UDP) — mismatched peers never reach the TCP messaging layer
- Default-off design preserves the zero-configuration experience for home users
- When an unprotected peer receives a protected ANNOUNCE, they are prompted via `SecretJoinDialog` to enter the passphrase, maintaining discoverability while respecting the sender's intent
- Persisted in `~/.nocloudchat/settings.json` so passphrase survives app restarts

**Security note**: SHA-256 of a passphrase is not equivalent to a strong key exchange. This feature provides social/usability isolation, not cryptographic authentication. Full encryption (TLS/Noise) remains a Phase 2 goal.

