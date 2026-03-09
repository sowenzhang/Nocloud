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
UTF-8 encoded JSON, max size 512 bytes:

```json
{
  "type": "ANNOUNCE",
  "version": 1,
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "port": 49152,
  "secretHash": "a3f1..."
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `type` | string | yes | Always `"ANNOUNCE"` |
| `version` | integer | yes | Protocol version (currently `1`) |
| `id` | string | yes | UUID v4, stable per app session |
| `port` | integer | yes | TCP server port for this instance |
| `secretHash` | string | no | SHA-256 hex of the network passphrase (see below) |

> **Note**: The `name` field was removed in v1.1. Display names are now exchanged via TCP HELLO handshake after discovery to limit information broadcast over UDP.

### Network Passphrase

When a user enables the network passphrase feature in Settings, each ANNOUNCE packet includes a `secretHash` field containing the hex-encoded SHA-256 hash of the configured passphrase. The plaintext passphrase is never transmitted.

**Filtering rules (applied by the receiver):**

| My `secretHash` | Peer `secretHash` | Action |
|---|---|---|
| set | same value | Accept peer normally |
| set | different value | Silently drop ANNOUNCE |
| set | absent | Silently drop ANNOUNCE |
| absent | absent | Accept peer normally |
| absent | set | Emit `onSecretRequired` event; prompt user to enter passphrase |

When `onSecretRequired` fires, the UI shows `SecretJoinDialog`, allowing the user to enter the passphrase. On confirmation, the hash is stored and the local peer enables passphrase mode, allowing the peer to be discovered on the next broadcast cycle.

The feature is **off by default** to preserve the zero-configuration experience.

### Peer Lifecycle
- Peer becomes **visible** when first `ANNOUNCE` is received and passes secret filtering
- Peer is **updated** if `port` or source IP changes
- Peer is **removed** if no `ANNOUNCE` received for 12 seconds
- Self-announcements (matching own `id`) are silently ignored
- Peers exceeding the table cap of 32 are silently dropped

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

#### File Offer Message
```json
{
  "type": "file_offer",
  "id": "transfer-uuid-v4",
  "from": "sender-peer-id",
  "fromName": "Alice",
  "to": "recipient-peer-id",
  "fileName": "photo.jpg",
  "fileSize": 2097152,
  "transferPort": 52341,
  "timestamp": 1709337600000
}
```

| Field | Type | Description |
|---|---|---|
| `type` | string | `"file_offer"` |
| `fileName` | string | Original filename |
| `fileSize` | integer | Total byte size |
| `transferPort` | integer | Port of the sender's one-shot TCP file server |

#### HELLO Message
Sent immediately after a new peer is discovered via UDP. Used to exchange display names without broadcasting them over UDP.

```json
{
  "type": "hello",
  "id": "msg-uuid-v4",
  "from": "sender-peer-id",
  "fromName": "Alice",
  "to": "recipient-peer-id",
  "timestamp": 1709337600000
}
```

The receiver updates its peer name and reciprocates with its own HELLO if it hasn't done so yet.

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

