# NoCloudChat 📡

**Zero-registration, instant local-network chat.**
Launch on any device on the same Wi-Fi or LAN — no sign-up, no server, no internet needed.

---

## Requirements

| Dependency | Version | Notes |
|---|---|---|
| [JDK 21](https://adoptium.net) | 21+ | Eclipse Temurin recommended |
| Gradle Wrapper | 8.11.1 | Bundled — auto-downloads on first run |

No other runtime dependencies. All networking uses Java stdlib (`DatagramSocket`, `ServerSocket`).

---

## Quick Start

```bash
# Run the app (downloads dependencies on first run, ~1 min)
./gradlew run          # Linux / macOS
gradlew.bat run        # Windows
```

That's it. The app opens immediately and begins scanning the local subnet.
To demo peer discovery, run `./gradlew run` on a **second machine** (or a second terminal on the same machine).

> **First-run note**: Gradle will download Compose Multiplatform dependencies (~200 MB). Subsequent runs take ~2 seconds.

---

## How It Works

```
[Instance A]                         [Instance B]
  UDP broadcast → port 54321    →    receives ANNOUNCE
  ← UDP broadcast ← port 54321  ←   sends ANNOUNCE
         peer lists populated on both sides

  click peer B  →  TCP connect → peer B's port  →  deliver message
```

1. **Discovery** — Each instance broadcasts a UDP `ANNOUNCE` packet every 3 seconds to the subnet broadcast address. Peers hear this and appear in the list.
2. **Messaging** — Each instance runs a TCP server on a random OS-assigned port (advertised in the ANNOUNCE). To send a message, a TCP connection is opened, a length-prefixed JSON payload is written, then the connection closes.
3. **UI** — JetBrains Compose Multiplatform renders natively using Skia. State flows via Kotlin coroutines + StateFlow.

---

## Tech Stack

| Component | Technology |
|---|---|
| UI Framework | JetBrains Compose Multiplatform 1.7.3 |
| Language | Kotlin 2.0.21 |
| Runtime | JVM 21 (Temurin recommended) |
| Peer Discovery | Java `DatagramSocket` (UDP broadcast) |
| Messaging | Java `ServerSocket` / `Socket` (TCP) |
| Async | Kotlin Coroutines + StateFlow |
| JSON | org.json (single JAR, zero transitive deps) |
| Build | Gradle 8.11.1 |

---

## Project Structure

```
NoCloudChat/
├── build.gradle.kts             ← Build config
├── settings.gradle.kts
├── gradlew / gradlew.bat        ← Gradle wrapper
├── src/main/kotlin/com/nocloudchat/
│   ├── Main.kt                  ← Entry point (application {})
│   ├── App.kt                   ← Root @Composable
│   ├── model/
│   │   ├── Peer.kt
│   │   └── Message.kt
│   ├── network/
│   │   ├── Discovery.kt         ← UDP broadcast peer discovery
│   │   └── Messenger.kt         ← TCP messaging
│   ├── state/
│   │   └── AppState.kt          ← Reactive state (StateFlow)
│   └── ui/
│       ├── theme/Theme.kt       ← Colours, typography
│       ├── components/Avatar.kt
│       ├── Sidebar.kt           ← Peer list panel
│       ├── ChatPanel.kt         ← Chat view
│       ├── WelcomePanel.kt
│       ├── SettingsDialog.kt
│       └── ToastHost.kt
├── docs/                        ← PRD, architecture, protocol specs
├── design/                      ← Theme, mockups, assets
└── README.md
```

---

## Network Requirements

- All devices must be on the **same subnet** (same Wi-Fi network or LAN segment)
- UDP port **54321** must not be firewalled for broadcast traffic
- TCP on a **random ephemeral port** must be reachable between devices

> **Windows Firewall**: On first run, Windows may prompt to allow Java through the firewall. Allow access on **Private networks**.

---

## Build Native Installer

```bash
./gradlew packageDistributionForCurrentOS
```

Creates platform-native installers:
- Windows: `.msi` in `build/compose/binaries/main/msi/`
- macOS: `.dmg` in `build/compose/binaries/main/dmg/`
- Linux: `.deb` in `build/compose/binaries/main/deb/`

---

## MVP Feature Status

| Feature | Status |
|---|---|
| Subnet peer discovery | ✅ Done |
| Peer list UI | ✅ Done |
| One-to-one text messaging | ✅ Done |
| Display name settings | ✅ Done |
| Message history (session) | ✅ Done |
| Unread message badges + toasts | ✅ Done |
| Dark theme (Compose Material3) | ✅ Done |
| Native distribution packaging | ✅ Ready |
| File sharing | 🔜 Phase 2 |
| Voice messages | 🔜 Phase 2 |
| Group chat | 🔜 Phase 3 |

---

## Tested Platforms

| Platform | Status |
|---|---|
| Windows 11 (JDK 21) | ✅ |
| macOS 14 (JDK 21) | 🔜 |
| Ubuntu 22.04 (JDK 21) | 🔜 |

---

*Built 2026-03-02 / 03*

