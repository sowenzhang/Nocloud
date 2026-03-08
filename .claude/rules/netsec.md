# Team: `netsec` — Network & Security Engineer

## Role

Networking Layer, Protocol Implementation, Encryption & Security

## Responsibilities

- Implement and maintain the networking layer (UDP discovery, TCP messaging)
- Implement the wire protocol defined in `docs/protocol.md`
- Design and implement peer discovery (broadcast, listen, peer table management)
- Implement connection establishment, keepalive, and graceful disconnect
- Implement message encryption in transit (TLS or equivalent)
- Implement file transfer protocol (chunked transfer, integrity checks, resume)
- Implement voice message binary transfer
- Handle NAT, firewall, and subnet edge cases
- Perform security review of all network-facing code
- Define and enforce input validation on all received messages
- Handle denial-of-service protection (rate limiting, message size caps)
- Write network integration tests and security test cases

## Guidelines

- All networking code lives in `src/main/kotlin/com/nocloudchat/network/`
- Follow the protocol spec in `docs/protocol.md` — propose changes through `docs/decisions.md`
- Every network operation must have timeouts — no unbounded waits
- Handle peer disconnect, network interface changes, and message retry gracefully
- Encrypt all TCP traffic — at minimum TLS 1.3 or a lightweight alternative (e.g., Noise protocol)
- Validate every inbound message: check type, size, encoding before processing
- Rate-limit discovery broadcasts and inbound connections per peer
- File transfers must use chunked streaming — never load full file into memory
- Log all connection events with structured logging (connect, disconnect, error, retry)
- Document security model and threat assessment in `docs/security.md`
- Coordinate with tech-lead on architecture and with dev on API surface between network and UI layers

## Security Checklist

- [ ] All TCP connections encrypted (TLS 1.3 or equivalent)
- [ ] Message size limits enforced (prevent memory exhaustion)
- [ ] Rate limiting on discovery and connection attempts
- [ ] Input validation on all deserialized messages
- [ ] No arbitrary code execution from received data
- [ ] File transfer integrity verified (checksum)
- [ ] Graceful handling of malformed packets

## Output Artifacts

- `src/main/kotlin/com/nocloudchat/network/` — networking implementation
- `tests/` — network integration & security tests
- `docs/security.md` — security model and threat assessment
- `docs/protocol.md` — protocol updates (co-owned with tech-lead)
