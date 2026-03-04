# NoCloudChat — System Architecture

**Version**: 1.0 | **Date**: 2026-03-02 | **Author**: Tech Lead

---

## 1. Overview

NoCloudChat uses an Electron shell with a strict process separation between the main (Node.js) process and the renderer (browser sandbox). All networking runs in the main process; the renderer only handles UI.

```
┌─────────────────────────────────────────────────────────────┐
│                        NoCloudChat Process                    │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    Main Process (Node.js)             │   │
│  │                                                      │   │
│  │  ┌──────────────┐    ┌─────────────────────────────┐ │   │
│  │  │  Discovery   │    │       Messenger             │ │   │
│  │  │  (UDP :54321)│    │  (TCP server: random port)  │ │   │
│  │  └──────┬───────┘    └───────────────┬─────────────┘ │   │
│  │         │                            │               │   │
│  │         └──────────┬─────────────────┘               │   │
│  │                    │ IPC (contextBridge)              │   │
│  └────────────────────┼─────────────────────────────────┘   │
│                       │                                      │
│  ┌────────────────────┼─────────────────────────────────┐   │
│  │              Renderer Process (Chromium)             │   │
│  │                    │                                 │   │
│  │  ┌─────────────────▼───────────────────────────────┐ │   │
│  │  │         app.js (Vanilla JS SPA)                 │ │   │
│  │  │   Peer List View ↔ Chat View ↔ Settings Panel   │ │   │
│  │  └─────────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Component Descriptions

### 2.1 Discovery (`src/network/discovery.js`)
- **Protocol**: UDP broadcast
- **Port**: 54321 (fixed, well-known)
- **Behaviour**:
  - Binds to port 54321 with `reuseAddr: true` to allow multiple instances per host
  - Broadcasts `ANNOUNCE` JSON to subnet broadcast address every 3 seconds
  - Listens for `ANNOUNCE` from other instances
  - Maintains a `Map<peerId, PeerInfo>` with `lastSeen` timestamps
  - Prunes stale peers every 5 seconds (timeout = 12s)
  - Fires `onPeersChanged` callback to main.js when peer list changes

### 2.2 Messenger (`src/network/messenger.js`)
- **Protocol**: TCP with length-prefix framing
- **Port**: OS-assigned random port (announced via Discovery)
- **Behaviour**:
  - Starts a TCP server on `0.0.0.0:0` (OS picks port)
  - Incoming connections: framing buffer handles partial reads
  - Outgoing messages: new TCP connection per message → write → close
  - Fires `onMessage` callback for each complete received message

### 2.3 Main Process (`src/main.js`)
- Initialises Messenger first (to get TCP port), then Discovery
- Maintains in-memory message history (`Map<peerId, Message[]>`)
- Registers IPC handlers (get-peers, send-message, get-messages, etc.)
- Pushes peer updates and new messages to renderer via `webContents.send`

### 2.4 Preload (`src/preload.js`)
- Uses `contextBridge.exposeInMainWorld` to expose a `window.noCloudChat` API
- All IPC is one-directional through typed methods — no raw IPC access

### 2.5 Renderer (`src/renderer/`)
- Vanilla JS single-page app (no framework, no build step)
- `app.js`: state management, DOM manipulation, event handlers
- `index.html`: static structure
- `style.css`: full dark theme with CSS custom properties

---

## 3. Data Flow: Sending a Message

```
User types text → clicks Send
  → renderer: window.noCloudChat.sendMessage(peerId, text)
  → IPC invoke: 'send-message'
  → main.js: look up peer in Discovery.getPeerList()
  → main.js: Messenger.sendMessage(peer, payload)
    → open TCP connection to peer.ip:peer.port
    → write [4-byte length][JSON payload]
    → close connection
  → return msg object to renderer
  → renderer: append bubble to chat view
```

---

## 4. Data Flow: Receiving a Message

```
Remote instance sends TCP data to our Messenger server
  → Messenger._handleSocket: buffer → parse JSON → onMessage callback
  → main.js: store in messageHistory, send IPC event 'new-message'
  → preload: ipcRenderer.on('new-message', callback)
  → renderer: append bubble if active chat, else show toast + badge
```

---

## 5. IPC API Summary

| Channel | Direction | Description |
|---|---|---|
| `get-my-info` | renderer→main | Get own peer ID and display name |
| `get-peers` | renderer→main | Get current peer list |
| `send-message` | renderer→main | Send text to a peer |
| `get-messages` | renderer→main | Get message history for a peer |
| `set-display-name` | renderer→main | Update display name |
| `peers-updated` | main→renderer | Push: peer list changed |
| `new-message` | main→renderer | Push: new incoming message |

---

## 6. Security Model

- `contextIsolation: true` — renderer cannot access Node APIs directly
- `nodeIntegration: false` — Node.js not available in renderer
- `sandbox: false` — needed only for preload script
- All networking runs exclusively in main process
- No external network access (no HTTP calls, no DNS lookups outside LAN)

