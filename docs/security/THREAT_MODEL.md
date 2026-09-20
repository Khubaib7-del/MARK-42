# Threat Model

Scope: Selvard, Android-first privacy-first security environment, normal third-party app constraints (no root, no device-owner, no OEM privileges).

Sources consulted: Android Developers documentation (VPN, package visibility, privacy indicators, tapjacking/overlay protections), Android Open Source Project (privacy indicators, enterprise/security docs), Google Play policies (VpnService, QUERY_ALL_PACKAGES, Device and Network Abuse), Play Integrity API documentation, OWASP MASVS/MSTG, abuse.ch/Spamhaus and HIBP API terms. Claims below were checked against these sources during Phase 0; re-verify at implementation time for API/policy drift.

## System assets and trust boundaries

**Assets:** user-declared identities (emails), security event history, app inventory observations, network destination metadata, policy preferences, protected asset designations, app secrets (API keys), device integrity observations.

**Trust boundaries:** (1) Android app sandbox vs other apps; (2) our app vs OS (we cannot trust OS integrity — we can only attest it); (3) device vs cloud (any backend is untrusted); (4) our app vs threat-intelligence providers (providers see minimized queries); (5) our app vs the user (transparent controls; no hidden collection).

**Attacker classes:** remote attacker (phishing/network), malicious-app author (spyware/overlay/fake apps), physical attacker (device seizure), compromised/sketchy provider (data-hungry SDK), malicious dependency (supply chain), and — unique to this product class — **the security app itself gone wrong** (surveillance drift), which we treat as a first-class adversary.

## Legend

Each category documents the 20 required attributes. "Capability column" values use: AVAILABLE / PARTIALLY AVAILABLE / REQUIRES USER PERMISSION / REQUIRES DEVICE-OS SUPPORT / REQUIRES CLOUD / NOT AVAILABLE TO NORMAL APPS.

---

## A. Link-based attacks

1. **Attack name:** Phishing / malicious URLs (including open-redirect abuse and punycode/IDN homoglyphs).
2. **Mechanism:** URL delivered through any channel; user taps; credential form or drive-by payload on destination.
3. **Typical delivery:** SMS, chat apps, email, social feeds, browser redirects, QR codes.
4. **User interaction required:** Tap (or none on drive-by pages).
5. **Data at risk:** Credentials, session tokens, PII, financial data.
6. **Potential impact:** Account takeover, malware download, financial fraud.
7. **Android detection capabilities:** Share intent check-in; manual URL check; foreground clipboard check; local + remote URL/domain reputation; DNS/certificate metadata from VpnService path; redirect-chain analysis (client-side fetch in controlled channel).
8. **Android restrictions:** No silent interception of links tapped in browsers or chat apps (third-party apps cannot hook other apps' intents). Default-browser role could intercept but conflicts with our privacy stance.
9. **Our app can observe:** URL + redirect chain + destination metadata only when the user explicitly submits the URL.
10. **Our app can prevent:** User from opening a link after warning; block DNS resolution at Network Guardian (destination-level), even if user proceeds.
11. **Requires user permission:** None for manual check; VpnService consent for DNS-level block; share intent is explicit user action.
12. **Requires cloud infrastructure:** Optional — reputation lookups, domain age/cert intel; local feeds work offline with reduced coverage.
13. **Requires privileged/OS-level:** Nothing additional.
14. **Impossible for normal apps:** Automatic pre-tap interception in arbitrary apps.
15. **Detection confidence:** High with reputation hit; Medium for heuristics (brand impersonation, typosquat patterns); Low without TI.
16. **False-positive risks:** Legit shorteners, newly registered legit domains, parked domains.
17. **False-negative risks:** Zero-day phishing kit, compromised legit site, fresh domain not yet on feeds.
18. **Appropriate messaging:** "Credential harvesting indicators detected" + evidence list; never "AI says dangerous".
19. **Mitigation strategy:** Link Guardian pipeline (PRD §7) + evidence-first verdicts + UNKNOWN when data insufficient.
20. **Future possibility:** In-app isolated browser view (V2) that *does* give full pre-open analysis — this is the honest path to interception.

## B. Messaging attacks

1. **Attack name:** Smishing (SMS-based phishing) and chat-app link lures.
2. **Mechanism:** Social-engineered message urges action; link/attachment leads to phishing or malware.
3. **Delivery:** SMS, RCS, WhatsApp, Telegram, Signal, etc.
4. **Interaction:** Tap link / save & install attachment.
5. **Data at risk:** Credentials, OTPs, financial details.
6. **Impact:** ATO, fraud, malware install.
7. **Android detection capabilities:** Only user-forwarded check (share into Link Guardian). Play Integrity app-access-risk for overlay abuse during tap. Network Guardian sees resulting destinations.
8. **Restrictions:** We cannot read SMS (READ_SMS is restricted and against our principles); NotificationListenerService access to message bodies requires special access, Play declaration, and creates surveillance risk — deliberately not used in MVP.
9. **Can observe:** URLs the user submits; destinations contacted afterward.
10. **Can prevent:** DNS-level block of phishing destination; warn on user-submitted links.
11. **Requires user permission:** None beyond explicit sharing; VPN consent for network path.
12. **Requires cloud:** Optional reputation intel.
13. **Requires privileged:** SMS access would require restricted permission + Play approval — rejected on privacy grounds.
14. **Impossible for normal apps:** Silent scanning of message bodies without notification access + its privacy cost.
15. **Confidence:** High when user submits link and TI confirms; otherwise UNKNOWN (channel itself is invisible to us).
16. **FP risks:** Overzealous domain warnings in benign forwarded links.
17. **FN risks:** Entirely invisible when the user taps without checking.
18. **Messaging:** "Messaging apps are not monitored. Share any suspicious link for analysis." — explicit NOT MONITORED disclosure.
19. **Mitigation:** Education flow + frictionless share entry point + share-target discoverability.
20. **Future:** Optional, clearly-consented notification-link extraction (declared, off by default) — requires full privacy re-review + Play declaration; V2 decision.

## C. Email attacks

1. **Attack name:** Email phishing / malicious attachments.
2. **Mechanism:** Credential lures, weaponized attachments, gift-card/BEC lures.
3. **Delivery:** Any email client.
4. **Interaction:** Tap link, open attachment, act on instructions.
5. **Data at risk:** Credentials, corporate data, payments.
6. **Impact:** BEC, ATO, malware.
7. **Detection capabilities:** User-submitted link analysis only; network destinations post-tap.
8. **Restrictions:** No email content access without notification listener or default-mail role — both rejected for MVP (surveillance risk / Play restrictions).
9. **Can observe:** Submitted links; post-tap destinations.
10. **Can prevent:** DNS-level blocking of known-bad destinations.
11. **Requires user permission:** Sharing is explicit user action.
12. **Requires cloud:** Optional URL reputation.
13. **Requires privileged:** Default email app role (rejected).
14. **Impossible for normal apps:** Silent email scanning.
15. **Confidence:** As category A once link submitted; channel otherwise UNKNOWN.
16. **FP risks:** Same as A.
17. **FN risks:** Channel invisible by design.
18. **Messaging:** NOT MONITORED disclosure + share-to-check affordance.
19. **Mitigation:** Same as B.
20. **Future:** Same re-review gate as B.

## D. Social-media attacks

1. **Attack name:** Social-platform phishing, DM lures, fake-support, malvertising in feeds.
2. **Mechanism:** In-platform DMs/comments with links; ads leading to fake stores/login pages.
3. **Delivery:** Platform apps + embedded browsers.
4. **Interaction:** Tap link, enter credentials on fake page, install fake app.
5. **Data at risk:** Platform + reused credentials, payment data.
6. **Impact:** ATO, fraud, malware.
7. **Detection capabilities:** User-submitted links; destination-level network blocking (many platforms resolve links in in-app browsers — destinations still resolve through system DNS and thus our VPN path).
8. **Restrictions:** Platform webviews don't expose URLs to us; no content APIs.
9. **Can observe:** Submitted URLs; DNS destinations.
10. **Can prevent:** Destination blocks.
11. **Permission:** None beyond VPN consent.
12. **Cloud:** Optional reputation intel.
13. **Privileged:** None possible.
14. **Impossible:** Reading feed/DM content.
15. **Confidence:** Medium-High at destination level; channel content unknown.
16. **FP:** Shortener false positives.
17. **FN:** Zero-day destinations.
18. **Messaging:** Destination blocked with evidence; "we cannot see inside social apps" honesty.
19. **Mitigation:** Link Guardian + Network Guardian synergy.
20. **Future:** None beyond V2 isolated browser.

## E. Browser attacks

1. **Attack name:** Malvertising, drive-by downloads, fake login pages, malicious redirects in browsers.
2. **Mechanism:** Compromised ad networks or sites redirect to exploit/phishing hosts.
3. **Delivery:** Chrome/custom tabs/in-app browsers.
4. **Interaction:** Sometimes none (drive-by), usually tap.
5. **Data at risk:** Credentials, device compromise via OS/app exploits.
6. **Impact:** Malware install, ATO.
7. **Detection capabilities:** DNS-level (our VPN sees domain lookups — high coverage); TLS prevents page content analysis; APK download events visible if file lands in Downloads (user-selected scan).
8. **Restrictions:** No browser interception; HTTPS content invisible; Chrome's own Safe Browsing overlaps.
9. **Can observe:** DNS lookups + destination IPs + timing per UID.
10. **Can prevent:** Known-bad domain resolution; warn timeline correlation.
11. **Permission:** VpnService consent.
12. **Cloud:** Feed-based (local blocklists updated remotely); page-content verdicts impossible locally.
13. **Privileged:** None.
14. **Impossible:** Content-level phishing detection inside browsers.
15. **Confidence:** High for known-bad DNS; UNKNOWN for content-level threats.
16. **FP:** Aggressive blocklists breaking sites — mitigated with tiered lists + user override.
17. **FN:** Fresh domains, legit-site compromise.
18. **Messaging:** "Domain blocked: known malware distribution site (URLhaus)" + NOT MONITORED note for page content.
19. **Mitigation:** Tiered blocklists; tracker list as separate category; override UX with warning.
20. **Future:** V2 in-app isolated browser for full content analysis.

## F. Download attacks

1. **Attack name:** Malicious file downloads (APK, documents, scripts).
2. **Mechanism:** Drive-by prompts, fake update pages, "invoice"/"resume" lures.
3. **Delivery:** Browser, chat, email.
4. **Interaction:** Tap to download; usually install/open.
5. **Data at risk:** Full device compromise (APK), doc exploit payloads.
6. **Impact:** Malware persistence, ransomware, spyware.
7. **Detection capabilities:** DNS-level pre-download; static analysis of user-selected files (hash intel, manifest analysis for APKs); download-directory watching is not possible for arbitrary apps' private dirs but user can point us at files.
8. **Restrictions:** No filesystem-wide monitoring without MANAGE_EXTERNAL_STORAGE (Play-restricted, invasive); no execution-based analysis on device (prohibited by our principles).
9. **Can observe:** Hashes, package metadata, certificate info of user-submitted files; network origin (DNS context).
10. **Can prevent:** Block download host (DNS); warn on detected-malicious file; block install link.
11. **Permission:** VPN consent; explicit file selection.
12. **Cloud:** Hash reputation lookups; remote sandbox (V2+).
13. **Privileged:** None.
14. **Impossible:** Automatic scanning of every downloaded file system-wide; dynamic detonation on device.
15. **Confidence:** High on known-bad hash; Medium on static heuristics; UNKNOWN on novel samples without cloud.
16. **FP:** Packed legitimate apps, dual-use tools.
17. **FN:** Custom-built malware unseen by intel.
18. **Messaging:** "SHA-256 matched known Android malware family (feed)" with evidence; "no known match — this does NOT mean safe".
19. **Mitigation:** Static pipeline (see MALWARE_ANALYSIS.md) + honest unknowns.
20. **Future:** Remote isolated sandbox detonation (V2), automated on-download scanning within our own V2 browser.

## G. APK / application attacks

1. **Attack name:** Malicious/fake APK installation (trojanized banks, fake antiviruses, sideloaded spyware).
2. **Mechanism:** App requests overbroad permissions; impersonates brands; hides launcher icon; abuses accessibility.
3. **Delivery:** Third-party stores, sideloading links, deceptive "Play Store" clones.
4. **Interaction:** Install + grant (often accessibility/device-admin).
5. **Data at risk:** Everything — SMS, screen content, credentials, location.
6. **Impact:** Spyware persistence, banking fraud.
7. **Detection capabilities:** Package inventory + manifest analysis (requested permissions, installer source, signature state, target SDK — old targetSdk is a risk signal); Play Integrity play-protect verdict (whether Play Protect is enabled / found risky apps) via opt-in verdicts; hash intel for user-submitted APK files.
8. **Restrictions:** Cannot inspect app internals/memory; cannot see granted-permission state of other apps; sideloaded apps not attested by Play Integrity appIntegrity; QUERY_ALL_PACKAGES needed for full inventory (Play declaration).
9. **Can observe:** Installed-package metadata for visible packages; new-package events (PACKAGE_ADDED broadcasts while our process lives); user-selected APK files.
10. **Can prevent:** Pre-install warnings on user-submitted APKs; DNS blocks to known distribution sites. Cannot block installation itself (that is OS-level).
11. **Permission:** QUERY_ALL_PACKAGES (declared); file selection.
12. **Cloud:** Hash reputation; remote static analysis service (V2).
13. **Privileged:** Install-blocking requires device owner/Play Protect — not available.
14. **Impossible:** Detecting malware purely from behavior inside another app's sandbox.
15. **Confidence:** Medium-High for impersonation/overbroad-permission patterns; High for hash match; UNKNOWN runtime behavior.
16. **FP:** Legit apps with unusual-but-justified permissions; enterprise apps.
17. **FN:** Legitimately-signed, permission-minimal spyware; zero-day families.
18. **Messaging:** "Accessibility abuse pattern: 4 of 4 high-risk markers" style enumerated evidence; never "this app is malware" without matched intel.
19. **Mitigation:** App Guardian risk signals + Play Integrity + education on sideloading.
20. **Future:** Remote sandbox triage; AVF-based analysis if platform ever exposes it to apps (it does not today).

## H. Network attacks

1. **Attack name:** Rogue Wi-Fi / ARP/DNS spoofing, MitM on unencrypted traffic, forced-portal credential phishing.
2. **Mechanism:** Attacker on-path positions between device and services.
3. **Delivery:** Hostile networks (cafés, airports, fake hotspots).
4. **Interaction:** Connecting to network; sometimes none.
5. **Data at risk:** Unencrypted traffic; credentials on captive portals.
6. **Impact:** Session hijack, credential theft, malware injection into HTTP.
7. **Detection capabilities:** VpnService path lets us observe DNS server reachability and destination metadata; cleartext-protocol detection (HTTP destinations visible as destinations not content); certificate validity checks are the OS's job (Android 17+ enables CT by default); Android 17 ACCESS_LOCAL_NETWORK restricts local-net scanning abuse.
8. **Restrictions:** Cannot decrypt TLS; cannot inspect other apps' socket payloads beyond metadata/flow visibility at VPN interface (practical: DNS + SNI absent — only IP/DNS; SNI is encrypted with DoH/DoT unless OS sends plaintext).
9. **Can observe:** DNS lookups, destination IPs/ports, per-UID flows, cleartext port usage.
10. **Can prevent:** Block bad destinations; block cleartext protocols per policy; cannot prevent TLS MitM by CAs — OS trust store governs.
11. **Permission:** VpnService consent.
12. **Cloud:** Feeds only.
13. **Privileged:** None needed for the above; network-level IDS impossible.
14. **Impossible:** Full packet inspection (TLS), detecting MitM using a rogue CA the user installed (we can *detect* user-installed CAs presence and warn — actually detecting list of user CAs is possible via KeyChain inspection prompts? No — apps cannot enumerate user-installed CAs. We can warn in education flow only.) — mark as partial/education.
15. **Confidence:** Medium — destination metadata is strong for known-bad, silent on content attacks.
16. **FP:** CDNs shared by malicious and benign sites.
17. **FN:** TLS-encrypted C2 to reputable infrastructure.
18. **Messaging:** "Connection to known-malicious host blocked" vs "Content of encrypted connections is not visible to us by design".
19. **Mitigation:** Network Guardian policy; guide users away from user-CA installs; DoH up-resolver choice (documented, privacy-reviewed).
20. **Future:** TLS-destination (SNI) analysis via eBPF impossible; not pursued.

## I. Identity attacks

1. **Attack name:** Identity theft / impersonation using breached PII.
2. **Mechanism:** Breach data reused for credential stuffing, SIM-swap-assisted ATO, synthetic identity.
3. **Delivery:** Remote, off-device (user's Android device is victim not the attack surface).
4. **Interaction:** None on-device.
5. **Data at risk:** Emails, phones, passwords, national IDs in breach dumps.
6. **Impact:** Financial fraud, account takeover.
7. **Detection capabilities:** Breach-intel lookup for user-declared identities (HIBP v3 API); stealer-log exposure checks (HIBP).
8. **Restrictions:** Cannot discover accounts or breaches beyond authorized feeds; cannot verify identity ownership beyond user declaration + confirmation mechanisms.
9. **Can observe:** Breach entries matching declared identities.
10. **Can prevent:** Nothing directly; can warn + guide remediation.
11. **Permission:** Explicit consent for each identity; biometric gate for identity vault.
12. **Cloud:** Yes — HIBP lookups are inherently remote (k-anonymity hashing on Pro plan reduces disclosure).
13. **Privileged:** None.
14. **Impossible:** "Magically find every site where this email was registered" — explicitly disclaimed.
15. **Confidence:** High for indexed breaches; coverage unknown beyond feed.
16. **FP:** Low (exact-match breach feeds).
17. **FN:** High — unindexed/private breaches, data brokers beyond scope.
18. **Messaging:** State model VERIFIED_CONNECTION / POSSIBLE_ACCOUNT / KNOWN_EXPOSURE / HISTORICAL_EXPOSURE / UNKNOWN.
19. **Mitigation:** HIBP integration (ADR-010), minimization (store only what's needed), deletion flows.
20. **Future:** More providers, domain-verification monitoring.

## J. Privacy attacks

1. **Attack name:** Stalkerware / commercial spyware / partnerware.
2. **Mechanism:** App with accessibility/device-admin/screen-record privileges installed by someone with device access; hides itself.
3. **Delivery:** Physical install, "family safety" apps.
4. **Interaction:** Install + permission grants by the attacker (not the user).
5. **Data at risk:** Location, messages, screen content, credentials.
6. **Impact:** Intimate surveillance, safety risk.
7. **Detection capabilities:** App inventory signals (accessibility-class apps, device-admin apps, hidden-launcher-icon apps, no-market-installer apps); Play Integrity appAccessRiskVerdict flags apps with accessibility/overlay/screen-capture access (opt-in verdict, requires Play distribution); package visibility limits completeness.
8. **Restrictions:** Cannot observe *actual* sensor use by other apps; cannot detect spyware hidden inside Private Space (not visible); Play Protect scope limited to Play-known malware.
9. **Can observe:** Static risk markers above; boot events; new-package events.
10. **Can prevent:** Nothing directly; educate + flag + guide removal.
11. **Permission:** QUERY_ALL_PACKAGES declaration; Play Integrity setup.
12. **Cloud:** None required; optional reputation.
13. **Privileged:** None.
14. **Impossible:** Detecting sophisticated stalkerware with no manifest markers and Play-integrity-clean runtime.
15. **Confidence:** Medium for marker-based detection; UNKNOWN for well-hidden tools.
16. **FP:** Legit accessibility tools (screen readers), parental-control apps — must be called out distinctly.
17. **FN:** High for custom/enterprise stalkerware.
18. **Messaging:** "App X has accessibility access and was installed outside Play — combination associated with spyware. This is not proof of malicious behavior."
19. **Mitigation:** App Guardian + Device Integrity + clear disclosure; at-risk-user guide.
20. **Future:** Research-grade heuristics + Play Protect verdict deep integration.

## K. Account attacks

1. **Attack name:** Account takeover (ATO) — credential stuffing, phishing, OAuth abuse, session hijack.
2. **Mechanism:** Reused passwords from breaches; phishing kits; malicious OAuth grants; SIM-swap.
3. **Delivery:** Remote.
4. **Interaction:** Usually off-device.
5. **Data at risk:** Cloud accounts, email (recovery hub!), banking.
6. **Impact:** Cascade compromise via email recovery chains.
7. **Detection capabilities:** Breach exposure of declared identities (I); OAuth-connection review via provider APIs where user authorizes us (Google/MS security-checkup surfaces — V2 integration, requires OAuth scopes we must privacy-review); destination blocks of phishing hosts.
8. **Restrictions:** We cannot see account sessions, provider-side alerts, or SIM status; integration requires per-provider OAuth and their policy.
9. **Can observe:** Declared-identity breach hits; (V2, consented) third-party connection lists.
10. **Can prevent:** DNS-level phishing-host blocks; password-hygiene guidance (Pwned Passwords k-anonymity checks are free API).
11. **Permission:** Explicit identity/consent; OAuth grants if V2 integrations.
12. **Cloud:** Yes for breach/OAuth intel.
13. **Privileged:** None.
14. **Impossible:** Generic discovery of all user accounts.
15. **Confidence:** High for feed hits; UNKNOWN beyond.
16. **FP:** Low; FN: High (unindexed breaches).
17. **FN risks:** See above.
18. **Messaging:** "Email identity found in breach X (2023, credential data class)". Guidance: password change + 2FA.
19. **Mitigation:** Identity engine + guidance; never claim "your account is secure".
20. **Future:** Provider security-checkup integrations (privacy-reviewed), stealer-log alerts.

## L. Device integrity attacks

1. **Attack name:** OS/root tampering, bootloader unlock, Magisk-based cloaking, emulated environments.
2. **Mechanism:** Attacker roots device for persistence/control; or attacker runs the *app* on an emulator to abuse services.
3. **Delivery:** Physical access or user-invited (sideloading "mod" OS).
4. **Interaction:** Bootloader unlock + physical flashing.
5. **Data at risk:** Full device.
6. **Impact:** Total compromise; sandbox escape of our own protections.
7. **Detection capabilities:** Play Integrity deviceIntegrity (MEETS_DEVICE / MEETS_BASIC / STRONG integrity verdicts); Keystore key attestation (verified-boot state via hardware attestation); boot_count changes; SELinux/ro.debuggable build markers.
8. **Restrictions:** Hardware-backed attestation is the strongest available signal but verdicts are point-in-time; Magisk/hide tools actively fight Play Integrity (arms race); we cannot kernel-monitor.
9. **Can observe:** Integrity verdicts, unexpected reboots (see R), our own tampering (signature check).
10. **Can prevent:** Nothing (device integrity is OS state) — we inform.
11. **Permission:** Play Integrity setup (no user permission); backend verification optional.
12. **Cloud:** Integrity verdict decryption/verification (Play server) — point queries only.
13. **Privileged:** Deeper boot verification impossible.
14. **Impossible:** Reliable detection of a fully-invisible rootkit on-device.
15. **Confidence:** High when verdicts fail; Medium when they pass (arms race).
16. **FP:** Unlocked-bootloader enthusiasts; custom ROM users — must be informative, not accusatory.
17. **FN:** Cloaked root (DenyList).
18. **Messaging:** "Device failed Play Integrity strong-integrity check (Android security patch level too old / bootloader unlocked)". Factual only.
19. **Mitigation:** Device Integrity engine + posture input; never block user from their own device data.
20. **Future:** Enhanced verdicts (deviceRecall beta) as Google ships them.

## M. Physical-access attacks

1. **Attack name:** Device seizure, forced unlock, extraction (brute-force passcode, chip-off, Cellebrite-class).
2. **Mechanism:** Physical possession + bypass techniques.
3. **Delivery:** Theft, border seizure, domestic coercion.
4. **Interaction:** Attacker has hands on device.
5. **Data at risk:** Everything on device, incl. our event store.
6. **Impact:** Total compromise incl. our own records.
7. **Detection capabilities:** Boot events (boot during "impossible" hours), failed-unlock signals (not directly observable — device uses WorkManager + `setRequiresDeviceUnlocked`-style semantics; actual lock-screen stats unavailable) — mark NOT MONITORED; unexpected reboot recorded; integrity checks post-boot.
8. **Restrictions:** No failed-unlock-count API for third-party apps; no SIM-removal broadcast since Android 10 (carrier config only); USB attack surface reduced by OEMs.
9. **Can observe:** Boot events, unusual boot timing vs user behavior baseline (correlation only).
10. **Can prevent:** We protect our own data: SQLCipher + Keystore keys in StrongBox (hardware bound, wipes on reset), biometric gate, FLAG_SECURE. We cannot protect other apps' data.
11. **Permission:** Biometric; none else.
12. **Cloud:** None.
13. **Privileged:** Zero — extraction-resistance is OEM/OS territory.
14. **Impossible:** Real physical-security guarantees; detecting chip-off.
15. **Confidence:** Low-Medium — we record facts (reboot at 03:14) not intent.
16. **FP:** Legit overnight updates/restarts (documented OEM auto-reboot).
17. **FN:** Silent extraction without reboot.
18. **Messaging:** "Device restarted at 03:14. Integrity check completed. No persistence indicators found." — never "attacker restarted your device".
19. **Mitigation:** Honest event records + Private Space education for sensitive apps + strong encryption of our store.
20. **Future:** Desktop-agent correlation (V3), duress features out of scope.

## N. Data leakage

1. **Attack name:** Data exfiltration by apps / cloud-side leakage of user data.
2. **Mechanism:** Overprivileged app uploads contacts/photos/device IDs; provider-side breach.
3. **Delivery:** Legit-appearing apps; third-party SDKs.
4. **Interaction:** Granted permissions (often pre-granted or socialized).
5. **Data at risk:** Contacts, photos, precise location, behavioral profiles.
6. **Impact:** Privacy loss, profiling, targeted abuse.
7. **Detection capabilities:** App inventory permission analysis (declared vs category norms); per-app destination profiling via VpnService (which *domains* an app contacts — e.g., known data-broker endpoints on tracker lists); volume anomalies visible as traffic counts.
8. **Restrictions:** Cannot see payload content; cannot see intra-app choices; app "data safety" claims on Play are self-declared (not verifiable by us).
9. **Can observe:** Destination metadata per app; permission declarations; install source.
10. **Can prevent:** Block tracker destinations (policy); deny DNS for known broker endpoints. Cannot prevent SDK activity that uses allowed endpoints.
11. **Permission:** VpnService consent; QUERY_ALL_PACKAGES declaration.
12. **Cloud:** Tracker/feed lists.
13. **Privileged:** None.
14. **Impossible:** Verifying an app does not leak *within* allowed traffic; content-level inspection.
15. **Confidence:** Medium — destination profiling is indicative, not proof.
16. **FP:** Shared CDNs (analytics vs tracking distinction).
17. **FN:** First-party data collection by the app itself.
18. **Messaging:** "App X contacts N tracker endpoints; blocked by policy" — with endpoint list, not moralizing claims.
19. **Mitigation:** Tracker tier of Network Guardian + App Guardian transparency.
20. **Future:** Broader destination classification; on-device DNS-log analytics.

## O. Supply-chain risks

1. **Attack name:** Compromised dependencies, malicious SDKs, typosquatted packages, backdoored build tooling.
2. **Mechanism:** We consume libraries; a poisoned transitive dependency ships malicious code in *our* app.
3. **Delivery:** Build time (our side) — or user-side via fake "update APKs".
4. **Interaction:** None by the user (trust in us).
5. **Data at risk:** Everything our app can access.
6. **Impact:** Our app becomes the attack — catastrophic for a security product.
7. **Detection capabilities:** Dependency lockfiles, checksums, automated vulnerability scanning (OWASP dep-check), provenance checks; user-side: Play-verified signature, no self-updating code (Play policy forbids non-Play updates).
8. **Restrictions:** Perfect SBOM/transparency is an industry-open problem.
9. **Can observe:** Our own build inputs; user-side package signature verification.
10. **Can prevent:** Tight intake (DEPENDENCY_POLICY.md), CI secret scanning, signed builds, minimal dep set, no dynamic code loading (Play policy + our principles).
11. **Permission:** N/A (process controls).
12. **Cloud:** None required.
13. **Privileged:** Reproducible-build attestation (future consideration).
14. **Impossible:** Fully eliminating transitive risk; vendored-review only mitigation.
15. **Confidence:** Medium — process risk, not signal detection.
16. **FP:** Heuristic scanners flag packed/cryptographic libs.
17. **FN:** Slow-burn project takeover (xz-class).
18. **Messaging:** Public dependency policy + disclosure; incident transparency (INCIDENT_RESPONSE.md).
19. **Mitigation:** DEPENDENCY_POLICY ledger + CI gates + minimal-surface architecture.
20. **Future:** Reproducible builds, in-toto style attestations.

## P. Sensor abuse

1. **Attack name:** Camera/microphone/location abuse by apps.
2. **Mechanism:** App with granted sensor permissions records silently.
3. **Delivery:** Overprivileged apps, spyware.
4. **Interaction:** Permission grant at install/runtime (often years earlier).
5. **Data at risk:** Audio, imagery, location history.
6. **Impact:** Surveillance, extortion, burglary planning.
7. **Detection capabilities:** System privacy indicators exist (Android 12+, status-bar green/blue dot, Privacy Dashboard) — we can NOT receive third-party "app used sensor" events; PermissionManager recent-access APIs apply to our own app. Play Integrity appAccessRiskVerdict (opt-in) reports *currently risky apps* capable of capturing screen/displaying overlays/controlling device. Static: which apps *hold* sensitive permissions (declared + we can't see grant state — see restrictions) is partially visible.
8. **Restrictions:** AppOps usage history for other packages requires signature/development permissions — NOT AVAILABLE to us; grant state of other apps not visible.
9. **Can observe:** Permission declarations; integrity app-access-risk verdict windows; our own rationale events.
10. **Can prevent:** Nothing directly — sensors are OS-mediated; we educate (indicator dots, per-app toggles) and give historical records for what IS observable (installs, permission sets at install, integrity windows).
11. **Permission:** QUERY_ALL_PACKAGES declaration + Play Integrity opt-in.
12. **Cloud:** None.
13. **Privileged:** AppOps stats (signature) — impossible.
14. **Impossible:** Real-time third-party sensor-use events; replacing OS indicators.
15. **Confidence:** Low-Medium — we are honest that real-time sensor-use is the OS's role, not ours.
16. **FP:** Flagging permission-heavy legit apps (camera apps need camera).
17. **FN:** We miss actual abuse events entirely (they are OS-private).
18. **Messaging:** "Real-time sensor indicators are provided by Android — we show history, correlation, and risk context instead". NOT MONITORED where true.
19. **Mitigation:** Privacy Monitor = historical/correlation/context layer; education.
20. **Future:** Android 17 `ACCESS_LOCAL_NETWORK`-era platform evolution may add signals; monitor AOSP.

## Q. Application permission abuse

1. **Attack name:** Accessibility/overlay/device-admin abuse (tapjacking, fake-login overlays, screen-control malware).
2. **Mechanism:** Malicious app gets a11y/SAW permission; intercepts input, reads screens, performs invisible clicks; overlays fake UI on banking apps.
3. **Delivery:** Socialized permission grants ("enable to continue").
4. **Interaction:** User grants special access.
5. **Data at risk:** Credentials, 2FA codes, screen content, funds.
6. **Impact:** Full interactive compromise.
7. **Detection capabilities:** Play Integrity `appAccessRiskVerdict` (apps present that can capture screen, display overlays, or control device); inventory flags for apps requesting BIND_ACCESSIBILITY_SERVICE / SYSTEM_ALERT_WINDOW / device-admin; Android 12 blocks full-occlusion untrusted touches (opacity ≥0.8) by default; Android 16 `accessibilityDataSensitive` guidance for other apps (not ours to enforce).
8. **Restrictions:** We cannot see which app *currently holds* a11y granted-state — Settings.Secure.ACCESSIBILITY_ENABLED etc. restricted; our detection relies on declaration + Play Integrity windows.
9. **Can observe:** Declarations, a11y-capable app install events, integrity verdict windows.
10. **Can prevent:** Nothing OS-level; we warn early and loudly.
11. **Permission:** QUERY_ALL_PACKAGES + Play Integrity.
12. **Cloud:** None required.
13. **Privileged:** Real-time a11y grant monitoring (impossible for third-party apps).
14. **Impossible:** Blocking an overlay attack in progress inside another app's screen.
15. **Confidence:** Medium-High for flagging a11y-capable apps; UNKNOWN whether actually malicious.
16. **FP:** Legit screen readers/automation — must support allowlisting.
17. **FN:** Abuse without those permissions (IME-based).
18. **Messaging:** Enumerated markers ("accessibility-declared + sideloaded + hidden icon"); never "this app is malicious" on heuristics alone.
19. **Mitigation:** Highest-priority warning tier + education on the attack pattern.
20. **Future:** Deeper Play Integrity signals as Google expands them.

## R. Unexpected device events

1. **Attack name:** Unexpected restart/crash/shutdown events (proxy signals for attacks — forced reboot for unlock attempts, persistence install, kernel exploitation, or just bugs).
2. **Mechanism:** Reboot occurs; attacker may have used the window.
3. **Delivery:** N/A — event observation.
4. **Interaction:** None.
5. **Data at risk:** N/A (observational).
6. **Impact:** Indicator of compromise (weak signal alone).
7. **Detection capabilities:** RECEIVE_BOOT_COMPLETED + BOOT_COUNT (Settings.Global) give boot times; uptime deltas; battery/pull events via system broadcasts; WorkManager resumption anomalies.
8. **Restrictions:** Broadcasts only while process alive or via manifest receivers; no shutdown-time hook guaranteed.
9. **Can observe:** Boot timestamps, count deltas, surrounding integrity verdicts.
10. **Can prevent:** N/A.
11. **Permission:** RECEIVE_BOOT_COMPLETED (normal).
12. **Cloud:** None.
13. **Privileged:** None needed.
14. **Impossible:** Root cause attribution on-device.
15. **Confidence:** Low for *meaning*, High for *occurrence*.
16. **FP:** OEM auto-reboots, low battery, OS updates (we check known-update windows where feasible).
17. **FN:** Silent freezes; reboots before our process records boot (BOOT_COUNT comparison catches them).
18. **Messaging:** "Device restarted at 02:45. This is recorded as an event. We do not claim to know the cause." — factual, no causality invention.
19. **Mitigation:** Incident timeline correlation ("occurred shortly after X"); post-boot integrity self-check.
20. **Future:** Cross-device correlation in V3.

## S. Malware persistence

1. **Attack name:** Persistence mechanisms — device-admin abuse, accessibility persistence, boot receivers, notification-listener persistence, Play-Protect disabling.
2. **Mechanism:** Malware ensures reboot survival; resists uninstall ("cannot uninstall" screens).
3. **Delivery:** Initial infection (via G or F).
4. **Interaction:** Initial install only.
5. **Data at risk:** Persistent surveillance.
6. **Impact:** Long-term compromise.
7. **Detection capabilities:** Inventory of device-admin/a11y/boot-receiver apps (declarations visible); uninstall-difficulty guidance; Play Integrity playProtectVerdict (Play Protect disabled + found-risky-apps state — opt-in verdicts).
8. **Restrictions:** Cannot observe persistence execution; broadcast receivers registered by other apps are visible via inventory but runtime-registered receivers invisible.
9. **Can observe:** Declared persistence capabilities; Play Protect state via verdict.
10. **Can prevent:** Guide removal (device-admin deactivation walkthrough); nothing OS-level.
11. **Permission:** QUERY_ALL_PACKAGES + Play Integrity.
12. **Cloud:** Optional reputation.
13. **Privileged:** None.
14. **Impossible:** Detecting in-memory/fileless persistence.
15. **Confidence:** Medium for declared capabilities; UNKNOWN for behavioral persistence.
16. **FP:** Device-admin MDM, legit apps with boot receivers (common!) — category context required.
17. **FN:** Novel persistence mechanisms.
18. **Messaging:** "App declares device-admin + boot-receiver persistence — used by both legit device managers and spyware. Review requested."
19. **Mitigation:** Risk tiering + uninstall-assist flows.
20. **Future:** Play Protect verdict expansion; behavior research.

## T. Credential harvesting

1. **Attack name:** Credential harvesting — fake login pages, OAuth-app phishing, keylogging via a11y, SMS-OTP interception.
2. **Mechanism:** Credential capture at point of entry (web forms/overlays) or in transit (accessibility keylogging, a11y OTP readers).
3. **Delivery:** All above (A, B, C, E, Q).
4. **Interaction:** User authenticates into attacker-controlled UI or grants read access.
5. **Data at risk:** Primary accounts — email is the keystone risk (password reset hub).
6. **Impact:** Cascade ATO across all services.
7. **Detection capabilities:** Link Guardian heuristics on user-submitted URLs (login-form markers, brand mismatch, new-domain+cert signals); DNS-level blocking of known kits (e.g., domains hosting known phishing kits on feeds); a11y-capable app flags (Q); Pwned Passwords k-anonymity checks (free HIBP API) as hygiene guidance; post-hoc breach hits (I).
8. **Restrictions:** No browser form monitoring; no keystroke visibility (rightly); OTP interception is attacker-domain (SIM swap invisible to apps — carrier domain).
9. **Can observe:** Submitted URLs' characteristics; destinations; a11y-capable app presence.
10. **Can prevent:** Block known kit hosts at DNS; warn on high-risk URL submissions.
11. **Permission:** Sharing/VPN as above.
12. **Cloud:** Phishing-kit feeds, Pwned Passwords (no auth required), breach API (auth).
13. **Privileged:** None.
14. **Impossible:** Detecting a zero-day kit's fake login page before intel catches it, on-device, inside a browser.
15. **Confidence:** High on confirmed feed hits; Low-Medium on pre-impact heuristics.
16. **FP:** Legit new sites with login forms.
17. **FN:** Fresh kits, a11y keyloggers without other markers.
18. **Messaging:** The canonical example: "We detected a login form resembling a known banking service. Evidence: [domain age, redirect chain, form markers]. Action: connection blocked. Confidence: high."
19. **Mitigation:** Link+App+Network Guardian converge on this category; education on email-as-keystone.
20. **Future:** V2 in-app browser credential-check; stealer-log exposure monitoring (HIBP Pro).

---

## Priority summary

Highest product impact for MVP: T, A, Q, G, J (interactive compromise), then E, H, N, S (destination/metadata), then L, R, I, K (integrity/identity), with B, C, D, F, M, O, P addressed through the same engines plus process/policy controls. The correlation engine is what converts these weak individual signals (R alone proves nothing; a11y flags alone prove nothing) into useful incidents — while never overstating causality.
