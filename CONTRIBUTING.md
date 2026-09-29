# Contributing to Selvard

Thank you for your interest in contributing to **Selvard**! We are building a privacy-first, on-device Android security environment that empowers individuals with transparent, evidence-first security without turning the security system into a surveillance system.

## Governing Principles

Before contributing code or documentation, please review our core architectural rules:

1. **Local-First by Architecture**: All analysis, evaluation, and decision logic must execute entirely on the user's device. No telemetry, no background analytics, and no silent network calls.
2. **Never Claim "Safe"**: Absence of detection is never evidence of safety. Our UI and verdict models use honest states ("No known threat", "Open with warning", "Blocked"), never false assurances.
3. **Evidence-First Verdicts**: Every warning and block must be accompanied by explicit, verifiable evidence (e.g., typosquat distance, brand impersonation markers, matched threat feeds).
4. **Pure Domain Logic**: The `:core:domain` module is written in pure Kotlin (Kotlin Multiplatform) and must NEVER depend on Android framework dependencies (`android.*`). All platform evidence collection lives in `:android:app`.
5. **No Fake Features**: We never simulate or fake capabilities that Android restricts or forbids (such as inspecting TLS payloads or intercepting arbitrary app traffic).

---

## Development Setup

### Prerequisites

- **JDK 17 or JDK 21**
- **Android SDK** with:
  - Platform SDK 35 (`platforms;android-35`)
  - Build-Tools 35.0.0 (`build-tools;35.0.0`)
  - Platform-Tools (ADB)
- **Android Studio** (Koala / Ladybug or later)

### Initializing the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/Khubaib7-del/MARK-42.git
   cd MARK-42
   ```

2. Configure your local Android SDK location in `local.properties`:
   ```properties
   sdk.dir=/path/to/your/android-sdk
   ```

3. Run the baseline validation suite:
   ```bash
   # Windows PowerShell
   .\gradlew.bat :core:domain:jvmTest detekt :android:app:lintDebug :android:app:assembleDebug

   # macOS / Linux
   ./gradlew :core:domain:jvmTest detekt :android:app:lintDebug :android:app:assembleDebug
   ```

---

## Quality & Security Gates

Every pull request must pass all continuous integration gates:

- **JVM Unit Tests (`:core:domain:jvmTest`)**: All 158+ pure domain tests must pass.
- **Static Analysis (`detekt`)**: Code must adhere strictly to repository formatting and static analysis rules without suppression.
- **Android Lint (`:android:app:lintDebug`)**: Clean lint run. Note: `QUERY_ALL_PACKAGES` is permitted with explicit Play declaration and is tracked as an intentional warning.
- **Manifest Policy**: No undeclared or prohibited permissions (`ManifestPolicyTest`).
- **Secrets Scan**: Ensure no API keys, credentials, or keystores are committed.

---

## Submitting Pull Requests

1. Create a feature branch from `hoplite/taras-0f2fc809` or `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Commit your changes with clear, descriptive commit messages adhering to conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`).
3. Verify all tests and lint pass locally before pushing.
4. Open a Pull Request detailing the problem solved, testing conducted, and any security boundaries impacted.

---

## Security Vulnerability Reports

If you discover a potential security vulnerability, please do NOT create a public issue. Refer to our [Security Policy](SECURITY.md) and report it privately to **khubaibnazeer8@gmail.com**.
