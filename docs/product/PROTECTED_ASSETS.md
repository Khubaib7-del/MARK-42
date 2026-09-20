# Protected Assets Model

## Concept

Protected Assets are user-designated entities (apps or identities) whose security events are prioritized, surfaced faster, and never suppressed by notification batching. This models the real risk asymmetry: a phishing site hitting a banking app matters more than one hitting a news app.

## Default categories (suggested, user-customizable)

| Category | Default members (detect by known packages/domains, user-adjustable) | Priority behaviors |
| --- | --- | --- |
| Banking & payments | User's banking apps (detected via package categories; user confirms) | Immediate notification on risk markers touching them; destination-policy warnings |
| Identity anchors | Email app/accounts, Google/Microsoft/Apple accounts | Breach-exposure alerting; 2FA guidance |
| Communication | WhatsApp/Telegram/Email | Warning on overlay-capable installs (Q-category threats) |
| Work | Slack/Jira/GitHub/business apps | Elevated posture sensitivity while flagged |
| Personal | Photos, documents, password manager | Storage-risk + sensor-abuse education |
| Developer/AI | Dev tools, AI apps, cloud consoles | Token/credential-harvesting awareness flows |

## Semantics

- Asset designation is user input (never auto-inferred without confirmation) and itself an event (auditable).
- Risk engine: events affecting protected assets receive a priority multiplier and stricter default thresholds; the multiplier and reasoning are visible in evidence ("elevated because asset is marked BANKING").
- Privacy: designating assets reveals nothing externally; it is local policy input only.
- Honest limit: protected assets change *surfacing and thresholds*, never what we can observe (capability matrix is unchanged).
