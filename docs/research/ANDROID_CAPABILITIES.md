# Android Capability Matrix

**Mandatory reference before implementation.** Verified against Android Developers, AOSP, and Google Play policy documentation during Phase 0 (2026-09). Re-verify per phase for API/policy drift — Android 17 (API 37) changes (cross-profile loopback restriction, `ACCESS_LOCAL_NETWORK`, default Certificate Transparency) are recent and still moving.

Legend — Possible?: YES / PARTIAL / NO | Reliability: signal stability across OEMs & API levels | Privacy risk: risk our use creates for the user.

| Capability | Possible? | Permission | Android API | Reliability | Privacy Risk | MVP? |
| --- | --- | --- | --- | --- | --- | --- |
| URL interception (other apps' links) | NO | — | — | n/a | n/a (would be high) | No — share-intent instead |
| Share-intent handling | YES | none | `ACTION_SEND` activity | High | None (user-initiated) | **Yes** |
| Clipboard analysis | PARTIAL | foreground only | Android 10+ restricts to focused app / default IME | High | Medium (user pasted content) | **Yes** (paste-in check only) |
| VPN monitoring (traffic through tunnel) | YES | VpnService + user consent + Play declaration | `VpnService` | High | **High** (all device traffic routes through us → mitigated by local-only processing) | **Yes** |
| DNS monitoring | YES | via VpnService | tunnel DNS interception | High | High (destination metadata) | **Yes** |
| HTTPS visibility (payload) | NO | — | TLS | — | n/a | Never (no MITM by principle) |
| App inventory | PARTIAL | `QUERY_ALL_PACKAGES` (Play declaration; device security = permitted use) or scoped `<queries>` | `PackageManager` | High | Medium (inventory is personal data per Play) | **Yes** |
| Other apps' requested permissions | YES | same as inventory | `PackageManager.getInstalledPackages` | High | Medium | **Yes** |
| Other apps' *granted* permission state | NO | restricted | — | — | — | No — documented limitation |
| Camera activity (other apps) | NO (system indicator only) | — | AppOps stats signature-gated | — | — | No — NOT MONITORED label |
| Microphone activity (other apps) | NO (same) | — | — | — | — | No — NOT MONITORED label |
| Location usage (other apps) | NO (same) | — | — | — | — | No; we use no location at all |
| Notification access | PARTIAL (special access) | NotificationListenerService + Play-restricted use | — | High | **Very high** (reads message bodies) | No — deferred, privacy review required |
| Accessibility (content inspection) | NO — prohibited by our principles | would be a11y service | — | — | Extreme | Never |
| Screen-capture *detection* (of us) | PARTIAL | — | `FLAG_SECURE` prevents our own capture; no detection of others | High for prevention | None | Yes (FLAG_SECURE on sensitive screens) |
| APK analysis (user-selected files) | YES | user file selection | static manifest/hash/cert parsing | High | None local | **Yes** |
| Malware dynamic analysis on device | NO | — | prohibited by principle; execution risk | — | Extreme | Never; remote sandbox (V2, ADR) |
| Device integrity | YES | Play Integrity (no user permission) | `IntegrityManager`, key attestation | High (hardware-backed) | Low | **Yes** |
| Reboot/boot detection | YES | `RECEIVE_BOOT_COMPLETED` (normal) | `ACTION_BOOT_COMPLETED`, `Settings.Global.BOOT_COUNT` | High | None | **Yes** |
| Storage encryption | YES | Android Keystore | Keystore/StrongBox + SQLCipher/EncryptedFile | High | None | **Yes** |
| Application isolation (sandbox other apps) | NO | device-owner/OEM only | — | — | — | No — documented |
| Private Space visibility | NO (from outside) | — | Android 15 feature | — | — | No — guidance only |
| Virtualization (AVF) | NO for normal apps | privileged | Android Virtualization Framework not exposed to third party | — | — | No — research watch |
| Play Integrity verdicts | YES | Play distribution + opt-in for enhanced verdicts | Standard/classic requests | High | Low (device signals to Google) | **Yes** |
| Per-app network attribution (UID) | PARTIAL | via VpnService | tunnel + UID mapping | Medium (OEM quirks; Android 17 loopback changes) | Medium | **Yes** (where feasible, honest fallback) |
| Brand impersonation / phishing heuristics | YES (client-side) | none | URL parsing + feeds | Medium (heuristic) | None | **Yes** |
| Breach intelligence | PARTIAL (cloud) | HIBP subscription | v3 API | High for feed coverage | Medium (query disclosure) | **Yes** (consented) |

## Critical notes per capability

1. **VpnService** — Play requires a declaration form and prominent in-app disclosure before activation; device security apps are an explicitly permitted use class. Our design is **local loopback only**: traffic is filtered on-device and never tunnels to a remote server (ADR-003). This is the single most trust-critical design decision in the product: it converts the "VPN" from a surveillance risk into a local filter.
2. **QUERY_ALL_PACKAGES** — Play treats installed-app inventory as personal & sensitive data. Device security apps are a permitted use, but require the Permissions Declaration Form and prominent disclosure. We must not extend use beyond the declared purpose.
3. **Package visibility inside Private Space (Android 15+)** — apps inside a user's Private Space are not visible to us outside it. We surface this honestly: inventory is annotated as "device profile only".
4. **Granted-permission state of other apps** — not available to third-party apps. App Guardian therefore reports *declared* permissions and flags risk patterns; UI copy must never imply we know what another app currently holds.
5. **Sensor usage events by other apps** — Android provides system indicators (green/blue dots, Privacy Dashboard) precisely because third-party apps cannot see these events. We complement with declarations, Play Integrity `appAccessRiskVerdict` windows, and historical/correlation context — and label real-time sensor use NOT MONITORED.
6. **Notification access** — technically possible with special access, but reads message content (extreme privacy risk) and Play restricts it to core use. Deferred; any future use must survive a full privacy re-review and Play declaration. MVP uses explicit share-intent instead.
7. **Clipboard** — background clipboard access blocked since Android 10; we only analyze what the user pastes while our app is focused. No background clipboard monitoring.
8. **Play Integrity enhanced verdicts** — `appAccessRiskVerdict` (screen-capture/overlay/control-capable apps present), `playProtectVerdict` (Play Protect state + found-risky-apps), `MEETS_STRONG_INTEGRITY` (patch level) are opt-in; verify current availability/licensing requirements at implementation (ADR review Phase 6). Verdict verification against Play servers requires backend — MVP fetches verdicts but treats locally-unverifiable payloads as unverified signals.
9. **Per-UID network attribution** — possible in the tunnel via UID-to-flow mapping (NetGuard-style). Keep expectations honest: some OEM VPN stacks and Android 17's cross-profile loopback changes affect edge cases; feature must degrade to "unattributed" transparently.
10. **Certificate Transparency** — Android 17+ enables CT verification by default, strengthening platform defenses we do not need to reimplement.

## Derived engine feasibility

| Engine | Verdict |
| --- | --- |
| Link Guardian | PARTIALLY AVAILABLE — explicit-input analysis only; verdicts honest and evidence-backed; full pre-open interception impossible for normal apps (V2 in-app browser is the honest path) |
| Network Guardian | PARTIALLY AVAILABLE — DNS/destination metadata strong; content invisible; local-only VPN with Play declaration |
| App Guardian | PARTIALLY AVAILABLE — declarations & metadata yes; runtime internals & grant state no |
| Privacy Monitor | PARTIALLY AVAILABLE — historical/correlation layer over OS indicators; real-time third-party sensor events NOT MONITORED |
| Identity Exposure | REQUIRES CLOUD (consented, minimized, HIBP) |
| Device Integrity | AVAILABLE (Play Integrity + boot events), verdicts point-in-time |
| Incident Engine | AVAILABLE — all our own data |

**Rule of record:** if a capability in this matrix says NO, no UI element, notification, or marketing copy may imply otherwise. Discovery of new platform capabilities (e.g., future Android releases) updates this file first, then code.
