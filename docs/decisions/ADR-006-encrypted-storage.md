# ADR-006: Keystore-held keys + encrypted Room database

**Status:** Accepted (Phase 0) — SQLCipher vs field-level encryption pending Phase 2 review

## Context
The event store and identity vault are sensitive (a seized device must not yield our records). Custom crypto is prohibited; Android Keystore provides hardware-backed keys. Options: Room + SQLCipher (whole-DB), field-level Keystore encryption, or EncryptedFile.

## Decision
Keystore/StrongBox-generated AES-256 master key (non-exportable) wrapping database encryption. Whole-DB via SQLCipher is the primary candidate, **conditional** on a maintenance/security review (community library, third-party dependency); fallback is field-level encryption with the same keyring. EncryptedFile for user exports. `allowBackup=false` on store.

## Consequences
- + Hardware-bound key material; biometric gate composes via Keystore auth-bound keys; no custom crypto.
- − SQLCipher adoption adds a third-party dependency to review (DEPENDENCY_POLICY ledger); worst-case field-level encryption complicates query patterns.
