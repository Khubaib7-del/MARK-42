# ADR-006: Keystore-held keys + encrypted Room database

**Status:** Accepted (Phase 0) — **resolved in Phase 2: field-level AES-256-GCM, no SQLCipher**

## Context
The event store and identity vault are sensitive (a seized device must not yield our records). Custom crypto is prohibited; Android Keystore provides hardware-backed keys. Options: Room + SQLCipher (whole-DB), field-level Keystore encryption, or EncryptedFile.

## Decision
Keystore/StrongBox-generated AES-256 master key (non-exportable) wrapping database encryption. Whole-DB via SQLCipher is the primary candidate, **conditional** on a maintenance/security review (community library, third-party dependency); fallback is field-level encryption with the same keyring. EncryptedFile for user exports. `allowBackup=false` on store.

## Consequences
- + Hardware-bound key material; biometric gate composes via Keystore auth-bound keys; no custom crypto.
- − Field-level encryption complicates query patterns (compensated: index plaintext category/timestamp columns, keep ciphertext in a single payload column).

## Phase 2 resolution (2026-09-21)

Implemented as **field-level AES-256-GCM** over the canonical event JSON (`EventPayloadCrypto`, JCA `AES/GCM/NoPadding`, 12-byte IV, 128-bit tag), keyed by the Android Keystore master key (`KeystoreKeyring`, StrongBox where available). SQLCipher was **not adopted**: it would add a third-party C-layer dependency whose maintenance burden outweighs the benefit while the threat model is satisfied by per-record authenticated encryption (tamper evidence + hardware-held key). Decision is revisitable if whole-database encryption becomes a requirement (e.g., cross-row queries over encrypted fields in Phase 8).
