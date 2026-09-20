# ADR-005: Append-only encrypted event store with in-process Flow bus

**Status:** Accepted (Phase 0)

## Context
The incident timeline is the product's memory. Events must be immutable (tamper-evident history), privacy-classified, retention-enforced, and deletable. Engine decoupling requires an internal bus; heavy IPC (AIDL/MQTT-style) is unnecessary inside one process.

## Decision
In-process event bus (Kotlin Flow, buffered, backpressure-safe) feeding an append-only Room/SQLCipher event store. Schema versioned (kotlinx.serialization); corrections are new events, not edits. Retention + deletion are scheduled policy-engine jobs with tested completeness.

## Consequences
- + Auditability; simple crash-recovery story; timeline is a pure query over the store; deletion is tractable.
- − Storage grows until retention trims (bounded per DATA_CLASSIFICATION); write amplification on old devices (measured in Phase 11).
