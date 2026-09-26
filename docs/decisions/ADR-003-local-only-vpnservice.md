# ADR-003: Network Guardian as a local-only VpnService DNS filter

**Status:** Implemented (Phase 4, 2026-09-26)

**Implementation notes (Phase 4):** realized as a split-tunnel DNS filter — only the route to the
VPN's own DNS address enters the tunnel; all other traffic bypasses Selvard entirely and is never
seen by the app (stronger than the original design's full-tunnel loopback). Blocked names are
answered REFUSED purely on-device (zero network egress); allowed lookups are forwarded unmodified
to the device's current system resolver (read via ConnectivityManager, so filtering never silently
changes who resolves names). Pure parsing/crafting logic (DNS, IP/UDP codec) lives in core and is
unit-tested, including an independently computed IP-checksum golden value. Pending: on-device
verification (no emulator in sandbox), IPv6 DNS bypass detection, battery benchmark (§7), per-UID
attribution (deferred until Phase 5 brings package identity).

## Context
VpnService routes all device traffic through our app. That is simultaneously the product's strongest capability (destination filtering) and its gravest privacy risk: a "security VPN" that tunnels to vendor servers is the exact surveillance-drift pattern the product must avoid. Play policy requires declaration + prominent disclosure; device security apps are a permitted use.

## Decision
Use VpnService **only** as an on-device filtering interface: DNS interception + destination metadata, loopback-forwarded to the OS network. **No remote tunnel endpoint exists in the design.** No TLS interception ever. Per-UID attribution where feasible with honest "unattributed" fallback. Fail-closed while enabled; UI must show PROTECTION OFF immediately when the tunnel is down.

## Consequences
- + No user traffic ever reaches us — verifiable trust claim; no server cost; no latency penalty; offline-capable.
- − Cannot offer "VPN protection" marketing (correctly so); some OEM VPN-stack quirks; battery cost of packet handling must be measured (SECURITY_TESTING §7).
