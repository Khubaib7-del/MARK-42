# ADR-011: Native dark UI and explicit coverage contract

- Status: Accepted for development; release/device validation remains required
- Date: 2026-10-01

## Context

The current seven-section interface is visually dense, light-heavy, and inconsistent. Its Home presentation
also suggests stronger coverage than the engines can establish. An owner-provided Gesso reference suggests
institutional calm but contains invented percentages, identities, and findings. It is not a source of security data.

## Decision

Keep native Compose and the existing domain/security boundaries. Use an ink-based Material theme,
bundled Manrope, locally generated pinned Lucide vectors, subtle native surface gradients, and five
bottom destinations. Group links/network/apps/privacy under Shield. Replace Home's decorative metrics
with actual local-history posture, tool state, recommendations, and recorded activity. Preserve real collectors,
consent flows, identity masking, and temporal-only correlation. Use scrollable evidence sheets and bounded lists.

Keep the actual intro logo assembly with a dark handoff, skippable one-shot motion and reduced-motion support.
Persist disclosure completion without persisting or granting security permissions.

Display manual link blocking decisions as **Do not open**: there is no evidence that a manual check blocks
traffic in the app from which the link came. Request Android VPN approval before starting the DNS service;
never optimistically write the running state from a UI toggle. Keep sample-intelligence limitations visible.

## Consequences

No WebView conversion, blur framework, analytics dependency, cloud dashboard, fabricated scores or new
security capability is introduced. UI assets bring static license obligations; licenses are bundled with the app.
Existing Material icons may coexist during migration. Core logic remains platform-neutral.

The event schema is unchanged: legacy link `BLOCKED` actions remain encoded historically, but are interpreted
as recommendations in the UI. A future event-schema migration can distinguish decision from enforcement
at persistence time; raw action labels must not imply enforcement without a corresponding observation.

The original 2–3-minute device lifetime report is not explained by a UI build passing. VPN pump failure/status,
network changes, IPv6 bypass, battery impact, and device-specific behavior require a separate reproducible
runtime investigation. This UI milestone must not be presented as production security certification.
