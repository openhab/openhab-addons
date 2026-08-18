# ADR-011: Expose ATW Cloud Schedule Management via ThingActions, Not Channels/Items

## Status

Proposed

## Context

`docs/changes/add-melcloud-home-schedule-management/` scopes list/create/update/delete of
an ATW unit's cloud schedule entries, plus a per-unit schedule master enable/disable.

Every existing MELCloud Home channel models a single scalar value (a temperature, a mode,
a switch) that maps cleanly onto one openHAB Item. A unit's schedule is structurally
different: a variable-length list of entries, each itself a small record (`id`, `days`,
`time`, `power`, per-zone temperatures/modes, tank temperature, forced-hot-water flag).
There is no natural single-value Channel/Item pairing for "the list of schedules," and
representing each possible entry slot as its own set of Channels would require a fixed,
arbitrary upper bound on entry count that the API does not impose.

More importantly, create/update/delete are imperative operations with multiple named
parameters (e.g. create needs `days`, `time`, `power`, `operationModeZone1`,
`setTemperatureZone1`, ... all at once, not one field at a time the way `handleCommand`
processes a single channel command today). Modeling this as Channel writes would mean
either one channel per field (losing the atomicity of "these fields together become one
schedule entry") or inventing a serialized-string protocol for a single "create schedule"
channel, both worse fits than openHAB's existing mechanism for exactly this shape of
problem: `ThingActions`.

## Decision

Schedule list/create/update/delete/enable are implemented as `ThingActions` on the
`atw-unit` Thing (an `@ActionInput`/`@ActionOutput`-annotated action class, per the
pattern openHAB core documents for binding actions), not as Channels or Items.

Planned action signatures (to be finalized once
`docs/changes/add-melcloud-home-schedule-management/proposal.md`'s Open Questions are
resolved by `$Spec`/`$Architect`):

- `listSchedules()` → the unit's current schedule entries
- `createSchedule(days, time, power, operationModeZone1, setTemperatureZone1, ...)` → the
  new entry's generated `id`
- `updateSchedule(id, ...)` → success/failure
- `deleteSchedule(id)` → success/failure
- `setSchedulesEnabled(enabled)` → success/failure

This is scoped to the `atw-unit` Thing type only — no channel-level changes to
`mel-cloud-home-atw-unit.xml` or `channels.xml` are needed for this change.

## Consequences

### Positive

- Each schedule write is atomic (all fields of one entry submitted together), matching
  the underlying API's own request shape.
- No arbitrary limit on schedule entry count, and no channel-count bloat for a feature
  most users won't touch.
- Rules access schedule management the same way they already access other binding
  actions (`actions.get(...).createSchedule(...)`), a familiar pattern for openHAB rule
  authors.

### Negative

- ThingActions are less discoverable than Channels for UI-only users (no dashboard
  widget without a rule); this is an accepted trade-off given the feature's
  automation-first audience (see this change's proposal, Intent).
- `listSchedules()`'s return shape needs its own DTO-to-rule-value mapping (likely a
  `List<Map<String, Object>>` or similar), since ThingActions don't have direct access to
  openHAB's typed Item/State system the way Channels do.
