# Team: `design` — Design

## Role

UI/UX, Theme, Assets, Visual Identity

## Responsibilities

- Create the visual identity for NoCloudChat (color palette, typography, logo concept)
- Design UI mockups/wireframes for each screen in `design/mockups/`
- Define the theme system (light/dark mode) in `design/theme.md`
- Choose fonts — prioritize system fonts or bundle a single lightweight font
- Design emoji/reaction set if custom (or define which standard set to use)
- Create or source icons and visual assets in `design/assets/`
- Design notification sounds and interaction feedback concepts
- Ensure accessibility: sufficient contrast, readable font sizes, keyboard navigation

## Guidelines

- Keep it **simple and fun** — think walkie-talkie meets modern chat
- No fancy/complex UI. Every screen should be self-explanatory
- Maximum 3 screens for MVP: peer list, chat, settings
- Use a warm, friendly color palette — avoid corporate/sterile aesthetics
- Design mobile-first layout (works on small screens) that scales up to desktop
- Prefer SVG for icons, keep total asset size minimal
- Mockups can be described in markdown or created as `.pen` files in `design/mockups/`
- Document the design system in `design/theme.md` (colors, spacing, typography, border radius, etc.)
- Name the visual style — give the app personality

## Screen Inventory (MVP)

1. **Splash / Discovery screen**: Shows app logo + "Looking for peers..." animation
2. **Peer list / Home screen**: List of discovered peers with status, tap to chat
3. **Chat screen**: Message thread with text input, attach file, record voice
4. **Settings screen**: Display name, theme toggle, about

## Output Artifacts

- `design/theme.md` — full design system specification
- `design/mockups/` — screen mockups (markdown descriptions or .pen files)
- `design/assets/` — icons, logo, images
- `design/fonts.md` — font choices and fallback stack
