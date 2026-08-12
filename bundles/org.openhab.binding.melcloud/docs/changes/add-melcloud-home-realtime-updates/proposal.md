# Proposal: MELCloud Home Push-Accelerated State Sync

## Intent

`MelCloudHomeAccountHandler` currently learns about state changes only from a fixed
`GET /context` poll every `CONTEXT_POLL_INTERVAL_SECONDS` (60s). A change made outside
openHAB — the MELCloud Home app, or a physical remote — is invisible to openHAB Things
for up to 60 seconds after it happens.

The `andrew-blake/melcloudhome` Home Assistant integration solves the equivalent problem
with a WebSocket listener that never becomes a source of truth: it only signals that a
unit's state may be stale and triggers an out-of-cycle REST refresh ("push accelerates,
REST decides" — see that project's ADR-019). Adopting the same pattern narrows the
staleness window for this binding without changing where state values actually come from.

This narrows, but does not close, the stale-cache window described in the companion
change `add-melcloud-home-request-resilience` (see that proposal's Open Questions):
dedup there still compares against whatever `/context` snapshot is currently cached, and
a delta can arrive after a dedup check has already run.

## Scope

In scope:

- A WebSocket connection, owned by `MelCloudHomeAccountHandler`, to MELCloud Home's push
  channel.
- On receiving a delta for a unit with a registered listener, triggering an immediate,
  debounced `/context` poll + fan-out, outside the fixed 60s schedule.
- The existing fixed-interval poll continues unchanged as the baseline/fallback — it does
  not get removed or lengthened.
- A config option to disable the push channel entirely, falling back to fixed-interval
  polling only.
- Automatic reconnect with backoff on WebSocket failure; a WebSocket outage must not
  affect bridge `ONLINE`/`OFFLINE` status.

Out of scope:

- Trusting or parsing delta payload content as a state source — every value still comes
  from `/context`.
- Any change to how control commands are sent (see `add-melcloud-home-request-resilience`).
- Tuning the debounce window beyond a first reasonable default; that can be revisited once
  real traffic is observed.

## Open Questions

- The exact WebSocket handshake (hash exchange endpoint, `wss://` URL contract) is not yet
  confirmed for this codebase. The static reverse-engineering analysis referenced by
  ADR-002 (`reversed.md`) may already cover it; if not, fresh traffic capture is needed
  before `$Architect`/`$Dev` can start.
- Whether this requires a new dependency (an OSGi-compatible WebSocket client) is a
  `pom.xml` question — per project rules, only `$Architect` may propose that, with human
  approval required.
- Debounce window length for the out-of-cycle refresh (avoid hammering `/context` if
  multiple deltas arrive in a burst) — needs an `$Architect` decision, informed by
  whatever pacing `add-melcloud-home-request-resilience` introduces.

---
