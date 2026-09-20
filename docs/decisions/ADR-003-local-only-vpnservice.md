# ADR-003: Network Guardian as a local-only VpnService DNS filter

**Status:** Accepted (Phase 0)

## Context
VpnService routes all device traffic through our app. That is simultaneously the product's strongest capability (destination filtering) and its gravest privacy risk: a "security VPN" that tunnels to vendor servers is the exact surveillance-drift pattern the product must avoid. Play policy requires declaration + prominent disclosure; device security apps are a permitted use.

## Decision
Use VpnService **only** as an on-device filtering interface: DNS interception + destination metadata, loopback-forwarded to the OS network. **No remote tunnel endpoint exists in the design.** No TLS interception ever. Per-UID attribution where feasible with honest "unattributed" fallback. Fail-closed while enabled; UI must show PROTECTION OFF immediately when the tunnel is down.

## Consequences
- + No user traffic ever reaches us — verifiable trust claim; no server cost; no latency penalty; offline-capable.
- − Cannot offer "VPN protection" marketing (correctly so); some OEM VPN-stack quirks; battery cost of packet handling must be measured (SECURITY_TESTING §7).
