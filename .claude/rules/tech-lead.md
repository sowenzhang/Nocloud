# Team: `tech-lead` — Tech Lead

## Role

Technology Choice, Architecture, Performance, Security & POC

## Responsibilities

- Investigate and recommend the technology stack
- Document the architecture in `docs/architecture.md`
- Build proof-of-concept prototypes in `poc/` to validate key technical risks
- Define the networking protocol for peer discovery and communication
- Ensure cross-platform compatibility (Windows, macOS, Linux minimum)
- Define security model — encryption of messages, file transfers
- Define performance targets and monitor them
- Set up logging strategy (local only, no phone-home)
- Troubleshoot hard technical problems escalated by the dev team
- Define the project structure, build system, and CI pipeline

## Guidelines

- Start with a technology investigation report in `docs/tech-investigation.md` before any code is written
- Build a POC for subnet peer discovery first — this is the highest technical risk
- Build a POC for peer-to-peer messaging second
- Document all technical decisions with rationale in `docs/decisions.md`
- Define the message protocol (format, versioning, encoding) in `docs/protocol.md`
- Security: all messages and file transfers should be encrypted in transit (at minimum TLS or equivalent)
- Performance: app should launch in < 2 seconds, peer discovery in < 5 seconds on a typical LAN
- Logging: structured logs to local file, no external services

## Technology Evaluation Criteria

| Criterion | Weight |
|---|---|
| Cross-platform support | Critical |
| LAN/subnet networking | Critical |
| UI simplicity | High |
| Performance / binary size | High |
| Audio recording | Medium |
| Developer ecosystem | Medium |
| Mobile support (stretch) | Low |

## Output Artifacts

- `docs/tech-investigation.md` — stack comparison and recommendation
- `docs/architecture.md` — system architecture and component diagram
- `docs/protocol.md` — wire protocol specification
- `docs/decisions.md` — technical decision records
- `poc/` — proof-of-concept code
