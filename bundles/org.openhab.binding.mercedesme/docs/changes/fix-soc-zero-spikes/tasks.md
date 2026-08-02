# Tasks: Respect Attribute Validity Status for Percentage Channels

## 1. Utils

- [ ] 1.1 In `Utils.isNil()`, also return `true` when
      `value.getStatus() != VehicleEvents.AttributeStatus.VALUE_VALID_VALUE`, in addition to the
      existing `nil_value` check. (Scenarios: nil_value flag set, status not received, status
      invalid, status not available, status valid)

## 2. Mapper

- [ ] 2.1 In the "Percentages" branch of `Mapper.getChannelStateMap()` (`MB_KEY_SOC`,
      `MB_KEY_TANKLEVELPERCENT`, `MB_KEY_ADBLUELEVELPERCENT`, `MB_KEY_ECOSCORE_ACCEL`,
      `MB_KEY_ECOSCORE_CONSTANT`, `MB_KEY_ECOSCORE_COASTING`), check `Utils.isNil(value)` first and
      set `state = UnDefType.UNDEF` when true, else keep the existing `QuantityType` construction.
      (Scenarios: SoC update without usable value during charging, SoC update with a valid value)

## 3. Tests

- [ ] 3.1 Add `UtilsTest` cases: `isNil()` returns `true` for `status = VALUE_NOT_RECEIVED`,
      `VALUE_INVALID`, `VALUE_NOT_AVAILABLE` (with `int_value = 0`), and `false` for
      `status = VALUE_VALID` with a populated value.
- [ ] 3.2 Add `MapperTest` case: `MB_KEY_SOC` with `status = VALUE_NOT_RECEIVED` and `int_value = 0`
      maps to a `ChannelStateMap` whose state is `UnDefType.UNDEF`, not `0 %`.
- [ ] 3.3 Confirm existing `MapperTest`/`UtilsTest` cases for a valid SoC value (e.g. `74 %`) still
      pass unchanged.

## 4. Mapper - remaining numeric branches (added after community report)

- [ ] 4.1 Wrap the "Kilometer values" case (`MB_KEY_ODO`, `MB_KEY_RANGEELECTRIC`,
      `MB_KEY_OVERALL_RANGE`, `MB_KEY_RANGELIQUID`, `MB_KEY_DISTANCE_START`,
      `MB_KEY_DISTANCE_RESET`, `MB_KEY_ECOSCORE_BONUS`) with the `Utils.isNil(value)` → `UNDEF`
      guard, keeping the existing unit-resolution logic in the `else` branch unchanged.
- [ ] 4.2 Wrap `MB_KEY_CHARGING_POWER` ("KiloWatt values") the same way - this removes the
      `Math.max(0, -1) == 0` masking that currently hides an unavailable reading as `0 kW`.
- [ ] 4.3 Wrap `MB_KEY_AVERAGE_SPEED_START`/`RESET` the same way - same `Math.max(0, ...)` masking
      as 4.2. Do not touch the pre-existing `lengthUnit`/`speedUnit` mix-up inside the branch; that is
      a separate bug, out of scope here (noted in `proposal.md`).
- [ ] 4.4 Wrap `MB_KEY_ELECTRICCONSUMPTIONSTART`/`RESET` and `MB_KEY_LIQUIDCONSUMPTIONSTART`/`RESET`
      the same way.
- [ ] 4.5 Wrap the tire pressure case (`MB_KEY_TIREPRESSURE_FRONT_LEFT`/`FRONT_RIGHT`/`REAR_LEFT`/
      `REAR_RIGHT`) the same way.
- [ ] 4.6 Add `MapperTest` cases for `MB_KEY_CHARGING_POWER` (invalid status → `UNDEF`, not `0 kW`;
      valid status → correct `kW` value) and `MB_KEY_ODO` (invalid status → `UNDEF`, not `-1 km`).

## 5. Fix found by `mvn test` - UOM-observer/nil coupling regression

- [ ] 5.1 In the Kilometer values, Average speed, Electric consumption, Liquid consumption, and Tire
      pressure cases, move the `value.hasXUnit()` / `UOMObserver` lookup out of the
      `Utils.isNil(value)`-false branch so it runs unconditionally, matching pre-change behavior.
- [ ] 5.2 Add a `MapperTest` case: a `MB_KEY_LIQUIDCONSUMPTIONSTART`-style attribute with
      `nil_value = true` but `combustion_consumption_unit` set still returns a `ChannelStateMap` with
      a non-null `UOMObserver` (`hasUomObserver()` true) and `state = UnDefType.UNDEF`.
- [ ] 5.3 Confirm `VehicleHandlerTest`'s four `"Trip Update Count"` assertions (`testBEVCharging`,
      `testBEVFullUpdateNoCapacities`, `testBEVImperialUnits`, `testEventStorage`) pass again.

---
