# Selvard source review — 9 October 2026

This review continues the existing native Kotlin/Compose implementation and preserves the owner's uncommitted UI and VPN work. The supplied initializer is product intent, not a request to rebuild a finished project from phase zero.

## Changes made in this review

- Replaced the two-page text-heavy disclosure journey with three illustrated, scrollable pages: understanding signals, privacy, and choosing coverage. Progress stays visible; actions stay outside scrolling content. Completion grants no permissions.
- Shortened the brand sequence from 4.9 to 3.4 seconds. Preserve the actual layered logo and blueprint-to-assembly concept. Play on first run, or explicit replay, rather than every cold start. Pause the clock while the activity is hidden; honor disabled system motion. Compact layouts omit the secondary principles line.
- Preserve saved Home navigation and scroll state across intro replay, and pane scroll state across guardian changes. Back from a secondary destination returns to Home. Peer destination crossfades are 150 ms; posture expansion is 180 ms.
- Added restrained surface elevation and a directional highlight border. Keep opaque fills for text contrast; no additional shader, blur, tracking, or animation library.
- Home labels an empty history "No signals yet" rather than Normal. Network disclosures are shorter while preserving VPN replacement, notification, battery, sample-data, and coverage information. Navigation labels stay on one line at large font sizes.
- Removed the unused PostureView screen, FoundationBanner, onboarding compatibility wrapper, obsolete tonal-wash constant, unused package-kind list, and unused mist resource. Removed immersive window mutation so the intro keeps system bars stable and avoids Android's fullscreen instruction overlay. Kept engines, domain policies, active helpers, tests, and original brand assets.
- Move app inventory analysis and posture computation off the UI thread.
- Handle exposure-check, vault-load, identity-removal, and share-target event-storage failures. Cancellation propagates; operational errors are visible or logged as fixed codes without private content.
- Expose existing local OS exit diagnostics in Settings. They distinguish app crashes, native crashes, ANRs, resource kills, and other exits on Android 11+. They do not include stack traces and cannot establish a root cause alone.
- Deletion runs independently for the event store and identity vault, with accurate partial receipts. Check the vault file deletion result. Added five domain tests for successful deletion, either partial failure, both failures, and cancellation.
- Removed an API-27-only style attribute from the unqualified API-26 theme after Android lint found it.

## Findings still relevant to release readiness

1. The roadmap's completed phases primarily establish source/build/domain-test milestones. They do not establish production reliability, full-device security, or completed owner-side release gates.
2. Link and DNS intelligence are development samples. Link checks do not retrieve pages, inspect credential forms, trace redirects, or provide live reputation. A link BLOCK verdict is a recommendation; DNS refusals are actual network actions.
3. The local VPN covers selected DNS traffic, with IPv6/DoH/DoT coverage limitations. It is not a complete firewall or encrypted-content inspection system. DNS relay is sequential; performance/battery measurements remain necessary.
4. Play Integrity integration, a biometric vault gate, per-UID attribution, and signature analysis are deferred. AndroidIntegritySource returns NOT_SUPPORTED for Play Integrity. Documentation must distinguish target design from current implementation.
5. Retention policies and their enforcer exist in the domain, but no Android production caller invokes RetentionEnforcer. Automatic retention deadlines are therefore not verified/enforced by the current app wiring. Do not promise automatic purging.
6. Room queries currently apply category/time matching after a limited recent fetch. A filtered query may omit older matching events. Home and Timeline explicitly disclose their bounded latest-event history, but this needs a separate store correction before broader query features.
7. The encrypted vault uses direct file writes. Atomic persistence and interrupted-write recovery deserve a separate storage hardening pass; do not silently reset unreadable data or weaken encryption.
8. Release signing, dependency vulnerability scanning, Play declarations, assistive-technology review, and real-device battery/network/crash validation remain release gates.
9. No confirmed stack trace from the owner's phone is available. Fixes to inspected failure paths must not be described as a proven fix for the reported 60–90 second crash.

## Validation

See UI_REDESIGN_VALIDATION.md for commands, observed emulator checks, and limits. Local review artifacts are under review/polish-2026-10-09. Original review artifacts were preserved.

## Motion references

- https://developer.android.com/develop/ui/compose/animation/quick-guide
- https://developer.android.com/develop/ui/compose/animation/value-based

Use platform Compose APIs, keep peer tabs as peers, defer per-frame reads to drawing/layer callbacks, and reserve the elaborate brand motion for first run or requested replay.
