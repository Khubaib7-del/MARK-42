# Play Policy Declarations (draft answers for console submission)

## VpnService declaration

- Core functionality: local-only DNS filtering (Network Guardian). Split
  tunnel: only DNS packets are routed into the app; every other packet
  bypasses it — no remote endpoint exists in the design (ADR-003).
- Data: blocked names are answered REFUSED on-device (zero egress);
  allowed lookups are forwarded unmodified to the device's own resolver.
  No traffic content is read, stored, or transmitted.
- Prominent disclosure: blocking in-app consent screen (flow F3) before
  first enable, plain language, separate from onboarding; non-dismissable
  notification while active; instant OFF state surfaced in UI.

## QUERY_ALL_PACKAGES (Permissions Declaration Form)

- Core purpose: device-security scanning (permitted use). App Guardian
  inventories installed packages to analyze declared-permission risk
  patterns, installer provenance, and legacy target SDKs.
- Why broad visibility is needed: scoped package queries cannot build the
  inventory the security analysis requires; alternatives were considered
  and are insufficient for the stated core purpose.
- Prominent disclosure: in-context screen at first App Guardian use
  stating what is read (package names, declared permissions, installer)
  and what is never read or claimed (granted state, app contents).

## Sensitive permissions not used (state if asked)

No SMS, contacts, call log, location, accessibility, notification-listener,
camera, microphone, or media permissions — requested or read. Manifest
budget is pinned by `ManifestPolicyTest` (8 allowlisted, 13 prohibited).

## Target audience / content / ads

General audience, Everyone rating target; no ads; no in-app purchases;
free. No account creation.

## Release track note

First submission goes to the internal testing track, then closed, then
open testing, then production (see `docs/product/ROLLOUT_PLAN.md`). The
current build is debug-signed for sideload testing; the Play track builds
must be cut with the release key (OWNER ACTION — keystore creation and
Play App Signing enrollment).
