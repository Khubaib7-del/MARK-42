# Selvard Privacy Policy

Effective 2026-09-27. Applies to Selvard v0.8.0 (Phase 12) sideloaded from
GitHub releases. Developer contact: khubaibnazeer8@gmail.com.

## Principle

Local-first by architecture: analysis happens on your device, findings stay
on your device, and nothing leaves it except the narrow, consented queries
listed below. **There is no analytics SDK, no account, no tracking.**

## What stays on your device (never transmitted)

| Data | Storage |
| --- | --- |
| Link verdicts and evidence (derived; raw URL paths are never stored) | Encrypted store, 180 days |
| DNS destination history while the filter is active | Encrypted store, 90 days |
| App inventory snapshots (changes only) | Encrypted store, until app removed + 90 days |
| Declared identities and breach records | Encrypted vault, until you delete them |
| Consent records | Life of app + deletion receipt |
| Play Integrity verdicts | 30 days |
| Boot and device events | 180 days |
| Crash logs (local only, PII-free by construction) | 7 days |

The store key lives in Android Keystore (StrongBox hardware when available)
and is never exportable. App backup is disabled (`allowBackup=false`), so
recorded data never enters cloud backup.

## What leaves your device, and only with your action

- **Threat-intelligence lookups:** a normalized domain (never the full URL
  path — paths can carry tokens and personal data) is looked up against
  threat feeds. Blocklists themselves are downloaded to the device; absence
  of a provider yields UNKNOWN, never a safety claim.
- **Breach checks:** when you declare an identity and explicitly consent per
  identity, a query goes to Have I Been Pwned (subscription API,
  k-anonymity prefix where the plan permits). The consent screen states
  exactly what the query reveals before you confirm.

## What we deliberately never collect

Message contents, contacts, browsing history, keystrokes, clipboard data,
notification bodies, precise location (never requested), other apps' private
data, raw traffic payloads. The app requests no sensitive Android permission
beyond the DRD §9 budget (network filter, notifications, package inventory,
boot events).

## Your rights

Settings → delete your recorded data any time: the store and every declared
identity are wiped and the app shows a receipt stating exactly what was
removed. Shorter retention is always available; longer retention never
happens without your explicit consent for a stated feature.

## Children

Selvard is a general-audience security tool. It collects no data that would
enable child-directed profiling; breach checks run only on identities you
declare yourself.

## Changes

Material changes ship with the release notes and, where consent-relevant,
an in-app notice before the new behavior activates. The history of this
file is version-controlled in the public repo.
