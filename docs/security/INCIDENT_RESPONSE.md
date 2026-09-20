# Incident Response

## A. Product security incidents (our product is the subject)

| Scenario | Procedure |
| --- | --- |
| Discovered vulnerability in the app | Triage severity -> private advisory -> patch -> coordinated disclosure + advisory publication; Play review-track expedite where applicable |
| Malicious/compromised dependency | Freeze affected builds; identify shipped versions; ship removal/fixed build; audit for behavior; disclosure |
| Leaked API key / signing material | Revoke+rotate immediately; assess blast radius (client keys are per-install and wrapped — see SECURITY_ARCHITECTURE §3.3); log rotation in timeline; public notice if user-impacting |
| TI provider compromise (malicious verdicts) | Kill provider in config flip (provider abstraction exists for exactly this); fall back to local feeds + UNKNOWN semantics; post-mortem with provider |
| Backend compromise (if backend ships) | Clients treat backend as untrusted by design (ADR-007) — verify no privilege existed; rotate transport keys; notify |
| User data leak from our systems | Stated scope is tiny by design (see PRIVACY_ARCHITECTURE); identify what existed; breach notification per applicable law; publish report |
| False-positive security event (user-impacting) | Fix verdict path; add regression test from the report; public handling — honesty about errors is brand-defining for this product |
| False-negative discovered | Feed/heuristic gap analysis; improve; document in threat model revision |

## B. Process rules

1. Severity ladder: S1 (active harm/leak) -> response immediately; S2 (potential) -> 72h plan; S3 -> next release.
2. Security updates follow a controlled release process: expedited review track, in-app notice, changelog states what changed and why.
3. All S1/S2 incidents produce a written post-mortem stored in the repo (redacted) — blameless, engineering-focused.
4. A security contact and disclosure policy page are published at first public release.

## C. In-app incident handling (user-facing model)

In-app "incidents" are correlated event clusters (Incident Engine). Rules: incidents carry evidence, actions, confidence; correlation language is "occurred shortly after" unless causal proof exists; users can dismiss false positives, which become training data for heuristic tuning (locally, and only shared with consent as non-content statistics, if ever).
