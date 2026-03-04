# NoCloudChat — Screen Mockups

**Date**: 2026-03-02

---

## Screen 1: Splash / Discovery

Shown for ~1.2 seconds on launch while networking initialises.

```
┌─────────────────────────────────────────────┐
│                                             │
│                                             │
│                    📡                       │
│                                             │
│              NoCloudChat                      │
│         (gradient accent text)              │
│                                             │
│           ◎  ◎  ◎  ◎                       │
│        [radar animation - expanding rings]  │
│                                             │
│         Scanning local network…             │
│           (muted subtitle text)             │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
Background: #1a1a2e (deep navy)
```

---

## Screen 2: Main Layout (Peer List + Chat)

The core app screen. Sidebar on the left, content on the right.

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│ ┌──────────────────┐  ┌──────────────────────────────────┐  │
│ │ [A] Alice    ⚙  │  │ [B] Bob                   🟢 Online│  │
│ │─────────────────│  │──────────────────────────────────│  │
│ │ ON YOUR NETWORK │  │                                  │  │
│ │ [B] Bob          │  │                                  │  │
│ │  ● 192.168.1.5   │  │         Today                    │  │
│ │ [C] Charlie   2  │  │                                  │  │
│ │  ● 192.168.1.8   │  │                   ┌────────────┐ │  │
│ │ [D] Dev Machine  │  │                   │ Hey Bob! 👋 │ │  │
│ │  ● 192.168.1.12  │  │                   │  10:30 AM  │ │  │
│ │                  │  │                   └────────────┘ │  │
│ │                  │  │ ┌─────────────┐                  │  │
│ │                  │  │ │ Hey Alice!  │                  │  │
│ │                  │  │ │  10:31 AM   │                  │  │
│ │                  │  │ └─────────────┘                  │  │
│ │                  │  │                                  │  │
│ │                  │  │ ┌─────────────────────────────┐  │  │
│ │ ● Scanning subnet│  │ │ Type a message… (Enter)  ➤  │  │
│ └──────────────────┘  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

Legend:
[A] = Avatar with initials
● = Online indicator (green dot)
2  = Unread message badge (red pill)
➤  = Send button (round, accent)
```

### Sidebar Anatomy
- **My info bar** (top): Avatar + display name + settings gear icon
- **Section label**: "ON YOUR NETWORK" (uppercase, muted, small)
- **Peer list**: Scrollable list of peer cards
  - Each card: avatar + online dot + name + IP address + unread badge
- **Scanning indicator** (bottom): pulsing dot + "Scanning subnet…"

### Chat Area Anatomy
- **Header**: Peer avatar + name + online status
- **Messages**: Scrollable, reverse-chronological presentation
  - Date dividers between day groups
  - Outgoing (right): accent red bubbles
  - Incoming (left): dark blue bubbles with sender name
- **Input bar**: Expanding textarea + round send button

### Welcome State (no peer selected)
```
┌──────────────────────────────────────────────┐
│                                              │
│                                              │
│                   💬                         │
│             (large, dimmed)                  │
│                                              │
│          Select a peer to chat               │
│                                              │
│    Click anyone in the sidebar to start      │
│    an instant, private conversation —        │
│    no accounts needed.                       │
│                                              │
│                                              │
└──────────────────────────────────────────────┘
```

---

## Screen 3: Settings Panel (Modal Overlay)

Triggered by clicking the user info bar. Appears as a centered modal.

```
┌────────────────────────────────────────────────────┐
│                (dimmed + blurred bg)               │
│                                                    │
│        ┌───────────────────────────────┐           │
│        │ ⚙ Settings                    │           │
│        │                               │           │
│        │ DISPLAY NAME                  │           │
│        │ ┌─────────────────────────┐   │           │
│        │ │ Alice                   │   │           │
│        │ └─────────────────────────┘   │           │
│        │                               │           │
│        │ YOUR PEER ID                  │           │
│        │ ┌─────────────────────────┐   │           │
│        │ │ 550e8400-e29b-41d4-...  │   │           │
│        │ └─────────────────────────┘   │           │
│        │                               │           │
│        │               [Cancel] [Save] │           │
│        └───────────────────────────────┘           │
│                                                    │
└────────────────────────────────────────────────────┘

- Press Escape or click overlay to cancel
- Press Enter to save
- Name limited to 32 characters
- Peer ID is read-only (informational)
```

---

## Screen 4: Toast Notification

Appears bottom-right when a message arrives from a non-active peer.

```
                    ┌──────────────────────┐
                    │ Charlie              │  ← sender name (amber)
                    │ Hey, are you there?  │  ← message text (truncated)
                    └──────────────────────┘
                        ↑
                 slides in from right,
                 auto-dismisses after 4s
```

