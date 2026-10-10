<div align="center" style="background: linear-gradient(135deg, #071f24 0%, #0b4f4a 46%, #8fe388 100%); border-radius: 24px; padding: 20px; margin: 0 auto 28px;">
  <img src="assets/brand/banner.webp" alt="Selvard — A privacy-first Android security environment" width="960" style="display: block; width: 100%; max-width: 960px; height: auto; border-radius: 16px;">
</div>

<div align="center">

# Selvard (The Self-Warden)
**A Privacy-First, Local-First On-Device Android Security Shield**

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--35)-brightgreen?logo=android&logoColor=white)](android/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21_Multiplatform-purple?logo=kotlin&logoColor=white)](core/domain/)
[![Architecture](https://img.shields.io/badge/Architecture-Local--First-0A6C60)](docs/ARCHITECTURE.md)
[![Build Status](https://img.shields.io/badge/Build-Passing_(178%2F178_Tests)-success)](docs/product/UI_REDESIGN_VALIDATION.md)
[![Static Analysis](https://img.shields.io/badge/Detekt-Clean-3E8E9E)](config/detekt/detekt.yml)
[![Accessibility](https://img.shields.io/badge/A11y-WCAG_2.1_AA-blue)](docs/DRD.md)
[![License](https://img.shields.io/badge/License-Proprietary-darkgray)](LICENSE)

*Selvard* means **the self-warden**. Link heuristics, permission review, and sample DNS filtering run on your device. Optional identity exposure checks contact HIBP with separate consent.

Current source review: [implemented preview scope](docs/product/MVP_SCOPE.md) and [9 October engineering review](docs/product/PROJECT_REVIEW_2026-10-09.md). Phase completion records do not replace the remaining device and release checks.

[**⬇️ Download Latest APK**](#-download--sideload-guide) • [**✨ Key Features**](#-core-guardians) • [**⚠️ Download & Install Help**](#-install-help--troubleshooting) • [**🤝 Contributing**](CONTRIBUTING.md) • [**📜 Code of Conduct**](CODE_OF_CONDUCT.md)

[**Download `selvard-v0.9.2-ui-polish.apk` (v0.9.2, UI polish)**](https://github.com/Khubaib7-del/MARK-42/releases/download/v0.9.2-ui-polish/selvard-v0.9.2-ui-polish.apk) — sideload on any Android 8.0+ device. This debug test build adds the reviewed onboarding, guardian UI polish, smoother motion, local runtime diagnostics, and VPN lifecycle fixes. The release notes list its remaining limits. Download stuck at 100%, or Play Protect blocking the install? See [install help](docs/product/INSTALL_HELP.md).

</div>

---

## 💡 Why Selvard?

Security software can receive sensitive access to a device. Selvard prioritizes local analysis, minimal collection, explicit consent for external identity checks, and visible capability limits.

**Selvard operates under an absolute governing principle:**
> *The security system must never become the surveillance system.*

- 🔒 **Zero Cloud Telemetry**: No Google Analytics, no telemetry SDKs, no user accounts, no tracking.
- 🛡️ **Local Analysis**: Sample DNS filtering, app permission heuristics, and link analysis run locally on your phone. Optional HIBP identity checks contact an external provider with consent.
- ⚖️ **Honest Limits**: We never claim your device is "100% Safe" (absence of evidence is not evidence of safety). We never pretend to have permissions or capabilities Android forbids.
- 💎 **Curvy Liquid-Glass Experience**: Clean, approachable, modern UI with interactive status beacons and CAD blueprint launch animations designed for human beings, not hacker consoles.

---

## 📦 Download & Sideload Guide

### Latest Release
👉 [**Download `selvard-v0.9.2-ui-polish.apk` (v0.9.2, UI polish)](https://github.com/Khubaib7-del/MARK-42/releases/download/v0.9.2-ui-polish/selvard-v0.9.2-ui-polish.apk)
- **File size**: ~16.9 MB
- **Target OS**: Android 8.0 Oreo (API 26) through Android 15 (API 35)
- **SHA-256**: `178671d92500b86c4b8df64d6148d81f2b39c9003828811838b0a1e5be397b08` (also attached to the release as `selvard-v0.9.2-ui-polish.apk.sha256`).

---

## 🛠️ Install Help & Troubleshooting

Full write-up with sources and what we did and did not verify: [`docs/product/INSTALL_HELP.md`](docs/product/INSTALL_HELP.md).

### 1. Download stuck at 100% (10.33 MB / 10.33 MB)?
- **Most likely cause**: the link was opened inside an in-app tab (Chrome Custom Tab) from another app such as GitHub mobile or WhatsApp. Downloads started there are known to hang at 100%. The file GitHub serves is complete and correctly labeled.
- **Fix**: in the tab's `⋮` menu tap **Open in Chrome** (or paste the link into Chrome/Firefox/Brave directly) and download again. Then open Chrome `⋮` > **Downloads**; if it shows a "might be harmful" warning for this APK, tap **Keep** / **Download anyway**.
- **Other routes**: download on a computer and copy the APK to the phone, or use `adb install -r`. Compare the file with the release's `.sha256` to confirm it is intact.
- We have not reproduced the stall on a device, so the exact cause is unconfirmed; the install-help page says so.

### 2. "App blocked to protect your device" (Play Protect)?
- **Why**: Play Protect has not seen this developer before. Selvard builds are signed with the generic Android *debug* certificate for now. That is expected for pre-release software, and it is neither a verdict that the app is harmful nor evidence that it is safe.
- **Install**: tap **More details** > **Install anyway**. If only **Got it** is offered, see the install-help page for the Play Protect setting.
- **Permanent fix**: a stable owner-held release signing key plus Android developer registration (details and dates in the install-help page).

---

## 🛡️ Core Guardians

| Guardian | What It Does On-Device | What It Never Does (Honest Limits) |
| :--- | :--- | :--- |
| 🔗 **Link Guardian** | Analyzes shared/pasted URLs for typosquats, brand impersonation, punycode/IDN homoglyphs, and local threat lists. | Never intercepts in-browser clicks silently (Android forbids it; user-initiated only). |
| 🌐 **Network Guardian** | Local split-tunnel DNS filter via `VpnService`. Refuses bundled sample matches locally; allowed lookups use the configured resolver. | Other traffic bypasses Selvard. No remote VPN server or encrypted HTTPS inspection; IPv6/encrypted DNS coverage remains limited. |
| 📱 **App Guardian** | On-demand inventory audit of declared permissions, dangerous stalkerware combos (e.g. SMS + Notifications), and legacy target SDKs. | Never reads other apps' internal data or granted runtime states. Results are risk signals, not malware verdicts. |
| 👁️ **Privacy Monitor** | Tracks observable system facts: package install/update/removal timelines, permission-change snapshots, and reboot timestamps. | Explicitly labels sensor use (mic/camera/GPS) as `NOT MONITORED` (Android OS owns sensor dots). |
| 🔐 **Identity Vault** | Keystore-backed encrypted identity vault with consented Have I Been Pwned (HIBP) breach queries. | Separate full-address consent; requires a subscription key. Biometric gating is deferred; no match does not mean safe. |
| ⏱️ **Incident Timeline** | Groups security events by temporal proximity into clear incident streams. | Never invents false causality. Uses honest *"occurred shortly after"* language. |

---

## 🏗️ Architecture & Technical Foundation

Selvard is structured strictly as a clean multi-module architecture:

```
├── core/domain      # Pure Kotlin Multiplatform domain logic (178 JVM unit tests)
│                    # Zero Android framework dependencies (no android.*)
├── android/app      # Evidence collectors, Jetpack Compose UI, Room database
├── assets/brand     # Brand identity, SVG vector marks, launcher icons
├── docs/            # Engineering architecture, PRD, DRD, and threat models
└── config/detekt    # Strict static analysis rules
```

### Verification Suite
Selvard enforces continuous verification gates across every commit:
```bash
# Run pure domain tests (178/178 passing in the 9 October review)
.\gradlew.bat :core:domain:jvmTest

# Run static analysis (zero warnings)
.\gradlew.bat detekt

# Run Android lint
.\gradlew.bat :android:app:lintDebug

# Assemble debug APK
.\gradlew.bat :android:app:assembleDebug
```

---

## 🎨 Visual Identity & Brand

<div align="center" style="background: linear-gradient(135deg, #f4fff1 0%, #d6f5ce 45%, #79d68a 100%); border-radius: 20px; padding: 22px; margin: 24px auto;">
  <img src="assets/brand/logo-live.webp" alt="Selvard primary logo" width="360" style="display: block; width: min(100%, 360px); height: auto; margin: 0 auto; border-radius: 14px;">
</div>

- **The mark**: two dark ribbons enclosing a green core, supplied by the project owner and used unmodified as the primary logo. The launch animation is built from this logo ([`docs/brand/BRAND_IDENTITY.md` §3d](docs/brand/BRAND_IDENTITY.md); [preview](docs/brand/launch-sequence.webp)). The earlier keystone-arch vector sketch is retained only as a secondary mark.
- **Palette**: Deep Ink (`#0C1322`), Verdant Green (`#0A6C60`), Signal Green (`#54E454`), and Stillwater Teal (`#3E8E9E`).
- Full brand specification: [`docs/brand/BRAND_IDENTITY.md`](docs/brand/BRAND_IDENTITY.md).

---

## 📄 License & Community

- **License**: Proprietary Software. All rights reserved. See [`LICENSE`](LICENSE).
- **Code of Conduct**: Contributor Covenant v2.1. See [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).
- **Contributing**: Community contribution guide and setup instructions. See [`CONTRIBUTING.md`](CONTRIBUTING.md).
- **Security Policy**: Vulnerability disclosure protocol. See [`SECURITY.md`](SECURITY.md).
- **Developer Contact**: [khubaibnazeer8@gmail.com](mailto:khubaibnazeer8@gmail.com).
