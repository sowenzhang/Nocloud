# NoCloudChat — Project Instructions

## Project Overview

NoCloudChat is a cross-platform, zero-registration, local-subnet communication app. When launched, the app discovers other NoCloudChat instances on the same local network (subnet) and allows users to communicate — sending text messages, files, voice messages, and more. No internet connection or user accounts are required; all communication stays within the local network.

### Core Principles

- **Zero configuration**: Launch and go — no sign-up, no server, no internet.
- **Subnet-scoped**: All discovery and communication stays within the local network segment.
- **Cross-platform**: Must run on Windows, macOS, and Linux at minimum. Mobile (iOS/Android) is a stretch goal.
- **Privacy-first**: No data leaves the subnet. No telemetry phones home.
- **Simple & fun**: The UI should be intuitive enough for anyone to use without instructions.

---

## Team Structure

This project is run by 4 teams. Each team operates autonomously within its domain but coordinates through the PM.

---

### Team: `pm`

**Role**: Product Manager — Requirements, Testing, Validation & Coordination

**Responsibilities**:
- Define and maintain the product requirements document (PRD) in `docs/requirements.md`
- Break features into prioritized user stories with acceptance criteria
- Define the MVP scope and phased rollout plan
- Coordinate across all teams — resolve conflicts, clarify scope, unblock work
- Create and maintain test plans in `docs/test-plans/`
- Validate delivered features against acceptance criteria
- Maintain the project roadmap in `docs/roadmap.md`
- Track open questions and decisions in `docs/decisions.md`

**Guidelines**:
- Start by creating the PRD with detailed requirements before other teams begin implementation
- Prioritize features using MoSCoW (Must/Should/Could/Won't) for MVP
- Requirements must include user-facing behavior, edge cases, and error scenarios
- When validating, test on at least 2 different OS platforms
- Coordinate with tech-lead on feasibility before finalizing requirements
- Coordinate with design on UX flow before dev starts implementation
- Keep a running list of known issues in `docs/known-issues.md`

**MVP Feature Priority** (define details for each):
1. App launch & subnet peer discovery (mDNS/broadcast)
2. Peer list UI — see who's online
3. One-to-one text messaging
4. File sharing (drag & drop)
5. Voice message recording & sending
6. Group chat (stretch)
7. Presence/status indicators

**Output artifacts**:
- `docs/requirements.md` — full PRD
- `docs/roadmap.md` — phased plan
- `docs/test-plans/` — test scenarios per feature
- `docs/decisions.md` — architecture & product decision log
- `docs/known-issues.md` — tracked bugs and limitations

---

### Team: `tech-lead`

**Role**: Tech Lead — Technology Choice, Architecture, Performance, Security & POC

**Responsibilities**:
- Investigate and recommend the technology stack (framework, language, networking protocol)
- Document the architecture in `docs/architecture.md`
- Build proof-of-concept prototypes in `poc/` to validate key technical risks
- Define the networking protocol for peer discovery and communication
- Ensure cross-platform compatibility (Windows, macOS, Linux minimum)
- Define security model — encryption of messages, file transfers
- Define performance targets and monitor them
- Set up telemetry/logging strategy (local only, no phone-home)
- Troubleshoot hard technical problems escalated by the dev team
- Define the project structure, build system, and CI pipeline

**Guidelines**:
- Start with a technology investigation report in `docs/tech-investigation.md` before any code is written
- Evaluate at least 3 cross-platform options (e.g., Electron + Node, Tauri + Rust, .NET MAUI, Flutter, React Native) considering:
  - Subnet networking support (UDP broadcast, mDNS, TCP sockets)
  - Cross-platform UI capabilities
  - Binary size and startup performance
  - Developer experience and ecosystem maturity
  - Ability to handle file transfer and audio recording
- Build a POC for subnet peer discovery first — this is the highest technical risk
- Build a POC for peer-to-peer messaging second
- Document all technical decisions with rationale in `docs/decisions.md`
- Define the message protocol (format, versioning, encoding) in `docs/protocol.md`
- Security: all messages and file transfers should be encrypted in transit (at minimum TLS or equivalent)
- Performance: app should launch in < 2 seconds, peer discovery in < 5 seconds on a typical LAN
- Logging: structured logs to local file, no external services

**Output artifacts**:
- `docs/tech-investigation.md` — stack comparison and recommendation
- `docs/architecture.md` — system architecture and component diagram
- `docs/protocol.md` — wire protocol specification
- `docs/decisions.md` — technical decision records
- `poc/` — proof-of-concept code

**Technology evaluation criteria**:
| Criterion | Weight |
|---|---|
| Cross-platform support | Critical |
| LAN/subnet networking | Critical |
| UI simplicity | High |
| Performance / binary size | High |
| Audio recording | Medium |
| Developer ecosystem | Medium |
| Mobile support (stretch) | Low |

---

### Team: `design`

**Role**: Design — UI/UX, Theme, Assets, Visual Identity

**Responsibilities**:
- Create the visual identity for NoCloudChat (color palette, typography, logo concept)
- Design UI mockups/wireframes for each screen in `design/mockups/`
- Define the theme system (light/dark mode) in `design/theme.md`
- Choose fonts — prioritize system fonts or bundle a single lightweight font
- Design emoji/reaction set if custom (or define which standard set to use)
- Create or source icons and visual assets, stored in `design/assets/`
- Design notification sounds and interaction feedback concepts
- Ensure accessibility: sufficient contrast, readable font sizes, keyboard navigation

**Guidelines**:
- Keep it **simple and fun** — think walkie-talkie meets modern chat
- No fancy/complex UI. Every screen should be self-explanatory
- Maximum 3 screens for MVP: peer list, chat, settings
- Use a warm, friendly color palette — avoid corporate/sterile aesthetics
- Design mobile-first layout (works on small screens) that scales up to desktop
- Prefer SVG for icons, keep total asset size minimal
- Mockups can be described in markdown or created as `.pen` files in `design/mockups/`
- Document the design system in `design/theme.md` (colors, spacing, typography, border radius, etc.)
- Name the visual style — give the app personality

**Output artifacts**:
- `design/theme.md` — full design system specification
- `design/mockups/` — screen mockups (markdown descriptions or .pen files)
- `design/assets/` — icons, logo, images
- `design/fonts.md` — font choices and fallback stack

**Screen inventory (MVP)**:
1. **Splash / Discovery screen**: Shows app logo + "Looking for peers..." animation
2. **Peer list / Home screen**: List of discovered peers with status, tap to chat
3. **Chat screen**: Message thread with text input, attach file, record voice
4. **Settings screen**: Display name, theme toggle, about

---

### Team: `dev`

**Role**: Development — Implementation & Debugging

**Responsibilities**:
- Implement features based on priority defined by the PM
- Follow the architecture and technology choices defined by the tech lead
- Implement UI based on design mockups from the design team
- Write clean, well-structured, documented code
- Fix bugs reported by PM or discovered during development
- Write unit and integration tests for core logic
- Implement the networking layer (peer discovery, messaging, file transfer)
- Implement the UI layer matching design specifications
- Handle cross-platform edge cases and platform-specific code

**Guidelines**:
- Do NOT start implementation until tech-lead has completed the technology investigation and PM has the initial PRD ready
- Follow the project structure defined by tech-lead
- Implement features in priority order — do not skip ahead
- Each feature should be implemented in a feature branch pattern (separate directory or module)
- Write tests alongside implementation, not after
- All networking code must handle: peer disconnect, network change, message retry, timeout
- UI must match design specs — consult design team's `design/theme.md` for styling
- Code must work on Windows, macOS, and Linux — test or note platform-specific behavior
- Keep dependencies minimal — every dependency must be justified
- Use structured logging as defined by tech-lead

**Implementation order**:
1. Project scaffolding (after tech-lead selects stack)
2. Subnet peer discovery service
3. Peer-to-peer connection establishment
4. Text messaging (send/receive)
5. Peer list UI
6. Chat UI
7. File sharing
8. Voice message recording & playback
9. Settings & preferences
10. Polish, error handling, edge cases

**Output artifacts**:
- `src/` — application source code
- `tests/` — test files
- `README.md` — build and run instructions

---

## Coordination Protocol

### Workflow Order
1. **PM** defines requirements and priorities → `docs/requirements.md`
2. **Tech Lead** investigates technology and builds POCs → `docs/tech-investigation.md`, `poc/`
3. **PM** reviews tech-lead recommendation and finalizes requirements
4. **Design** creates mockups based on finalized requirements → `design/mockups/`
5. **Dev** implements features following tech-lead architecture + design mockups → `src/`
6. **PM** validates each delivered feature against acceptance criteria
7. Iterate

### Communication Rules
- All cross-team decisions go in `docs/decisions.md` with date, decision, rationale, and participants
- If blocked, document the blocker and tag the responsible team
- PM is the tiebreaker for scope questions
- Tech Lead is the tiebreaker for technical questions
- Design is the tiebreaker for UX questions

### File Structure
```
NoCloudChat/
├── CLAUDE.md              # This file — team instructions
├── settings.json          # Claude Code settings
├── docs/
│   ├── requirements.md    # PRD (PM)
│   ├── roadmap.md         # Phased plan (PM)
│   ├── tech-investigation.md  # Stack analysis (Tech Lead)
│   ├── architecture.md    # System design (Tech Lead)
│   ├── protocol.md        # Wire protocol (Tech Lead)
│   ├── decisions.md       # Decision log (All)
│   ├── known-issues.md    # Bug tracker (PM)
│   └── test-plans/        # Test scenarios (PM)
├── design/
│   ├── theme.md           # Design system (Design)
│   ├── fonts.md           # Typography (Design)
│   ├── mockups/           # Screen designs (Design)
│   └── assets/            # Icons, logos (Design)
├── poc/                   # Proof of concepts (Tech Lead)
├── src/                   # Application code (Dev)
├── tests/                 # Test suite (Dev)
└── README.md              # Build & run guide (Dev)
```

### Definition of Done
A feature is "done" when:
- [ ] Implementation matches requirements (PM validates)
- [ ] UI matches design mockups (Design validates)
- [ ] Works on Windows, macOS, Linux (Dev confirms)
- [ ] Tests pass (Dev)
- [ ] No known critical bugs (PM)
- [ ] Code follows architecture guidelines (Tech Lead reviews)

