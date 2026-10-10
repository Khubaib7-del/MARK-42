# UI redesign and source review validation

Date: 9 October 2026 (Asia/Karachi). Package: app.selvard. The reviewed source was subsequently published as version 0.9.2-ui-polish / versionCode 11. The release APK is identified by its filename and attached SHA-256 checksum.

## Build and source checks

Ran the existing offline Gradle toolchain:

```powershell
.\gradlew.bat :core:domain:jvmTest :android:app:assembleDebug detekt :android:app:lintDebug --offline
```

- 178 JVM domain tests, zero failures. Five added tests cover independent deletion receipts and cancellation.
- Debug APK assembles. Both Android and domain Detekt tasks pass.
- Android lint: zero errors, five warnings: OldTargetApi, DataExtractionRules, QueryAllPackagesPermission, ObsoleteSdkInt (launcher resource qualifier), MonochromeLauncherIcon. These remain visible; no new baseline suppressions were added.
- Python review scripts compile; git diff --check reports no whitespace errors.
- Existing owner changes were preserved. No external telemetry, new dependency, remote VPN endpoint, or permission was added.

## Emulator observations

Android 14 / API 34, existing selvard_test AVD, 1080 x 2400. A cold-boot software-graphics session was used after the original emulator disconnected. During boot, Android's System UI and several system services had ANRs; that is documented rather than counted as an app pass. The System UI dialog was dismissed with Wait before continuing.

The screen walkthrough captured all three onboarding pages and every destination: Home, Links, Apps, Network, Privacy, Identity, Timeline, Settings. One early home capture preceded the transition and showed onboarding; use home-after-idle or home-final for Home evidence.

A 150-second Home idle check kept PID 4216 unchanged and the Android crash buffer empty. This exceeds the reported 60–90 second window on this emulator. Historical exit records include older October 1 crashes and package updates; they are not failures of this run.

Additional checks and final motion evidence are listed in review/polish-2026-10-09/quality-results.json and its screenshots/video. Large text exposed a wrapping bottom-navigation label; maxLines=1 with ellipsis and the full semantic description correct it. Fullscreen mode exposed Android's introductory fullscreen overlay; removing the immersive window mutation keeps system bars and geometry stable. The final recording is intro-final.mp4.

The active-VPN test kept PID 6786 stable for 120 seconds, retained the running state, and then stopped through the real switch. Diagnostics rendered local exit reasons; the deletion dialog was cancelled; Back from Settings returned to Home; a package scan completed; 150% text and landscape were captured; normal and disabled-motion intro replay returned to Home. The final intro was recorded again after removing fullscreen mode, its frames were visually reviewed, and the crash buffer was empty. A full log-export overwrite failed once in the test harness; final logs were collected again with a bounded 3,000-line tail.

Delivered debug artifact: review/selvard-ui-review-2026-10-09.apk (18,199,699 bytes), SHA-256:

```text
25c8a79a388be4bb3ac0566bd114a5f8074da060f1be53b3ee5053108445217f
```

## Limits

This is a local engineering/visual review, not an external penetration test, full accessibility certification, network-coverage proof, or battery/latency benchmark. The owner's phone and original stack trace were unavailable. The fixes to inspected identity/share-target failure paths and emulator stability results do not establish the cause of that phone's crash.

No real identity was added and no HIBP provider request was made during testing. A destructive-confirmation dialog was opened and cancelled; recorded data was not cleared through it. Display settings changed for screenshots were restored. VPN test consent is confined to the emulator.

Outstanding engineering findings and current feature limits are documented in PROJECT_REVIEW_2026-10-09.md and MVP_SCOPE.md. Automatic retention wiring, filtered store queries, atomic vault persistence, production intelligence, and release gates remain open.
