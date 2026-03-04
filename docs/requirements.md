# NoCloudChat — Product Requirements Document

**Version**: 1.0
**Date**: 2026-03-02
**Author**: PM Team
**Status**: Approved (MVP)

---

## 1. Product Overview

NoCloudChat is a zero-registration, zero-server, peer-to-peer communication app for local area networks. When launched, it automatically discovers other NoCloudChat instances on the same subnet and allows instant text messaging — no accounts, no internet, no configuration required.

**Tagline**: *"Like a walkie-talkie, but for your whole team."*

---

## 2. Problem Statement

In shared offices, events, classrooms, or venues, people often need to communicate quickly within a physical space. Current solutions (Slack, WhatsApp, etc.) require internet access, accounts, and onboarding. NoCloudChat solves this by providing instant, local-only chat that works the moment you open the app.

---

## 3. Target Users

- Conference/hackathon participants on shared Wi-Fi
- Office teams wanting secure, intranet-only communication
- Schools and classrooms (no student data leaves the network)
- Anyone who needs quick LAN chat without setup

---

## 4. MVP Scope (MoSCoW)

### Must Have (MVP v1)
- [ ] App launches and immediately scans the subnet for peers
- [ ] Peer list shows all discovered NoCloudChat instances with display name and IP
- [ ] One-to-one text messaging (send and receive)
- [ ] Display name customisable in settings
- [ ] Works on Windows, macOS, and Linux
- [ ] Messages persist for the duration of the session (in-memory)
- [ ] Unread message count badge on peer list

### Should Have (Phase 2)
- [ ] File sharing (drag and drop)
- [ ] Voice message recording and playback
- [ ] Typing indicator

### Could Have (Phase 3)
- [ ] Group chat rooms
- [ ] Emoji reactions
- [ ] Presence status (away, busy, available)
- [ ] Persistent message history (local file)

### Won't Have (MVP)
- [ ] Internet communication (by design)
- [ ] User accounts or authentication
- [ ] End-to-end encryption (LAN-only, deferred to Phase 2)
- [ ] Mobile apps

---

## 5. User Stories

### US-01: Peer Discovery
**As a** user who just opened NoCloudChat,
**I want to** see a list of other NoCloudChat users on my network within 5 seconds,
**So that** I can start chatting without any setup.

**Acceptance Criteria**:
- App shows a "Scanning subnet…" indicator immediately on launch
- Within 5 seconds on a typical LAN, any peer running NoCloudChat appears in the list
- Each peer shows their display name and IP address
- A peer that shuts down is removed from the list within 15 seconds
- Multiple peers (3+) are all visible simultaneously

---

### US-02: Peer List
**As a** user,
**I want to** see a list of online peers with clear status indicators,
**So that** I know who is available to chat.

**Acceptance Criteria**:
- Peer list shows all discovered peers with display name
- Each peer shows a green online indicator
- Peer count badge shows total number
- Clicking a peer opens the chat view
- If no peers are found, a helpful empty state is shown

---

### US-03: Text Messaging
**As a** user,
**I want to** type and send text messages to a selected peer,
**So that** we can communicate in real time.

**Acceptance Criteria**:
- Message is sent on pressing Enter (Shift+Enter for newline)
- Sent messages appear in the chat immediately (no waiting)
- Received messages appear in the chat automatically (no refresh)
- Messages show sender name and timestamp
- Outgoing and incoming messages are visually distinct (different colors/alignment)
- Input field supports multi-line messages (up to ~5 lines before scroll)
- Empty messages are not sent
- Message history is maintained for the duration of the session

---

### US-04: Display Name
**As a** user,
**I want to** set a display name that peers see,
**So that** I'm identifiable on the network.

**Acceptance Criteria**:
- Default name is the OS username on first launch
- Settings panel is accessible by clicking the user avatar / name area
- Name change takes effect immediately (peers see the new name within one broadcast cycle, ~3 sec)
- Name is limited to 32 characters
- Empty name is not accepted

---

### US-05: New Message Notification
**As a** user chatting with multiple peers,
**I want to** be notified of new messages from other peers,
**So that** I don't miss messages while focused on a conversation.

**Acceptance Criteria**:
- Unread message count badge appears on peer in the list
- Toast notification appears briefly when a message arrives from a non-active peer
- Badge clears when the chat with that peer is opened

---

## 6. Non-Functional Requirements

### Performance
- App launches and shows UI in < 2 seconds
- Peer discovery takes < 5 seconds on a typical LAN
- Message delivery latency < 500ms on the same subnet
- Memory usage < 200 MB during normal operation

### Security
- All communication stays within the local subnet
- No data is transmitted to the internet
- No telemetry, analytics, or phone-home functionality

### Usability
- The entire app is usable without reading any documentation
- Maximum 2 clicks to send a first message to a new peer
- App must work at 1024×768 minimum resolution

### Reliability
- App handles peer disconnect gracefully (no crash, peer removed from list)
- Network interface change (Wi-Fi switch) is handled gracefully
- App handles rapid peer join/leave without UI flicker

### Platform
- Windows 10+ (64-bit)
- macOS 12+
- Ubuntu 20.04+ / Debian-based Linux

---

## 7. Out of Scope for MVP

- End-to-end encryption
- File transfer
- Voice/audio messaging
- Group chats
- Persistent message storage
- Push notifications (OS-level)
- Mobile support
- Internet relay

---

## 8. Success Metrics (Hackathon Demo)

1. Two instances launched on the same machine discover each other ✓
2. Two instances on different machines on the same LAN communicate ✓
3. End-to-end message round-trip visible in < 1 second ✓
4. New user can send their first message within 30 seconds of launch ✓

