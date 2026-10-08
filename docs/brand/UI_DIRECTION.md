# Selvard UI direction — institutional calm

## Intent

The owner-provided `institutional-calm-design.md` is inspiration, not an implementation specification.
Selvard remains a native Kotlin/Compose Android app. The goal is a calm security environment with
strong visual hierarchy and evidence users can inspect, rather than a text-heavy website or fictional dashboard.

## Visual system

- Dark ink canvas (`#0C1322`), layered glass surfaces (`#19202E`, `#212938`).
- Off-white foreground (`#EDF1F7`), muted slate supporting text (`#9FB0C6`).
- Signal green (`#54E454`) is a restrained action accent; amber/coral carry warning tones alongside words and icons.
- Bundled Manrope typography; no runtime font request. Compact editorial headers, deliberate type hierarchy.
- 8 dp spacing rhythm, 20–24 dp screen/card padding, 24–28 dp rounded surfaces.
- Glass is implemented as native opaque gradients with faint borders. No experimental shader dependency,
  unreadable blur, stock imagery, or permanent background pulse.
- Green atmosphere belongs around the Home posture panel, not behind every paragraph.
- Preserve the actual layered brand emblem. Intro progresses from drafting to assembly to an ink handoff;
  it can be skipped and honors disabled motion. The 3.4-second sequence runs on first use or requested replay,
  pauses while hidden, and preserves saved Home navigation/scroll state on return.

## Information architecture

Five bottom destinations: **Home / Shield / Timeline / Identity / Settings**.
Shield groups **Links / Network / Apps / Privacy** in a horizontally scrollable tab row.
Home actions can navigate directly into a guardian pane. Routes, not enum ordinals, are saved.
Each tool owns one bounded vertical scroll/list. Evidence is expandable or in a scrollable bottom sheet.

Home shows recorded posture, deliberate link entry, real tool state, fact-based recommendations, and recent activity.
It never fabricates encrypted-storage meters, zero-egress claims, percentages, identity matches, or protected assets.
Empty history is labeled "No signals yet", rather than a Normal assessment. The three-step first-run journey
introduces evidence, privacy, and optional coverage with a static boundary motif and persistent controls.

## State and language contract

- Posture assesses a bounded local event history, not the whole device; show that boundary.
- Quiet/empty states are not safety claims. Provider/read failures mean unknown/unavailable.
- A Link Guardian `BLOCK` verdict is **Do not open**, not proof of traffic interception. Historical link
  events encoded as `BLOCKED` are also displayed as a recommendation, whereas actual DNS refusals may say blocked.
- Links and DNS currently use development sample intelligence. State this visibly.
- Android VPN approval is requested before service start. UI running state comes from the service,
  not the switch handler; service lifecycle reliability is a separate engineering issue.
- Onboarding completion is persisted locally, but grants no permission and starts no engine.
- Identity keeps explicit provider/data disclosure and separate full-address consent; API keys are session-only.
- Delete-all requires a destructive confirmation. Do not infer all-or-nothing deletion if one store fails.

## Accessibility and validation

Core tests cover selected dark text/chip/button contrast pairs, status vocabulary, relative time, and attention logic.
These do not establish WCAG compliance for every rendered screen. Device font scaling, touch reachability,
landscape, reduced motion, and assistive technology require review. Maintain words/icons alongside semantic color.
Debug builds permit screenshots for review; release `FLAG_SECURE` remains enforced.

See `docs/product/UI_REDESIGN_VALIDATION.md` for observed checks and outstanding device/runtime work.
