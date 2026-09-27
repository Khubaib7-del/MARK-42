# Staged Rollout Plan

## Tracks and gates

| Stage | Audience | Entry gate | Exit gate |
| --- | --- | --- | --- |
| GitHub sideload (now) | Owner + testers | v0.8.0-phase11+ APK attached | Install succeeds via non-Chrome path; honesty limits accepted |
| Play internal testing | Owner + up to 100 testers | Release-signed build, listing + declarations filed | 7-day crash-free ≥ 99.5%, install success on API 26–35 |
| Play closed track | Invited cohort | Internal exit met | False-positive report rate reviewed; battery delta within target |
| Play open testing | Public volunteers | Closed exit met | 14-day crash-free ≥ 99.5%, verdict-accuracy spot checks pass |
| Production | Everyone | Open exit met + release checklist green | Monitored rollout (10% → 50% → 100% with 48h holds) |

## What is measured per stage

Install success (including the Play Protect unknown-developer warning
fading as reputation builds — it cannot be bypassed in code, only earned
through signed, policy-clean releases and install history); crash-free
sessions; VPN battery delta (target < 3%/day overhead); false-positive
verdict reports (each becomes a regression test per incident-response
rules); on-device cold start (< 2 s to posture).

## Rollback

Halted rollout per track; hotfix ships via expedited review with an in-app
notice stating what changed and why. S1/S2 incidents follow
`docs/security/INCIDENT_RESPONSE.md` with a redacted post-mortem in-repo.

## Current position

GitHub-sideload stage. Play stages need OWNER ACTIONS: release keystore,
Play Console listing, declaration forms, and device-captured screenshots
(none of these can be produced from this browser-based sandbox).
