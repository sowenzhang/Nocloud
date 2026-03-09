# NoCloudChat — Project Memory

## Project Overview
- Zero-config LAN chat app — no internet, no accounts, no server
- Kotlin Multiplatform + Compose Desktop/Android
- Location: C:\src\hackathon\NoCloudChat

## Current Architecture (as of 2026-03-08)
- **Build**: Kotlin Multiplatform (`kotlin("multiplatform")`), AGP 8.2.2, JDK 21, Gradle 8.11
- **Targets**: `jvm("desktop")` + `androidTarget()`
- **Source sets**:
  - `src/commonMain/` — shared: model, network, state, all UI composables, App.kt, Preferences.kt
  - `src/desktopMain/` — Main.kt (entry), Platform.kt actuals (awt.Desktop, SSID via CLI), tools/GenerateIcon.kt
  - `src/androidMain/` — MainActivity.kt, Platform.kt actuals (stubs), AndroidManifest.xml
  - `src/main/` — legacy desktop sources kept for reference
- **Key files**:
  - `build.gradle.kts` — full KMP config
  - `settings.gradle.kts` — pluginManagement with google(), mavenCentral()
  - `src/commonMain/kotlin/com/nocloudchat/Platform.kt` — expect declarations
  - `src/commonMain/kotlin/com/nocloudchat/state/AppState.kt` — main ViewModel
  - `src/commonMain/kotlin/com/nocloudchat/network/Discovery.kt` — UDP peer discovery
  - `src/commonMain/kotlin/com/nocloudchat/network/Messenger.kt` — TCP messaging
  - `src/desktopMain/kotlin/com/nocloudchat/network/DesktopSsid.kt` — detectSsid() via OS CLI

## Implemented Features
- Subnet peer discovery (UDP broadcast port 54321)
- One-to-one text messaging (TCP length-prefixed JSON)
- File sharing (chunked TCP transfer)
- Network passphrase security (opt-in, SHA-256 hash in ANNOUNCE, default off)
- Dark/light mode toggle (persisted to ~/.nocloudchat/settings.json)
- Settings dialog: display name, dark/light mode, network passphrase section
- Join protected network dialog (SecretJoinDialog.kt)
- Android support via KMP (MainActivity, Platform actuals)
- Native packages: MSI (Windows), DMG (macOS), DEB (Linux)
- CI/CD: .github/workflows/ci.yml + release.yml

## Networking Protocol
- ANNOUNCE (UDP): { type, version, id, port, secretHash? }
- HELLO (TCP): exchange display names after discovery
- TEXT (TCP): { type:"text", id, from, fromName, to, text, timestamp }
- FILE_OFFER (TCP): { type:"file_offer", id, from, to, fileName, fileSize, transferPort }
- Discovery port: 54321, Messaging port: ephemeral (OS-assigned)

## Tech Decisions
- DEC-007: KMP migration for Android (2026-03-08)
- DEC-008: Network passphrase SHA-256 (2026-03-08)
- Preferences stored at ~/.nocloudchat/settings.json (desktop), /storage/emulated/0/... (Android stub)

## CI/CD
- ci.yml: compileKotlin + test on push/PR, matrix: ubuntu/macos/windows
- release.yml: packageMsi/Dmg/Deb + assembleDebug APK on tag v*.*.*, creates GitHub Release

## User Preferences
- PM coordinates teams, Tech Lead reviews architecture, engineers implement
- No confirmation needed for engineering work when user is away
