# NoCloudChat — Project Constitution

## Project Overview

NoCloudChat is a cross-platform, zero-registration, local-subnet communication app. It discovers other instances on the same LAN and enables text messaging, file sharing, and voice messages — no internet, no accounts, no servers.

## Core Principles

- **Zero configuration**: Launch and go — no sign-up, no server, no internet.
- **Subnet-scoped**: All discovery and communication stays within the local network.
- **Cross-platform**: Windows, macOS, Linux required. Mobile is a stretch goal.
- **Privacy-first**: No data leaves the subnet. No telemetry.
- **Simple & fun**: Intuitive enough to use without instructions.

## Tech Stack

- **Language**: Kotlin
- **UI**: Compose Multiplatform (Desktop)
- **Build**: Gradle 8.11 / JDK 21
- **Networking**: UDP broadcast (discovery) + TCP sockets (messaging)
- **Protocol**: JSON-based messages (see `docs/protocol.md`)

## Team Structure

6 autonomous teams coordinated through the PM. Role-specific rules are in `.claude/rules/`:

| Team | Role | Rules File |
|------|------|------------|
| `pm` | Product Manager — requirements, testing, validation | `.claude/rules/pm.md` |
| `tech-lead` | Tech Lead — architecture, performance, POC | `.claude/rules/tech-lead.md` |
| `design` | Design — UI/UX, theme, assets, visual identity | `.claude/rules/design.md` |
| `dev` | Development — frontend, UI, interaction | `.claude/rules/dev.md` |
| `srm` | SRM Engineer — CI/CD, GitHub Actions, releases | `.claude/rules/srm.md` |
| `netsec` | Network & Security — networking layer, encryption | `.claude/rules/netsec.md` |

## Workflow

1. **PM** defines requirements → `docs/requirements.md`
2. **Tech Lead** investigates tech & builds POCs → `docs/tech-investigation.md`, `poc/`
3. **PM** reviews and finalizes requirements
4. **Design** creates mockups → `design/mockups/`
5. **Dev** implements features → `src/`
6. **PM** validates against acceptance criteria
7. Iterate

## Communication Rules

- All cross-team decisions go in `docs/decisions.md` with date, decision, rationale
- PM is tiebreaker for scope, Tech Lead for technical, Design for UX questions
- If blocked, document the blocker and tag the responsible team

## File Structure

```
NoCloudChat/
├── CLAUDE.md                  # Global constitution (this file)
├── .claude/rules/             # Role-specific rules
│   ├── pm.md
│   ├── tech-lead.md
│   ├── design.md
│   ├── dev.md
│   ├── srm.md
│   └── netsec.md
├── .github/workflows/         # CI/CD (SRM)
├── docs/                      # Documentation
│   ├── requirements.md        # PRD (PM)
│   ├── roadmap.md             # Phased plan (PM)
│   ├── tech-investigation.md  # Stack analysis (Tech Lead)
│   ├── architecture.md        # System design (Tech Lead)
│   ├── protocol.md            # Wire protocol (Tech Lead + Netsec)
│   ├── security.md            # Security model (Netsec)
│   ├── build-release.md       # Build & release docs (SRM)
│   ├── decisions.md           # Decision log (All)
│   ├── known-issues.md        # Bug tracker (PM)
│   └── test-plans/            # Test scenarios (PM)
├── design/                    # Design artifacts
│   ├── theme.md
│   ├── fonts.md
│   ├── mockups/
│   └── assets/
├── poc/                       # Proof of concepts (Tech Lead)
├── src/                       # Application code (Dev)
├── tests/                     # Test suite (Dev)
└── README.md                  # Build & run guide
```

## Definition of Done

A feature is "done" when:
- [ ] Implementation matches requirements (PM validates)
- [ ] UI matches design mockups (Design validates)
- [ ] Works on Windows, macOS, Linux (Dev confirms)
- [ ] Tests pass (Dev)
- [ ] No known critical bugs (PM)
- [ ] Code follows architecture guidelines (Tech Lead reviews)
- [ ] CI pipeline passes on all platforms (SRM confirms)
- [ ] Network code reviewed for security (Netsec reviews)

