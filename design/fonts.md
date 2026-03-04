# NoCloudChat — Typography

**Date**: 2026-03-02

---

## Font Strategy

**No bundled fonts.** NoCloudChat uses the operating system's native font stack to:
- Eliminate download size (no WOFF/TTF files)
- Ensure fast rendering (already cached by OS)
- Feel native on each platform

---

## Primary Font Stack

```css
font-family: 'Inter', 'Segoe UI', -apple-system, BlinkMacSystemFont,
             'Helvetica Neue', Arial, sans-serif;
```

| Font | Platform |
|---|---|
| Inter | If installed (common on modern systems) |
| Segoe UI | Windows 10+ |
| -apple-system | macOS / iOS (SF Pro) |
| BlinkMacSystemFont | Chrome on macOS |
| Helvetica Neue | Older macOS |
| Arial | Fallback |

---

## Monospace Font Stack

Used for: peer IDs, IP addresses

```css
font-family: 'JetBrains Mono', 'Cascadia Code', 'Fira Code',
             'Consolas', 'Courier New', monospace;
```

| Font | Platform |
|---|---|
| JetBrains Mono | If installed (developer machines) |
| Cascadia Code | Windows 11 Terminal |
| Fira Code | Common on developer machines |
| Consolas | Windows (built-in) |
| Courier New | Universal fallback |

---

## Font Size Reference

| Context | Size | Notes |
|---|---|---|
| App title (splash) | 32px | Bold, gradient fill |
| Section labels | 11px | Uppercase, tracked |
| Peer name | 14px | Semibold |
| Message body | 14px | Regular |
| Timestamps | 10px | Regular |
| Peer IP | 11px | Monospace |
| Buttons | 14px | Semibold |
| Modal heading | 18px | Bold |

