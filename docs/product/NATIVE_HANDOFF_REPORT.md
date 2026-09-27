# Selvard — Full Session Handoff Report (Hoplite → Antigravity / native Android Studio)

Date: 2026-09-27. Branch: `hoplite/taras-0f2fc809`. Head: Phase 12 commit
`efa75cd`. Latest release: `v0.8.0-phase12`. Package `app.selvard`,
minSdk 26, targetSdk/compileSdk 35. Contact: khubaibnazeer8@gmail.com.
Audience: the owner, working next in Antigravity with Android Studio, real
devices, and Gemini 3.8 Flash.

## 1. Where this started (your raw prompt, what it became)

Your raw ask: an Android-first (desktop later) cybersecurity software, MVP
for Android only, cleaner curvy liquid-glass interface (never a hacker
dashboard), privacy above all, a serious name and logo, and coverage for:
malicious links from groups/Telegram/Reddit/X/email/SMS; data encryption
and firewalling; unexpected restarts; Apple-style sensor-dot visibility;
silent data leakage; sensitive apps (banking, Gmail, docs, social,
WhatsApp, business, dev/GitHub/Jira/AI apps, antivirus, hardware-fault
fallbacks); a who-has-my-data exposure list; and a virtual in-phone
sandbox that feeds attackers a decoy virus.

ChatGPT's structured spec (the two large `pasted-text.txt` attachments,
58 sections) turned that into the build order actually followed: product
vision → threat model (20 categories A–T) → Android-reality check → core
engines → event bus → risk engine → posture → privacy architecture → TI →
local-first → MVP → V2/V3 → documentation gates, with an explicit
no-fake-features rule.

## 2. Raw-prompt item → what shipped → honest limit

| Raw item | Shipped | Limit (platform truth, never faked) |
| --- | --- | --- |
| Bad links everywhere (groups, Telegram, Reddit, X, mail, SMS, phishing) | Link Guardian: share-intent + manual + foreground-paste analysis, punycode/IDN, typosquat/brand heuristics, local TI lookup, evidence-first verdicts (Phase 3) | No interception inside other apps' surfaces — Android forbids it; analysis is user-initiated (spec §05, MVP_SCOPE) |
| Encryption + "firewall" + security interface | Keystore/StrongBox encrypted Room store, Keystore-held AES-256, backup off, cleartext banned; Network Guardian local-only DNS filter (Phase 4) | No full per-app firewall on stock Android; split tunnel routes only DNS in, everything else bypasses — stated in UI and listing |
| Unexpected restarts | Timestamp-only boot records, device-integrity facts (Phase 6) | Pre-install history unknowable; no backfill; dedupe is process-lifetime |
| Sensor-dot (camera/mic/location use by others) | Privacy coverage matrix with honest NOT MONITORED / OS LIMITATION labels (Phase 6) | Third-party sensor use is OS-owned; we surface OS indicators only |
| Silent data leakage | Privacy Monitor facts + per-app DNS destination profiling (domains only, never payloads) | TLS content never read by design; no MITM ever |
| Sensitive apps, dev apps, hardware fallbacks | Protected Assets, App Guardian permission-pattern + installer + legacy-SDK analysis (Phase 5) | Granted-state of other apps unreadable; hardware faults not fully detectable; no result is a malware verdict |
| Who-has-my-data exposure list | Identity Exposure: encrypted vault, per-identity consented HIBP v3 checks (Phase 7) | Cannot enumerate forgotten third-party sharing; only breach exposure of identities you declare |
| In-phone sandbox feeding attackers a decoy virus | Explicitly rejected per spec §10: NEVER execute unknown malicious code on-device. Shipped static/hash/manifest/permission analysis + provider abstraction; remote isolated sandbox is V2+ (ADR-gated) | This was the biggest raw-vs-platform conflict; decoy execution would make Selvard the malware vector |
| Curvy liquid-glass UI, no hacker theater | Phase 9: 7 bottom tabs, restrained palette, words-not-color posture, WCAG 2.1 AA, no auto-animation, disclosure-first onboarding | Screen-reader + on-device visual pass still pending (owner-side) |
| Name + logo as trust signals | Selvard ("self-warden"), owner-supplied primary mark adopted as launcher icon + banner (brand commits `1032830`–`1a6a87f`) | Screenshots/feature graphic still owner-side |
| Desktop later, MVP Android-only | `/core` KMP holds all decision logic as pure functions; `/android` only collects evidence and renders (ADR-001) | No `/desktop` yet; V3 reuses `/core` unchanged |

## 3. What was built (Phases 0–12, verified)

- Phase 0: 19 docs (PRD/DRD/architecture/roadmap/threat model/security + privacy architecture/ADRs).
- Phase 1: Gradle multi-module scaffold, CI (detekt, JVM tests, lint, gitleaks, assemble), disclosure-first onboarding.
- Phase 2: event model/bus, encrypted store, risk + policy engines, retention.
- Phase 3: Link Guardian + share target + manual check + verdict/evidence UI (52/52 tests at the time).
- Phase 4: split-tunnel VpnService DNS filter, prominent-disclosure consent, foreground notification, honest OFF states (70/70 at the time).
- Phase 5: App Guardian inventory + permission catalog + combo detection; v0.1.1 scan-crash fix (§5 below).
- Phase 6: Privacy Engine facts + coverage matrix (99/99 at the time).
- Phase 7: Identity Exposure HIBP vault (consented, session-only key).
- Phase 8: incident correlation (temporal proximity only, causal-lint) + timeline UI.
- Phase 9: 7-tab IA + restrained visual system + WCAG AA + deletion receipt (129/129 at the time).
- Phase 10: hardening review, 13 findings H1–H13 fixed (§5), report archived (138/138).
- Phase 11: MVP test matrix — 158/158 JVM tests (accuracy corpus, pipeline integration, failure injection, manifest lockdown), report `docs/security/PHASE11_MVP_TEST_REPORT.md`.
- Phase 12: release pack — privacy policy, `SECURITY.md` contact, honest store listing, Play declaration drafts, rollout plan, checklist, incident dry run; `v0.8.0-phase12` released.

Current gates: 158/158 `:core:domain:jvmTest`, `detekt` clean,
`:android:app:lintDebug` clean, `:android:app:assembleDebug` green.
Debug APK ≈ 10.3 MB, Android 8.0+.

## 4. Every issue found, and its state

### 4a. Security findings H1–H13 (Phase 10, all fixed, tests hold them)

- H1/H2 (High): pump fail-open-via-crash — absurd IHL lengths and hostile DNS names crashed the filter thread, killing all filtering. Now range-validated, fail closed.
- H3: blind indexing in checksum/u16 readers → window-validated rejects.
- H4: unbounded share-intent token → `SharedUrlParser` (8 KiB cap, never throws) + 1000-case fuzz.
- H5: HIBP k-anonymity compared case-sensitively → lowercase suffixes missed real breaches; now case-insensitive.
- H6: unbounded HIBP body read/parse → 256 KiB read cap + parse caps.
- H7: vault codec trusted shapes → structural validation + size caps.
- H8: `maskEmail` crashed on corrupt values → `***` fallback.
- H9: over-long blocked hostnames threw inside the recording coroutine → truncated to `MAX_REF_LENGTH` first.
- H10: API key in plain field → password-masked, session-only.
- H11: tap-jacking on delete-all → `filterTouchesWhenObscured` on the host view.
- H12: screenshots of sensitive screens → `FLAG_SECURE` on Main + CheckLink activities.
- H13: cleartext default + unshrunk release → cleartext banned + `network_security_config` + `isShrinkResources`.

### 4b. Functional bugs found by measurement (fixed, gated)

- v0.1.1 App Guardian scan crash: permission-heavy real-device packages exceeded `MAX_FINDINGS` (24) and killed the app on Scan. Now capped heaviest-first with the omission stated, best-effort event recording, error render instead of close, regression test.
- Phase 11 `bbc`→`hsbc` false positive: distance-2 typosquat flagged `bbc.com`. Root cause was short-label collision; distance-2 now needs labels ≥ 4 chars. Corpus holds it: 0/36 benign warnings, 0 benign BLOCKs.
- Detekt breaks during Phase 11 (fixed, not weakened): `LoopWithTooManyJumpStatements` (DNS loop refactored to single continue), `UseCheckOrError` (test throws → `error()`), `CyclomaticComplexMethod` (brand checks split into `typosquatFinding`/`embeddedBrandFinding`), plus a bad string-escape rewrite and a BOM stripped from the Phase 11 report.

### 4c. Known-open debt (disclosed in every release note; your Antigravity backlog)

No external pen test. No dependency vulnerability scan or Gradle lockfiles (CI: gitleaks only). No on-device run. IPv6 DNS bypass undetected. Single-threaded pump. Battery benchmark pending. Field-study false-positive rate pending. Sample feeds only (no real TI blocklists). No biometric vault gate. No Play Integrity backend. Debug-signed builds only.

## 5. The download/install problem (your screenshots, fully diagnosed)

Evidence in-thread: Chrome red "Dangerous site" page for `tis-codipwe.work`; `selvard-v0.1.0-phase5.apk` stalled at 10.33/10.33 MB; Play Protect "App blocked… hasn't seen an app from this developer before."

- Chrome-at-100%: Chrome Safe Browsing holds the unknown debug-signed APK (generic local debug key, no reputation) and never releases the `.crdownload`. Same Google account → same verdict on phone and laptop. Workarounds: Firefox/Opera download, laptop transfer + rename off `.crdownload`, or pause Safe Browsing briefly. Not a repo bug.
- Play Protect block: expected for a new developer + sideloaded debug build; reputation is earned through signed, policy-clean releases and install history — it cannot be bypassed in code.
- Distribution posture: APKs ship correctly via GitHub Releases assets (all 9 releases; v0.8.0 link verified live, served as `application/vnd.android.package-archive`). Never link raw repo files. Do NOT host the APK on GitHub Pages instead: Pages is static web hosting with no release integrity metadata and the same (or worse) browser warnings — a Pages landing page linking to Releases is fine, the binary must stay on Releases (then Play/F-Droid).
- Permanent fix order: release keystore + Play App Signing → Play internal → closed → open → production (plan in `docs/product/ROLLOUT_PLAN.md`); optionally F-Droid (free, builds from source, no Play fees).

## 6. Hoplite constraints (what this sandbox could never do)

Browser-based agent workspace: no `/dev/kvm`, no emulator, no physical devices — so no `VpnService` lifecycle runs, no API 26–35 visibility matrix, no battery/RSS/cold-start numbers, no screen-reader pass, no release-APK smoke. No trusted environment for a release keystore (must never be created here). No device screenshots or feature graphic. JVM tests + detekt + lint + assemble are the verified ceiling from here; everything device-side is yours.

## 7. Antigravity native runbook (do in this order)

1. Clone, open in Android Studio, run `./gradlew :core:domain:jvmTest detekt :android:app:lintDebug :android:app:assembleDebug` to confirm the green baseline.
2. Create the release keystore locally (Build → Generate Signed Bundle/APK, 25y+ validity, V2+V3/V4), enroll in Play App Signing; never commit it.
3. `connectedCheck` on an API 26–35 emulator matrix + one physical device: tunnel start/stop, VPN-consent revoke, reboot; confirm OFF surfaces instantly and nothing is claimed while OFF; run the Phase 11 report §3 steps.
4. Measure: Battery Historian VPN delta (< 3%/day target), RSS with 6 months of events (< 150 MB), cold start < 2 s, event-bus latency (< 200 ms), throughput.
5. Capture screenshots (Home, Security verdict, Network consent, Timeline, Settings receipt) + 1024×500 feature graphic; tick `STORE_LISTING.md` boxes.
6. File Play Console listing + VpnService declaration + QUERY_ALL_PACKAGES form using `PLAY_DECLARATIONS.md` answers; start the internal track per `ROLLOUT_PLAN.md`.
7. Optional free channel: F-Droid submission (repo builds from source, kills the Chrome-hold class of complaint).
8. Cut the release-signed track builds; keep GitHub Releases for sideload testers with the honest-limits note.

## 8. File map for Gemini

Spec: attachments `art_upload_a52bdd3153c04b078c1e3e1a06be49b4`/`art_upload_b5f5fef95b874e98976acb0ea72a14af` `pasted-text.txt` (58-section master), `art_upload_fa0872421a854bf8a8b8980729ba04c1` (download thread). Truth docs: `docs/product/MVP_SCOPE.md`, `docs/security/THREAT_MODEL.md`, `docs/security/PRIVACY_ARCHITECTURE.md`, `DATA_CLASSIFICATION.md`, `SECURITY_TESTING.md`, `HARDENING_REPORT.md`, `PHASE11_MVP_TEST_REPORT.md`, `INCIDENT_RESPONSE.md` + `INCIDENT_RESPONSE_DRY_RUN.md`. Release pack: `docs/product/PRIVACY_POLICY.md`, `STORE_LISTING.md`, `PLAY_DECLARATIONS.md`, `ROLLOUT_PLAN.md`, `RELEASE_CHECKLIST.md`, `SECURITY.md`. Code: `core/domain` (pure decisions + 158 tests), `android/app` (collect + render). Brand: `assets/brand/`, `mipmap-*` launcher icon.

## 9. Bottom line

Everything buildable and verifiable without devices is done and released (v0.8.0-phase12, all 12 phases, 158/158 green, limits disclosed). Everything remaining needs your hands, your keystore, and your devices: sign → Play tracks → measure → screenshots → rollout. The decoy-virus sandbox stays rejected by design; the download friction ends with signed Play/F-Droid distribution, not with code.

