# NoCloudChat — System Architecture

**Version**: 2.0 | **Date**: 2026-03-08 | **Author**: Tech Lead

---

## 1. Overview

NoCloudChat is a Kotlin Multiplatform (KMP) application targeting Desktop (JVM) and Android. The UI is built with JetBrains Compose Multiplatform and rendered natively via Skia on all platforms. All networking code runs in shared Kotlin coroutines on the IO dispatcher.

```
┌─────────────────────────────────────────────────────────────────┐
│                        commonMain (shared)                       │
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────────┐ │
│  │  Discovery  │  │  Messenger  │  │       FileTransfer        │ │
│  │ (UDP :54321)│  │  (TCP random│  │  (TCP one-shot per file)  │ │
│  └──────┬──────┘  │   port)     │  └──────────────────────────┘ │
│         │         └──────┬──────┘                               │
│         └────────┬────────┘                                      │
│                  ▼                                               │
│            ┌──────────┐                                          │
│            │ AppState │  (StateFlow ViewModel)                   │
│            └────┬─────┘                                          │
│                 ▼                                                │
│          ┌────────────┐                                          │
│          │  Compose UI│  (App, Sidebar, ChatPanel, …)            │
│          └────────────┘                                          │
└─────────────────────────────────────────────────────────────────┘
         ▲                              ▲
┌────────┴───────────┐      ┌───────────┴──────────┐
│   desktopMain      │      │     androidMain       │
│                    │      │                       │
│  Main.kt           │      │  MainActivity.kt      │
│  Platform.kt       │      │  Platform.kt          │
│  DesktopSsid.kt    │      │  (SSID stub)          │
│  GenerateIcon.kt   │      │                       │
└────────────────────┘      └───────────────────────┘
```

---

## 2. Component Descriptions

### 2.1 Discovery (`commonMain/network/Discovery.kt`)
- **Protocol**: UDP broadcast
- **Port**: 54321 (fixed, `SO_REUSEADDR`)
- **Behaviour**:
  - Broadcasts `ANNOUNCE` JSON to all subnet broadcast addresses every 3 seconds
  - Maintains a `ConcurrentHashMap<peerId, PeerEntry>` with `lastSeen` timestamps
  - Prunes stale peers after 12 seconds of silence
  - Rate-limits inbound broadcasts: max 5 packets per 10 s per source IP
  - Enforces optional network passphrase via `secretHash` (SHA-256) in ANNOUNCE
  - Fires `onPeersChanged` callback when the peer list changes

### 2.2 Messenger (`commonMain/network/Messenger.kt`)
- **Protocol**: TCP with 4-byte big-endian length-prefix framing
- **Port**: OS-assigned random ephemeral port; advertised in each ANNOUNCE
- **Behaviour**:
  - Starts a `ServerSocket(0)` — OS picks port
  - New TCP connection per message (fire-and-forget)
  - Max message payload: 10 MB (enforced on receive)
  - Fires `onMessage(Message)` callback for each valid inbound message

### 2.3 FileTransfer (`commonMain/network/FileTransfer.kt`)
- **Protocol**: Dedicated TCP connection per transfer
- Sender opens a one-shot `ServerSocket(0)`, announces port in `FILE_OFFER` message
- Wire format: 8-byte `Long` total size header, then raw bytes
- Chunked streaming (64 KB buffer) — never loads full file into memory
- Progress callbacks `onProgress`, `onComplete`, `onFailed` wired to `AppState`

### 2.4 AppState (`commonMain/state/AppState.kt`)
- Central reactive state container — acts as a ViewModel
- All mutable state exposed as `StateFlow<T>` (peers, messages, transfers, theme, etc.)
- Launches `Discovery` and `Messenger` during `init`
- Coordinates HELLO handshake: exchanges display names on new peer discovery
- Uses `expect` functions (`openFileInExplorer`, `getDownloadDirectory`, `detectSsidPlatform`) for platform-specific behaviour

### 2.5 UI (`commonMain/ui/`)
- Pure `@Composable` functions — no platform-specific UI code
- `App` — root composable, hosts all dialogs and layout
- `Sidebar` — peer list, network trust bar, settings gear
- `ChatPanel` — message thread, text input, file picker (desktop: Swing JFileChooser)
- `SettingsDialog` — name, dark/light mode toggle, passphrase toggle
- `ToastHost` — animated slide-in notification for background messages
- `SecretJoinDialog` — prompted when a protected peer is discovered

---

## 3. Data Flow: Sending a Message

```
User types text → presses Alt+Enter or Send button
  → ChatPanel: scope.launch { state.sendMessage(peerId, text) }
  → AppState.sendMessage: build Message object
  → Messenger.sendMessage(peer, msg):
      open TCP to peer.ip:peer.port (5s timeout)
      write [4-byte length][JSON payload]
      close connection
  → AppState: appendMessage(peerId, msg) → _messages StateFlow updated
  → Compose recompose: new bubble appears in LazyColumn
```

---

## 4. Data Flow: Receiving a Message

```
Remote instance opens TCP to our Messenger ServerSocket
  → Messenger.handleSocket: read [length][bytes] → parse JSON → onMessage(msg)
  → AppState.handleIncoming(msg):
      if HELLO: update resolvedNames + peer display name, optionally reciprocate
      else: appendMessage, increment unreadCounts, emit ToastEvent
  → StateFlow updates trigger Compose recomposition:
      active chat → new bubble; inactive → unread badge + toast
```

---

## 5. Platform-Specific Layers (expect/actual)

| Function | commonMain (expect) | desktopMain (actual) | androidMain (actual) |
|---|---|---|---|
| `openFileInExplorer(path)` | expect | `java.awt.Desktop.open(parent)` | Android Intent (TODO) |
| `getDownloadDirectory()` | expect | `~/Downloads/NoCloud Chat` | `/storage/emulated/0/Download/…` |
| `detectSsidPlatform()` | expect | Calls `detectSsid()` via OS CLI | null (requires Context) |

---

## 6. Source Set Structure

```
src/
├── commonMain/kotlin/com/nocloudchat/
│   ├── App.kt                    ← Root @Composable
│   ├── Platform.kt               ← expect declarations
│   ├── Preferences.kt            ← Settings persistence
│   ├── model/
│   │   ├── Message.kt
│   │   └── Peer.kt
│   ├── network/
│   │   ├── Discovery.kt          ← UDP peer discovery
│   │   ├── Messenger.kt          ← TCP messaging
│   │   └── FileTransfer.kt       ← File transfer
│   ├── state/
│   │   └── AppState.kt           ← Reactive ViewModel
│   └── ui/
│       ├── theme/Theme.kt
│       ├── components/Avatar.kt
│       ├── Sidebar.kt
│       ├── ChatPanel.kt
│       ├── WelcomePanel.kt
│       ├── SettingsDialog.kt
│       ├── AboutDialog.kt
│       ├── ToastHost.kt
│       └── SecretJoinDialog.kt
├── desktopMain/kotlin/com/nocloudchat/
│   ├── Main.kt                   ← application {} entry point
│   ├── Platform.kt               ← actual implementations
│   ├── network/DesktopSsid.kt    ← SSID via OS CLI (netsh/airport/nmcli)
│   └── tools/GenerateIcon.kt     ← Icon generator utility
└── androidMain/kotlin/com/nocloudchat/
    ├── MainActivity.kt           ← ComponentActivity entry point
    └── Platform.kt               ← actual implementations (stubs)
```

---

## 7. Security Model

- **Network passphrase**: opt-in SHA-256 hash in ANNOUNCE; mismatched peers silently dropped
- **Rate limiting**: max 5 UDP packets per 10 s per source IP; prevents broadcast flooding
- **Message size caps**: 512 bytes max UDP, 10 MB max TCP payload
- **Input validation**: all inbound JSON validated before processing; malformed packets dropped
- **No external access**: zero outbound connections outside the LAN subnet
- **Peer table cap**: max 32 peers to prevent memory exhaustion
- **Trust store**: per-network-ID trust recorded in `~/.nocloudchat/settings.json`

See `docs/security.md` for the full threat model.
