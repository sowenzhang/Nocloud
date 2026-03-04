# NoCloudChat — Design System

**Visual Identity**: *RadioWave* — warm, connected, friendly
**Date**: 2026-03-02

---

## 1. Brand Personality

NoCloudChat feels like a walkie-talkie transformed into a modern chat app — immediate, physical, personal. The design uses:
- Deep navy backgrounds evoking a "night ops" feel
- Warm reds and ambers as accent colors (energy, urgency, warmth)
- Clean white text for maximum readability
- Subtle glows and animations for liveliness

---

## 2. Color Palette

### Core Colors

| Token | Value | Usage |
|---|---|---|
| `--bg` | `#1a1a2e` | App background |
| `--surface` | `#16213e` | Sidebar, header, input bar |
| `--surface-2` | `#0f3460` | Peer list active state, incoming messages |
| `--surface-3` | `#162040` | Hover states |
| `--border` | `rgba(255,255,255,0.07)` | Dividers, card borders |

### Brand Colors

| Token | Value | Usage |
|---|---|---|
| `--accent` | `#e94560` | Primary action, outgoing messages, online dot CTA |
| `--accent-hover` | `#c73050` | Button hover |
| `--accent-2` | `#f5a623` | Scanning dot, toast sender name, gradient |

### Text Colors

| Token | Value | Usage |
|---|---|---|
| `--text` | `#eef2ff` | Primary content text |
| `--text-muted` | `#7b7f9e` | Secondary text, labels, peer IP |
| `--text-dim` | `#4a4e6e` | Timestamps, dividers, empty state text |

### Semantic Colors

| Token | Value | Usage |
|---|---|---|
| `--online` | `#4ade80` | Online status indicator |
| `--msg-out-bg` | `#e94560` | Outgoing message bubble |
| `--msg-in-bg` | `#1e2d50` | Incoming message bubble |

---

## 3. Typography

**Primary font stack** (system fonts, no download required):
```
'Inter', 'Segoe UI', -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif
```

**Monospace font stack** (peer IDs, IPs):
```
'JetBrains Mono', 'Cascadia Code', 'Fira Code', 'Consolas', monospace
```

### Type Scale

| Usage | Size | Weight | Color |
|---|---|---|---|
| App title | 32px | 700 | gradient |
| Section label | 11px | 700 | `--text-muted` (uppercase) |
| Peer name | 14px | 600 | `--text` |
| Message text | 14px | 400 | `--text` |
| Peer IP | 11px | 400 | `--text-muted` (mono) |
| Timestamp | 10px | 400 | `--text-dim` |
| Button | 14px | 600 | varies |

---

## 4. Spacing

Base unit: **4px**

| Scale | Value | Usage |
|---|---|---|
| xs | 4px | Icon padding |
| sm | 8px | Compact padding |
| md | 12–14px | Standard padding |
| lg | 16–20px | Section padding |
| xl | 24–28px | Modal padding |
| 2xl | 32px | Wide spacing |

---

## 5. Border Radius

| Token | Value | Usage |
|---|---|---|
| `--radius-sm` | 6px | Small elements, inner bubbles |
| `--radius` | 10px | Cards, inputs, buttons |
| `--radius-lg` | 18px | Message bubbles, large panels |
| `--radius-pill` | 999px | Badges, count pills |
| 50% | — | Avatars |

---

## 6. Shadows

```css
--shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
```

Used for: modal panels, toast notifications.

---

## 7. Components

### Avatar
- Size: 36×36px (sidebar), 32×32px (chat header)
- Shape: circle (50% border-radius)
- Content: initials (max 2 chars, uppercase)
- Background: deterministic gradient based on peer ID hash
- Own avatar: accent gradient (#e94560 → #f5a623)

### Peer Card
- Padding: 10px
- Border-radius: 10px
- Active state: `rgba(233,69,96,0.15)` background
- Hover state: `rgba(255,255,255,0.05)` background
- Online dot: 9×9px green circle, positioned bottom-left of avatar

### Message Bubble
- Outgoing (right-aligned): accent red (#e94560), bottom-right radius 6px
- Incoming (left-aligned): dark blue (#1e2d50), bottom-left radius 6px
- Padding: 9px 14px
- Max-width: 72% of chat area
- Border-radius: 18px (with one corner flattened for direction)

### Input Field
- Background: `--bg`
- Border: 1px solid `--border`, transitions to `--accent` on focus
- Border-radius: 18px (pill)
- Auto-resizing textarea, max height 120px

### Send Button
- Size: 42×42px circle
- Background: `--accent`
- Icon: ➤ (18px)
- Hover: darken 15%
- Active: scale(0.92)

### Settings Panel
- Modal overlay with blur backdrop
- Panel size: 360px × auto
- Border-radius: 18px
- Animation: slide up + fade in (200ms ease)

---

## 8. Animation

| Use case | Duration | Easing |
|---|---|---|
| Message appear | 180ms | ease |
| Modal open/close | 200ms | ease |
| Toast slide in/out | 300ms | ease |
| Splash fade out | 400ms | ease |
| Radar ring expand | 2000ms | ease-out (infinite) |
| Button active press | 100ms | ease |

---

## 9. App Icon Concept

Emoji-based for MVP: 📡 (satellite dish)
Color: accent gradient applied as background gradient to icon frame.

Future: Custom SVG — a stylised radio tower with radiating arcs in the accent color palette.

