# Proposal: MELCloud Home Request Pacing and Command Deduplication

## Intent

Every MELCloud Home control command today goes straight from `handleCommand()` to a
`PUT /monitor/ataunit/{id}` or `PUT /monitor/atwunit/{id}` call via
`MelCloudHomeApiClient`, with no spacing between calls and no check for whether the call
is redundant. An openHAB rule that sets several channels on the same unit at once (mode,
temperature, fan speed) fires one API call per channel in quick succession — the same
burst pattern that caused the `andrew-blake/melcloudhome` Home Assistant integration to
hit MELCloud rate limits (see that project's ADR-018). That project's fix was two
complementary layers: a minimum-interval pacer between API calls, and deduplication that
skips a control call when the requested value already matches the last known state.

Adopting both reduces rate-limit risk for this binding under the same usage pattern,
without changing the poll interval chosen in the existing `home-account` bridge
(`CONTEXT_POLL_INTERVAL_SECONDS = 60`).

## Scope

In scope:

- A shared, per-bridge minimum-interval pacer wrapping every outbound
  `MelCloudHomeApiClient` call (both the `/context` poll and unit control calls), since
  the rate limit is account-level, not per-unit.
- Deduplication in the ATA/ATW unit handlers: before sending a control `PUT`, compare the
  requested field values against the last known unit state (the most recent `/context`
  fan-out for that unit) and skip the call if every field already matches.
- Always sending the call when no prior known state exists yet for the unit (e.g.
  immediately after startup, before the first poll has completed).

Out of scope:

- Exposing the pacing interval as user-facing Thing configuration; start with a fixed
  constant, informed by the HA project's chosen value (0.5s) pending confirmation of this
  binding's own rate-limit behavior.
- Solving the stale-cache trade-off this creates: the state a dedup check compares against
  is only as fresh as the last `/context` poll or push-triggered refresh. If a user changes
  a unit out-of-band (app, remote) and then issues the same value from openHAB before the
  cache catches up, dedup will silently drop that command. This is the same trade-off HA's
  ADR-018 documents, and `add-melcloud-home-realtime-updates` narrows the window but does
  not close it. Ship that change first or alongside this one, not instead of it.

## Open Questions

- Confirmed pacing interval: no MELCloud Home rate-limit threshold is documented for this
  binding yet. Default to the HA project's 0.5s as a starting point unless `$Architect`
  decides otherwise.
- Whether the pacer is a new shared component owned by `MelCloudHomeAccountHandler` (and
  handed to unit handlers alongside the existing `getAccessToken()`/`getApiClient()`
  pattern) or lives inside `MelCloudHomeApiClient` itself — an OSGi service-boundary /
  threading-model decision for `$Architect`.
- Whether dedup silently dropping a command should be logged at `debug` only (matching
  existing logging conventions in `MelCloudHomeAccountHandler`) or surfaced more visibly —
  flag for `$Architect`/`$QA`.

---
