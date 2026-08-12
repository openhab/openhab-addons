# ADR-009: Missing Value in MELCloud Home API Response Maps to `UNDEF`

## Status

Accepted

## Context

Several MELCloud Home ATA/ATW channels are backed by optional fields: a
`settings` entry that may be absent from the unit's `/context` payload (e.g.
`OutdoorTemperature` — confirmed absent for several real ATW units and
firmware states, see the `home-account`/`home-*-unit` handlers' investigation
history), an energy/COP telemetry value the API has no data for yet, or a
`GET /report/v1/trendsummary` reading with no genuine (non-synthetic)
datapoint in the queried window.

Until now, `MelCloudHomeAtaUnitHandler#onAtaUnitUpdated` and
`MelCloudHomeAtwUnitHandler#onAtwUnitUpdated` (plus their telemetry-polling
counterparts) read these values through `Optional`/nullable accessors and
called `updateState(...)` only when a value was present
(`optional.ifPresent(value -> updateState(...))` or `if (value != null) {
updateState(...) }`). When the value was absent, no `updateState` call
happened at all. In openHAB, skipping the call has two different effects
depending on history:

- Before the first successful reading, the linked Item stays at openHAB's
  built-in `NULL` state (uninitialized).
- Once a channel has received at least one real value, a later "missing"
  response leaves the Item silently showing the last known value — which may
  by then be stale or simply wrong (e.g. a unit that used to report outdoor
  temperature and then stops).

Neither case actively tells the user "there is currently no value for this",
which is exactly what openHAB's `UnDefType.UNDEF` exists for.

## Decision

- Convention: whenever a channel's source value is absent from the MELCloud
  Home API response (an empty `Optional` from a DTO accessor, or a `null`
  from a telemetry/trend lookup), the corresponding channel is updated with
  `UnDefType.UNDEF` instead of being left untouched.
- Implemented via `Optional#ifPresentOrElse(consumer, () -> updateState(CHANNEL, UnDefType.UNDEF))`
  (replacing the previous `ifPresent(consumer)`), and the equivalent
  `if (value != null) { ... } else { updateState(CHANNEL, UnDefType.UNDEF); }`
  shape for nullable fields (`rssi`, energy/COP telemetry).
- Scope: this covers fields that are genuinely _absent from the JSON_. It
  does **not** cover the separate case of a value being _present but
  unrecognized_ by this binding's word/code lookup tables (unknown fan
  speed, vane position, or operation mode word) — those keep their existing
  "log at debug, leave the channel untouched" behavior, since an unmapped
  word is a different failure mode (a binding gap) than a genuinely missing
  reading, and silently flipping such channels to `UNDEF` on every poll
  could mask a mapping bug that should surface in the logs instead.
- Applies to: `MelCloudHomeAtaUnitHandler` (`setTemperature`, `roomTemperature`,
  `fanSpeed`, `vaneHorizontal`, `vaneVertical`, `errorCode`, `energyConsumed`,
  `outdoorTemperature`, `rssi`) and `MelCloudHomeAtwUnitHandler`
  (`setTemperatureZone1`/`2`, `roomTemperatureZone1`/`2`,
  `operationModeZone2`, `setTankWaterTemperature`, `tankWaterTemperature`,
  `outdoorTemperature`, `errorCode`, `rssi`, `energyConsumed`,
  `energyProduced`, `cop`).

## Consequences

### Positive

- A missing reading is now visible and actionable in rules/UI (`UNDEF` is a
  first-class, testable state) instead of silently showing a stale or
  never-set value.
- Consistent, documented behavior across both unit types — see the
  "MELCloud Home Things" channel section in the README.

### Negative

- A channel that flaps between "value present" and "value absent" across
  polls (e.g. a unit intermittently reporting `OutdoorTemperature`) will now
  also flap between a number and `UNDEF`, which adds extra state-change
  events/persistence entries that did not exist before.
- Rules or item scripts that assumed "no update happened" as their signal
  for "nothing changed" must be reviewed; they will now receive an explicit
  `UNDEF` state change instead of silence.
