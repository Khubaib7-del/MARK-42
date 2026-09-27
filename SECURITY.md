# Security Policy

## Supported versions

| Version | Supported |
| --- | --- |
| 0.8.x (Phase 12, pre-Play release) | Yes — latest GitHub release only |
| < 0.8.0 | No — update to the latest release |

## Reporting a vulnerability

Email **khubaibnazeer8@gmail.com** with subject `[SELVARD-SECURITY]`.
Include: app version, Android version and device, steps to reproduce, what you
expected versus what you saw. Do not open public issues for suspected
vulnerabilities; they will be triaged privately first.

Response targets follow `docs/security/INCIDENT_RESPONSE.md`: S1 (active
harm or leak) immediately; S2 (potential harm) a 72-hour plan; S3 next
release. Every S1/S2 produces a written post-mortem stored in this repo
(redacted, blameless, engineering-focused).

## Scope honesty

In scope: on-device verdicts, the local DNS filter, the encrypted event
store, consent and disclosure flows, exported-component surface.
Out of scope: capabilities Selvard never claims (TLS content, granted
permission state of other apps, sensor use by other apps — see
`docs/product/MVP_SCOPE.md`), social-engineering the maintainer, or attacks
requiring physical device access.

## No bug bounty

Selvard is a self-funded pre-release project: no paid bounty. Reporters are
credited in the release notes with their consent.
