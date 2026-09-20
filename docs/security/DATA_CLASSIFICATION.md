# Data Classification and Retention

## Classes

| Class | Definition | Examples in this product |
| --- | --- | --- |
| PUBLIC | No harm if disclosed | Blocklist entries, verdict taxonomy, policy names |
| LOW_SENSITIVITY | Mild harm | App package names in inventory, feed provider names |
| SENSITIVE | Personal, harmful if disclosed | Incident timeline, destination history (DNS), risk assessments |
| HIGHLY_SENSITIVE | Direct personal impact | Declared identities (emails), breach-exposure records, per-app network attribution |
| SECRET | Never stored in code/plain | API keys, DB master keys, signing material (Keystore/CI only) |

## Retention policy

| Data class / type | Retention | Rationale |
| --- | --- | --- |
| Link verdict events (derived evidence, no raw URL path) | 180 days | Useful history; no long-term need |
| Network destination events | 90 days | Correlation window; staleness limits surveillance value of our own store |
| App inventory snapshots (changes only) | Until app removed + 90 days | Signal value; inventory is re-derivable |
| Identity vault + breach records | Until user deletes | User owns it; deletable anytime |
| Consent events | Life of app + deletion receipt | Legal/audit trail |
| Play Integrity verdicts | 30 days | Point-in-time signal |
| Boot/device events | 180 days | Correlation history |
| Diagnostics exports | Never stored after export completes | Egress minimization |
| Crash logs (local only) | 7 days, PII-free by construction | Debuggability vs privacy |

Defaults are user-tightenable (shorter always allowed; longer never without explicit consent for a stated feature). Retention enforcement is tested (Phase 2, Phase 11).

## Storage mapping

Everything SENSITIVE and above lives in the encrypted store (Keystore-held key). Nothing SECRET lives in the repo or app binary. Raw message content, page content, or file content never enters storage — by schema, the event model has no field that can carry them (DRD §3 schema validation enforces this).
