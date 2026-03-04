# NoCloudChat — Technology Investigation

**Version**: 1.0 | **Date**: 2026-03-02 | **Author**: Tech Lead

---

## 1. Objective

Select the optimal cross-platform desktop application framework for NoCloudChat, with emphasis on:
1. Native UDP/TCP networking (critical for subnet peer discovery)
2. Cross-platform support (Windows, macOS, Linux)
3. Minimal runtime dependencies and fast startup
4. Developer velocity for a hackathon context

---

## 2. Candidates Evaluated

### Option A — Electron + Node.js
- **Language**: JavaScript/TypeScript
- **UI**: Chromium-based (HTML/CSS/JS)
- **Networking**: Node.js built-ins (`dgram`, `net`)

### Option B — Tauri + Rust
- **Language**: Rust (backend) + JavaScript/TypeScript (frontend)
- **UI**: OS webview (WebKit/Edge WebView2)
- **Networking**: Rust `std::net` or `tokio`

### Option C — Python + PyQt5
- **Language**: Python
- **UI**: Qt5 (native widgets)
- **Networking**: Python `socket` stdlib

---

## 3. Evaluation Matrix

| Criterion | Weight | Electron+Node | Tauri+Rust | Python+PyQt5 |
|---|---|---|---|---|
| Cross-platform support | Critical | ✅ Excellent | ✅ Good | ✅ Good |
| LAN/UDP networking | Critical | ✅ Native `dgram` | ✅ Native | ✅ Native `socket` |
| No extra runtime needed | High | ✅ None | ✅ None | ⚠️ Python runtime |
| Developer speed (hackathon) | High | ✅ Very fast | ⚠️ Slower (Rust) | ✅ Fast |
| UI quality / flexibility | High | ✅ Full web stack | ✅ Full web stack | ⚠️ Qt widgets |
| Binary / startup size | Medium | ⚠️ ~180 MB | ✅ ~5 MB | ⚠️ Variable |
| Audio recording (Phase 2) | Medium | ✅ Web Audio API | ✅ Via JS | ⚠️ PyAudio needed |
| Ecosystem maturity | Medium | ✅ Mature | ⚠️ Growing | ✅ Mature |
| Mobile support (stretch) | Low | ❌ Desktop only | ❌ Desktop only | ❌ No |

---

## 4. Detailed Analysis

### Electron + Node.js
**Pros**:
- UDP broadcast via `dgram.createSocket` works identically on all platforms with zero configuration
- TCP sockets via `net.createServer` / `net.Socket` — no extra libraries
- Rich ecosystem of npm packages for future features
- Familiar web tech for UI — fast iteration speed
- IPC between main/renderer is well-documented and battle-tested
- Largest community support and most examples

**Cons**:
- Large binary size (~150-200 MB) — not a concern for LAN use
- Higher memory usage than Tauri (~100-150 MB baseline)

### Tauri + Rust
**Pros**:
- Tiny binary (~5-15 MB), very fast startup
- Rust's strong networking primitives

**Cons**:
- Rust learning curve adds risk in a time-constrained hackathon
- Tauri IPC between Rust ↔ JS is more complex than Electron's
- UDP broadcast on Windows requires `SO_BROADCAST` setsockopt — more complex in Rust tokio

### Python + PyQt5
**Pros**:
- Very fast to write networking code
- Python `socket` module is simple

**Cons**:
- Requires Python runtime installation on target machine
- PyQt5 licensing (GPL) — commercial use requires paid licence
- Qt UI looks dated compared to web-based UI
- No built-in audio recording (needs `pyaudio` which has native build issues on Windows)

---

## 5. Decision

**Selected: Electron + Node.js (vanilla JS, no bundler)**

**Rationale**:
1. Node.js `dgram` and `net` modules provide the exact networking primitives needed — UDP broadcast and TCP sockets — with zero additional dependencies
2. The app requires zero external npm dependencies beyond `electron` itself for the MVP
3. Fastest path to a working, demountable app in a hackathon setting
4. The Chromium renderer gives us a full CSS/HTML UI toolkit enabling the warm, polished design required by the brief
5. No build step needed for MVP (vanilla JS renderer loaded directly from disk)

**Build simplicity**: `npm install && npm start` — two commands, always works.

