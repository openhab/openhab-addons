# Proposal: Respect Attribute Validity Status for Percentage Channels

## Intent

Users observe the State-of-Charge channel (and other percentage channels) briefly dropping to 0%
during active charging, producing visible spikes in persisted history/charts. Root cause: the
Mercedes backend occasionally pushes an attribute update whose `status` field is
`VALUE_NOT_RECEIVED` / `VALUE_INVALID` / `VALUE_NOT_AVAILABLE` while the numeric `int_value` is
still at its protobuf default of `0` (the `nil_value` oneof case is not used for this). The
binding's `Utils.isNil()` only inspects the `nil_value` oneof and never looks at `status`, so this
default `0` is forwarded to the channel as if it were a real reading.

## Scope

In scope:

- `Utils.isNil()` MUST also treat a non-`VALUE_VALID` `status` as "no usable value", in addition to
  the existing `nil_value` check. This is the single call site already used as the validity gate by
  most of `Mapper.java` and by position/max-soc handling in `VehicleHandler.java`.
- The "Percentages" branch in `Mapper.getChannelStateMap()` (SoC, tank level, AdBlue level,
  eco-score sub-scores) MUST check `Utils.isNil(value)` before building a `QuantityType` and yield
  `UnDefType.UNDEF` instead, mirroring the pattern already used for door/decklid/tire-pressure
  timestamp attributes in the same switch statement.
- **Added after community report** ([community.openhab.org/t/mercedes-me/136866/199](https://community.openhab.org/t/mercedes-me/136866/199)):
  a user testing 5.3.0.202608011758 observed that right after sending a command (window ventilate,
  door lock), _many_ attributes across different channel types briefly read as zero/default at once
  ("most of the parameters are zero incl. the location data"), not just State of Charge. That is
  consistent with the same root cause hitting a burst of attributes simultaneously: the backend
  marks several attributes `VALUE_NOT_RECEIVED` while a command is in flight, and every branch that
  builds a state from `Utils.getDouble()`/`Utils.getInt()` without checking `Utils.isNil()` first is
  exposed to it. The same report's repeated `Ignoring implausible max-soc limits [0, 0]` trace line
  confirms the pattern independently (see `VehicleHandler.updateMaxSocCommandOptions()`, which
  already uses `Utils.isNil()` and now benefits directly from the widened check).
  The following previously-out-of-scope branches in `Mapper.getChannelStateMap()` are now brought
  into scope, applying the identical `Utils.isNil(value)` → `UnDefType.UNDEF` guard used for
  Percentages:
  - "Kilometer values" (odo, electric/overall/liquid range, trip distance start/reset, eco-score
    bonus range) — currently leaks `-1` (`Utils.getDouble()`'s nil sentinel) as a literal `-1 km`
    reading instead of `UNDEF`.
  - "KiloWatt values" (charging power) and "Average speed" — currently **mask** the nil sentinel via
    `Math.max(0, ...)`, i.e. they show `0 kW` / `0 km/h`, the exact same misleading-zero symptom as
    the original State of Charge bug.
  - "KiloWatt/Hour" and "Litre" consumption (electric/liquid, start and reset) — leaks `-1`.
  - Tire pressure (all four wheels) — leaks `-1`.

Out of scope:

- The "Angle" branch (heading) and the "special String Value"/"Number Status" branches already guard
  with an explicit `< 0` check on the `Utils.getDouble()`/`Utils.getInt()` sentinel, so they already
  behave correctly once `Utils.isNil()` is fixed - no code change needed there.
- `Utils.getDurationString()` / `MB_KEY_DRIVEN_TIME_START`/`RESET` - already guarded, unaffected.
- A latent, unrelated bug spotted while reading the "Average speed" branch: on a unit lookup hit, it
  assigns the resolved unit to `lengthUnit` instead of `speedUnit` (dead write - `speedUnit` stays at
  its default), so a non-default speed unit from the backend is silently ignored. Not touched by this
  change; flagged for a separate fix.
- Door lock direction ("reacts inverted"), state getting stuck until the account thing is
  disabled/re-enabled, and "Wash Water"/"Park Brake" channels staying `UNDEF` until the item is
  reloaded, all also reported in the same forum post. None of these have a confirmed root cause from
  the available log excerpt (the reversed-boolean handling for the per-door lock **status** channels
  is intentional and already correct - see the `MB_KEY_DOORLOCKSTATUSFRONTRIGHT` etc. case; the
  "stuck" state would need a live reproduction with `REFRESH`/relink behavior traced). Left for a
  dedicated change once there's enough evidence to write a testable requirement.
- Map zoom issue from the same forum post - confirmed not a binding problem (Main UI/core), not
  pursued further.

## Regressions Found and Fixed During Implementation

Making the "Percentages" branch return `UnDefType.UNDEF` (this change's original SoC fix) exposed two
pre-existing unguarded `(QuantityType<?>) csm.getState()` casts that previously could never see
anything but a `QuantityType`: `VehicleHandler`'s `OH_CHANNEL_FUEL_LEVEL` handling (tank-remain/
tank-open derived from the fuel-level percent) and `energyUpdate()` (charged/uncharged/energy-to-max
derived from State of Charge / max-SoC). Both would throw `ClassCastException` the first time SoC or
tank level is `UNDEF`, which - given `handleUpdate()` iterates attributes in a single `forEach` with no
per-attribute try/catch - would abort processing of the rest of that update's attributes. Both call
sites now use `instanceof QuantityType<?> ... pattern` and leave the derived channel at its last known
value (fuel level) or push `UnDefType.NULL` (energy figures), matching the existing "no data" branch
already used elsewhere in the same functions.

A second regression was caught by `mvn test` (`VehicleHandlerTest`, four `"Trip Update Count"`
assertions failing 11 vs. 12): the widened-scope edits (Kilometer values, Average speed, Electric/
Liquid consumption, Tire pressure) nested the attribute's unit/`UOMObserver` lookup
(`value.hasXUnit()`) _inside_ the `Utils.isNil()`-false branch. That coupled two independent things -
"is this reading currently valid" and "does this attribute carry unit metadata" - which real vehicle
data contradicts: a BEV reports `liquidconsumptionstart`/`reset` as `nil_value=true` (no combustion
engine) while still carrying `combustion_consumption_unit` (see the test fixtures). With the observer
nested inside the nil-branch, it silently stayed `null` for a nil-but-unit-tagged attribute, which
means `VehicleHandler.updateChannel()`'s `if (deliveredObserver != null)` gate never ran and
`handleComplexTripPattern()` never fired - dropping the companion `*_UNIT` trip channel's update
entirely (not just showing a wrong value - no update at all, which is why the _count_ of distinct
updated channels dropped). Fixed by moving the unit/observer lookup back out so it runs
unconditionally in all five affected cases, independent of `Utils.isNil()` - restoring the original
observer-detection behavior while keeping the `UNDEF` value gating.

## Open Questions

- None. Verified against `AttributeStatus` enum in `vehicle-events.proto` (`VALUE_VALID = 0`,
  `VALUE_NOT_RECEIVED = 1`, `VALUE_INVALID = 3`, `VALUE_NOT_AVAILABLE = 4`) and against real
  `status` values already present in test fixture
  `src/test/resources/proto-json/MB-BEV-EQA-Charging.json`.

---
