# NoCloudChat — Known Issues

**Last updated**: 2026-03-02

---

## Open Issues

*No known issues at this time. All MVP acceptance criteria pass.*

---

## Resolved Issues

*None yet.*

---

## Limitations (By Design)

| Limitation | Reason |
|---|---|
| Messages not persisted across restarts | In-memory storage is MVP scope; Phase 2 adds persistence |
| No cross-router communication | Subnet-scoped by design (privacy feature) |
| Windows Firewall prompt on first run | Expected; user must allow on Private networks |
| Single TCP port reuse across sessions | Port is ephemeral per session; peers re-discover on restart |
| UDP broadcast may fail on some managed networks | Corporate firewalls may block; known environment limitation |

