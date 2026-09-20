# Competitive Research

Phase 0 landscape scan. Purpose: identify what exists, what users understand, what they dislike, and the differentiation gap — not to copy features.

## 1. Landscape

| Segment | Examples | What they do well | Structural weakness |
| --- | --- | --- | --- |
| Play Protect / built-in | Google Play Protect | Deep OS integration; always-on; zero setup | Opaque; weak user-facing evidence; no link/identity dimension |
| Mobile AV (Malwarebytes, Bitdefender, Norton) | Signature/heuristic scanners + web protection | Familiar; install-time scanning; strong brands | "Device is safe" false-confidence UX; heavy permissions; upsell-driven design; VPN bundles that *do* tunnel traffic (privacy irony) |
| DNS filtering / ad-block VPNs (Blokada, AdGuard, DuckDuckGo App Tracking Protection) | VpnService local filtering | Proves the local-VPN model works; users understand "blocked" lists | One-dimensional (ads/trackers); no incident context; no link/identity engines |
| Secure browsers (Firefox Focus, Brave) | Content-level protection at browser layer | Real content visibility (where links actually open) | Requires browser switch; no device-wide correlation |
| Breach monitors (Firefox Monitor, HIBP direct, Incogni) | Identity exposure & removal | HIBP brand trust; simple | Disconnected from device security; little correlation with on-device events |
| Enterprise MDM/EDR | Management + detection | Deep privilege = real prevention (install blocks, policy enforcement) | Not available to consumers; surveillance-shaped |

## 2. What users understand

"Virus scan", "block trackers", "my email was in a breach", "is this link safe". These mental models are assets: our engines map onto them (App Guardian, Network Guardian, Identity, Link Guardian) but must be *re-explained honestly*.

## 3. What users dislike

Battery drain and always-on VPN overhead; nagging upsells and fake-scarity alerts; opaque "100% safe" claims; security apps demanding excessive permissions (contacts, SMS — the surveillance-drift problem); dark-pattern "you are at risk, subscribe now".

## 4. Gaps / differentiation opportunity

1. **Honest posture** — nobody ships "no known threat" instead of "safe"; evidence-first alerts with confidence are rare outside enterprise.
2. **Local-only VPN filtering** — most security VPNs tunnel to *their* servers; a local loopback filter that provably ships no traffic to a vendor is a strong, demonstrable trust position.
3. **Correlation across categories** — no consumer product correlates link -> DNS block -> install -> reboot into one explainable incident.
4. **Privacy-first stance enforced architecturally** — no SMS/contact/location permission, no analytics SDK, allowlisted egress: verifiable claims instead of privacy-policy promises.

## 5. Position

We are not competing on scan depth with Play Protect (it wins that by OS privilege) or on filter-list size with DNS blockers. We compete on **coherence, honesty, and privacy architecture**: the control plane that explains what happened across links/apps/network/identity/device — and what it could not see.
