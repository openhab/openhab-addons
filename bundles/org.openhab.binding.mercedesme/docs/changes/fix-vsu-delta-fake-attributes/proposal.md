# Proposal: Fix Partial VehicleStatusUpdate Fabricating Zero Values for Untouched Attributes

## Intent

Live trace evidence (`VehicleHandler`/`Mapper`/`UOMObserver` TRACE logs from a real EQA, captured
2026-08-03 after toggling HVAC off): a partial (`full_update = false`) `VehicleStatusUpdate` arrives
whose raw `.toString()` dump shows exactly three populated top-level fields
(`precond_now`, `precond_state`, `vtime`) - the server genuinely only reported a preconditioning state
change. The very next log line reads "recived 95 attributes", and dozens of unrelated channels
(SoC, Range, Charge Power, tire pressure, odometer, ...) are logged with
`No Unit found for <key> - take default` and subsequently render as `0` in the UI instead of keeping
their last known value.

Root cause: `Mapper.fromVehicleStatusUpdate(VehicleStatusUpdate vsu)` unconditionally calls a
`putXxx(...)` helper for all 95 known typed fields on every incoming `VehicleStatusUpdate`, regardless
of whether that particular field was actually present on the incoming (possibly partial) message. Every
one of these fields is declared as a singular _message_-typed field in `vehicle-events.proto`
(e.g. `Int64RatioAttribute soc = 196;`, `DoubleAttribute charging_power = 37;`,
`DoublePressureAttribute tirepressure_front_left = 234;`), so proto3 generates a proper `hasXxx()`
presence check for each one - it is simply never consulted. `fromVehicleStatusUpdate()` has zero
`hasXxx()` calls in its ~170-line body (confirmed by inspection).

When a field is genuinely absent, `vsu.getXxx()` returns that field's default instance - `value = 0`,
an unset `VSUMetadata`. `Mapper.baseBuilder(metadata)` sets
`.setStatus(metadata.getStatusValue())`, and an unset `VSUMetadata.status` defaults to
`0 = AttributeStatus.VALUE_VALID`. `Utils.isNil()` (already hardened by
`docs/changes/fix-soc-zero-spikes`) only treats a value as invalid when `hasNilValue() && getNilValue()`
or `status != VALUE_VALID` - both false for these synthetic entries - so the fabricated `value = 0`
passes every existing validity guard as if it were genuinely reported data, overwriting the
previously-known SoC/Range/ChargePower/etc. with `0`. This is a distinct bug from both
`fix-soc-zero-spikes` (which hardened `isNil()` and the `Mapper` numeric branches for values that ARE
present but invalid) and `fix-enum-switch-mismatch` (bool/int_value oneof mismatch) - here, the value
never should have entered the map at all.

The `full_update` flag is correctly threaded through (`AccountHandler.handleMessage()` preserves it via
`VEPUpdate.Builder.setFullUpdate(...)`, and `VehicleHandler.handleUpdate()`'s "recived N attributes -
full update? false" log line confirms it's known at the point of processing), but nothing acts on it -
`fromVehicleStatusUpdate()` runs upstream of that distinction and always emits the same ~95-key map for
both full and delta updates.

## Scope

In scope:

- `Mapper.fromVehicleStatusUpdate()` MUST only add a map entry for a field when
  `vsu.hasXxx()` is `true` for that field, for all ~95 fields currently converted unconditionally
  (bool, plain int64/double, enum, distance, pressure, speed, ratio, clock-hour, consumption, and the
  three complex array-typed fields `temperature_points`, `charge_programs`, `auxheatwarnings`).
- A full `VehicleStatusUpdate` (`full_update = true`) MUST continue to populate all fields the server
  actually sent, unchanged from today - this only removes entries that were never genuinely present.

Out of scope:

- The old `VEPUpdate` (non-typed) push path - it was never affected, since the server only ever sends
  changed attributes in that format.
- Changing how `VehicleHandler`/`Mapper.getChannelStateMap()` interpret an entry once it's in the map -
  those guards (`isNil()`, the Switches `int_value`/`bool_value` handling) are correct and unchanged;
  this fix stops fabricated entries from being created in the first place.
- The 3 complex array-typed fields' internal conversion logic (`putTemperaturePoints`,
  `putChargePrograms`, `putAuxheatwarnings`) - only their presence gating changes, not their bodies.

## Open Questions

- None. `hasXxx()` was confirmed to exist for representative fields across every group (bool, enum,
  distance, pressure, ratio, and the two `*ArrayAttribute` complex types) directly against the generated
  `com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate` class
  (`src/3rdparty/java/com/daimler/mbcarkit/proto/VehicleEvents.java`), not just inferred from the
  `.proto` schema.

---
