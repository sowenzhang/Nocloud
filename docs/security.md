# NoCloudChat — Security Model

**Version**: 1.0 | **Date**: 2026-03-08 | **Author**: Netsec / Tech Lead

---

## 1. Design Principles

NoCloudChat operates entirely within a local subnet. There is no cloud backend, no authentication server, and no telemetry. The security model is designed around four goals:

1. **Subnet isolation** — no traffic leaves the local network
2. **Opt-in group isolation** — passphrase protection for shared/semi-public networks
3. **Denial-of-service hardening** — rate limiting and size caps on all inbound data
4. **No persistent secrets** — nothing sensitive stored in plaintext

---

## 2. Network Passphrase

### Mechanism

When enabled in Settings, ANNOUNCE UDP packets include a `secretHash` field containing the hex-encoded SHA-256 digest of the user's passphrase.

```
passphrase "my-home-wifi" → SHA-256 → "a3f1c2d4..."  (64 hex chars)
```

Peers with non-matching or absent `secretHash` values are **silently dropped** at the Discovery layer and never reach the messaging layer.

### Properties

- **Plaintext never transmitted**: only the hash is sent over UDP
- **Default off**: no passphrase = no hash = open discovery (zero-config home use)
- **Persisted as hash**: `~/.nocloudchat/settings.json` stores only the hash, never the passphrase
- **Symmetric**: both peers must use the same passphrase (or neither uses one) to communicate

### Limitations

SHA-256 of a passphrase is not a cryptographic key exchange. It provides:
- Social/usability isolation on shared networks
- Protection against casual eavesdroppers

It does **not** provide:
- Protection against a determined attacker who captures UDP broadcasts and brute-forces weak passphrases
- Authentication of peer identity
- Encryption of message contents

Full encryption (TLS 1.3 or Noise protocol) is planned for Phase 2 (see `docs/roadmap.md`).

---

## 3. Rate Limiting

UDP discovery broadcasts are rate-limited per source IP to prevent denial-of-service via broadcast flooding.

| Resource | Limit |
|---|---|
| ANNOUNCE packets per source IP | 5 per 10-second window |
| Peer table size | 32 peers maximum |
| UDP packet size | 512 bytes maximum (oversized packets silently dropped) |
| TCP message payload | 10 MB maximum |

Rate tracking is maintained in a `ConcurrentHashMap` and pruned every 5 seconds.

---

## 4. Input Validation

All inbound data is validated before processing:

| Layer | Validation |
|---|---|
| UDP ANNOUNCE | JSON parse must succeed; `type` must be `"ANNOUNCE"`; `id` must be non-blank UUID; `port` must be > 0 |
| TCP message | `length` header must be > 0 and ≤ 10 MB; JSON parse must succeed; required fields checked before use |
| File transfer | Total size read from 8-byte header; read capped to declared size; destination is a safe path under `destDir` |

Malformed inputs are silently discarded — no error is sent back to the sender.

---

## 5. No Telemetry / No External Network Access

- Zero DNS lookups outside the LAN
- Zero HTTP/HTTPS connections
- Zero third-party analytics SDKs
- No crash reporting
- Structured logs are written to local file only (`~/.nocloudchat/`) if enabled; never transmitted

---

## 6. Network Trust Store

A per-network-ID trust store is maintained in `~/.nocloudchat/settings.json` under the `trustedNetworks` key.

- Network ID is derived from the private IP CIDR (e.g., `192.168.1.0/24`) or SSID if detectable
- Trust must be granted explicitly by the user via the sidebar prompt
- Trusted networks enable peer discovery; untrusted networks show a warning banner
- Trust can be revoked by editing `settings.json` (UI revocation planned for Phase 2)

---

## 7. Threat Model

### Protected Against

| Threat | Mitigation |
|---|---|
| Strangers on shared network discovering you | Network passphrase (opt-in) |
| Broadcast flood from a malicious peer | Rate limiting (5 pkts / 10s per IP) |
| Memory exhaustion via large UDP packets | 512-byte cap on UDP; oversized packets dropped |
| Memory exhaustion via large TCP messages | 10 MB cap enforced before allocation |
| Memory exhaustion via unlimited peers | 32-peer table cap |
| Malformed JSON injection | Defensive parse with try/catch; malformed input discarded |

### Not Protected Against (Phase 2+)

| Threat | Notes |
|---|---|
| Message content eavesdropping | TCP messages sent in plaintext — TLS/Noise planned for Phase 2 |
| Peer identity spoofing | No certificate/public-key authentication — planned for Phase 2 |
| Passphrase brute force | SHA-256 hash is public; weak passphrases are guessable |
| File integrity tampering | No checksum on file transfers yet — planned for Phase 2 |
| LAN-level MITM attacks | Requires encryption + identity verification — Phase 2 |

---

## 8. Sensitive Data Storage

| Data | Storage location | Format |
|---|---|---|
| Display name | `~/.nocloudchat/settings.json` | Plaintext string |
| Dark mode preference | `~/.nocloudchat/settings.json` | Boolean |
| Trusted network IDs | `~/.nocloudchat/settings.json` | Array of CIDR strings or SSIDs |
| Network passphrase | `~/.nocloudchat/settings.json` | SHA-256 hex hash only |
| Messages | In-memory only | Not persisted |
| Received files | `~/Downloads/NoCloud Chat/` | Raw files (user-visible) |
