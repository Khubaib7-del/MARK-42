# Incident Response Dry Run (Phase 12)

Tabletop drill against `INCIDENT_RESPONSE.md`, run 2026-09-27 over the
current tree. Two scenarios walked end to end.

## Scenario A (S2): user reports a false-positive BLOCK on a benign domain

- Detection: store-listing "report a verduct" path → maintainer email
  (`SECURITY.md`).
- Triage: reproduce with `LinkGuardian.analyze` on JVM; classify against
  the accuracy corpus (`LinkGuardianAccuracyTest`); severity S2 (potential
  user harm: blocked legitimate access).
- Response: heuristic/feed fix + regression test from the report (drill
  verified this loop is real — the Phase 11 `bbc`→`hsbc` distance-2 false
  positive was found and fixed exactly this way, with the corpus gate
  updated to hold the fix).
- Disclosure: fixed build + release note stating the error honestly
  (honesty about errors is brand-defining per the response plan).
- Drill gap found and fixed: no published security contact existed, so a
  reporter had no channel — `SECURITY.md` was created in this phase.

## Scenario B (S1 walkthrough): suspected Keystore key compromise

- Triage: S1 — immediate response. Blast-radius assessment per
  SECURITY_ARCHITECTURE §3.3: keys are per-install, hardware-bound,
  non-exportable; server-side blast radius is nil by design (no backend
  holds keys).
- Response: rotate affected material, ship patched build on the expedited
  Play review track once enrolled, in-app notice, redacted post-mortem
  stored in-repo.
- Drill gap noted (owner action): expedited-track access and the release
  keystore do not exist yet — both are Phase 12 owner actions in
  `docs/product/RELEASE_CHECKLIST.md` (items 11, 14).

## Verdict

Dry run complete. The response loop demonstrably works for verdict bugs
(Scenario A ran for real in Phase 11); the S1 path is procedural until
Play enrollment exists.
