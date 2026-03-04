# HouseLink Security Roadmap

This document defines the security hardening plan for HouseLink (LAN-only home messaging app).

Scope:
- Router-based home Wi-Fi only
- No external internet
- No user accounts
- No cloud backend

Threat model:
- Any device on the same subnet may be malicious
- Public Wi-Fi and hotspots must not allow discovery
- Devices may attempt spoofing, flooding, or replay attacks

---

# Version 1 – Core Security Hardening

## P0 – Must Ship

### 1. Trusted Network Allowlist (Discovery Gate)

Discovery MUST NOT run on untrusted networks.

#### Requirements:
- Maintain a local list of trusted networks.
- Identify network using:
    - SSID
    - BSSID (if available)
    - Gateway IP
- On first connection to a new Wi-Fi:
    - Discovery OFF by default.
    - Show UI prompt: "Trust this network?"
- If network changes:
    - Re-evaluate trust.
    - Disable discovery if not trusted.

#### Additional Rules:
- Disable discovery automatically if:
    - Network is open (no WPA/WPA2/WPA3)
    - Network appears to be hotspot/tethered
    - Captive portal detected

Discovery must never auto-enable on unknown networks.

---

### 3. Minimize Broadcast Data (Privacy Hardening)

Discovery packets must not leak personal metadata.

Current risk:
- Broadcasting display name and identifiers exposes user info.

#### Change:
Discovery packet must include only:
- devicePublicId (random UUID)
- port
- protocol version
- timestamp
- nonce
- signature (if pairing enabled later)

DO NOT broadcast:
- display name
- avatar
- device type
- user identifiers

Friendly metadata must be exchanged only after secure handshake.

---

### 7. UX Safety Signals

The app must clearly communicate network trust state.

#### UI Requirements:

Show persistent network status indicator:
- ✅ Trusted Network – Discovery ON
- ⚠️ Untrusted Network – Discovery OFF

On untrusted networks:
- Show non-dismissible banner until acknowledged.
- Provide button: "Trust this Network"

Provide global action:
- "Disable Discovery Everywhere"

Make it difficult to accidentally enable discovery on unsafe networks.

---

## P1 – Strong Hardening (Still v1)

### 2. Pairing + HMAC-Signed Discovery

Prevent spoofing on LAN.

#### Pairing Model:
- First device creates Home Code (6–8 digits) or QR.
- Joining devices enter code.
- Derive shared secret key.
- Store locally.

#### Discovery Packet Extension:
Add:
- timestamp
- nonce
- HMAC-SHA256 signature over:
  version|devicePublicId|port|timestamp|nonce

Reject packets if:
- Signature invalid
- Timestamp older than 60 seconds
- Nonce reused
- Packet size exceeds limit

Unauthenticated announces must be ignored once pairing is enabled.

---

### 4. Rate Limiting + Peer Table Hardening

Protect against flooding and memory abuse.

#### Rules:
- Max peers: 32
- Drop packets > 512 bytes
- Rate limit per IP:
    - Max 5 announces per 10 seconds
- Maintain LRU eviction for stale peers
- Debounce UI updates (batch changes every 250–500 ms)

#### Concurrency:
Peer map must be protected:
- Use Mutex OR
- Use single coroutine actor ownership model

No concurrent mutation allowed.

---

# Version 2 – Advanced Security (Future)

## 5. Replace UDP Broadcast with mDNS (Optional Upgrade)

Evaluate migration from raw UDP broadcast to mDNS / Bonjour.

Goals:
- Reduce noisy broadcast traffic
- Improve cross-platform compatibility
- Better coexistence with router behavior

mDNS must still include authentication token or signature.

Do NOT rely on mDNS alone for security.

---

## 6. End-to-End Message Encryption

All messages must be encrypted between devices.

#### Recommended:
- Noise protocol OR
- Libsodium-based handshake

Encryption:
- AEAD (ChaCha20-Poly1305 or AES-GCM)
- Unique nonce per message
- Session keys derived during pairing

Messages must not be readable by:
- Other LAN devices
- Router
- Packet sniffers

---

# Non-Goals (v1 and v2)

- No cloud relay
- No internet federation
- No multi-subnet routing
- No NAT traversal
- No push notifications

---

# Security Principles

1. Never trust the LAN.
2. Discovery must be opt-in per network.
3. Authentication before visibility.
4. Minimize broadcast metadata.
5. Encrypt everything eventually.
6. UX must reflect security state clearly.

---

# Implementation Notes for Claude

- Modify Discovery.kt to add trust gate before broadcast/listen loops start.
- Introduce NetworkTrustManager component.
- Introduce PeerAuthValidator component.
- Add PacketValidator to enforce size, timestamp, nonce, signature.
- Refactor peer map into actor model to avoid race conditions.
- All changes must be backward compatible with existing UI.

Do not weaken UX for security.
Do not auto-enable discovery silently.

Security over convenience.