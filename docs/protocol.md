# NoCloudChat — Wire Protocol Specification

**Version**: 1.0 | **Date**: 2026-03-02 | **Author**: Tech Lead

---

## 1. Discovery Protocol

### Transport
- **Layer**: UDP
- **Port**: 54321 (all instances bind this port with `SO_REUSEADDR`)
- **Destination**: Subnet broadcast address (computed per interface) + `255.255.255.255` fallback
- **Frequency**: Every 3 seconds per instance

### Packet Format
UTF-8 encoded JSON, max size ~256 bytes:

```json
{
  "type": "ANNOUNCE",
  "version": 1,
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Alice",
  "port": 49152
}
```

| Field | Type | Description |
|---|---|---|
| `type` | string | Always `"ANNOUNCE"` |
| `version` | integer | Protocol version (currently `1`) |
| `id` | string | UUID v4, stable per app session |
| `name` | string | Display name (max 32 chars) |
| `port` | integer | TCP server port for this instance |

### Peer Lifecycle
- Peer becomes **visible** when first `ANNOUNCE` is received
- Peer is **updated** if `name` or `port` changes
- Peer is **removed** if no `ANNOUNCE` received for 12 seconds
- Self-announcements (matching own `id`) are silently ignored

---

## 2. Messaging Protocol

### Transport
- **Layer**: TCP
- **Port**: Ephemeral (OS-assigned), advertised in `ANNOUNCE.port`
- **Connection model**: New connection per message (stateless, fire-and-forget)
- **Server**: Each instance binds `0.0.0.0:0`

### Framing

```
┌──────────────────────┬─────────────────────────────────────┐
│  Length (4 bytes)    │  Payload (N bytes, UTF-8 JSON)       │
│  Big-endian uint32   │                                      │
└──────────────────────┴─────────────────────────────────────┘
```

- `Length` = byte length of the UTF-8 encoded JSON payload (not character count)
- Maximum payload size: 10 MB (safety limit; typical text messages are < 1 KB)

### Message Types

#### Text Message
```json
{
  "type": "text",
  "id": "msg-uuid-v4",
  "from": "sender-peer-id",
  "fromName": "Alice",
  "to": "recipient-peer-id",
  "text": "Hello there!",
  "timestamp": 1709337600000
}
```

| Field | Type | Description |
|---|---|---|
| `type` | string | `"text"` |
| `id` | string | UUID v4, unique per message |
| `from` | string | Sender's peer ID |
| `fromName` | string | Sender's display name at send time |
| `to` | string | Recipient's peer ID |
| `text` | string | Message content (max ~1 MB) |
| `timestamp` | integer | Unix timestamp in milliseconds |

---

## 3. Error Handling

| Scenario | Behaviour |
|---|---|
| Peer port unreachable | 5-second TCP connect timeout, error surfaced to UI |
| Malformed JSON payload | Silently dropped |
| Oversized message (>10 MB) | Connection dropped by receiver |
| Self-message (same peer ID) | Silently dropped by receiver |
| UDP bind failure | Warning logged; discovery degraded |

---

## 4. Versioning

- `version` field in `ANNOUNCE` allows future protocol changes
- Receivers SHOULD process messages from any version
- Incompatible future versions may introduce a `min_version` field

---

## 5. Ports Summary

| Port | Protocol | Purpose |
|---|---|---|
| 54321 | UDP | Peer discovery broadcasts |
| ephemeral | TCP | Message delivery (one per instance) |

