# Release Checklist (Phase 12 exit gate)

| # | Item | Status | Evidence / owner action |
| --- | --- | --- | --- |
| 1 | JVM unit/integration gates green | DONE | 158/158 `:core:domain:jvmTest`, report `docs/security/PHASE11_MVP_TEST_REPORT.md` |
| 2 | Static analysis + lint clean | DONE | `detekt` clean, `:android:app:lintDebug` clean |
| 3 | APK assembles | DONE | `:android:app:assembleDebug` green, attached to GitHub release |
| 4 | Privacy policy published | DONE | `docs/product/PRIVACY_POLICY.md` |
| 5 | Security contact + disclosure policy | DONE | `SECURITY.md` (+ `INCIDENT_RESPONSE.md` §B4) |
| 6 | Store listing drafted (honest capabilities) | DONE | `docs/product/STORE_LISTING.md` |
| 7 | Play declarations drafted (VPN + inventory) | DONE | `docs/product/PLAY_DECLARATIONS.md` |
| 8 | Staged rollout plan | DONE | `docs/product/ROLLOUT_PLAN.md` |
| 9 | Incident-response dry run | DONE | `docs/security/INCIDENT_RESPONSE_DRY_RUN.md` |
| 10 | Phase 12 release cut | DONE | v0.8.0-phase12, README download link verified live |
| 11 | Release-signed build + Play App Signing | OWNER ACTION | Needs keystore creation in a trusted environment (never this sandbox) |
| 12 | On-device smoke (API 26–35 matrix) | OWNER ACTION | Needs emulator/physical device — Antigravity plan |
| 13 | Device screenshots + feature graphic | OWNER ACTION | Capture during native testing |
| 14 | Play Console listing + declaration filing | OWNER ACTION | After item 11 |
| 15 | External pen test | DEFERRED | No third-party assessment to date — stated in release notes |
| 16 | Dependency vulnerability scan + lockfiles | DEFERRED | CI has gitleaks only — stated in release notes |
| 17 | IPv6 DNS bypass, single-threaded pump, battery benchmark | DEFERRED | Carried product debt — stated in release notes |

Release decision rule: items 1–10 complete; 11–14 are owner-side and block
the Play tracks, not the GitHub-sideload stage; 15–17 are disclosed limits.
