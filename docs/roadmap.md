# NoCloudChat — Product Roadmap

**Date**: 2026-03-02

---

## Phase 1 — MVP (Hackathon Demo)

**Goal**: Runnable, demountable app showing core value proposition.

| Feature | Owner | Status |
|---|---|---|
| Project scaffolding | Tech Lead / Dev | ✅ Done |
| UDP peer discovery | Dev | ✅ Done |
| TCP messaging | Dev | ✅ Done |
| Peer list UI | Dev | ✅ Done |
| Chat UI | Dev | ✅ Done |
| Display name settings | Dev | ✅ Done |
| Unread badges + toast notifications | Dev | ✅ Done |
| Dark theme / design system | Design | ✅ Done |
| README + docs | PM / Tech Lead | ✅ Done |

---

## Phase 2 — Enhanced Communication

**Goal**: Richer messaging capabilities.

| Feature | Priority | Notes |
|---|---|---|
| File transfer (drag & drop) | High | ✅ Done — Chunked TCP transfer |
| Network passphrase (security) | High | ✅ Done — SHA-256 hash in ANNOUNCE |
| Android support (KMP) | High | ✅ Done — Kotlin Multiplatform |
| Voice message recording | High | Kotlin audio capture + send as binary |
| Typing indicator | Medium | TYPING message type |
| Message delivery receipts | Medium | DELIVERED message type |
| Persistent message history | Medium | `~/.nocloudchat/` local storage |
| End-to-end encryption | High | TLS or Noise protocol for privacy |

---

## Phase 3 — Advanced Features

**Goal**: Team-level features and polish.

| Feature | Priority | Notes |
|---|---|---|
| Group chat rooms | High | New ROOM message type |
| Emoji reactions | Medium | React to messages |
| Presence / status | Medium | Away, Busy, Available |
| System tray / notifications | Medium | OS-native notifications |
| Custom avatar | Low | Image file or initials |
| Light/dark theme toggle | Low | ✅ Done — Compose Material3 theme swap |

---

## Stretch Goals

| Feature | Notes |
|---|---|
| iOS / Android app | Android ✅ Done via KMP; iOS possible with Compose Multiplatform |
| Screen sharing | Platform-specific screen capture (LAN only) |
| Ephemeral channels | Auto-delete after session |

