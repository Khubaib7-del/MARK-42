Rebuild this design in your project. Match it exactly: same layout,
typography, color, spacing, radius, shadow, and motion. Do not invent
new visuals.

## design.md

# Institutional Calm — Style Reference
> calm, precise, institutional, airy

**Theme:** light

The posture card is an editorial headline: one calm statement of fact that anchors everything below it, with evidence subordinate and clearly labeled. A light, airy surface system that reads as precision infrastructure rather than consumer security theater. The organic green field from the launch animation lives only as a feathered ambient wash behind the posture hero, keeping the rest of the screen crisp and legible. Generous whitespace and a tight left-rail grid give every data point room to breathe without feeling sparse.

**Ground truth (computed from tokens + reference HTML):** light theme · page #8eb89a · ink #b8d4bb · primary #0a6c60 · secondary #54e454 · display "Manrope" · body "Manrope". Where the description above conflicts with these values or the Reference HTML, the tokens and HTML are authoritative.

## Tokens: Colors

| Name | Value | Token | Role | Usage | Contrast |
|------|-------|-------|------|-------|----------|
| Canvas | `#8eb89a` | `--gesso-canvas` | Page background, the floor everything sits on. | Outermost background: body, full-bleed sections. Mirrors Neutral 50. | n/a |
| Surface recessed | `#88b194` | `--gesso-surface-recessed` | Sunken surface below the canvas. | Inset wells: input fields, progress tracks, code blocks. | n/a |
| Surface | `#b8d4bb` | `--gesso-surface` | Card and panel fill, raised above the canvas. | Cards, panels, sheets, table rows. Mirrors Neutral 100. | n/a |
| Surface elevated | `#c1d9c3` | `--gesso-surface-elevated` | Top elevation tier. | Modals, dropdowns, popovers, tooltips. | n/a |
| Divider | `rgba(255,255,255,0.04)` | `--gesso-divider` | Hairline borders and separators. | 1px rules between rows and sections. Never for text. | n/a |
| Foreground | `#ffffff` | `--gesso-fg` | Primary text and high-emphasis icons. | Body copy, headings, primary icons. Mirrors Neutral 900. | AA 4.5:1 on canvas (guaranteed) |
| Foreground muted | `#3b5d48` | `--gesso-fg-muted` | Secondary text. | Captions, metadata, placeholders, disabled labels. Mirrors Neutral 600. | AA 3.0:1 on canvas (guaranteed) |
| Primary | `#0a6c60` | `--gesso-primary` | Brand accent, FILL only (alias: --gesso-accent). | CTA fills, active and selected states, focus rings. 2 to 3 per screen. Do NOT use as text, reach for --gesso-accent-text. | Pair with --gesso-on-accent for the label on top. |
| On primary | `#FFFFFF` | `--gesso-on-accent` | Text and icons on a filled primary. | Label color for buttons and chips filled with --gesso-primary. | Contrast-derived against --gesso-primary. |
| Accent (as text) | `#ffffff` | `--gesso-accent-text` | AA-safe accent for text and icons. | Use THIS for accent-colored links, headings, and icons. Use --gesso-primary for fills. | AA 4.5:1 on canvas (guaranteed). |
| Secondary | `#54e454` | `--gesso-secondary` | Supporting brand accent. | Secondary fills, logo discs, supporting highlights. | Pair with on-fill text per --gesso-on-accent. |
| Secondary (as text) | `#ffffff` | `--gesso-accent-2-text` | AA-safe secondary for text. | Secondary accent used as text or icons. | AA 4.5:1 on canvas (guaranteed). |
| Neutral 50 | `#8eb89a` | `--gesso-neutral-50` | Page background. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 100 | `#b8d4bb` | `--gesso-neutral-100` | Surface. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 200 | `#9ebba2` | `--gesso-neutral-200` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 300 | `#84a28b` | `--gesso-neutral-300` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 400 | `#6b8b74` | `--gesso-neutral-400` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 500 | `#53735d` | `--gesso-neutral-500` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 600 | `#3b5d48` | `--gesso-neutral-600` | Muted text and dividers. | Ramp access by step; prefer the role token above where one exists. | AA 3.0:1 on canvas. |
| Neutral 700 | `#818e79` | `--gesso-neutral-700` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 800 | `#c2c4b7` | `--gesso-neutral-800` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Neutral 900 | `#ffffff` | `--gesso-neutral-900` | Primary text. | Ramp access by step; prefer the role token above where one exists. | AA 4.5:1 on canvas. |
| Neutral 950 | `#ffffff` | `--gesso-neutral-950` | Neutral ramp step. | Ramp access by step; prefer the role token above where one exists. | n/a |
| Success | `#12823b` | `--gesso-success` | Positive signals (gains, completed states). | Meaning only, never decoration. | AA 3.0:1 on canvas, chroma-floored distinct. |
| Warning | `#ae5f05` | `--gesso-warning` | Caution states. | Meaning only, never decoration. | AA 3.0:1 on canvas, chroma-floored distinct. |
| Error | `#DC2626` | `--gesso-error` | Errors, destructive actions, negative signals. | Meaning only, never decoration. | AA 3.0:1 on canvas, chroma-floored distinct. |
| Data 1 | `#006808` | `--gesso-data-1` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |
| Data 2 | `#00830e` | `--gesso-data-2` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |
| Data 3 | `#009f13` | `--gesso-data-3` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |
| Data 4 | `#18bb23` | `--gesso-data-4` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |
| Data 5 | `#43d645` | `--gesso-data-5` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |
| Data 6 | `#62f161` | `--gesso-data-6` | Categorical data-viz series color. | Charts and series, applied in order. | n/a |

## Tokens: Typography

### Manrope — Display. Headings, hero copy, large numerical specimens. · `--gesso-font-display`
- **Weights:** 300, 400, 500, 600, 700
- **Line height:** 1.1
- **Letter spacing:** -0.02em
- **Role:** Display. Headings, hero copy, large numerical specimens.

### Manrope — Body. Paragraphs, labels, UI chrome. · `--gesso-font-body`
- **Weights:** 300, 400, 500, 600, 700
- **Line height:** 1.5
- **Letter spacing:** 0em
- **Role:** Body. Paragraphs, labels, UI chrome.

### Manrope — Mono. Code, numerical tickers, mono-spaced metadata. · `--gesso-font-mono`
- **Weights:** 300, 400, 500, 600, 700
- **Line height:** 1.4
- **Letter spacing:** 0em
- **Role:** Mono. Code, numerical tickers, mono-spaced metadata.

### Type Scale

| Role | Size | Line Height | Letter Spacing | Token |
|------|------|-------------|----------------|-------|
| H1 | 48px | 1.2 | — | `--gesso-text-4xl` |
| H2 | 40px | 1.2 | — | `--gesso-text-3xl` |
| H3 | 32px | 1.2 | — | `--gesso-text-2xl` |
| Body | 16px | 1.5 | — | `--gesso-text-base` |
| Caption | 11px | 1.5 | — | `--gesso-text-xs` |

## Tokens: Spacing & Shapes

**Base unit:** 8px

**Density:** comfortable

### Spacing Scale

| Name | Value | Token |
|------|-------|-------|
| space-1 | 8px | `--gesso-space-1` |
| space-2 | 16px | `--gesso-space-2` |
| space-3 | 24px | `--gesso-space-3` |
| space-4 | 32px | `--gesso-space-4` |
| space-6 | 48px | `--gesso-space-6` |
| space-8 | 64px | `--gesso-space-8` |
| space-12 | 96px | `--gesso-space-12` |
| space-16 | 128px | `--gesso-space-16` |
| space-24 | 192px | `--gesso-space-24` |
| space-32 | 256px | `--gesso-space-32` |

### Border Radius

| Element | Value |
|---------|-------|
| none | 0px |
| sm | 10px |
| md | 16px |
| lg | 24px |
| full | 9999px |

### Shadows

| Name | Value | Token |
|------|-------|-------|
| sm | `0 1px 2px rgba(12,19,34,0.08)` | `--gesso-shadow-sm` |
| md | `0 4px 12px rgba(12,19,34,0.10)` | `--gesso-shadow-md` |
| lg | `0 16px 40px rgba(12,19,34,0.14)` | `--gesso-shadow-lg` |

## Components

### Card
**Role:** Container surface for content groupings.

Background --gesso-neutral-50 (#8eb89a), border 1px solid --gesso-neutral-200 (#e5e6e9), border-radius var(--gesso-radius-md) (16px), padding 32px, --gesso-shadow-sm. Body font for content; display font for any embedded headline. Text fg --gesso-neutral-900 (#b8d4bb).

### Primary Button
**Role:** Highest-emphasis action. Reserved for the main CTA per screen.

Background --gesso-primary (#0a6c60), text auto-picked for max contrast (white or near-black), padding 24px 48px, border-radius var(--gesso-radius-md) (16px), font-family --gesso-font-body, font-weight 500. Hover: mix toward --gesso-fg by 10-12%. Use 1-2 per screen, never more.

### Secondary Button
**Role:** Supporting action next to a primary CTA.

Background transparent, border 1.5px solid --gesso-primary (#0a6c60), text --gesso-primary, padding 24px 48px (minus 1.5px each axis to compensate for the border), border-radius var(--gesso-radius-md) (16px), body font, weight 500.

### Input
**Role:** Single-line text entry. Default form field.

Background --gesso-neutral-100 (#b8d4bb), border 1px solid --gesso-neutral-300 (#d2d3d7), border-radius var(--gesso-radius-md) (16px), padding 24px 32px, font-size 16px (prevents iOS zoom), body font. Focus: border --gesso-primary, ring 3px --gesso-primary at 14% alpha.

### Badge
**Role:** Compact label for status, tags, counts.

Background --gesso-primary (#0a6c60) at 12% alpha, text --gesso-primary, padding 16px 24px, border-radius var(--gesso-radius-full) (9999px), font-size 12px, body font, weight 500, uppercase, letter-spacing 0.04em.

### Modal
**Role:** Focus-stealing overlay for confirmations or short flows.

Background --gesso-neutral-50 (#8eb89a), border 1px solid --gesso-neutral-200, border-radius var(--gesso-radius-lg) (24px), padding 48px, --gesso-shadow-lg. Backdrop: fg at 40% alpha + blur 12px. Max-width 480px on mobile, 560px on desktop.

### Nav Item
**Role:** Single entry in a navigation list (sidebar, top bar, or tab row).

Padding 16px 24px, border-radius var(--gesso-radius-sm) (10px), text --gesso-neutral-700 (#484c53), body font, weight 400. Hover: bg --gesso-neutral-100. Active: bg --gesso-primary at 12% alpha, text --gesso-primary, weight 500.

### Avatar
**Role:** Identity surface for users / accounts / brands.

Background --gesso-secondary (#54e454), text auto-picked for contrast on secondary, border-radius var(--gesso-radius-full) (9999px), aspect-ratio 1:1. Size variants: 24/32/40/48px. Initials at body weight 500, sized to ~40% of the avatar diameter.

## Do's and Don'ts

### Do

- One clean grotesque/geometric sans across the board (Inter / SF / system-ui), with at most a mono only for codes. Display = semibold-to-bold (600-700) headlines at comfortable but not oversized scale; body = regular 400 charcoal; metadata labels = regular in muted grey, often slightly smaller. Tight-to-normal tracking, no all-caps except tiny section eyebrows (DEPART / ARRIVE) which get +0.04em.
- Tone-locked light. canvas = #FFFFFF to #F7F7F5 (near-white, faintly warm-neutral ground); surface = pure #FFFFFF flat cards lifted off canvas; ink = charcoal #1A1A1A / #222 (never pure black for body); muted = grey #8A8A8E for labels and inactive nav. One sparing accent only, an orange (#E8632A-ish) OR a muted moss green (#5A6B2F) used for the single primary CTA, active tab, and key data emphasis; semantic green/red reserved for status deltas. Accent occupies <=10% of any screen.
- Generous whitespace is the defining posture; layout is functional and grid-aligned on an 8px rhythm. Mobile (393x852): full-bleed media hero (map/photo) flowing into a rounded white bottom sheet or stacked flat cards; sticky bottom tab bar and sticky primary CTA.
- Apply --gesso-primary (#0a6c60) to a maximum of 2-3 elements per screen: a button, a highlight, a badge. Never paint large areas with primary.
- Use --gesso-radius-md (16px) for cards and inputs, --gesso-radius-full for badges and avatars. Inner radii inside a parent: subtract the parent's padding from its radius.
- Build hierarchy with the neutral scale, not extra hues. 90%+ of any screen should be neutrals; chromatic colors carry meaning, never decoration.

### Don't

- Never use playful multi-color blocks or filled colored cards, accent appears on at most ONE element per region
- Never use raw/oversized display type or heavy black weights as decoration
- Never add drop shadows beyond a single near-invisible ambient lift; no stacked or colored shadows
- Never wrap cards in visible borders, use a white-on-near-white ground and hairline dividers instead
- Don't use Inter as the display font. It's the most overused font in tech. Pick something with character from the fontHints display list.
- Don't use #3B82F6 / indigo-600 as primary unless explicitly briefed. Default blue is the hallmark of a generic SaaS aesthetic.

## Surfaces

| Level | Name | Value | Purpose |
|-------|------|-------|---------|
| 0 | Page | `#8eb89a` | Default page background. The lightest surface. |
| 1 | Raised | `#b8d4bb` | Cards, panels, sidebars: anything that sits on top of the page. |
| 2 | Sunken | `#e5e6e9` | Inset surfaces (search bars, code blocks, disabled fields). |
| 3 | Overlay | `#8eb89a` | Modals and floating panels. Same hue as page; depth comes from --gesso-shadow-lg. |

## Agent Prompt Guide

**Quick Color Reference**

- Primary: #0a6c60
- Secondary: #54e454
- Page bg: #8eb89a
- Body fg: #ffffff
- Muted fg: #3b5d48
- Success: #12823b

**Example Component Prompts**

1. Build a card component. Background #8eb89a, border 1px solid #e5e6e9, border-radius 16px, padding 32px, body font (Manrope), text color #b8d4bb. Use Tailwind v4 arbitrary values: bg-[#8eb89a].

2. Build a primary button. Background #0a6c60, text white (or #b8d4bb if primary is light), padding 12px 24px, border-radius 16px, font weight 500, hover: shift bg toward #b8d4bb by 10%.

3. Build a heading hierarchy (CSS-standard roles). H1: 3rem display font (Manrope) weight 700, H2: 2.5rem, H3: 2rem, Body: 1rem body font (Manrope), Caption: 0.6875rem, color #b8d4bb.

4. Build a form input. Background #b8d4bb, border 1px solid #d2d3d7, border-radius 16px, padding 12px 16px, font-size 16px, focus border #0a6c60.

5. Build a navigation bar. Background #8eb89a, items at color #484c53 body weight, hover bg #b8d4bb, active item color #0a6c60 weight 500.

## Similar Brands

- **Linear** — Same Swiss grid lineage: sharp edges, restrained accent, monospace for metadata.
- **Vercel** — Black-and-white discipline with a single high-impact accent.
- **Raycast** — Dense, gridded, accent-as-punctuation. Slightly warmer.
- **Stripe** — Clean, confident, precise. Wider radius and softer shadows than this system.

## Screens

### 1. Institutional Calm

- Role: screen

<details><summary>HTML</summary>

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=393">
<title>Selvard, Home</title>

<style>:root { --ink: #0C1322; --ink2: #101A2E; --offwhite: #EDF1F7; --slate: #9FB0C6; --verdant: #0A6C60; --signal: #54E454; --forest: #2F5D50; --mutedg: #5A8F6E; --sage: #A3C9A8; --cream: #E9F5DB; --gesso-primary: #0a6c60; --gesso-secondary: #54e454; --gesso-success: #54E454; --gesso-warning: #c9a227; --gesso-error: #e0705a; --glass: rgba(255,255,255,0.055); --glass2: rgba(255,255,255,0.035); --hairline: rgba(255,255,255,0.10); --r: 24px; --r2: 28px; --dur: 220ms; --ease: cubic-bezier(0.2,0,0,1) }
* { margin: 0; padding: 0; box-sizing: border-box }
html,body { width: 393px; min-height: 915px }
body { padding: 47px 16px 34px 16px; background: var(--ink); color: var(--offwhite); font-family: var(--gesso-font-display); font-size: 15px; line-height: var(--gesso-leading-normal); -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale }
.ambient { position: absolute; top: 0; left: 0; width: 393px; height: 380px; z-index: 0; pointer-events: none; background: radial-gradient(60% 55% at 8% 0%, rgba(47,93,80,0.55), transparent 70%),
    radial-gradient(50% 45% at 95% 5%, rgba(163,201,168,0.28), transparent 70%),
    radial-gradient(55% 50% at 80% 90%, rgba(233,245,219,0.14), transparent 70%),
    radial-gradient(50% 55% at 10% 95%, rgba(90,143,110,0.30), transparent 70%); opacity: .55 }
main { position: relative; z-index: 1 }
.t-hero { font-family: var(--gesso-font-display); font-weight: 500; font-size: var(--gesso-text-3xl); line-height: 44px; letter-spacing: var(--gesso-tracking-tight) }
.t-body { font-family: var(--gesso-font-display); font-weight: 400; font-size: var(--gesso-text-base); line-height: var(--gesso-leading-normal) }
.t-meta { font-family: var(--gesso-font-display); font-weight: 400; font-size: .75rem; line-height: var(--gesso-leading-snug); letter-spacing: .08em; text-transform: uppercase; color: var(--slate) }
.num { font-feature-settings: "tnum" }
.top { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--gesso-space-1) }
.wordmark { font-weight: 700; font-size: .9375rem; letter-spacing: .32em; color: var(--offwhite) }
.greet { color: var(--slate); font-size: .9375rem; margin-bottom: var(--gesso-space-3) }
.glass { background: var(--glass); border-radius: var(--r2); border: 1px solid var(--hairline); box-shadow: inset 0 1px 0 rgba(255,255,255,0.09), 0 8px 24px rgba(0,0,0,0.28); padding: var(--gesso-space-3) }
.glass--quiet { background: var(--glass2); border-radius: var(--r); padding: var(--gesso-space-2) 20px }
.posture { margin-bottom: var(--gesso-space-2) }
.posture-state { display: flex; align-items: baseline; gap: 12px; margin: var(--gesso-space-1) 0 20px }
.state-word { font-size: var(--gesso-text-2xl); line-height: 44px; font-weight: 500; letter-spacing: var(--gesso-tracking-tight) }
.conf { font-size: .75rem; letter-spacing: .06em; color: var(--signal); text-transform: none }
.reasons { display: flex; flex-direction: column; gap: 12px }
.reason { display: flex; align-items: center; gap: 12px }
.reason .lbl { width: 64px; font-size: .75rem; letter-spacing: .08em; text-transform: uppercase; color: var(--slate) }
.reason .what { flex: 1; font-size: .875rem; color: var(--offwhite); min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.reason .st { font-size: .75rem; color: var(--mutedg); letter-spacing: .04em; flex-shrink: 0 }
.track { flex: 1; height: 6px; border-radius: var(--gesso-radius-full); background: rgba(255,255,255,0.08); position: relative; overflow: hidden }
.track .fill { position: absolute; inset: 0; right: auto; border-radius: var(--gesso-radius-full); background: var(--forest) }
.reason[data-ok="1"] .track .fill { background: var(--mutedg) }
.reason[data-ok="0"] .track .fill { background: var(--gesso-neutral-100) }
.nest { margin-bottom: var(--gesso-space-2); display: flex; align-items: center; gap: 20px }
.nest-copy { flex: 1; min-width: 0 }
.nest-title { font-weight: 700; font-size: 1.125rem; line-height: var(--gesso-leading-snug); margin-bottom: 4px }
.nest-sub { font-size: .875rem; color: var(--slate) }
.ring { width: 88px; height: 88px; flex-shrink: 0 }
.nest-actions { display: flex; align-items: center; gap: 12px; margin-top: var(--gesso-space-2) }
.btn { font-family: var(--gesso-font-display); font-weight: 700; font-size: .875rem; padding: 12px var(--gesso-space-3); border-radius: var(--gesso-radius-full); border: none; cursor: pointer; transition: transform var(--dur) var(--ease),filter var(--dur) var(--ease) }
.btn-primary { background: var(--verdant); color: #ffffff }
.btn-primary:hover { filter: brightness(1.12) }
.btn-primary:active { transform: translateY(1px) scale(0.98) }
.btn-ghost { background: transparent; color: var(--sage); padding: 12px var(--gesso-space-1) }
.btn-ghost:hover { color: var(--offwhite) }
button:focus-visible,a:focus-visible,[role="button"]:focus-visible { outline: 1px solid var(--gesso-neutral-200); outline-offset: 2px }
.sec { margin: var(--gesso-space-4) 0 12px; display: flex; align-items: baseline; justify-content: space-between }
.sec h2 { font-size: .75rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; color: var(--slate) }
.seclink { font-size: .8125rem; color: var(--signal); text-decoration: none }
.seclink:hover { color: var(--offwhite) }
.attention { display: flex; flex-direction: column }
.att-row { display: flex; align-items: center; gap: var(--gesso-space-2); padding: var(--gesso-space-2) 4px; border-bottom: 1px solid rgba(255,255,255,0.05) }
.att-row:last-child { border-bottom: none }
.att-icon { width: 44px; height: 44px; border-radius: var(--gesso-radius-md); background: rgba(255,255,255,0.06); display: flex; align-items: center; justify-content: center; flex-shrink: 0 }
.att-copy { flex: 1; min-width: 0 }
.att-copy strong { display: block; font-weight: 700; font-size: .9375rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.att-copy span { display: block; font-size: .8125rem; color: var(--slate); white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.att-act { flex-shrink: 0 }
.chips { display: flex; flex-wrap: wrap; gap: var(--gesso-space-1); margin-bottom: var(--gesso-space-1) }
.chip { display: inline-flex; align-items: center; gap: var(--gesso-space-1); padding: 10px var(--gesso-space-2); border-radius: var(--gesso-radius-full); background: rgba(255,255,255,0.05); border: 1px solid var(--gesso-divider, rgba(0,0,0,0.06)); font-size: .8125rem; color: var(--offwhite); cursor: pointer; transition: background var(--dur) var(--ease) }
.chip:hover { background: rgba(255,255,255,0.10) }
.chip .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--signal) }
.events { display: flex; flex-direction: column }
.ev-row { display: flex; align-items: center; gap: var(--gesso-space-2); padding: var(--gesso-space-2) 4px; border-bottom: 1px solid rgba(255,255,255,0.05) }
.ev-row:last-child { border-bottom: none }
.ev-img { width: 44px; height: 44px; border-radius: var(--gesso-radius-md); overflow: hidden; flex-shrink: 0; position: relative; isolation: isolate }
.ev-img::after { content: ''; position: absolute; inset: 0; background: var(--forest); mix-blend-mode: multiply; opacity: .25 }
.ev-img img { width: 100%; height: 100%; object-fit: cover; filter: grayscale(.4) contrast(1.05) brightness(1.05) }
.ev-copy { flex: 1; min-width: 0 }
.ev-copy strong { display: block; font-weight: 700; font-size: .9375rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.ev-copy span { display: block; font-size: .8125rem; color: var(--slate); white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.badge { flex-shrink: 0; font-size: .6875rem; letter-spacing: .06em; padding: 5px 10px; border-radius: var(--gesso-radius-full); white-space: nowrap }
.b-ok { color: var(--signal); background: var(--gesso-secondary) }
.b-sus { color: var(--gesso-warning); background: rgba(201,162,39,0.12) }
.b-blk { color: var(--gesso-error); background: rgba(224,112,90,0.12) }
.cta-anchor { position: fixed; left: var(--gesso-space-2); right: var(--gesso-space-2); bottom: var(--gesso-space-12); z-index: 5 }
.cta { width: 100%; padding: var(--gesso-space-2) var(--gesso-space-3); border-radius: var(--r); background: var(--verdant); color: #fff; border: none; font-family: var(--gesso-font-display); font-weight: 700; font-size: var(--gesso-text-base); cursor: pointer; transition: transform var(--dur) var(--ease),filter var(--dur) var(--ease),box-shadow var(--dur) var(--ease); box-shadow: var(--gesso-shadow-sm, 0 1px 2px rgba(0,0,0,0.06))}
.cta:hover { filter: brightness(1.1); transform: translateY(-1px) }
.cta:active { transform: translateY(1px) scale(0.99) }
.ic { width: 16px; height: 16px; stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round }
.ic-lg { width: 24px; height: 24px }
.att-act .ic { color: var(--slate) }
@media (prefers-reduced-motion:reduce) {*,*::before,*::after { transition-duration: 0.01ms !important; animation-duration: 0.01ms !important }}</style>
<style>/* gesso-icon-base v1 */
.ic { display: inline-block; width: 16px; height: 16px; vertical-align: -0.125em; flex-shrink: 0; line-height: 0; }
.ic svg { width: 100%; height: 100%; display: block; }
svg.ic { width: 16px; height: 16px; display: inline-block; vertical-align: -0.125em; flex-shrink: 0; }
.ic[data-icon-style="line"] { stroke-width: var(--ic-stroke, 2); }
.ic[data-icon-style="line"] svg path, .ic[data-icon-style="line"] svg circle, .ic[data-icon-style="line"] svg rect, .ic[data-icon-style="line"] svg line, .ic[data-icon-style="line"] svg polyline, .ic[data-icon-style="line"] svg polygon { stroke-width: inherit; }
.ic-sm { --ic-stroke: 2.25; }
.ic-xs { --ic-stroke: 2.5; }
svg.ic-lg, .ic-lg svg { width: 24px; height: 24px; }
svg.ic-xl, .ic-xl svg { width: 32px; height: 32px; }
svg.ic-2xl, .ic-2xl svg { width: 32px; height: 32px; }
.ic-lg { --ic-stroke: 1.75; }
.ic-xl { --ic-stroke: 1.5; }
.ic-2xl { --ic-stroke: 1.5; }
button { border: 0; background: transparent; padding: 0; font: inherit; color: inherit; cursor: pointer; -webkit-appearance: none; appearance: none; }
</style>
<style id="gesso-component-styles">body {
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
}

button {
  border: 0;
  background: transparent;
  -webkit-appearance: none;
  appearance: none;
  font: inherit;
  color: inherit;
  padding: 0;
  cursor: pointer;
}

[data-component="Card"]:not([class]):not([style]) {
  background: var(--gesso-neutral-800) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  padding: var(--gesso-space-4) !important;
  color: var(--gesso-neutral-50) !important;
  box-shadow: none !important;
}

[data-component="Button"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: var(--gesso-space-2) !important;
  white-space: nowrap !important;
  background: var(--gesso-primary) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  font-weight: 600 !important;
  padding: var(--gesso-space-3) var(--gesso-space-6) !important;
  border: 0 !important;
  border-radius: var(--gesso-radius-full) !important;
  cursor: pointer !important;
  min-height: 48px !important;
  text-decoration: none !important;
}
[data-component="Button"][data-variant="secondary"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
}
[data-component="Button"][data-variant="ghost"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  background: transparent !important;
  color: var(--gesso-neutral-50) !important;
}

[data-component="Input"]:not([class]):not([style]) {
  display: block !important;
  width: 100% !important;
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  padding: var(--gesso-space-3) var(--gesso-space-4) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  min-height: 48px !important;
  outline: none !important;
}
[data-component="Input"]:not([class]):not([style])::placeholder {
  color: var(--gesso-neutral-400) !important;
}

[data-component="Select"]:not([class]):not([style]) {
  display: block !important;
  width: 100% !important;
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  padding: var(--gesso-space-3) var(--gesso-space-4) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  min-height: 48px !important;
}

[data-component="Toggle"]:not([class]):not([style]), [data-component="Checkbox"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  gap: var(--gesso-space-2) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-sm) !important;
}

[data-component="Badge"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  gap: var(--gesso-space-1) !important;
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-xs) !important;
  font-weight: 500 !important;
  padding: var(--gesso-space-1) 10px !important;
  border-radius: var(--gesso-radius-full) !important;
  line-height: 1.2 !important;
  max-height: 24px !important;
}

[data-component="StatCell"]:not([class]):not([style]) {
  display: flex !important;
  flex-direction: column !important;
  gap: 4px !important;
  background: transparent !important;
  border: 0 !important;
  padding: 0 !important;
}
[data-component="StatCell"]:not([class]):not([style]) > :first-child {
  font-family: var(--gesso-font-display) !important;
  font-weight: 700 !important;
  color: var(--gesso-neutral-50) !important;
}

[data-component="ListRow"]:not([class]):not([style]):not(ul):not(ol) {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 12px 16px !important;
  border-bottom: 1px solid rgba(10,10,10,0.05) !important;
  background: transparent !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  min-height: 56px !important;
}
[data-component="ListRow"]:not([class]):not([style]):last-child {
  border-bottom: 0 !important;
}
/* A mislabeled list container stays a list, so its rows stack. */
ul[data-component="ListRow"]:not([class]):not([style]),
ol[data-component="ListRow"]:not([class]):not([style]) {
  display: block !important;
  list-style: none !important;
  margin: 0 !important;
  padding: 0 !important;
}
/* Two-column enforcement (info LEFT / action RIGHT, no floating middle).
   The trailing affordance pins to the right edge so the row reads as exactly
   two zones; everything else clusters on the left and the gutter collapses.
   Action-less rows carry no Button/Toggle child, so nothing pins and they
   flow naturally. min-width:0 lets the text zone shrink + truncate instead
   of forcing a wrap (the prompt forbids subtitle wrapping). Only inside a
   row WE laid out (bare): a model-styled row keeps its own arrangement. */
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"],
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"] > button,
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"] > a,
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Toggle"] {
  margin-inline-start: auto !important;
  flex-shrink: 0 !important;
}
[data-component="ListRow"]:not([class]):not([style]) > * {
  min-width: 0 !important;
}

[data-component="Avatar"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 40px !important;
  height: 40px !important;
  flex-shrink: 0 !important;
  border-radius: var(--gesso-radius-full) !important;
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-display) !important;
  font-size: var(--gesso-text-sm) !important;
  font-weight: 600 !important;
  overflow: hidden !important;
}
[data-component="Avatar"]:not([class]):not([style]) img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

[data-component="EmptyState"]:not([class]):not([style]) {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 16px !important;
  padding: 48px 24px !important;
  text-align: center !important;
  color: var(--gesso-neutral-400) !important;
  font-family: var(--gesso-font-body) !important;
}

[data-component="TabBar"]:not([class]):not([style]) {
  display: flex !important;
  align-items: stretch !important;
  background: var(--gesso-neutral-900) !important;
  border-top: 1px solid rgba(10,10,10,0.05) !important;
  padding: 8px 8px 0 8px !important;
}

[data-gesso-marker-wrap],
[data-component][style*="display: contents"],
[data-component="Button"]:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"]) {
  display: contents !important;
  padding: 0 !important;
  margin: 0 !important;
  background: none !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  min-height: 0 !important;
  color: inherit !important;
  font: inherit !important;
}</style>
<style id="gesso-chrome-clearance">
body { padding-top: 44px !important; padding-bottom: 80px !important; }
[data-sticky-action="true"] {
  box-sizing: border-box !important;
  display: flex !important;
  gap: 12px !important;
  align-items: stretch !important;
  padding: 12px 16px !important;
}
[data-sticky-action="true"] > button,
[data-sticky-action="true"] > a,
[data-sticky-action="true"] > [role="button"] {
  flex: 1 1 0 !important;
  min-height: 48px !important;
  min-width: 0 !important;
}
</style>
<style id="gesso-text-wrap">h1,h2,h3{text-wrap:balance}p,li,figcaption,blockquote{text-wrap:pretty}</style>
<style id="gesso-font-smoothing">html{-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale}</style>
<style id="gesso-image-outline">img:not([data-illustration]):not([data-icon]):not([aria-hidden="true"]){outline:1px solid rgba(255,255,255,0.05);outline-offset:-1px}</style>
<style id="gesso-tokens">

:root {
  --gesso-primary: #0a6c60;
  --gesso-secondary: #54e454;
  --gesso-neutral-50: #8eb89a;
  --gesso-neutral-100: #b8d4bb;
  --gesso-neutral-200: #e5e6e9;
  --gesso-neutral-300: #d2d3d7;
  --gesso-neutral-400: #5a8f6e;
  --gesso-neutral-500: #888b91;
  --gesso-neutral-600: #5a8f6e;
  --gesso-neutral-700: #484c53;
  --gesso-neutral-800: #30343c;
  --gesso-neutral-900: #b8d4bb;
  --gesso-neutral-950: #8eb89a;
  --gesso-success: #16A34A;
  --gesso-warning: #c36b05;
  --gesso-error: #DC2626;
  --gesso-font-display:"Manrope", system-ui, -apple-system, sans-serif;
  --gesso-font-body:"Manrope", system-ui, -apple-system, sans-serif;
  --gesso-font-mono:"Manrope", ui-monospace, "JetBrains Mono", monospace;
  --gesso-leading-tight: 1.15;
  --gesso-leading-snug: 1.35;
  --gesso-leading-normal: 1.5;
  --gesso-leading-relaxed: 1.7;
  --gesso-tracking-tight: -0.01em;
  --gesso-tracking-normal: 0em;
  --gesso-tracking-wide: 0.04em;
  --gesso-radius-sm: 10px;
  --gesso-radius-md: 16px;
  --gesso-radius-lg: 24px;
  --gesso-radius-full: 9999px;
  --gesso-shadow-sm: 0 1px 2px rgba(12,19,34,0.08);
  --gesso-shadow-md: 0 4px 12px rgba(12,19,34,0.10);
  --gesso-shadow-lg: 0 16px 40px rgba(12,19,34,0.14);
  --gesso-text-lg: 1.25rem;
  --gesso-text-sm: 0.875rem;
  --gesso-text-xl: 1.5rem;
  --gesso-text-xs: 0.6875rem;
  --gesso-text-2xl: 2rem;
  --gesso-text-3xl: 2.5rem;
  --gesso-text-4xl: 3rem;
  --gesso-text-base: 1rem;
  --gesso-text-5xl: 3rem;
  --gesso-text-6xl: 3.75rem;
  --gesso-text-7xl: 4.5rem;
  --gesso-text-8xl: 6rem;
  --gesso-text-9xl: 8rem;
  --gesso-space-unit: 8px;
  --gesso-space-1: 8px;
  --gesso-space-2: 16px;
  --gesso-space-3: 24px;
  --gesso-space-4: 32px;
  --gesso-space-6: 48px;
  --gesso-space-8: 64px;
  --gesso-space-12: 96px;
  --gesso-space-16: 128px;
  --gesso-space-24: 192px;
  --gesso-space-32: 256px;
  --gesso-duration-fast: 120ms;
  --gesso-duration-base: 200ms;
  --gesso-duration-slow: 360ms;
  --gesso-easing-default: cubic-bezier(0.2,0,0,1);
  --gesso-easing-emphasis: cubic-bezier(0.34,1.56,0.64,1);
}

img,svg,video{max-width:100%;height:auto;}</style>
<style id="gesso-role-lock">:root{--gesso-fg:#0c1322 !important;--gesso-fg-muted:#5a8f6e !important;--gesso-accent:#0a6c60 !important;--gesso-accent-2:#54e454 !important;}</style>
<!--gesso-fonts:start--><style>@font-face{font-family:"Manrope";font-style:normal;font-weight:200 800;font-display:swap;src:url(/fonts/Manrope-Variable.woff2) format("woff2");}</style><style id="gesso-font-lock">:root{--gesso-font-display:"Manrope", system-ui, -apple-system, sans-serif !important;--gesso-font-body:"Manrope", system-ui, -apple-system, sans-serif !important;--gesso-font-mono:"Manrope", ui-monospace, "JetBrains Mono", monospace !important;}</style><!--gesso-fonts:end-->
</head>
<body data-brief-id="screen-root" data-brief-role="screen">
<section data-chrome="status-bar" data-brief-id="chrome-status-bar" data-brief-role="status-bar" style="position:fixed;top:0;left:0;right:0;height:44px;z-index:100;pointer-events:none;color:#0A0A0A;font-family:var(--gesso-font-body, system-ui);background:transparent;">
  <span data-time aria-hidden="true" style="position:absolute;left:34.68px;top:50%;transform:translateY(-50%);font-size:17px;font-weight:600;letter-spacing:-0.4px;line-height:1;font-variant-numeric:tabular-nums;">9:41</span>
  <span aria-hidden="true" style="position:absolute;right:14.67px;top:50%;transform:translateY(-50%);display:inline-flex;align-items:center;">
    <svg width="67" height="12" viewBox="0 0 67 12" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <path d="M2 7.00696C2.55228 7.00696 3 7.45467 3 8.00696V10.007C2.99982 10.5591 2.55218 11.007 2 11.007H1C0.447824 11.007 0.000175969 10.5591 0 10.007V8.00696C0 7.45467 0.447715 7.00696 1 7.00696H2ZM6.66699 5.00696C7.21913 5.00713 7.66699 5.45478 7.66699 6.00696V10.007C7.66682 10.559 7.21902 11.0068 6.66699 11.007H5.66699C5.11482 11.007 4.66717 10.5591 4.66699 10.007V6.00696C4.66699 5.45467 5.11471 5.00696 5.66699 5.00696H6.66699ZM11.333 2.67297C11.8852 2.67297 12.3328 3.12084 12.333 3.67297V10.007C12.3328 10.5591 11.8852 11.007 11.333 11.007H10.333C9.78098 11.0068 9.33318 10.559 9.33301 10.007V3.67297C9.33318 3.12095 9.78098 2.67315 10.333 2.67297H11.333ZM16 0.339966C16.5523 0.339966 17 0.787681 17 1.33997V10.007C16.9998 10.5591 16.5522 11.007 16 11.007H15C14.4478 11.007 14.0002 10.5591 14 10.007V1.33997C14 0.787681 14.4477 0.339966 15 0.339966H16Z" fill="currentColor"></path>
      <path fill-rule="evenodd" clip-rule="evenodd" d="M27.4468 8.40057C28.7223 7.32168 30.5911 7.32168 31.8667 8.40057C31.9307 8.45859 31.9675 8.54074 31.9692 8.62713C31.9709 8.71359 31.9372 8.79702 31.8755 8.8576L29.8784 10.8732C29.8199 10.9324 29.74 10.966 29.6567 10.966C29.5735 10.966 29.4936 10.9324 29.435 10.8732L27.437 8.8576C27.3754 8.797 27.3415 8.71351 27.3432 8.62713C27.3451 8.54068 27.3826 8.45855 27.4468 8.40057ZM24.7817 5.71111C27.5298 3.15507 31.7855 3.15512 34.5337 5.71111C34.5956 5.77097 34.6313 5.85351 34.6323 5.93963C34.6332 6.02581 34.5992 6.10892 34.5386 6.1701L33.3843 7.33709C33.2653 7.45619 33.0727 7.45885 32.9507 7.34295C32.0482 6.52576 30.8742 6.07337 29.6567 6.07342C28.44 6.07392 27.2666 6.52628 26.3647 7.34295C26.2427 7.45877 26.0501 7.45617 25.9311 7.33709L24.7768 6.1701C24.7162 6.10903 24.6823 6.02572 24.6831 5.93963C24.684 5.8535 24.7197 5.77095 24.7817 5.71111ZM22.1167 3.02947C26.3318 -1.0097 32.9818 -1.00995 37.1968 3.02947C37.2577 3.08942 37.2919 3.17156 37.2925 3.25701C37.293 3.34257 37.259 3.42481 37.1987 3.48553L36.0425 4.65252C35.9233 4.77199 35.7307 4.77324 35.6098 4.65545C34.0039 3.12872 31.8725 2.27764 29.6567 2.27752C27.4406 2.27751 25.3088 3.12856 23.7026 4.65545C23.5818 4.77344 23.389 4.77222 23.27 4.65252L22.1137 3.48553C22.0535 3.42476 22.0194 3.34259 22.02 3.25701C22.0206 3.1715 22.0557 3.08939 22.1167 3.02947Z" fill="currentColor"></path>
      <path d="M61.663 0C63.1358 0 64.33 1.19423 64.33 2.66699V8.66699C64.3298 10.1396 63.1356 11.333 61.663 11.333H44.997C43.5243 11.333 42.3302 10.1396 42.33 8.66699V2.66699C42.33 1.19423 43.5242 0 44.997 0H61.663ZM44.997 1C44.0765 1 43.33 1.74652 43.33 2.66699V8.66699C43.3302 9.58732 44.0766 10.333 44.997 10.333H61.663C62.5834 10.333 63.3298 9.58732 63.33 8.66699V2.66699C63.33 1.74652 62.5835 1 61.663 1H44.997ZM57.997 2C58.7331 2.00018 59.3298 2.59689 59.33 3.33301V8C59.33 8.73627 58.7332 9.33283 57.997 9.33301H45.663C44.9268 9.33283 44.33 8.73627 44.33 8V3.33301C44.3302 2.59689 44.9269 2.00018 45.663 2H57.997ZM65.33 3.66699C66.1347 4.0058 66.6581 4.7939 66.6581 5.66699C66.658 6.54 66.1346 7.32826 65.33 7.66699V3.66699Z" fill="currentColor"></path>
    </svg>
  </span>
</section>
<script>(function(){var t=document.querySelector('[data-brief-role="status-bar"] [data-time]');if(!t)return;function tick(){var d=new Date();var h=d.getHours();var m=String(d.getMinutes()).padStart(2,'0');h=h%12||12;t.textContent=h+':'+m;}tick();setInterval(tick,15000);})();</script>
<div class="ambient" aria-hidden="true"></div>
<main id="main-content">

  <header data-brief-id="nav-top" data-brief-role="nav-top">
    <div class="top">
      <span class="wordmark">SELVARD</span>
      
      <svg data-logo="selvard-mark" width="32" height="32" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M16 4 C24 4 28 10 28 16 C28 22 24 26 18 26" fill="none" stroke="#2F5D50" stroke-width="5" stroke-linecap="round"></path>
        <path d="M16 28 C8 28 4 22 4 16 C4 10 8 6 14 6" fill="none" stroke="#0A6C60" stroke-width="5" stroke-linecap="round"></path>
        <circle cx="16" cy="16" r="3.5" fill="#54E454"></circle>
      </svg>
    </div>
    <p class="greet">Good morning, Priya</p>
  </header>

  <section data-component="Card" data-variant="elevated" class="glass posture"
           data-brief-id="hero-posture" data-brief-role="hero">
    <span class="t-meta">Security posture</span>
    <div class="posture-state">
      <span class="state-word">NORMAL</span>
      <span class="conf">High confidence</span>
    </div>
    
    <div class="reasons" data-brief-id="viz-posture-domains" data-brief-role="viz">
      <div class="reason" data-ok="1">
        <span class="lbl">Network</span><span class="what">DNS filtering on</span>
        <span class="track"><span class="fill" style="width: 92%"></span></span>
        <span class="st">Protected</span>
      </div>
      <div class="reason" data-ok="1">
        <span class="lbl">Apps</span><span class="what">One app to review</span>
        <span class="track"><span class="fill" style="width: 74%"></span></span>
        <span class="st">Attention</span>
      </div>
      <div class="reason" data-ok="0">
        <span class="lbl">Identity</span><span class="what">One address exposed</span>
        <span class="track"><span class="fill" style="width: 48%"></span></span>
        <span class="st">Attention</span>
      </div>
      <div class="reason" data-ok="1">
        <span class="lbl">Device</span><span class="what">OS limits noted</span>
        <span class="track"><span class="fill" style="width: 84%"></span></span>
        <span class="st">Protected</span>
      </div>
    </div>
  </section>

  <section data-component="Card" data-variant="base" class="glass glass--quiet nest"
           data-brief-id="card-nest-check" data-brief-role="card">
    <svg data-viz="nest-ring" class="ring" viewBox="0 0 88 88" role="img" aria-label="68 percent along today's check path">
      <circle cx="44" cy="44" r="38" fill="none" stroke="rgba(255,255,255,0.10)" stroke-width="7"></circle>
      <circle cx="44" cy="44" r="38" fill="none" stroke="#0A6C60" stroke-width="7" stroke-linecap="round"
              pathLength="100" stroke-dasharray="68 100" transform="rotate(-90 44 44)"></circle>
      <text x="44" y="50" text-anchor="middle" font-family="Manrope" font-size="20" font-weight="500" fill="#EDF1F7" class="num">68%</text>
    </svg>
    <div class="nest-copy">
      <div class="nest-title num">84</div>
      <p class="nest-sub">Today's check, 68% along the path. Finish the check to unlock the identity sweep.</p>
      <div class="nest-actions">
        <button data-component="Button" data-variant="primary" class="btn btn-primary"
                data-brief-id="cta-continue-check" data-brief-role="cta">Continue</button>
        <span class="t-meta" style="text-transform: none; letter-spacing: var(--gesso-tracking-normal)">Ready when you are</span>
      </div>
    </div>
  </section>

  <div class="sec">
    <h2>Needs attention</h2>
    <a class="seclink" href="#" data-brief-id="link-attention-all" data-brief-role="button">View all</a>
  </div>
  <section data-component="ListRow" data-variant="with-action" class="attention"
           data-brief-id="list-attention" data-brief-role="list">
    <div class="att-row" data-brief-id="list-attention-item-0" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/globe" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M12 2a14.5 14.5 0 0 0 0 20a14.5 14.5 0 0 0 0-20M2 12h20"></path></g></svg></span>
      <div class="att-copy"><strong>Turn on DNS-over-HTTPS</strong><span>Network · 2 minutes</span></div>
      <button class="att-act" aria-label="Open network settings" data-brief-id="btn-doh-open" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
    <div class="att-row" data-brief-id="list-attention-item-1" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/message" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092a10 10 0 1 0-4.777-4.719"></path></svg></span>
      <div class="att-copy"><strong>Review Ledger Companion</strong><span>Apps · notification access</span></div>
      <button class="att-act" aria-label="Review app permissions" data-brief-id="btn-app-review" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
    <div class="att-row" data-brief-id="list-attention-item-2" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/mail" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m22 7l-8.991 5.727a2 2 0 0 1-2.009 0L2 7"></path><rect width="20" height="16" x="2" y="4" rx="2"></rect></g></svg></span>
      <div class="att-copy"><strong>Swap the breached email</strong><span>Identity · new address</span></div>
      <button class="att-act" aria-label="Open identity details" data-brief-id="btn-identity-open" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
  </section>

  <div class="sec"><h2>Protected assets</h2></div>
  <section data-component="Card" data-variant="outlined" class="chips"
           data-brief-id="chips-assets" data-brief-role="card-grid">
    <button class="chip" data-brief-id="chip-banking" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/credit-card" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="14" x="2" y="5" rx="2"></rect><path d="M2 10h20"></path></g></svg>Banking</button>
    <button class="chip" data-brief-id="chip-email" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/mail" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m22 7l-8.991 5.727a2 2 0 0 1-2.009 0L2 7"></path><rect width="20" height="16" x="2" y="4" rx="2"></rect></g></svg>Email</button>
    <button class="chip" data-brief-id="chip-work" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/folder" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"></path></svg>Work</button>
    <button class="chip" data-brief-id="chip-dev" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/key" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m15.5 7.5l2.3 2.3a1 1 0 0 0 1.4 0l2.1-2.1a1 1 0 0 0 0-1.4L19 4m2-2l-9.6 9.6"></path><circle cx="7.5" cy="15.5" r="5.5"></circle></g></svg>Dev</button>
  </section>

  <div class="sec"><h2>Recent events</h2></div>
  <section data-component="ListRow" data-variant="with-avatar" class="events"
           data-brief-id="list-events" data-brief-role="list">
    <div class="ev-row" data-brief-id="list-events-item-0" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/2a043a9a59a9.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/lush-tropical-jungle-at-sunrise-in-sri-lanka-34139024/" data-stock-photographer="Tharu 569" /></span>
      <div class="ev-copy"><strong>Link checked</strong><span>messenger-metro.co · just now</span></div>
      <span class="badge b-ok">NO KNOWN THREAT</span>
    </div>
    <div class="ev-row" data-brief-id="list-events-item-1" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/94c308b1e3bb.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/creek-falling-down-the-stones-8364991/" data-stock-photographer="Chris F" /></span>
      <div class="ev-copy"><strong>App flagged</strong><span>Ledger Companion · 10:04</span></div>
      <span class="badge b-sus">SUSPICIOUS</span>
    </div>
    <div class="ev-row" data-brief-id="list-events-item-2" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/d15d613da60d.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/silhouette-of-mountains-near-the-lake-5874130/" data-stock-photographer="Dan Voican" /></span>
      <div class="ev-copy"><strong>DNS query blocked</strong><span>ads-gw.trk.net · 09:41</span></div>
      <span class="badge b-blk">BLOCKED</span>
    </div>
  </section>

  <div class="cta-anchor">
    <button data-component="Button" data-variant="primary" class="cta"
            data-brief-id="cta-check-link" data-brief-role="cta">Check a link</button>
  </div>

</main>

<section data-chrome="tab-bar" data-component="TabBar" data-brief-id="chrome-tab-bar" data-brief-role="tab-bar" style="position:fixed;bottom:0;left:0;right:0;height:80px;display:flex;align-items:flex-start;padding:8px 8px 0 8px;background:var(--gesso-canvas, rgba(250,250,250,0.95));backdrop-filter:saturate(180%) blur(20px);-webkit-backdrop-filter:saturate(180%) blur(20px);border-top:1px solid var(--gesso-divider, rgba(10,10,10,0.08));z-index:100;font-family:var(--gesso-font-body, system-ui);">
    <div data-brief-id="nav:home" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 0 1-1 1h-5v-7h-6v7H4a1 1 0 0 1-1-1z"></path></svg></span>
      <span style="color:inherit!important">Home</span>
    </div>
    <div data-brief-id="nav:shield" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle><text x="12" y="12" text-anchor="middle" dominant-baseline="central" font-size="11" font-weight="600" fill="currentColor" font-family="system-ui, sans-serif">S</text></svg></span>
      <span style="color:inherit!important">Shield</span>
    </div>
    <div data-brief-id="nav:timeline" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="8" r="4"></circle><path d="M4 22c0-4.4 3.6-8 8-8s8 3.6 8 8"></path></svg></span>
      <span style="color:inherit!important">Timeline</span>
    </div>
    <div data-brief-id="nav:identity" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle><text x="12" y="12" text-anchor="middle" dominant-baseline="central" font-size="11" font-weight="600" fill="currentColor" font-family="system-ui, sans-serif">I</text></svg></span>
      <span style="color:inherit!important">Identity</span>
    </div>
    <div data-brief-id="nav:settings" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"></circle><path d="M19 12a7 7 0 0 0-.1-1.3l2-1.5-2-3.4-2.3 1a7 7 0 0 0-2.2-1.3L13.7 2h-3.4l-.5 2.5a7 7 0 0 0-2.2 1.3l-2.3-1-2 3.4 2 1.5A7 7 0 0 0 5 12a7 7 0 0 0 .1 1.3l-2 1.5 2 3.4 2.3-1a7 7 0 0 0 2.2 1.3l.5 2.5h3.4l.5-2.5a7 7 0 0 0 2.2-1.3l2.3 1 2-3.4-2-1.5A7 7 0 0 0 19 12z"></path></svg></span>
      <span style="color:inherit!important">Settings</span>
    </div>
</section><script id="gesso-scroll-clearance">(function(){
function ready(fn){if(document.readyState!=='loading')fn();else document.addEventListener('DOMContentLoaded',fn);}
function adjust(){
  var sels=['[data-brief-role="tab-bar"]','[data-chrome="tab-bar"]','[data-sticky-action="true"]'];
  var vh=window.innerHeight||document.documentElement.clientHeight||0;
  if(!vh)return;
  var max=0;
  for(var s=0;s<sels.length;s++){
    var nodes=document.querySelectorAll(sels[s]);
    for(var i=0;i<nodes.length;i++){
      var el=nodes[i];
      var cs=getComputedStyle(el);
      if(cs.position!=='fixed'&&cs.position!=='sticky')continue;
      if(cs.display==='none'||cs.visibility==='hidden')continue;
      var r=el.getBoundingClientRect();
      if(!r.height)continue;
      var occ=vh-r.top; // height of the band this element occupies up from the viewport bottom
      if(occ<=0||occ>=vh)continue; // skip elements not currently pinned near the bottom
      if(occ>max)max=occ;
    }
  }
  if(max>0)document.body.style.setProperty('padding-bottom',(Math.ceil(max)+16)+'px','important');
}
ready(adjust);
window.addEventListener('load',adjust);
window.addEventListener('resize',adjust);
})();</script><script data-gesso-contrast="6">(function(){
function ready(fn){if(document.readyState!=='loading')fn();else document.addEventListener('DOMContentLoaded',fn);}
var run=function(){
  // ListRow layout guard (v3). The component sheet forces display:flex on
  // every ListRow so two-zone rows read as two zones; a row the model
  // authored as a STACK (a label line over a progress bar) collapses under
  // it: the head shrinks to its text and the bar drops to zero width. A
  // selector cannot tell the two apart, a measurement can: a flexed row
  // with a descendant that has height but no width was broken by the
  // enforcement, so it goes back to block flow. Rows that laid out fine are
  // never touched.
  function layoutGuard(){
    var rows=document.querySelectorAll('[data-component="ListRow"]');
    for(var i=0;i<rows.length;i++){
      var row=rows[i];
      if(getComputedStyle(row).display!=='flex')continue;
      if(row.children.length<2)continue;
      var all=row.querySelectorAll('*'),collapsed=false;
      for(var j=0;j<all.length;j++){
        var b=all[j].getBoundingClientRect();
        if(b.width<2&&b.height>1&&b.height<40){collapsed=true;break;}
      }
      if(!collapsed)continue;
      row.style.setProperty('display','block','important');
      row.setAttribute('data-gesso-listrow-guard','stack');
    }
  }
  layoutGuard();
  // Collapsed flex button guard (v6, 2026-09-15). A COLUMN flex parent turns
  // flex: 1 (basis 0%) into "collapse to one text line": the OG's two-up
  // .btn rule (flex: 1; height: 44px) reused for a full-width CTA rendered
  // 20px tall with no padding. Measurement, not a selector: only a button
  // with grow > 0 and a zero basis whose parent stacks vertically, and only
  // when a content basis actually makes it taller, is touched.
  function flexButtonGuard(){
    var els=document.querySelectorAll('button,[role="button"],a[class*="btn"],a[class*="button"]');
    for(var i=0;i<els.length;i++){
      var el=els[i],p=el.parentElement;if(!p)continue;
      var ps=getComputedStyle(p);
      if(ps.display.indexOf('flex')<0||ps.flexDirection.indexOf('column')<0)continue;
      var cs=getComputedStyle(el);
      if(!(parseFloat(cs.flexGrow)>0))continue;
      var basis=cs.flexBasis;if(basis!=='0%'&&basis!=='0px')continue;
      var before=el.getBoundingClientRect().height;
      var prev=el.style.flexBasis;el.style.flexBasis='auto';
      if(!(el.getBoundingClientRect().height>before+1))el.style.flexBasis=prev;
    }
  }
  flexButtonGuard();
  function lin(c){c=c/255;return c<=0.03928?c/12.92:Math.pow((c+0.055)/1.055,2.4);}
  function lum(rgb){return 0.2126*lin(rgb[0])+0.7152*lin(rgb[1])+0.0722*lin(rgb[2]);}
  function ratio(a,b){var x=lum(a),y=lum(b);return (Math.max(x,y)+0.05)/(Math.min(x,y)+0.05);}
  // An element carries text of its own when any DIRECT child is a non-empty
  // text node. Leaves qualify, and so does mixed content (icon + label).
  function hasOwnText(el){
    var cn=el.childNodes;
    for(var q=0;q<cn.length;q++){if(cn[q].nodeType===3&&cn[q].nodeValue&&cn[q].nodeValue.trim())return true;}
    return false;
  }
  // WCAG threshold for an element: 3:1 for large text, 4.5:1 otherwise.
  function needFor(cs){var fs=parseFloat(cs.fontSize)||16;var fw=parseInt(cs.fontWeight,10)||400;return (fs>=24||(fs>=18.66&&fw>=700))?3:4.5;}
  // A host is SOLID when the text sits on one opaque colour: no gradient or
  // image anywhere on the host itself. Only solid hosts get the WCAG tier;
  // gradients and photos keep the catastrophic 2:1 rule, because a single
  // authored ink across a gradient legitimately passes at one end and fails
  // at the other (the 2026-08 demotion).
  function solidHost(host){
    var hcs=getComputedStyle(host);
    var bi=hcs.backgroundImage;
    if(bi&&bi!=='none')return false;
    if(host===document.body)return true;
    var c=parseRgb(hcs.backgroundColor);
    return !!(c&&c[3]>0.5);
  }
  // Computed colors are NOT always rgb(): color-mix / relative-color /
  // wide-gamut authored values serialize as color(srgb r g b / a) (and
  // exotic spaces as their own functions). The old rgb-only regex made
  // every such element INVISIBLE to this floor (field bug 2026-08-18:
  // 1.05:1 filter chips authored via color-mix shipped unremediated).
  // Fast paths for rgb()/color(srgb); everything else resolves through
  // a 1x1 canvas, memoized per unique string.
  var colorCanvas=null,colorMemo={};
  function parseRgb(s){
    if(!s)return null;
    if(Object.prototype.hasOwnProperty.call(colorMemo,s))return colorMemo[s];
    var out=null;
    var m=s.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)$/);
    if(m){out=[+m[1],+m[2],+m[3],m[4]!=null?+m[4]:1];}
    else{
      m=s.match(/^color\(srgb\s+([\d.]+)\s+([\d.]+)\s+([\d.]+)(?:\s*\/\s*([\d.]+%?))?\)$/);
      if(m){
        var al=m[4]!=null?(m[4].indexOf('%')>=0?parseFloat(m[4])/100:+m[4]):1;
        out=[Math.round(+m[1]*255),Math.round(+m[2]*255),Math.round(+m[3]*255),al];
      }else if(s!=='transparent'&&s.indexOf('url(')<0){
        try{
          if(!colorCanvas)colorCanvas=document.createElement('canvas');
          colorCanvas.width=1;colorCanvas.height=1;
          var cx2=colorCanvas.getContext('2d',{willReadFrequently:true});
          if(cx2){
            cx2.clearRect(0,0,1,1);
            cx2.fillStyle=s;
            cx2.fillRect(0,0,1,1);
            var px=cx2.getImageData(0,0,1,1).data;
            out=[px[0],px[1],px[2],px[3]/255];
          }
        }catch(e){out=null;}
      }
    }
    colorMemo[s]=out;
    return out;
  }
  // One stop matcher for gradient strings: rgb() and color(srgb) forms.
  var STOP_RE_SRC="(rgba?\\([^)]*\\)|color\\(srgb[^)]*\\))(?:\\s+(-?[\\d.]+)%)?";
  // The LAST linear-gradient in a background-image shorthand paints at the
  // BOTTOM of the stack: that is the base page/card color layer.
  function lastLinearGradient(bi){
    var at=bi.lastIndexOf('linear-gradient(');
    if(at<0)return null;
    var i=at+16,depth=1;
    while(i<bi.length&&depth>0){var ch=bi[i];if(ch==='(')depth++;else if(ch===')')depth--;i++;}
    return bi.slice(at+16,i-1);
  }
  // The gradient's color AT the element's position. A page-scale dusk
  // gradient can span 1.2:1-dark at the top and 4.5:1-light at the bottom;
  // sampling one stop for every element (the old behavior) measured all of
  // them against the same color and passed genuinely unreadable text.
  function gradientColorAt(bi,host,cx,cy){
    var g=lastLinearGradient(bi);
    if(g==null)return null;
    var hr=host.getBoundingClientRect();
    if(!hr.width||!hr.height)return null;
    var tx=Math.min(1,Math.max(0,(cx-hr.left)/hr.width));
    var ty=Math.min(1,Math.max(0,(cy-hr.top)/hr.height));
    var t=ty; // default axis: to bottom
    var head=g.split(',')[0]||'';
    var ang=head.match(/^\s*(-?[\d.]+)deg/);
    if(/^\s*to top\b/.test(head))t=1-ty;
    else if(/^\s*to right\b/.test(head))t=tx;
    else if(/^\s*to left\b/.test(head))t=1-tx;
    else if(ang){
      var a=((+ang[1])%360+360)%360;
      if(a<45||a>=315)t=1-ty;
      else if(a<135)t=tx;
      else if(a<225)t=ty;
      else t=1-tx;
    }
    var re=new RegExp(STOP_RE_SRC,"g"),mm;
    var stops=[];
    while((mm=re.exec(g))!==null){
      var c=parseRgb(mm[1]);
      if(c)stops.push({c:c,p:mm[2]!=null?(+mm[2])/100:null});
    }
    if(!stops.length)return null;
    if(stops[0].p==null)stops[0].p=0;
    if(stops[stops.length-1].p==null)stops[stops.length-1].p=1;
    for(var i2=1;i2<stops.length;i2++){
      if(stops[i2].p==null)stops[i2].p=stops[i2-1].p+(1-stops[i2-1].p)/(stops.length-i2);
    }
    var a1=stops[0],b1=stops[stops.length-1];
    for(var j=0;j<stops.length-1;j++){
      if(t>=stops[j].p&&t<=stops[j+1].p){a1=stops[j];b1=stops[j+1];break;}
    }
    var span=b1.p-a1.p;
    var f=span>0?(t-a1.p)/span:0;
    return[
      a1.c[0]+(b1.c[0]-a1.c[0])*f,
      a1.c[1]+(b1.c[1]-a1.c[1])*f,
      a1.c[2]+(b1.c[2]-a1.c[2])*f,
      a1.c[3]+(b1.c[3]-a1.c[3])*f
    ];
  }
  // Effective background at the element's own position: walk up compositing
  // EVERY paint layer (translucent card washes included; the old walk
  // ignored anything below 0.5 alpha, so a 0.3-alpha haze over a gradient
  // never entered the measurement) and interpolate gradients where the text
  // actually sits.
  function bgOf(el){
    var er=el.getBoundingClientRect();
    var cx=er.left+er.width/2,cy=er.top+er.height/2;
    var acc=null; // [r,g,b,coverage], layers accumulated top-down
    function put(c){
      if(!acc){acc=[c[0],c[1],c[2],c[3]];return;}
      var a1=acc[3],a2=c[3]*(1-a1),ao=a1+a2;
      if(ao<=0)return;
      acc=[(acc[0]*a1+c[0]*a2)/ao,(acc[1]*a1+c[1]*a2)/ao,(acc[2]*a1+c[2]*a2)/ao,ao];
    }
    for(var n=el;n&&n!==document.documentElement;n=n.parentElement){
      var cs=getComputedStyle(n);
      var bi=cs.backgroundImage;
      if(bi&&bi!=='none'&&bi.indexOf('gradient')>=0){
        var gc=gradientColorAt(bi,n,cx,cy);
        if(!gc){
          // Radial/conic or unparseable: keep the old base-layer heuristic
          // (last >= 0.5-alpha stop; low-alpha stops are overlays).
          var re2=new RegExp(STOP_RE_SRC,"g"),m2,op=null,any=null;
          while((m2=re2.exec(bi))!==null){
            var c2=parseRgb(m2[1]);
            if(!c2)continue;
            if(c2[3]>=0.5)op=c2;
            else if(c2[3]>0&&!any)any=c2;
          }
          gc=op||any;
        }
        if(gc){put([gc[0],gc[1],gc[2],Math.min(1,gc[3])]);if(acc&&acc[3]>=0.99)break;}
      }
      var c=parseRgb(cs.backgroundColor);
      if(c&&c[3]>0){put(c);if(acc[3]>=0.99)break;}
    }
    if(!acc)return[255,255,255];
    if(acc[3]<0.99)put([255,255,255,1]);
    return[Math.round(acc[0]),Math.round(acc[1]),Math.round(acc[2])];
  }
  function opaqueBg(cs){
    var c=parseRgb(cs.backgroundColor);
    if(c&&c[3]>0.5)return true;
    var bi=cs.backgroundImage;
    if(bi&&bi!=='none'&&bi.indexOf('gradient')>=0){
      var re=new RegExp(STOP_RE_SRC,"g"), mm;
      while((mm=re.exec(bi))!==null){var cc=parseRgb(mm[1]);if(cc&&cc[3]>=0.5)return true;}
    }
    return false;
  }
  // The element whose opaque background the text visually sits on (nearest
  // opaque ancestor; the page body as the floor).
  function bgHostOf(el){
    for(var n=el;n&&n!==document.documentElement;n=n.parentElement){
      if(opaqueBg(getComputedStyle(n)))return n;
    }
    return document.body;
  }
  // Is this text painted OVER a raster photo? bgOf() only sees solid/gradient
  // backgrounds, so without this an authored light-on-photo headline reads as
  // light-on-the-card-color, fails contrast, and gets flipped to BLACK — which
  // then lands unreadable on the actual photo. The photo covers the text's
  // bg-host only when a media box (img/video/picture, >=24px) is a DESCENDANT
  // of that host (so it paints over the host's background, under the text) and
  // its rect contains the text's center. This excludes: text in a sibling block
  // BELOW a hero image (geometry), and text inside its OWN opaque pill/chip
  // that floats over a photo (the pill, not the photo, is the bg-host — its
  // media set is empty), so those keep normal contrast remediation.
  function overImage(el){
    var er=el.getBoundingClientRect();
    if(!er.width||!er.height)return false;
    var cx=er.left+er.width/2, cy=er.top+er.height/2;
    var host=bgHostOf(el);
    var medias=host.querySelectorAll('img,video,picture');
    for(var j=0;j<medias.length;j++){
      var m=medias[j];
      if(m.contains(el))continue;
      var mr=m.getBoundingClientRect();
      if(mr.width<24||mr.height<24)continue;
      if(cx>=mr.left&&cx<=mr.right&&cy>=mr.top&&cy<=mr.bottom)return true;
    }
    return false;
  }
  // Walk leaf-text elements only. Skip status-bar (its fg is intentionally
  // light over arbitrary content), the tab bar (handled by its own pass below
  // so label and icon move TOGETHER), and elements the screen marked opt-out
  // via data-allow-low-contrast.
  // The bar selector must match every way the bar gets tagged: the addressable
  // pass labels it data-brief-role="nav-bottom" (NOT "tab-bar"), buttons carry
  // role "tab", and some bars only carry the class / id — guarding on the lone
  // role "tab-bar" lets a "nav-bottom" bar's labels through and splits them.
  var els=document.querySelectorAll('body *');
  var TAB_BAR_SKIP='[data-brief-role="tab-bar"],[data-brief-role="nav-bottom"],[data-brief-role="tab"],[data-brief-id="tab-bar"],nav.tab-bar,nav.tabbar,nav.bottom-nav,nav.nav-bar,nav.navbar';
  // Catastrophic-only floor. WCAG-level thresholds (4.5:1 / 3:1) made this
  // script re-litigate authored muted/tinted text tiers; readability belongs
  // to the scorer's contrast dimension. Below 2:1 the text is genuinely
  // unreadable, so only then do we intervene — but we remediate the whole
  // AUTHORED INK, not the lone element. One ink painted across a page-scale
  // gradient fails at the dark end and squeaks past the floor at the light
  // end; flipping only the sub-2 elements leaves the same paragraph half
  // white, half black. So: group leaf text by (bg-host, computed color); a
  // group with any sub-2 member flips per-element to the better pole, and
  // only where that pole IMPROVES the element's own local ratio (a member
  // already better off stays put).
  var groups={},order=[];
  for(var i=0;i<els.length;i++){
    var el=els[i];
    if(!hasOwnText(el))continue;
    if(el.closest&&(el.closest('[data-brief-role="status-bar"]')||el.closest(TAB_BAR_SKIP)||el.closest('[data-allow-low-contrast]')))continue;
    // No box, no verdict. Every decision below is a measurement, and an
    // element with no rect gives overImage() nothing to hit-test, so it would
    // report "not over a photo" and earn a flip it never needed.
    var er=el.getBoundingClientRect();
    if(!er.width||!er.height)continue;
    var cs=getComputedStyle(el);
    var fg=parseRgb(cs.color);
    if(!fg||fg[3]<0.1)continue;
    if(overImage(el)){
      // Text over a photo: bgOf() measured the card color, not the photo, so
      // any verdict here is a guess. Do NOTHING: never flip to black (the old
      // bug) and never force light either (the forced rgba(255,255,255,.94)
      // misfired on light photos and authored scrims). The designer owns
      // photo-overlay treatment.
      continue;
    }
    var bg=bgOf(el);
    // Measure the ink the user SEES: low-alpha text sits far closer to its
    // background than its raw color claims (0.62-alpha cream on tan reads
    // 2.2:1 raw but 1.7:1 composited).
    var eff=fg[3]<1?[fg[0]*fg[3]+bg[0]*(1-fg[3]),fg[1]*fg[3]+bg[1]*(1-fg[3]),fg[2]*fg[3]+bg[2]*(1-fg[3])]:fg;
    var r=ratio([eff[0],eff[1],eff[2]],bg);
    var host=bgHostOf(el);
    var key=(host.getAttribute&&host.getAttribute('data-brief-id')||host.tagName)+'|'+cs.color;
    if(!groups[key]){groups[key]={members:[],bad:false,solid:solidHost(host)};order.push(key);}
    groups[key].members.push({el:el,bg:bg,r:r,cs:cs,fg:fg});
    if(r<2)groups[key].bad=true;
  }
  for(var k=0;k<order.length;k++){
    var g2=groups[order[k]];
    if(!g2.bad)continue;
    for(var m3=0;m3<g2.members.length;m3++){
      var mem=g2.members[m3];
      var rB=ratio([0,0,0],mem.bg),rW=ratio([255,255,255],mem.bg);
      var best=Math.max(rB,rW);
      if(best<=mem.r)continue;
      mem.el.style.color=rB>rW?'rgba(0,0,0,0.92)':'rgba(255,255,255,0.92)';
    }
  }
  // WCAG tier (v3): text on a SOLID host that reads under its threshold gets
  // its authored ink shifted, minimally and as a GROUP, toward the pole that
  // helps, until every member of that ink clears its own threshold. Moving
  // the whole (host, colour) group keeps a muted tier a tier (everything in
  // it darkens by the same amount) instead of re-litigating one label at a
  // time, which is the failure the 2:1-only rule was reacting to. Gradient
  // and photo hosts never enter here.
  for(var t=0;t<order.length;t++){
    var g3=groups[order[t]];
    if(!g3.solid||!g3.members.length)continue;
    var anyLow=false;
    for(var m4=0;m4<g3.members.length;m4++){if(g3.members[m4].r<needFor(g3.members[m4].cs)){anyLow=true;break;}}
    if(!anyLow)continue;
    var first=g3.members[0];
    var c3=[first.fg[0],first.fg[1],first.fg[2]];
    if(first.fg[3]<1){c3=[first.fg[0]*first.fg[3]+first.bg[0]*(1-first.fg[3]),first.fg[1]*first.fg[3]+first.bg[1]*(1-first.fg[3]),first.fg[2]*first.fg[3]+first.bg[2]*(1-first.fg[3])];}
    var towardDark=lum(first.bg)>0.18;
    var ok=false;
    for(var step=0;step<40&&!ok;step++){
      ok=true;
      for(var m5=0;m5<g3.members.length;m5++){
        var mm5=g3.members[m5];
        if(ratio(c3,mm5.bg)<needFor(mm5.cs)){ok=false;break;}
      }
      if(ok)break;
      c3=towardDark?[c3[0]*0.9,c3[1]*0.9,c3[2]*0.9]:[c3[0]+(255-c3[0])*0.12,c3[1]+(255-c3[1])*0.12,c3[2]+(255-c3[2])*0.12];
    }
    var css3='rgb('+Math.round(c3[0])+','+Math.round(c3[1])+','+Math.round(c3[2])+')';
    for(var m6=0;m6<g3.members.length;m6++){
      var mem6=g3.members[m6];
      // only where the shift IMPROVES the element's own local ratio
      if(ratio(c3,mem6.bg)>mem6.r)mem6.el.style.setProperty('color',css3,'important');
    }
  }
  // Tab bar: label and icon are PINNED to one color (enforceTabBarLabelColor),
  // so the main pass skips the bar rather than split them. But a pinned pair
  // can still be pinned to an unreadable color (cream labels over a light
  // gradient tail). Remediate per tab ITEM, moving label + icon together:
  // inherited color for everything riding currentColor, plus explicit
  // fill/stroke attributes (fill="none" stays none). The original alpha keeps
  // the active/inactive hierarchy, floored at 0.85 so the flip actually reads.
  var bars=document.querySelectorAll(TAB_BAR_SKIP);
  for(var b3=0;b3<bars.length;b3++){
    var bar=bars[b3];
    if(bar.closest('[data-allow-low-contrast]'))continue;
    var labels=bar.querySelectorAll('*');
    var seen=[];
    for(var l3=0;l3<labels.length;l3++){
      var lab=labels[l3];
      if(!hasOwnText(lab))continue;
      var lcs=getComputedStyle(lab);
      var lfg=parseRgb(lcs.color);
      if(!lfg||lfg[3]<0.1)continue;
      var lbg=bgOf(lab);
      var leff=lfg[3]<1?[lfg[0]*lfg[3]+lbg[0]*(1-lfg[3]),lfg[1]*lfg[3]+lbg[1]*(1-lfg[3]),lfg[2]*lfg[3]+lbg[2]*(1-lfg[3])]:lfg;
      var lneed=needFor(lcs);
      var lr=ratio([leff[0],leff[1],leff[2]],lbg);
      if(lr>=lneed)continue;
      var item=lab;
      for(var p3=lab.parentElement;p3&&p3!==bar;p3=p3.parentElement)item=p3;
      if(seen.indexOf(item)>=0)continue;
      seen.push(item);
      // WCAG tier (v4): on a SOLID bar the item's alpha rises to the smallest
      // value that clears the threshold; the ink and the hierarchy stay, only
      // the transparency gives. Below 2:1 even at full alpha, or on a
      // gradient bar, the existing pole flip below takes over.
      if(lr>=2&&solidHost(bgHostOf(lab))){
        var aFix=null;
        for(var aa=Math.max(lfg[3],0.1);aa<=1.0001;aa+=0.05){
          var e2=[lfg[0]*aa+lbg[0]*(1-aa),lfg[1]*aa+lbg[1]*(1-aa),lfg[2]*aa+lbg[2]*(1-aa)];
          if(ratio(e2,lbg)>=lneed){aFix=Math.min(1,aa);break;}
        }
        if(aFix!==null){
          var subsA=item.querySelectorAll('*');
          for(var sA=-1;sA<subsA.length;sA++){
            var subA=sA<0?item:subsA[sA];
            var scsA=getComputedStyle(subA);
            var sfgA=parseRgb(scsA.color);
            if(!sfgA)continue;
            subA.style.setProperty('color','rgba('+sfgA[0]+','+sfgA[1]+','+sfgA[2]+','+aFix+')','important');
          }
          continue;
        }
      }
      // The guard's own floor (v5): an item at or above 2:1 that could not be
      // alpha-raised is an OPAQUE authored tier (a token gray such as
      // --gesso-fg-muted on a white bar), not a defect. The pole flip below
      // would paint it the active pole at alpha 1 and erase the active tab:
      // every inactive tab went rgb(0,0,0) beside a black aria-current tab
      // (project fcf5cdf4, 2026-09-14, "no active state on the menu").
      // Only a genuinely unreadable item (< 2:1) still flips, which is the
      // catastrophic-only rule the rest of this script lives by.
      if(lr>=2)continue;
      var pole=ratio([0,0,0],lbg)>ratio([255,255,255],lbg)?[0,0,0]:[255,255,255];
      var subs=item.querySelectorAll('*');
      for(var s3=-1;s3<subs.length;s3++){
        var sub=s3<0?item:subs[s3];
        var scs=getComputedStyle(sub);
        var sfg=parseRgb(scs.color);
        var al2=Math.max(sfg?sfg[3]:1,0.85);
        sub.style.color='rgba('+pole[0]+','+pole[1]+','+pole[2]+','+al2+')';
        if(sub.tagName==='path'||sub.tagName==='PATH'||sub.getAttribute){
          var fa=sub.getAttribute&&sub.getAttribute('fill');
          if(fa&&fa!=='none'&&fa!=='currentColor')sub.style.fill='currentColor';
          var sa=sub.getAttribute&&sub.getAttribute('stroke');
          if(sa&&sa!=='none'&&sa!=='currentColor')sub.style.stroke='currentColor';
        }
      }
    }
  }
};
// NEVER DECIDE FROM A LAYOUT THAT DOES NOT EXIST YET (Tob, 2026-08-22).
//
// This used to call run() straight from DOMContentLoaded, which inside an
// iframe is routinely BEFORE the frame has been given its size. Every
// getBoundingClientRect() then reads 0, so overImage() bails on its own first
// line and reports "not over a photo", bgOf() measures the body's near-white
// instead of the hero photo, and an authored white headline scores ~1.05:1
// and is flipped to rgba(0,0,0,0.92) INLINE. Single pass, so that wrong value
// is permanent. Live burn: a canvas frame showed a black hero headline over a
// dark photo while the component panel, whose iframe happened to be laid out
// before its DOMContentLoaded, showed the authored white. Same document, two
// answers, decided by a race.
//
// requestAnimationFrame IS the missing precondition: it fires only while the
// document is actually being rendered, and after that frame's layout, which is
// exactly when the measurements this script is made of become real. A hidden
// or display-locked document simply keeps its authored colours until it is
// shown. Still a single pass; it just starts one rendered frame later, so the
// re-flip the window.load re-run used to cause (text changing under someone
// already reading it) does not come back.
function start(){
  if(typeof requestAnimationFrame==='function')requestAnimationFrame(run);
  else run();
}
ready(start);
})();</script><script id="__GESSO_TOKEN_BRIDGE__">
window.addEventListener("message", function(e) {
  var d = e && e.data;
  if (!d || d.type !== "gesso-tokens" || !d.vars) return;
  var root = document.documentElement;
  for (var k in d.vars) {
    if (Object.prototype.hasOwnProperty.call(d.vars, k)) {
      root.style.setProperty(k, d.vars[k]);
    }
  }
});
</script></body>
</html>
```

</details>

## Quick Start

### CSS Custom Properties

```css
:root {
  /* Colors */
  --gesso-canvas: #8eb89a;
  --gesso-surface-recessed: #88b194;
  --gesso-surface: #b8d4bb;
  --gesso-surface-elevated: #c1d9c3;
  --gesso-divider: rgba(255,255,255,0.04);
  --gesso-fg: #ffffff;
  --gesso-fg-muted: #3b5d48;
  --gesso-primary: #0a6c60;
  --gesso-on-accent: #FFFFFF;
  --gesso-accent-text: #ffffff;
  --gesso-secondary: #54e454;
  --gesso-accent-2-text: #ffffff;
  --gesso-neutral-50: #8eb89a;
  --gesso-neutral-100: #b8d4bb;
  --gesso-neutral-200: #9ebba2;
  --gesso-neutral-300: #84a28b;
  --gesso-neutral-400: #6b8b74;
  --gesso-neutral-500: #53735d;
  --gesso-neutral-600: #3b5d48;
  --gesso-neutral-700: #818e79;
  --gesso-neutral-800: #c2c4b7;
  --gesso-neutral-900: #ffffff;
  --gesso-neutral-950: #ffffff;
  --gesso-success: #12823b;
  --gesso-warning: #ae5f05;
  --gesso-error: #DC2626;
  --gesso-data-1: #006808;
  --gesso-data-2: #00830e;
  --gesso-data-3: #009f13;
  --gesso-data-4: #18bb23;
  --gesso-data-5: #43d645;
  --gesso-data-6: #62f161;

  /* Typography — Font Families */
  --gesso-font-display: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --gesso-font-body: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --gesso-font-mono: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;

  /* Typography — Scale */
  --gesso-text-4xl: 48px;
  --gesso-leading-4xl: 1.2;
  --gesso-text-3xl: 40px;
  --gesso-leading-3xl: 1.2;
  --gesso-text-2xl: 32px;
  --gesso-leading-2xl: 1.2;
  --gesso-text-base: 16px;
  --gesso-leading-base: 1.5;
  --gesso-text-xs: 11px;
  --gesso-leading-xs: 1.5;

  /* Typography — Weights */
  --font-weight-light: 300;
  --font-weight-regular: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;

  /* Spacing */
  --spacing-unit: 8px;
  --gesso-space-1: 8px;
  --gesso-space-2: 16px;
  --gesso-space-3: 24px;
  --gesso-space-4: 32px;
  --gesso-space-6: 48px;
  --gesso-space-8: 64px;
  --gesso-space-12: 96px;
  --gesso-space-16: 128px;
  --gesso-space-24: 192px;
  --gesso-space-32: 256px;

  /* Border Radius */
  --radius-none: 0px;
  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --radius-full: 9999px;

  /* Shadows */
  --gesso-shadow-sm: 0 1px 2px rgba(12,19,34,0.08);
  --gesso-shadow-md: 0 4px 12px rgba(12,19,34,0.10);
  --gesso-shadow-lg: 0 16px 40px rgba(12,19,34,0.14);

  /* Surfaces */
  --surface-page: #8eb89a;
  --surface-raised: #b8d4bb;
  --surface-sunken: #e5e6e9;
  --surface-overlay: #8eb89a;
}
```

### Tailwind v4

```css
@theme {
  /* Colors */
  --gesso-canvas: #8eb89a;
  --gesso-surface-recessed: #88b194;
  --gesso-surface: #b8d4bb;
  --gesso-surface-elevated: #c1d9c3;
  --gesso-divider: rgba(255,255,255,0.04);
  --gesso-fg: #ffffff;
  --gesso-fg-muted: #3b5d48;
  --gesso-primary: #0a6c60;
  --gesso-on-accent: #FFFFFF;
  --gesso-accent-text: #ffffff;
  --gesso-secondary: #54e454;
  --gesso-accent-2-text: #ffffff;
  --gesso-neutral-50: #8eb89a;
  --gesso-neutral-100: #b8d4bb;
  --gesso-neutral-200: #9ebba2;
  --gesso-neutral-300: #84a28b;
  --gesso-neutral-400: #6b8b74;
  --gesso-neutral-500: #53735d;
  --gesso-neutral-600: #3b5d48;
  --gesso-neutral-700: #818e79;
  --gesso-neutral-800: #c2c4b7;
  --gesso-neutral-900: #ffffff;
  --gesso-neutral-950: #ffffff;
  --gesso-success: #12823b;
  --gesso-warning: #ae5f05;
  --gesso-error: #DC2626;
  --gesso-data-1: #006808;
  --gesso-data-2: #00830e;
  --gesso-data-3: #009f13;
  --gesso-data-4: #18bb23;
  --gesso-data-5: #43d645;
  --gesso-data-6: #62f161;

  /* Typography */
  --gesso-font-display: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --gesso-font-body: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --gesso-font-mono: 'Manrope', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;

  /* Typography — Scale */
  --gesso-text-4xl: 48px;
  --gesso-leading-4xl: 1.2;
  --gesso-text-3xl: 40px;
  --gesso-leading-3xl: 1.2;
  --gesso-text-2xl: 32px;
  --gesso-leading-2xl: 1.2;
  --gesso-text-base: 16px;
  --gesso-leading-base: 1.5;
  --gesso-text-xs: 11px;
  --gesso-leading-xs: 1.5;

  /* Spacing */
  --gesso-space-1: 8px;
  --gesso-space-2: 16px;
  --gesso-space-3: 24px;
  --gesso-space-4: 32px;
  --gesso-space-6: 48px;
  --gesso-space-8: 64px;
  --gesso-space-12: 96px;
  --gesso-space-16: 128px;
  --gesso-space-24: 192px;
  --gesso-space-32: 256px;

  /* Border Radius */
  --radius-none: 0px;
  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --radius-full: 9999px;

  /* Shadows */
  --gesso-shadow-sm: 0 1px 2px rgba(12,19,34,0.08);
  --gesso-shadow-md: 0 4px 12px rgba(12,19,34,0.10);
  --gesso-shadow-lg: 0 16px 40px rgba(12,19,34,0.14);
}
```


## Tokens (JSON)

```json
{
  "color": {
    "neutral": {
      "50": "#8eb89a",
      "100": "#b8d4bb",
      "200": "#e5e6e9",
      "300": "#d2d3d7",
      "400": "#5a8f6e",
      "500": "#888b91",
      "600": "#5a8f6e",
      "700": "#484c53",
      "800": "#30343c",
      "900": "#b8d4bb",
      "950": "#8eb89a"
    },
    "primary": "#0a6c60",
    "semantic": {
      "error": "#DC2626",
      "success": "#16A34A",
      "warning": "#c36b05"
    },
    "secondary": "#54e454"
  },
  "roles": {
    "muted": "#4d7a5e",
    "canvas": "#ffffff",
    "accents": [
      "#0a6c60",
      "#3ba03b"
    ],
    "surface": "#ffffff",
    "foreground": "#0c1322"
  },
  "motion": {
    "easing": {
      "default": "cubic-bezier(0.2,0,0,1)",
      "emphasis": "cubic-bezier(0.34,1.56,0.64,1)"
    },
    "duration": {
      "base": "200ms",
      "fast": "120ms",
      "slow": "360ms"
    }
  },
  "radius": {
    "lg": "24px",
    "md": "16px",
    "sm": "10px",
    "full": "9999px",
    "none": "0px"
  },
  "shadow": {
    "lg": "0 16px 40px rgba(12,19,34,0.14)",
    "md": "0 4px 12px rgba(12,19,34,0.10)",
    "sm": "0 1px 2px rgba(12,19,34,0.08)"
  },
  "spacing": {
    "unit": 8,
    "scale": {
      "1": "8px",
      "2": "16px",
      "3": "24px",
      "4": "32px",
      "6": "48px",
      "8": "64px",
      "12": "96px",
      "16": "128px",
      "24": "192px",
      "32": "256px"
    }
  },
  "approach": {
    "mood": "calm, precise, institutional, airy",
    "name": "Institutional Calm",
    "anchor": "calm, precise, institutional, airy"
  },
  "typeface": {
    "body": "Manrope",
    "mono": "Manrope",
    "scale": {
      "lg": "1.25rem",
      "sm": "0.875rem",
      "xl": "1.5rem",
      "xs": "0.6875rem",
      "2xl": "2rem",
      "3xl": "2.5rem",
      "4xl": "3rem",
      "base": "1rem"
    },
    "display": "Manrope",
    "weights": [
      300,
      400,
      500,
      600,
      700
    ],
    "bodyWeight": 400,
    "displayWeight": 500
  }
}
```

## Reference HTML

```html
<!DOCTYPE html>
<html lang="en">
<head>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="preconnect" href="https://api.fontshare.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Manrope:wght@300;400;500;600;700&display=swap">
<meta charset="UTF-8">
<meta name="viewport" content="width=393">
<title>Selvard, Home</title>
<link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700&display=swap" rel="stylesheet">
<style>:root { --ink: #0C1322; --ink2: #101A2E; --offwhite: #EDF1F7; --slate: #9FB0C6; --verdant: #0A6C60; --signal: #54E454; --forest: #2F5D50; --mutedg: #5A8F6E; --sage: #A3C9A8; --cream: #E9F5DB; --gesso-primary: #0a6c60; --gesso-secondary: #54e454; --gesso-success: #54E454; --gesso-warning: #c9a227; --gesso-error: #e0705a; --glass: rgba(255,255,255,0.055); --glass2: rgba(255,255,255,0.035); --hairline: rgba(255,255,255,0.10); --r: 24px; --r2: 28px; --dur: 220ms; --ease: cubic-bezier(0.2,0,0,1) }
* { margin: 0; padding: 0; box-sizing: border-box }
html,body { width: 393px; min-height: 915px }
body { padding: 47px 16px 34px 16px; background: var(--ink); color: var(--offwhite); font-family: var(--gesso-font-display); font-size: 15px; line-height: var(--gesso-leading-normal); -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale }
.ambient { position: absolute; top: 0; left: 0; width: 393px; height: 380px; z-index: 0; pointer-events: none; background: radial-gradient(60% 55% at 8% 0%, rgba(47,93,80,0.55), transparent 70%),
    radial-gradient(50% 45% at 95% 5%, rgba(163,201,168,0.28), transparent 70%),
    radial-gradient(55% 50% at 80% 90%, rgba(233,245,219,0.14), transparent 70%),
    radial-gradient(50% 55% at 10% 95%, rgba(90,143,110,0.30), transparent 70%); opacity: .55 }
main { position: relative; z-index: 1 }
.t-hero { font-family: var(--gesso-font-display); font-weight: 500; font-size: var(--gesso-text-3xl); line-height: 44px; letter-spacing: var(--gesso-tracking-tight) }
.t-body { font-family: var(--gesso-font-display); font-weight: 400; font-size: var(--gesso-text-base); line-height: var(--gesso-leading-normal) }
.t-meta { font-family: var(--gesso-font-display); font-weight: 400; font-size: .75rem; line-height: var(--gesso-leading-snug); letter-spacing: .08em; text-transform: uppercase; color: var(--slate) }
.num { font-feature-settings: "tnum" }
.top { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--gesso-space-1) }
.wordmark { font-weight: 700; font-size: .9375rem; letter-spacing: .32em; color: var(--offwhite) }
.greet { color: var(--slate); font-size: .9375rem; margin-bottom: var(--gesso-space-3) }
.glass { background: var(--glass); border-radius: var(--r2); border: 1px solid var(--hairline); box-shadow: inset 0 1px 0 rgba(255,255,255,0.09), 0 8px 24px rgba(0,0,0,0.28); padding: var(--gesso-space-3) }
.glass--quiet { background: var(--glass2); border-radius: var(--r); padding: var(--gesso-space-2) 20px }
.posture { margin-bottom: var(--gesso-space-2) }
.posture-state { display: flex; align-items: baseline; gap: 12px; margin: var(--gesso-space-1) 0 20px }
.state-word { font-size: var(--gesso-text-2xl); line-height: 44px; font-weight: 500; letter-spacing: var(--gesso-tracking-tight) }
.conf { font-size: .75rem; letter-spacing: .06em; color: var(--signal); text-transform: none }
.reasons { display: flex; flex-direction: column; gap: 12px }
.reason { display: flex; align-items: center; gap: 12px }
.reason .lbl { width: 64px; font-size: .75rem; letter-spacing: .08em; text-transform: uppercase; color: var(--slate) }
.reason .what { flex: 1; font-size: .875rem; color: var(--offwhite); min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.reason .st { font-size: .75rem; color: var(--mutedg); letter-spacing: .04em; flex-shrink: 0 }
.track { flex: 1; height: 6px; border-radius: var(--gesso-radius-full); background: rgba(255,255,255,0.08); position: relative; overflow: hidden }
.track .fill { position: absolute; inset: 0; right: auto; border-radius: var(--gesso-radius-full); background: var(--forest) }
.reason[data-ok="1"] .track .fill { background: var(--mutedg) }
.reason[data-ok="0"] .track .fill { background: var(--gesso-neutral-100) }
.nest { margin-bottom: var(--gesso-space-2); display: flex; align-items: center; gap: 20px }
.nest-copy { flex: 1; min-width: 0 }
.nest-title { font-weight: 700; font-size: 1.125rem; line-height: var(--gesso-leading-snug); margin-bottom: 4px }
.nest-sub { font-size: .875rem; color: var(--slate) }
.ring { width: 88px; height: 88px; flex-shrink: 0 }
.nest-actions { display: flex; align-items: center; gap: 12px; margin-top: var(--gesso-space-2) }
.btn { font-family: var(--gesso-font-display); font-weight: 700; font-size: .875rem; padding: 12px var(--gesso-space-3); border-radius: var(--gesso-radius-full); border: none; cursor: pointer; transition: transform var(--dur) var(--ease),filter var(--dur) var(--ease) }
.btn-primary { background: var(--verdant); color: #ffffff }
.btn-primary:hover { filter: brightness(1.12) }
.btn-primary:active { transform: translateY(1px) scale(0.98) }
.btn-ghost { background: transparent; color: var(--sage); padding: 12px var(--gesso-space-1) }
.btn-ghost:hover { color: var(--offwhite) }
button:focus-visible,a:focus-visible,[role="button"]:focus-visible { outline: 1px solid var(--gesso-neutral-200); outline-offset: 2px }
.sec { margin: var(--gesso-space-4) 0 12px; display: flex; align-items: baseline; justify-content: space-between }
.sec h2 { font-size: .75rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; color: var(--slate) }
.seclink { font-size: .8125rem; color: var(--signal); text-decoration: none }
.seclink:hover { color: var(--offwhite) }
.attention { display: flex; flex-direction: column }
.att-row { display: flex; align-items: center; gap: var(--gesso-space-2); padding: var(--gesso-space-2) 4px; border-bottom: 1px solid rgba(255,255,255,0.05) }
.att-row:last-child { border-bottom: none }
.att-icon { width: 44px; height: 44px; border-radius: var(--gesso-radius-md); background: rgba(255,255,255,0.06); display: flex; align-items: center; justify-content: center; flex-shrink: 0 }
.att-copy { flex: 1; min-width: 0 }
.att-copy strong { display: block; font-weight: 700; font-size: .9375rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.att-copy span { display: block; font-size: .8125rem; color: var(--slate); white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.att-act { flex-shrink: 0 }
.chips { display: flex; flex-wrap: wrap; gap: var(--gesso-space-1); margin-bottom: var(--gesso-space-1) }
.chip { display: inline-flex; align-items: center; gap: var(--gesso-space-1); padding: 10px var(--gesso-space-2); border-radius: var(--gesso-radius-full); background: rgba(255,255,255,0.05); border: 1px solid var(--gesso-divider, rgba(0,0,0,0.06)); font-size: .8125rem; color: var(--offwhite); cursor: pointer; transition: background var(--dur) var(--ease) }
.chip:hover { background: rgba(255,255,255,0.10) }
.chip .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--signal) }
.events { display: flex; flex-direction: column }
.ev-row { display: flex; align-items: center; gap: var(--gesso-space-2); padding: var(--gesso-space-2) 4px; border-bottom: 1px solid rgba(255,255,255,0.05) }
.ev-row:last-child { border-bottom: none }
.ev-img { width: 44px; height: 44px; border-radius: var(--gesso-radius-md); overflow: hidden; flex-shrink: 0; position: relative; isolation: isolate }
.ev-img::after { content: ''; position: absolute; inset: 0; background: var(--forest); mix-blend-mode: multiply; opacity: .25 }
.ev-img img { width: 100%; height: 100%; object-fit: cover; filter: grayscale(.4) contrast(1.05) brightness(1.05) }
.ev-copy { flex: 1; min-width: 0 }
.ev-copy strong { display: block; font-weight: 700; font-size: .9375rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.ev-copy span { display: block; font-size: .8125rem; color: var(--slate); white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.badge { flex-shrink: 0; font-size: .6875rem; letter-spacing: .06em; padding: 5px 10px; border-radius: var(--gesso-radius-full); white-space: nowrap }
.b-ok { color: var(--signal); background: var(--gesso-secondary) }
.b-sus { color: var(--gesso-warning); background: rgba(201,162,39,0.12) }
.b-blk { color: var(--gesso-error); background: rgba(224,112,90,0.12) }
.cta-anchor { position: fixed; left: var(--gesso-space-2); right: var(--gesso-space-2); bottom: var(--gesso-space-12); z-index: 5 }
.cta { width: 100%; padding: var(--gesso-space-2) var(--gesso-space-3); border-radius: var(--r); background: var(--verdant); color: #fff; border: none; font-family: var(--gesso-font-display); font-weight: 700; font-size: var(--gesso-text-base); cursor: pointer; transition: transform var(--dur) var(--ease),filter var(--dur) var(--ease),box-shadow var(--dur) var(--ease); box-shadow: var(--gesso-shadow-sm, 0 1px 2px rgba(0,0,0,0.06))}
.cta:hover { filter: brightness(1.1); transform: translateY(-1px) }
.cta:active { transform: translateY(1px) scale(0.99) }
.ic { width: 16px; height: 16px; stroke: currentColor; fill: none; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round }
.ic-lg { width: 24px; height: 24px }
.att-act .ic { color: var(--slate) }
@media (prefers-reduced-motion:reduce) {*,*::before,*::after { transition-duration: 0.01ms !important; animation-duration: 0.01ms !important }}</style>
<style>/* gesso-icon-base v1 */
.ic { display: inline-block; width: 16px; height: 16px; vertical-align: -0.125em; flex-shrink: 0; line-height: 0; }
.ic svg { width: 100%; height: 100%; display: block; }
svg.ic { width: 16px; height: 16px; display: inline-block; vertical-align: -0.125em; flex-shrink: 0; }
.ic[data-icon-style="line"] { stroke-width: var(--ic-stroke, 2); }
.ic[data-icon-style="line"] svg path, .ic[data-icon-style="line"] svg circle, .ic[data-icon-style="line"] svg rect, .ic[data-icon-style="line"] svg line, .ic[data-icon-style="line"] svg polyline, .ic[data-icon-style="line"] svg polygon { stroke-width: inherit; }
.ic-sm { --ic-stroke: 2.25; }
.ic-xs { --ic-stroke: 2.5; }
svg.ic-lg, .ic-lg svg { width: 24px; height: 24px; }
svg.ic-xl, .ic-xl svg { width: 32px; height: 32px; }
svg.ic-2xl, .ic-2xl svg { width: 32px; height: 32px; }
.ic-lg { --ic-stroke: 1.75; }
.ic-xl { --ic-stroke: 1.5; }
.ic-2xl { --ic-stroke: 1.5; }
button { border: 0; background: transparent; padding: 0; font: inherit; color: inherit; cursor: pointer; -webkit-appearance: none; appearance: none; }
</style>
<style id="gesso-component-styles">body {
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
}

button {
  border: 0;
  background: transparent;
  -webkit-appearance: none;
  appearance: none;
  font: inherit;
  color: inherit;
  padding: 0;
  cursor: pointer;
}

[data-component="Card"]:not([class]):not([style]) {
  background: var(--gesso-neutral-800) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  padding: var(--gesso-space-4) !important;
  color: var(--gesso-neutral-50) !important;
  box-shadow: none !important;
}

[data-component="Button"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: var(--gesso-space-2) !important;
  white-space: nowrap !important;
  background: var(--gesso-primary) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  font-weight: 600 !important;
  padding: var(--gesso-space-3) var(--gesso-space-6) !important;
  border: 0 !important;
  border-radius: var(--gesso-radius-full) !important;
  cursor: pointer !important;
  min-height: 48px !important;
  text-decoration: none !important;
}
[data-component="Button"][data-variant="secondary"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"][data-variant="secondary"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
}
[data-component="Button"][data-variant="ghost"]:not(:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"])):not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > button:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > a:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > [role="button"]:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > input[type="submit"]:not([class]):not([style]),
[data-component="Button"][data-variant="ghost"]:not([data-gesso-marker-wrap]) > input[type="button"]:not([class]):not([style]) {
  background: transparent !important;
  color: var(--gesso-neutral-50) !important;
}

[data-component="Input"]:not([class]):not([style]) {
  display: block !important;
  width: 100% !important;
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  padding: var(--gesso-space-3) var(--gesso-space-4) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  min-height: 48px !important;
  outline: none !important;
}
[data-component="Input"]:not([class]):not([style])::placeholder {
  color: var(--gesso-neutral-400) !important;
}

[data-component="Select"]:not([class]):not([style]) {
  display: block !important;
  width: 100% !important;
  background: var(--gesso-neutral-900) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  padding: var(--gesso-space-3) var(--gesso-space-4) !important;
  border: 1px solid rgba(10,10,10,0.05) !important;
  border-radius: var(--gesso-radius-md) !important;
  min-height: 48px !important;
}

[data-component="Toggle"]:not([class]):not([style]), [data-component="Checkbox"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  gap: var(--gesso-space-2) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-sm) !important;
}

[data-component="Badge"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  gap: var(--gesso-space-1) !important;
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-xs) !important;
  font-weight: 500 !important;
  padding: var(--gesso-space-1) 10px !important;
  border-radius: var(--gesso-radius-full) !important;
  line-height: 1.2 !important;
  max-height: 24px !important;
}

[data-component="StatCell"]:not([class]):not([style]) {
  display: flex !important;
  flex-direction: column !important;
  gap: 4px !important;
  background: transparent !important;
  border: 0 !important;
  padding: 0 !important;
}
[data-component="StatCell"]:not([class]):not([style]) > :first-child {
  font-family: var(--gesso-font-display) !important;
  font-weight: 700 !important;
  color: var(--gesso-neutral-50) !important;
}

[data-component="ListRow"]:not([class]):not([style]):not(ul):not(ol) {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 12px 16px !important;
  border-bottom: 1px solid rgba(10,10,10,0.05) !important;
  background: transparent !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-body) !important;
  font-size: var(--gesso-text-base) !important;
  min-height: 56px !important;
}
[data-component="ListRow"]:not([class]):not([style]):last-child {
  border-bottom: 0 !important;
}
/* A mislabeled list container stays a list, so its rows stack. */
ul[data-component="ListRow"]:not([class]):not([style]),
ol[data-component="ListRow"]:not([class]):not([style]) {
  display: block !important;
  list-style: none !important;
  margin: 0 !important;
  padding: 0 !important;
}
/* Two-column enforcement (info LEFT / action RIGHT, no floating middle).
   The trailing affordance pins to the right edge so the row reads as exactly
   two zones; everything else clusters on the left and the gutter collapses.
   Action-less rows carry no Button/Toggle child, so nothing pins and they
   flow naturally. min-width:0 lets the text zone shrink + truncate instead
   of forcing a wrap (the prompt forbids subtitle wrapping). Only inside a
   row WE laid out (bare): a model-styled row keeps its own arrangement. */
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"],
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"] > button,
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Button"] > a,
[data-component="ListRow"]:not([class]):not([style]) > [data-component="Toggle"] {
  margin-inline-start: auto !important;
  flex-shrink: 0 !important;
}
[data-component="ListRow"]:not([class]):not([style]) > * {
  min-width: 0 !important;
}

[data-component="Avatar"]:not([class]):not([style]) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 40px !important;
  height: 40px !important;
  flex-shrink: 0 !important;
  border-radius: var(--gesso-radius-full) !important;
  background: var(--gesso-neutral-800) !important;
  color: var(--gesso-neutral-50) !important;
  font-family: var(--gesso-font-display) !important;
  font-size: var(--gesso-text-sm) !important;
  font-weight: 600 !important;
  overflow: hidden !important;
}
[data-component="Avatar"]:not([class]):not([style]) img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

[data-component="EmptyState"]:not([class]):not([style]) {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 16px !important;
  padding: 48px 24px !important;
  text-align: center !important;
  color: var(--gesso-neutral-400) !important;
  font-family: var(--gesso-font-body) !important;
}

[data-component="TabBar"]:not([class]):not([style]) {
  display: flex !important;
  align-items: stretch !important;
  background: var(--gesso-neutral-900) !important;
  border-top: 1px solid rgba(10,10,10,0.05) !important;
  padding: 8px 8px 0 8px !important;
}

[data-gesso-marker-wrap],
[data-component][style*="display: contents"],
[data-component="Button"]:has(> button, > a, > [role="button"], > input[type="submit"], > input[type="button"]) {
  display: contents !important;
  padding: 0 !important;
  margin: 0 !important;
  background: none !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  min-height: 0 !important;
  color: inherit !important;
  font: inherit !important;
}</style>
<style id="gesso-chrome-clearance">
body { padding-top: 44px !important; padding-bottom: 80px !important; }
[data-sticky-action="true"] {
  box-sizing: border-box !important;
  display: flex !important;
  gap: 12px !important;
  align-items: stretch !important;
  padding: 12px 16px !important;
}
[data-sticky-action="true"] > button,
[data-sticky-action="true"] > a,
[data-sticky-action="true"] > [role="button"] {
  flex: 1 1 0 !important;
  min-height: 48px !important;
  min-width: 0 !important;
}
</style>
<style id="gesso-text-wrap">h1,h2,h3{text-wrap:balance}p,li,figcaption,blockquote{text-wrap:pretty}</style>
<style id="gesso-font-smoothing">html{-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale}</style>
<style id="gesso-image-outline">img:not([data-illustration]):not([data-icon]):not([aria-hidden="true"]){outline:1px solid rgba(255,255,255,0.05);outline-offset:-1px}</style>
<style id="gesso-tokens">

:root {
  --gesso-primary: #0a6c60;
  --gesso-secondary: #54e454;
  --gesso-neutral-50: #8eb89a;
  --gesso-neutral-100: #b8d4bb;
  --gesso-neutral-200: #e5e6e9;
  --gesso-neutral-300: #d2d3d7;
  --gesso-neutral-400: #5a8f6e;
  --gesso-neutral-500: #888b91;
  --gesso-neutral-600: #5a8f6e;
  --gesso-neutral-700: #484c53;
  --gesso-neutral-800: #30343c;
  --gesso-neutral-900: #b8d4bb;
  --gesso-neutral-950: #8eb89a;
  --gesso-success: #16A34A;
  --gesso-warning: #c36b05;
  --gesso-error: #DC2626;
  --gesso-font-display:"Manrope", system-ui, -apple-system, sans-serif;
  --gesso-font-body:"Manrope", system-ui, -apple-system, sans-serif;
  --gesso-font-mono:"Manrope", ui-monospace, "JetBrains Mono", monospace;
  --gesso-leading-tight: 1.15;
  --gesso-leading-snug: 1.35;
  --gesso-leading-normal: 1.5;
  --gesso-leading-relaxed: 1.7;
  --gesso-tracking-tight: -0.01em;
  --gesso-tracking-normal: 0em;
  --gesso-tracking-wide: 0.04em;
  --gesso-radius-sm: 10px;
  --gesso-radius-md: 16px;
  --gesso-radius-lg: 24px;
  --gesso-radius-full: 9999px;
  --gesso-shadow-sm: 0 1px 2px rgba(12,19,34,0.08);
  --gesso-shadow-md: 0 4px 12px rgba(12,19,34,0.10);
  --gesso-shadow-lg: 0 16px 40px rgba(12,19,34,0.14);
  --gesso-text-lg: 1.25rem;
  --gesso-text-sm: 0.875rem;
  --gesso-text-xl: 1.5rem;
  --gesso-text-xs: 0.6875rem;
  --gesso-text-2xl: 2rem;
  --gesso-text-3xl: 2.5rem;
  --gesso-text-4xl: 3rem;
  --gesso-text-base: 1rem;
  --gesso-text-5xl: 3rem;
  --gesso-text-6xl: 3.75rem;
  --gesso-text-7xl: 4.5rem;
  --gesso-text-8xl: 6rem;
  --gesso-text-9xl: 8rem;
  --gesso-space-unit: 8px;
  --gesso-space-1: 8px;
  --gesso-space-2: 16px;
  --gesso-space-3: 24px;
  --gesso-space-4: 32px;
  --gesso-space-6: 48px;
  --gesso-space-8: 64px;
  --gesso-space-12: 96px;
  --gesso-space-16: 128px;
  --gesso-space-24: 192px;
  --gesso-space-32: 256px;
  --gesso-duration-fast: 120ms;
  --gesso-duration-base: 200ms;
  --gesso-duration-slow: 360ms;
  --gesso-easing-default: cubic-bezier(0.2,0,0,1);
  --gesso-easing-emphasis: cubic-bezier(0.34,1.56,0.64,1);
}

img,svg,video{max-width:100%;height:auto;}</style>
<style id="gesso-role-lock">:root{--gesso-fg:#0c1322 !important;--gesso-fg-muted:#5a8f6e !important;--gesso-accent:#0a6c60 !important;--gesso-accent-2:#54e454 !important;}</style>
<!--gesso-fonts:start--><style id="gesso-font-lock">:root{--gesso-font-display:"Manrope", system-ui, -apple-system, sans-serif !important;--gesso-font-body:"Manrope", system-ui, -apple-system, sans-serif !important;--gesso-font-mono:"Manrope", ui-monospace, "JetBrains Mono", monospace !important;}</style><!--gesso-fonts:end-->
</head>
<body data-brief-id="screen-root" data-brief-role="screen">

<div class="ambient" aria-hidden="true"></div>
<main id="main-content">

  <header data-brief-id="nav-top" data-brief-role="nav-top">
    <div class="top">
      <span class="wordmark">SELVARD</span>
      
      <svg data-logo="selvard-mark" width="32" height="32" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M16 4 C24 4 28 10 28 16 C28 22 24 26 18 26" fill="none" stroke="#2F5D50" stroke-width="5" stroke-linecap="round"></path>
        <path d="M16 28 C8 28 4 22 4 16 C4 10 8 6 14 6" fill="none" stroke="#0A6C60" stroke-width="5" stroke-linecap="round"></path>
        <circle cx="16" cy="16" r="3.5" fill="#54E454"></circle>
      </svg>
    </div>
    <p class="greet">Good morning, Priya</p>
  </header>

  <section data-component="Card" data-variant="elevated" class="glass posture"
           data-brief-id="hero-posture" data-brief-role="hero">
    <span class="t-meta">Security posture</span>
    <div class="posture-state">
      <span class="state-word">NORMAL</span>
      <span class="conf">High confidence</span>
    </div>
    
    <div class="reasons" data-brief-id="viz-posture-domains" data-brief-role="viz">
      <div class="reason" data-ok="1">
        <span class="lbl">Network</span><span class="what">DNS filtering on</span>
        <span class="track"><span class="fill" style="width: 92%"></span></span>
        <span class="st">Protected</span>
      </div>
      <div class="reason" data-ok="1">
        <span class="lbl">Apps</span><span class="what">One app to review</span>
        <span class="track"><span class="fill" style="width: 74%"></span></span>
        <span class="st">Attention</span>
      </div>
      <div class="reason" data-ok="0">
        <span class="lbl">Identity</span><span class="what">One address exposed</span>
        <span class="track"><span class="fill" style="width: 48%"></span></span>
        <span class="st">Attention</span>
      </div>
      <div class="reason" data-ok="1">
        <span class="lbl">Device</span><span class="what">OS limits noted</span>
        <span class="track"><span class="fill" style="width: 84%"></span></span>
        <span class="st">Protected</span>
      </div>
    </div>
  </section>

  <section data-component="Card" data-variant="base" class="glass glass--quiet nest"
           data-brief-id="card-nest-check" data-brief-role="card">
    <svg data-viz="nest-ring" class="ring" viewBox="0 0 88 88" role="img" aria-label="68 percent along today's check path">
      <circle cx="44" cy="44" r="38" fill="none" stroke="rgba(255,255,255,0.10)" stroke-width="7"></circle>
      <circle cx="44" cy="44" r="38" fill="none" stroke="#0A6C60" stroke-width="7" stroke-linecap="round"
              pathLength="100" stroke-dasharray="68 100" transform="rotate(-90 44 44)"></circle>
      <text x="44" y="50" text-anchor="middle" font-family="Manrope" font-size="20" font-weight="500" fill="#EDF1F7" class="num">68%</text>
    </svg>
    <div class="nest-copy">
      <div class="nest-title num">84</div>
      <p class="nest-sub">Today's check, 68% along the path. Finish the check to unlock the identity sweep.</p>
      <div class="nest-actions">
        <button data-component="Button" data-variant="primary" class="btn btn-primary"
                data-brief-id="cta-continue-check" data-brief-role="cta">Continue</button>
        <span class="t-meta" style="text-transform: none; letter-spacing: var(--gesso-tracking-normal)">Ready when you are</span>
      </div>
    </div>
  </section>

  <div class="sec">
    <h2>Needs attention</h2>
    <a class="seclink" href="#" data-brief-id="link-attention-all" data-brief-role="button">View all</a>
  </div>
  <section data-component="ListRow" data-variant="with-action" class="attention"
           data-brief-id="list-attention" data-brief-role="list">
    <div class="att-row" data-brief-id="list-attention-item-0" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/globe" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M12 2a14.5 14.5 0 0 0 0 20a14.5 14.5 0 0 0 0-20M2 12h20"></path></g></svg></span>
      <div class="att-copy"><strong>Turn on DNS-over-HTTPS</strong><span>Network · 2 minutes</span></div>
      <button class="att-act" aria-label="Open network settings" data-brief-id="btn-doh-open" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
    <div class="att-row" data-brief-id="list-attention-item-1" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/message" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="M2.992 16.342a2 2 0 0 1 .094 1.167l-1.065 3.29a1 1 0 0 0 1.236 1.168l3.413-.998a2 2 0 0 1 1.099.092a10 10 0 1 0-4.777-4.719"></path></svg></span>
      <div class="att-copy"><strong>Review Ledger Companion</strong><span>Apps · notification access</span></div>
      <button class="att-act" aria-label="Review app permissions" data-brief-id="btn-app-review" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
    <div class="att-row" data-brief-id="list-attention-item-2" data-brief-role="list-item">
      <span class="att-icon"><svg data-icon="lucide/mail" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic ic-lg" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m22 7l-8.991 5.727a2 2 0 0 1-2.009 0L2 7"></path><rect width="20" height="16" x="2" y="4" rx="2"></rect></g></svg></span>
      <div class="att-copy"><strong>Swap the breached email</strong><span>Identity · new address</span></div>
      <button class="att-act" aria-label="Open identity details" data-brief-id="btn-identity-open" data-brief-role="button"><svg data-icon="lucide/chevron-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="m9 18l6-6l-6-6"></path></svg></button>
    </div>
  </section>

  <div class="sec"><h2>Protected assets</h2></div>
  <section data-component="Card" data-variant="outlined" class="chips"
           data-brief-id="chips-assets" data-brief-role="card-grid">
    <button class="chip" data-brief-id="chip-banking" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/credit-card" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="14" x="2" y="5" rx="2"></rect><path d="M2 10h20"></path></g></svg>Banking</button>
    <button class="chip" data-brief-id="chip-email" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/mail" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m22 7l-8.991 5.727a2 2 0 0 1-2.009 0L2 7"></path><rect width="20" height="16" x="2" y="4" rx="2"></rect></g></svg>Email</button>
    <button class="chip" data-brief-id="chip-work" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/folder" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"></path></svg>Work</button>
    <button class="chip" data-brief-id="chip-dev" data-brief-role="card" style="width: auto; height: auto; border-radius: 999px"><span class="dot"></span><svg data-icon="lucide/key" viewBox="0 0 24 24" fill="none" stroke="currentColor" data-icon-style="line" class="ic" aria-hidden="true" style="max-width:32px;max-height:32px"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="m15.5 7.5l2.3 2.3a1 1 0 0 0 1.4 0l2.1-2.1a1 1 0 0 0 0-1.4L19 4m2-2l-9.6 9.6"></path><circle cx="7.5" cy="15.5" r="5.5"></circle></g></svg>Dev</button>
  </section>

  <div class="sec"><h2>Recent events</h2></div>
  <section data-component="ListRow" data-variant="with-avatar" class="events"
           data-brief-id="list-events" data-brief-role="list">
    <div class="ev-row" data-brief-id="list-events-item-0" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/2a043a9a59a9.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/lush-tropical-jungle-at-sunrise-in-sri-lanka-34139024/" data-stock-photographer="Tharu 569" /></span>
      <div class="ev-copy"><strong>Link checked</strong><span>messenger-metro.co · just now</span></div>
      <span class="badge b-ok">NO KNOWN THREAT</span>
    </div>
    <div class="ev-row" data-brief-id="list-events-item-1" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/94c308b1e3bb.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/creek-falling-down-the-stones-8364991/" data-stock-photographer="Chris F" /></span>
      <div class="ev-copy"><strong>App flagged</strong><span>Ledger Companion · 10:04</span></div>
      <span class="badge b-sus">SUSPICIOUS</span>
    </div>
    <div class="ev-row" data-brief-id="list-events-item-2" data-brief-role="list-item">
      <span class="ev-img"><img src="https://rpreisbpsxrjwqsfeggf.supabase.co/storage/v1/object/public/direction-images/directions/23a4c47a-303d-4893-9d00-cbc8089724de/d15d613da60d.jpg" data-photo-aspect="portrait" alt="" style="max-width:100%" data-stock-provider="pexels" data-stock-page="https://www.pexels.com/photo/silhouette-of-mountains-near-the-lake-5874130/" data-stock-photographer="Dan Voican" /></span>
      <div class="ev-copy"><strong>DNS query blocked</strong><span>ads-gw.trk.net · 09:41</span></div>
      <span class="badge b-blk">BLOCKED</span>
    </div>
  </section>

  <div class="cta-anchor">
    <button data-component="Button" data-variant="primary" class="cta"
            data-brief-id="cta-check-link" data-brief-role="cta">Check a link</button>
  </div>

</main>

<section data-chrome="tab-bar" data-component="TabBar" data-brief-id="chrome-tab-bar" data-brief-role="tab-bar" style="position:fixed;bottom:0;left:0;right:0;height:80px;display:flex;align-items:flex-start;padding:8px 8px 0 8px;background:var(--gesso-canvas, rgba(250,250,250,0.95));backdrop-filter:saturate(180%) blur(20px);-webkit-backdrop-filter:saturate(180%) blur(20px);border-top:1px solid var(--gesso-divider, rgba(10,10,10,0.08));z-index:100;font-family:var(--gesso-font-body, system-ui);">
    <div data-brief-id="nav:home" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 0 1-1 1h-5v-7h-6v7H4a1 1 0 0 1-1-1z"></path></svg></span>
      <span style="color:inherit!important">Home</span>
    </div>
    <div data-brief-id="nav:shield" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle><text x="12" y="12" text-anchor="middle" dominant-baseline="central" font-size="11" font-weight="600" fill="currentColor" font-family="system-ui, sans-serif">S</text></svg></span>
      <span style="color:inherit!important">Shield</span>
    </div>
    <div data-brief-id="nav:timeline" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="8" r="4"></circle><path d="M4 22c0-4.4 3.6-8 8-8s8 3.6 8 8"></path></svg></span>
      <span style="color:inherit!important">Timeline</span>
    </div>
    <div data-brief-id="nav:identity" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"></circle><text x="12" y="12" text-anchor="middle" dominant-baseline="central" font-size="11" font-weight="600" fill="currentColor" font-family="system-ui, sans-serif">I</text></svg></span>
      <span style="color:inherit!important">Identity</span>
    </div>
    <div data-brief-id="nav:settings" data-active="false" style="display:flex;flex-direction:column;align-items:center;gap:4px;flex:1;font-size:10px;font-weight:500;color:var(--gesso-fg-muted, rgba(10,10,10,0.5));">
      <span style="width:24px;height:24px;display:inline-flex;align-items:center;justify-content:center;color:inherit;" aria-hidden="true"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="3"></circle><path d="M19 12a7 7 0 0 0-.1-1.3l2-1.5-2-3.4-2.3 1a7 7 0 0 0-2.2-1.3L13.7 2h-3.4l-.5 2.5a7 7 0 0 0-2.2 1.3l-2.3-1-2 3.4 2 1.5A7 7 0 0 0 5 12a7 7 0 0 0 .1 1.3l-2 1.5 2 3.4 2.3-1a7 7 0 0 0 2.2 1.3l.5 2.5h3.4l.5-2.5a7 7 0 0 0 2.2-1.3l2.3 1 2-3.4-2-1.5A7 7 0 0 0 19 12z"></path></svg></span>
      <span style="color:inherit!important">Settings</span>
    </div>
</section></body>
</html>
```

Use the tokens as CSS variables. Treat the reference HTML as the visual
source of truth; adapt structure to your framework, but do not deviate
from the visual system.
