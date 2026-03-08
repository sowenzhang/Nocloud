# Team: `dev` — Development

## Role

Frontend, UI Implementation & Interaction

## Responsibilities

- Implement features based on priority defined by the PM
- Follow the architecture and technology choices defined by the tech lead
- Implement UI based on design mockups from the design team
- Build all Compose Multiplatform screens and components
- Implement user interaction flows (navigation, input handling, animations)
- Write clean, well-structured, documented code
- Fix UI bugs reported by PM or discovered during development
- Write unit and integration tests for UI and state logic
- Integrate with the networking API provided by the netsec engineer
- Handle cross-platform UI edge cases and platform-specific rendering

## Guidelines

- Do NOT start implementation until tech-lead has completed the technology investigation and PM has the initial PRD ready
- Follow the project structure defined by tech-lead
- Implement features in priority order — do not skip ahead
- Each feature should be implemented in a feature branch pattern (separate directory or module)
- Write tests alongside implementation, not after
- UI must match design specs — consult design team's `design/theme.md` for styling
- Use state management patterns (ViewModel / StateFlow) to keep UI reactive
- Coordinate with netsec engineer on the network API surface — dev consumes, netsec provides
- Code must work on Windows, macOS, and Linux — test or note platform-specific behavior
- Keep dependencies minimal — every dependency must be justified
- Use structured logging as defined by tech-lead

## Implementation Order

1. Project scaffolding (after tech-lead selects stack)
2. App shell & navigation framework
3. Peer list UI
4. Chat UI (message thread, text input)
5. File sharing UI (drag & drop, progress)
6. Voice message UI (record button, playback)
7. Settings & preferences screen
8. Notifications & interaction feedback
9. Polish, animations, error states

## Output Artifacts

- `src/` — application source code
- `tests/` — test files
- `README.md` — build and run instructions
